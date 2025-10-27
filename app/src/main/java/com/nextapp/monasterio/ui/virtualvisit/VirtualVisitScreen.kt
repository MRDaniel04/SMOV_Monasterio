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
import androidx.compose.foundation.gestures.detectTapGestures
import android.graphics.Region
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.RectF
import androidx.core.graphics.PathParser
import android.widget.Toast // Para la función Toast
import androidx.compose.ui.platform.LocalContext // Para obtener el Contexto de Android




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
    // 👇 CAMBIO 1: Obtener el contexto
    val context = LocalContext.current

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
            painter = painterResource(id = R.drawable.mapa_monasterio_2),
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

        MonasterioTouchCanvas(
            scale = scale,
            offset = offset,
            onMonasteryClick = {
                // 👇 CAMBIO 2: Mostrar el Toast para verificación
                Toast.makeText(context, "¡Monasterio Tocado!", Toast.LENGTH_SHORT).show()
                // Cuando implementes la navegación, descomentarás esta línea:
                // navController.navigate("detalle_monasterio")
            }
        )

    }
}

@Composable
fun MonasterioTouchCanvas(
    scale: Float,
    offset: Offset,
    onMonasteryClick: () -> Unit
) {
    var touched by remember { mutableStateOf(false) }

    val monasteryPaths = remember {
        listOf(
            "M361.2,417.8L382.8,400.5L404.5,383.1L393,369.1L339.4,302.2L287.5,238.8L299,229.7L308.1,222.5L310.6,220.5L272.4,176.2L252.8,190.6L234,169.4L210.8,187.8L187.7,206.1L190.7,209.7L170.6,225.5L186.2,244.6L182.9,247.1L220.1,291.8L242.2,274L286.8,327.9L294.6,337.4L361.2,417.8Z",
            "M299,229.7L287.5,238.8L339.4,302.2L393,369.1L401.3,338.1L351.8,276.8L308.1,222.5L299,229.7Z",
            "M423.4,321.8L401.3,338.1L393,369.1L435.5,336.4L423.4,321.8Z",
            "M441.5,379.4L429.1,365.3L447.6,351L435.5,336.4L393,369.1L404.5,383.1L441.5,379.4Z",
            "M353.5,449.3L441.5,379.4L404.5,383.1L382.8,400.5L361.2,417.8L353.5,449.3Z",
            "M242.2,274L220.1,291.8L265.5,345.4L310.9,399L353.5,449.3L361.2,417.8L294.6,337.4L286.8,327.9L242.2,274Z"
        ).map { pathData ->
            androidx.core.graphics.PathParser.createPathFromPathData(pathData)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures { rawOffset ->
                    val transformedOffset = Offset(
                        (rawOffset.x - offset.x) / scale,
                        (rawOffset.y - offset.y) / scale
                    )

                    val touchedMonastery = monasteryPaths.any { path ->
                        val bounds = RectF()
                        path.computeBounds(bounds, true)
                        val region = Region()
                        region.setPath(path, Region(bounds.left.toInt(), bounds.top.toInt(), bounds.right.toInt(), bounds.bottom.toInt()))
                        region.contains(transformedOffset.x.toInt(), transformedOffset.y.toInt())
                    }

                    if (touchedMonastery) {
                        touched = true
                        onMonasteryClick()
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                monasteryPaths.forEach { path ->
                    val paint = android.graphics.Paint().apply {
                        color = if (touched) android.graphics.Color.parseColor("#DAA520") else android.graphics.Color.parseColor("#A35B3F")
                        style = android.graphics.Paint.Style.FILL
                    }
                    canvas.nativeCanvas.drawPath(path, paint)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewVirtualVisitScreen() {
    VirtualVisitScreen()
}