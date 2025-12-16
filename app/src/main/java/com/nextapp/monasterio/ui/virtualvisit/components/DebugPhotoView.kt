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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val d = drawable ?: return

        // 1. Obtener matriz de la imagen (Zoom y Pan)
        val matrixValues = FloatArray(9)
        imageMatrix.getValues(matrixValues)
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val scaleY = matrixValues[Matrix.MSCALE_Y]
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]

        val drawMatrix = Matrix().apply {
            setScale(scaleX * d.intrinsicWidth, scaleY * d.intrinsicHeight)
            postTranslate(transX, transY)
        }

        // 2. Dibujar Zonas Interactivas
        if (staticZones.isNotEmpty()) {
            staticZones.forEach { zone ->
                val baseColor = zone.color
                // Calculamos el alpha basado en el parpadeo
                val newAlpha = (Color.alpha(baseColor) * blinkingAlpha).toInt().coerceIn(0, 255)

                zonePaint.color = Color.argb(newAlpha, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))

                val pathCopy = Path(zone.path)
                pathCopy.transform(drawMatrix)
                canvas.drawPath(pathCopy, zonePaint)
            }
        }

        // 3. Dibujar Resaltado (Click)
        interactivePath?.let { path ->
            if (highlightColor != Color.TRANSPARENT) {
                val pathCopy = Path(path)
                pathCopy.transform(drawMatrix)
                highlightPaint.color = highlightColor
                canvas.drawPath(pathCopy, highlightPaint)
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

            // Definimos los límites centrados en (0,0) local.
            // El pin se dibuja hacia arri  ba desde el punto (0,0) que es la punta.
            icon.setBounds((-halfSize).toInt(), (-sizePx).toInt(), (halfSize).toInt(), 0)

            icon.draw(canvas)
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