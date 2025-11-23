package com.nextapp.monasterio.ui.virtualvisit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nextapp.monasterio.R
import kotlin.math.max

@Composable
fun GenericTutorialOverlay(
    title: String? = null,
    description: String,
    highlightCenter: Offset = Offset.Unspecified,
    highlightRadius: Float = 0f,
    highlightRectSize: Size? = null,
    dialogAlignment: Alignment? = null,
    buttonText: String,
    onCloseClicked: (Boolean) -> Unit
) {
    var dontShowAgain by remember { mutableStateOf(false) }
    // El estado del scroll ahora solo afecta a la descripción
    val descriptionScrollState = rememberScrollState()

    val parentDensity = LocalDensity.current
    val parentConfiguration = LocalConfiguration.current

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        CompositionLocalProvider(
            LocalDensity provides parentDensity,
            LocalConfiguration provides parentConfiguration
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val screenHeightPx = constraints.maxHeight.toFloat()
                val density = LocalDensity.current

                // 1. Determinar Alineación
                val finalAlignment = if (dialogAlignment != null) {
                    dialogAlignment
                } else {
                    if (highlightCenter != Offset.Unspecified) {
                        if (highlightCenter.y > (screenHeightPx / 2)) Alignment.TopCenter else Alignment.BottomCenter
                    } else {
                        Alignment.BottomCenter
                    }
                }

                // 2. Calcular altura máxima (igual que antes, para no tapar el foco)
                val maxCardHeight: Dp = if (highlightCenter != Offset.Unspecified) {
                    val safeMargin = with(density) { 32.dp.toPx() }
                    val focusEdgeY = if (highlightRectSize != null) {
                        if (finalAlignment == Alignment.TopCenter) highlightCenter.y - (highlightRectSize.height / 2) else highlightCenter.y + (highlightRectSize.height / 2)
                    } else {
                        if (finalAlignment == Alignment.TopCenter) highlightCenter.y - highlightRadius else highlightCenter.y + highlightRadius
                    }
                    val availableSpacePx = if (finalAlignment == Alignment.TopCenter) {
                        focusEdgeY - safeMargin
                    } else {
                        screenHeightPx - focusEdgeY - safeMargin
                    }
                    with(density) { max(150f, availableSpacePx).toDp() } // Mínimo un poco más alto ahora
                } else {
                    450.dp
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = finalAlignment
                ) {
                    // --- CANVAS (Fondo y Foco) ---
                    Canvas(
                        modifier = Modifier.fillMaxSize().clickable(enabled = false) {}.graphicsLayer { alpha = 0.99f }
                    ) {
                        drawRect(color = Color.Black.copy(alpha = 0.85f))
                        if (highlightCenter != Offset.Unspecified) {
                            if (highlightRectSize != null) {
                                val topLeft = highlightCenter - Offset(highlightRectSize.width / 2, highlightRectSize.height / 2)
                                val cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                                drawRoundRect(color = Color.Transparent, topLeft = topLeft, size = highlightRectSize, cornerRadius = cornerRadius, blendMode = BlendMode.Clear)
                                drawRoundRect(color = Color.White.copy(alpha = 0.3f), topLeft = topLeft - Offset(4f, 4f), size = Size(highlightRectSize.width + 8f, highlightRectSize.height + 8f), cornerRadius = cornerRadius, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f))
                            } else if (highlightRadius > 0) {
                                drawCircle(color = Color.Transparent, center = highlightCenter, radius = highlightRadius, blendMode = BlendMode.Clear)
                                drawCircle(color = Color.White.copy(alpha = 0.3f), center = highlightCenter, radius = highlightRadius + 4f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12f))
                            }
                        }
                    }

                    // --- TARJETA DEL DIÁLOGO ---
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .padding(horizontal = 16.dp)
                            .padding(
                                bottom = if (finalAlignment == Alignment.BottomCenter) 32.dp else 0.dp,
                                top = if (finalAlignment == Alignment.TopCenter) 60.dp else 0.dp
                            )
                            .navigationBarsPadding()
                            .statusBarsPadding()
                            // La altura se restringe dinámicamente aquí
                            .heightIn(max = maxCardHeight),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        // Usamos una Columna normal, SIN scroll global
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. CABECERA FIJA (Título)
                            if (title != null) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(12.dp))
                            }

                            // 2. CUERPO SCROLLABLE (Descripción con borde)
                            // Este Box ocupa el espacio intermedio y permite scroll si es necesario
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // weight(1f, fill = false) es clave: Ocupa el espacio disponible
                                    // pero no se estira si el texto es corto. Si es largo, se limita.
                                    .weight(1f, fill = false)
                                    // Borde fino alrededor del área de texto
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant, // Color sutil
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    // Clip para que el scroll no se salga de las esquinas redondeadas
                                    .clip(RoundedCornerShape(12.dp))
                                    // Padding interno para que el texto no toque el borde
                                    .padding(12.dp)
                                    // AQUÍ ESTÁ EL SCROLL: Solo afecta a este cuadro
                                    .verticalScroll(descriptionScrollState),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // 3. PIE FIJO (Checkbox y Botón)
                            // Estos elementos siempre estarán visibles abajo
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .clickable { dontShowAgain = !dontShowAgain }
                                        .padding(horizontal = 0.dp, vertical = 0.dp)
                                ) {
                                    Checkbox(
                                        checked = dontShowAgain,
                                        onCheckedChange = { dontShowAgain = it },
                                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.dont_show_again),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                Button(
                                    onClick = { onCloseClicked(dontShowAgain) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = buttonText.uppercase(),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}