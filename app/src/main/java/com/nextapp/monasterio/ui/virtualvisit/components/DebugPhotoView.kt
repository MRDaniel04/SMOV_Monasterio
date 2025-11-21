package com.nextapp.monasterio.ui.virtualvisit.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.chrisbanes.photoview.PhotoView

class DebugPhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : PhotoView(context, attrs, defStyle) {

    var highlightColor: Int = Color.TRANSPARENT
    var interactivePath: Path? = null

    data class StaticZoneData(val path: Path, val color: Int)
    var staticZones: List<StaticZoneData> = emptyList()

    var blinkingAlpha:Float=1.0f


    data class PinData(val x: Float, val y: Float, val iconId: Int?, val isPressed: Boolean)
    var pins: List<PinData> = emptyList()

    private val highlightPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f
        isAntiAlias = true
    }

    private val zonePaint = Paint().apply{
        style = Paint.Style.STROKE
        strokeWidth = 20f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val d = drawable ?: return
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

        if (staticZones.isNotEmpty()) {
            staticZones.forEach { zone ->
                val baseColor = zone.color
                val baseRed = Color.red(baseColor)
                val baseGreen = Color.green(baseColor)
                val baseBlue = Color.blue(baseColor)
                val baseAlpha = Color.alpha(baseColor)

                val newAlpha = (baseAlpha * blinkingAlpha).toInt()

                zonePaint.color = Color.argb(newAlpha, baseRed, baseGreen, baseBlue)
                val pathCopy = Path(zone.path)
                pathCopy.transform(drawMatrix)
                canvas.drawPath(pathCopy, zonePaint)
            }
        }

        interactivePath?.let { path ->
            if (highlightColor != Color.TRANSPARENT) {
                val pathCopy = Path(path)
                pathCopy.transform(drawMatrix)
                highlightPaint.color = highlightColor
                canvas.drawPath(pathCopy, highlightPaint)
            }
        }

        pins.forEach { pin ->
            val icon = ContextCompat.getDrawable(context, pin.iconId ?: 0) ?: return@forEach

            val imageX = pin.x * d.intrinsicWidth
            val imageY = pin.y * d.intrinsicHeight

            val screenX = imageX * scaleX + transX
            val screenY = imageY * scaleY + transY

            val scale = if (pin.isPressed) 0.85f else 1.0f
            val sizePx = 80f * scale
            val left = (screenX - sizePx / 2).toInt()
            val top = (screenY - sizePx).toInt()
            val right = (screenX + sizePx / 2).toInt()
            val bottom = (screenY).toInt()

            icon.setBounds(left, top, right, bottom)
            icon.draw(canvas)
        }
    }

    fun setImageFromUrl(url: String) {
        try {
            // Carga la imagen remota usando Glide (descarga y la muestra en el PhotoView)
            com.bumptech.glide.Glide.with(context)
                .asBitmap()
                .load(url)
                .into(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun moveVerticalFree(deltaY: Float) {

        // Esta función asume que está dentro de una clase que hereda de View (como DebugPhotoView).

        // Si deltaY es positivo (como +120f), la vista se moverá hacia abajo.
        // No hay chequeos de límites, por lo que puede moverse indefinidamente.

        // Aplicar el movimiento directamente a la propiedad de traslación de la View.
        this.translationY += deltaY

        Log.d("MOVE_FREE", "Desplazamiento aplicado: $deltaY. Nueva translationY: ${this.translationY}")
    }

    fun moveHorizontalFree(deltaX: Float) {

        // Esta función aplica un desplazamiento horizontal sin límites.

        // Si deltaX es positivo (+), la vista se moverá hacia la DERECHA.
        // Si deltaX es negativo (-), la vista se moverá hacia la IZQUIERDA.

        // Aplicar el movimiento directamente a la propiedad de traslación horizontal de la View.
        this.translationX += deltaX

        Log.d("MOVE_FREE_H", "Desplazamiento aplicado: $deltaX. Nueva translationX: ${this.translationX}")
    }





}