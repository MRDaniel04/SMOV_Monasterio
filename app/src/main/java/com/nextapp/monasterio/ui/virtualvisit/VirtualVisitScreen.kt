package com.nextapp.monasterio.ui.virtualvisit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.R
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualVisitScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monasterio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEEEEEE),
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->

        // 🔹 El contenido de la visita virtual dentro del padding del Scaffold
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ZoomableMap()
        }
    }
}

@Composable
private fun ZoomableMap() {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var boxWidth by remember { mutableStateOf(0f) }
    var boxHeight by remember { mutableStateOf(0f) }
    var imageWidth by remember { mutableStateOf(0f) }
    var imageHeight by remember { mutableStateOf(0f) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer { clip = true }
            .onGloballyPositioned {
                boxWidth = it.size.width.toFloat()
                boxHeight = it.size.height.toFloat()
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.mapa_monasterio),
            contentDescription = "Mapa del Monasterio",
            modifier = Modifier
                .graphicsLayer {
                    if (imageHeight != 0f && boxHeight != 0f) {
                        val scaleToFit = boxHeight / imageHeight
                        scaleX = scaleToFit * scale
                        scaleY = scaleToFit * scale
                    }
                    translationX = offset.x
                    translationY = offset.y
                }
                .onGloballyPositioned {
                    imageWidth = it.size.width.toFloat()
                    imageHeight = it.size.height.toFloat()
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (imageHeight == 0f || boxHeight == 0f) return@detectTransformGestures

                        val scaleToFit = boxHeight / imageHeight

                        // 🔹 Zoom mínimo dinámico
                        val minScaleToFillWidth = if (imageWidth != 0f)
                            (boxWidth / (imageWidth * scaleToFit))
                        else 1f

                        // 🔹 Limitar zoom máximo
                        val newScale = (scale * zoom).coerceIn(minScaleToFillWidth, 1.5f)
                        scale = newScale

                        // 🔹 Tamaños reales de la imagen escalada
                        val scaledImageWidth = imageWidth * scaleToFit * newScale
                        val scaledImageHeight = imageHeight * scaleToFit * newScale

                        // 🔹 Límites horizontales y verticales
                        val maxX = max((scaledImageWidth - boxWidth) / 2f, 0f)
                        val minX = -maxX
                        val maxY = max((scaledImageHeight - boxHeight) / 2f, 0f)
                        val minY = -maxY

                        // 🔹 Aumentar velocidad de desplazamiento
                        val speedFactor = 2f
                        val newOffsetX = (offset.x + pan.x * speedFactor).coerceIn(minX, maxX)
                        val newOffsetY = (offset.y + pan.y * speedFactor).coerceIn(minY, maxY)

                        offset = Offset(newOffsetX, newOffsetY)
                    }
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewVirtualVisitScreen() {
    VirtualVisitScreen()
}
