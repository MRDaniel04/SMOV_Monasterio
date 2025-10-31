package com.nextapp.monasterio.ui.virtualvisit.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.github.chrisbanes.photoview.PhotoView

class DebugPhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : PhotoView(context, attrs, defStyle) {

    var highlightColor: Int = Color.TRANSPARENT
    var interactivePath: Path? = null

    data class PinData(val x: Float, val y: Float, val iconId: Int, val isPressed: Boolean)
    var pins: List<PinData> = emptyList()

    private val highlightPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
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

        interactivePath?.let { path ->
            if (highlightColor != Color.TRANSPARENT) {
                val pathCopy = Path(path)
                val drawMatrix = Matrix().apply {
                    setScale(scaleX * d.intrinsicWidth, scaleY * d.intrinsicHeight)
                    postTranslate(transX, transY)
                }
                pathCopy.transform(drawMatrix)
                highlightPaint.color = highlightColor
                canvas.drawPath(pathCopy, highlightPaint)
            }
        }

        pins.forEach { pin ->
            val icon = ContextCompat.getDrawable(context, pin.iconId) ?: return@forEach

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
}