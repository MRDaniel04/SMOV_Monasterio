package com.nextapp.monasterio.ui.screens.pinEdition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData


@Composable
fun MovingPinOverlay(
    pinData: PinData,
    initialOffset: Offset,
    isPressed: Boolean = false,
    onPinDrag: (Offset) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    boxSize: IntSize
) {
    val context = LocalContext.current
    // 1. OBTENER LA DENSIDAD LOCAL para habilitar .toDp()
    val density = LocalDensity.current

    // La posición actual se mantendrá en pinDragOffset del padre (EdicionPines)
    var currentOffset by remember { mutableStateOf(initialOffset) }

    // Sincronizar el offset con el valor del estado padre
    LaunchedEffect(initialOffset) {
        currentOffset = initialOffset
    }

    val pinIconSize = 48.dp
    val buttonSize = 40.dp
    val pinOffsetCorrection = pinIconSize / 2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newOffset = currentOffset.plus(dragAmount)

                    // Lógica para mantener el pin dentro de los límites visibles de la pantalla (usando px)
                    val density = context.resources.displayMetrics.density
                    val safeAreaDp = 60.dp.value
                    val safeAreaPx = safeAreaDp * density

                    val boundedX = newOffset.x.coerceIn(0f + safeAreaPx, boxSize.width.toFloat() - safeAreaPx)
                    val boundedY = newOffset.y.coerceIn(0f + safeAreaPx, boxSize.height.toFloat() - safeAreaPx)

                    currentOffset = Offset(boundedX, boundedY)
                    onPinDrag(currentOffset)
                }
            }
    ) {
        // 2. USAR with(density) para habilitar las extensiones .toDp()
        with(density) {
            // --- Icono del Pin Flotante ---
            Icon(
                painter = painterResource(id = R.drawable.pin3), // Usamos el mismo icono de pin
                contentDescription = "Pin en movimiento",
                tint = Color.Red, // Destacamos el pin que se está moviendo
                modifier = Modifier
                    .offset(
                        x = currentOffset.x.toDp() - pinOffsetCorrection,
                        y = currentOffset.y.toDp() - pinIconSize
                    )
                    .size(pinIconSize)
            )

            // --- Botones de Control Flotantes (arriba del pin) ---
            Row(
                modifier = Modifier
                    .offset(
                        x = currentOffset.x.toDp() - pinIconSize,
                        y = currentOffset.y.toDp() - pinIconSize - buttonSize - 4.dp
                    )
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel, modifier = Modifier.size(buttonSize)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_24),
                        contentDescription = "Cancelar Movimiento",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Botón de Confirmación (✔️)
                IconButton(onClick = onConfirm, modifier = Modifier.size(buttonSize)) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_done_24),
                        contentDescription = "Confirmar Posición",
                        tint = Color(0xFF4CAF50), // Verde
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}