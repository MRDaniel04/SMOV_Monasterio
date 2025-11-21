package com.nextapp.monasterio.ui.virtualvisit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius // <-- Importante para bordes redondeados
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size         // <-- Importante para el tamaño cuadrado
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nextapp.monasterio.R

@Composable
fun GenericTutorialOverlay(
    title: String? = null,
    description: String,
    highlightCenter: Offset = Offset.Unspecified,
    highlightRadius: Float = 0f,
    // 👇 NUEVO: Tamaño para focos cuadrados/rectangulares (opcional)
    highlightRectSize: Size? = null,
    // 👇 NUEVO: Dónde colocar el diálogo (Arriba o Abajo)
    dialogAlignment: Alignment = Alignment.BottomCenter,
    buttonText: String,
    onCloseClicked: (Boolean) -> Unit
) {
    var dontShowAgain by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            // 👇 Usamos la alineación que pasamos por parámetro
            contentAlignment = dialogAlignment
        ) {
            // 1. El Canvas que dibuja el fondo y el agujero
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) {}
                    .graphicsLayer { alpha = 0.99f }
            ) {
                // Fondo Negro (Un poco más oscuro para mejor contraste)
                drawRect(color = Color.Black.copy(alpha = 0.85f))

                // El Agujero (Spotlight)
                if (highlightCenter != Offset.Unspecified) {

                    // --- CASO A: CUADRADO / RECTÁNGULO ---
                    if (highlightRectSize != null) {
                        val topLeft = highlightCenter - Offset(highlightRectSize.width / 2, highlightRectSize.height / 2)
                        val cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())

                        // Recorte transparente
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = topLeft,
                            size = highlightRectSize,
                            cornerRadius = cornerRadius,
                            blendMode = BlendMode.Clear
                        )
                        // Borde de luz (Brillo)
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.3f),
                            topLeft = topLeft - Offset(4f, 4f),
                            size = Size(highlightRectSize.width + 8f, highlightRectSize.height + 8f),
                            cornerRadius = cornerRadius,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                        )
                    }
                    // --- CASO B: CÍRCULO ---
                    else if (highlightRadius > 0) {
                        // Recorte transparente
                        drawCircle(
                            color = Color.Transparent,
                            center = highlightCenter,
                            radius = highlightRadius,
                            blendMode = BlendMode.Clear
                        )
                        // Borde de luz (Brillo)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            center = highlightCenter,
                            radius = highlightRadius + 4f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12f)
                        )
                    }
                }
            }

            // 2. La tarjeta de texto
            Surface(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(horizontal = 16.dp)
                    // 👇 Padding dinámico según si está arriba o abajo
                    .padding(
                        bottom = if (dialogAlignment == Alignment.BottomCenter) 32.dp else 0.dp,
                        top = if (dialogAlignment == Alignment.TopCenter) 60.dp else 0.dp
                    )
                    .navigationBarsPadding()
                    .statusBarsPadding(), // Importante si lo ponemos arriba
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { dontShowAgain = !dontShowAgain }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Checkbox(checked = dontShowAgain, onCheckedChange = { dontShowAgain = it })
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.dont_show_again),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { onCloseClicked(dontShowAgain) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = buttonText.uppercase())
                    }
                }
            }
        }
    }
}