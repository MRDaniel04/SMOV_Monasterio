package com.nextapp.monasterio.viewModels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.models.Diferencia

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImagenConToque(
    resId: Int,
    diferencias: List<Diferencia>,
    onHit: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val painter = painterResource(id = resId)
    val intrinsicSize = painter.intrinsicSize // Tamaño de la imagen original

    var imageSize by remember { mutableStateOf(Size.Zero) } // Tamaño del contenedor/Box en pantalla

    // Calcular el factor de escala X y Y basado en el tamaño del contenedor
    val scaleX = imageSize.width / if (intrinsicSize.width > 0) intrinsicSize.width else 1f
    val scaleY = imageSize.height / if (intrinsicSize.height > 0) intrinsicSize.height else 1f

    val actualScaleFactor = minOf(scaleX, scaleY)

    // Altura y ancho de la imagen visible escalada
    val visibleWidth = intrinsicSize.width * actualScaleFactor
    val visibleHeight = intrinsicSize.height * actualScaleFactor

    // Desplazamiento (padding) horizontal y vertical para centrar la imagen
    val displacementX = (imageSize.width - visibleWidth) / 2
    val displacementY = (imageSize.height - visibleHeight) / 2

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    imageSize = Size(size.width.toFloat(), size.height.toFloat())
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->

                        val mappedX = (offset.x - displacementX) / actualScaleFactor
                        val mappedY = (offset.y - displacementY) / actualScaleFactor

                        val diferenciaEncontrada = diferencias.find{diff ->
                            val isHit = (mappedX >= diff.rectX) &&
                                    (mappedX <= diff.rectX + diff.width) &&
                                    (mappedY >= diff.rectY) &&
                                    (mappedY <= diff.rectY + diff.height)
                            isHit
                        }
                        if(diferenciaEncontrada!=null && !diferenciaEncontrada.encontrada){
                            onHit(mappedX, mappedY)
                        }
                    }
                }
        )

        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 3.dp.toPx()
            val paintStyle = Stroke(width = strokeWidth)

            diferencias.filter { it.encontrada }.forEach { diff ->

                val mappedX = diff.rectX * actualScaleFactor
                val mappedY = diff.rectY * actualScaleFactor
                val mappedW = diff.width * actualScaleFactor
                val mappedH = diff.height * actualScaleFactor

                // Se debe sumar el desplazamiento para que el círculo esté centrado en la imagen visible
                val circleCenterX = displacementX + mappedX + (mappedW / 2)
                val circleCenterY = displacementY + mappedY + (mappedH / 2)
                val radioBase = 10.dp.toPx()

                drawCircle(
                    color = Color.Red,
                    center = Offset(circleCenterX, circleCenterY),
                    radius = (radioBase) + strokeWidth * 2,
                    style = paintStyle
                )
            }
        }
    }
}