package com.nextapp.monasterio.ui.virtualvisit.components

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.chrisbanes.photoview.PhotoView

class DebugPhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : PhotoView(context, attrs, defStyle) {

    private val TAG = "DEBUG_MAPA"

    var highlightColor: Int = Color.TRANSPARENT
    var interactivePath: Path? = null

    data class StaticZoneData(val path: Path, val color: Int)
    var staticZones: List<StaticZoneData> = emptyList()

    var blinkingAlpha: Float = 1.0f

    // CACHÉ: Guardamos TODOS los iconos (estáticos y GIFs) para no cargarlos en bucle
    private val drawableCache = mutableMapOf<Int, Drawable>()

    data class PinData(
        val x: Float,
        val y: Float,
        val iconId: Int?,
        val isPressed: Boolean,
        val isMoving: Boolean,
        val pinColor: Int = Color.WHITE
    )

    var pins: List<PinData> = emptyList()
        set(value) {
            field = value
            // No invalidamos aquí a lo loco, dejamos que el ciclo de animación lo haga si es necesario
            invalidate()
        }

    private val zonePaint = Paint().apply{
        style = Paint.Style.STROKE
        strokeWidth = 20f
        isAntiAlias = true
    }

    private val highlightPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f
        isAntiAlias = true
    }

    private val density = context.resources.displayMetrics.density

    // Objetos reutilizables para optimizar onDraw
    private val drawMatrix = Matrix()
    private val matrixValues = FloatArray(9)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val d = drawable ?: return

        // 1. Obtener matriz de la imagen (Zoom y Pan)
        imageMatrix.getValues(matrixValues)
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val scaleY = matrixValues[Matrix.MSCALE_Y]
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]

        drawMatrix.reset()
        drawMatrix.setScale(scaleX * d.intrinsicWidth, scaleY * d.intrinsicHeight)
        drawMatrix.postTranslate(transX, transY)

        // CALCULAR FACTOR DE ESCALA PARA EL GROSOR DE LINEA
        // Si escalamos el canvas, el grosor de línea también se escala.
        // Para mantenerlo constante en pantalla (20px), dividimos por el factor de escala.
        val scaleFactor = scaleX * d.intrinsicWidth
        val safeScale = if (scaleFactor > 0.1f) scaleFactor else 1f

        // 2. Dibujar Zonas Interactivas
        if (staticZones.isNotEmpty()) {
            canvas.save()
            canvas.concat(drawMatrix)

            // Ajustar grosor inversamente a la escala
            zonePaint.strokeWidth = 20f / safeScale

            staticZones.forEach { zone ->
                val baseColor = zone.color
                // Calculamos el alpha basado en el parpadeo
                val newAlpha = (Color.alpha(baseColor) * blinkingAlpha).toInt().coerceIn(0, 255)

                zonePaint.color = Color.argb(newAlpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
                // Dibujamos el path original transformado por la matriz del canvas
                canvas.drawPath(zone.path, zonePaint)
            }
            canvas.restore()
        }

        // 3. Dibujar Resaltado (Click)
        interactivePath?.let { path ->
            if (highlightColor != Color.TRANSPARENT) {
                canvas.save()
                canvas.concat(drawMatrix)

                // Ajustar grosor inversamente a la escala
                highlightPaint.strokeWidth = 15f / safeScale

                highlightPaint.color = highlightColor
                canvas.drawPath(path, highlightPaint)
                canvas.restore()
            }
        }

        // 4. DIBUJAR PINES (OPTIMIZADO)
        pins.forEach { pin ->
            val iconId = pin.iconId ?: 0
            if (iconId == 0) return@forEach

            // A. Obtener de caché o cargar UNA VEZ
            var icon = drawableCache[iconId]
            if (icon == null) {
                try {
                    val loaded = ContextCompat.getDrawable(context, iconId)
                    if (loaded != null) {
                        icon = loaded
                        // Si es animado, le damos el callback para que se mueva
                        if (icon is Animatable) {
                            icon.callback = this
                            icon.start()
                        }
                        drawableCache[iconId] = icon
                    }
                } catch (e: Exception) {
                }
            }

            if (icon == null) return@forEach

            // B. Configurar Tinte
            if (pin.pinColor != Color.WHITE) {
                icon.setTint(pin.pinColor)
            } else {
                icon.setTintList(null)
            }

            // C. Calcular Posición en Pantalla
            val imageX = pin.x * d.intrinsicWidth
            val imageY = pin.y * d.intrinsicHeight
            val screenX = imageX * scaleX + transX
            val screenY = imageY * scaleY + transY

            // D. Calcular Tamaño basado en DENSIDAD (dp) en lugar de píxeles brutos
            val pressScale = if (pin.isPressed) 0.85f else 1.0f

            // Usamos 40dp como tamaño base estándar (ajusta el 40f si lo quieres más grande/pequeño)
            val sizeDp = 40f
            val sizePx = sizeDp * density * pressScale

            val halfSize = sizePx / 2

            // E. DIBUJAR USANDO TRANSFORMACIÓN DE CANVAS
            canvas.save()
            // Movemos el "papel" a la posición del pin
            canvas.translate(screenX, screenY)
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.R) {
                // Definimos los límites centrados en (0,0) local.
                // El pin se dibuja hacia arri  ba desde el punto (0,0) que es la punta.
                icon.setBounds((-halfSize).toInt(), (-sizePx).toInt(), (halfSize).toInt(), 0)

                icon.draw(canvas)
            }
            else{
                val originalW = if (icon.intrinsicWidth > 0) icon.intrinsicWidth.toFloat() else 100f
                val originalH = if (icon.intrinsicHeight > 0) icon.intrinsicHeight.toFloat() else 100f
                val scaleFactor = sizePx / originalH

                val moveX_dp = -20f
                val moveY_dp = -40f

                canvas.translate(moveX_dp * density, moveY_dp * density)

                canvas.scale(scaleFactor, scaleFactor)

                icon.setBounds(
                    (-originalW / 2).toInt(),
                    (-originalH).toInt(),
                    (originalW / 2).toInt(),
                    0
                )

                icon.draw(canvas)
            }
            canvas.restore()
        }
    }

    // Necesario para que los GIFs se animen
    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || drawableCache.containsValue(who)
    }

    override fun invalidateDrawable(drawable: Drawable) {
        if (verifyDrawable(drawable)) {
            invalidate()
        } else {
            super.invalidateDrawable(drawable)
        }
    }

    fun setImageFromUrl(url: String) {
        try {
            com.bumptech.glide.Glide.with(context).asBitmap().load(url).into(this)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun getNormalizedImageCoords(screenX: Float, screenY: Float): PointF? {
        val d = drawable ?: return null
        val matrixValues = FloatArray(9)
        imageMatrix.getValues(matrixValues)

        val scaleX = matrixValues[Matrix.MSCALE_X]
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]

        val imageX = (screenX - transX) / scaleX
        val imageY = (screenY - transY) / scaleX

        val normalizedX = imageX / d.intrinsicWidth
        val normalizedY = imageY / d.intrinsicHeight

        val finalX = normalizedX.coerceIn(0f, 1f)
        val finalY = normalizedY.coerceIn(0f, 1f)

        return PointF(finalX, finalY)
    }

    fun moveVerticalFree(deltaY: Float) {
        this.translationY += deltaY
    }

    fun moveHorizontalFree(deltaX: Float) {
        this.translationX += deltaX
    }
}