package com.nextapp.monasterio.ui.screens.pinEdition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.R

@Composable
fun PinEditionToolbar(
    onPinAddClick: () -> Unit,
    onCrosshairClick: () -> Unit,
    onCancelEditClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier // El alineamiento (e.g., Alignment.TopEnd) se aplica aquí
            .padding(12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xBB000000)) // Fondo semi-transparente
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            // Botón 2: Añadir Pin
            IconButton(onClick = onPinAddClick) {
                Icon(
                    painter = painterResource(id = R.drawable.pin3),
                    contentDescription = "Añadir Pin",
                    tint = Color.White
                )
            }
            // Botón 3: Modo Cruz (Crosshair)
            IconButton(onClick = onCrosshairClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reajuste),
                    contentDescription = "Centrar/Posicionar",
                    tint = Color.White
                )
            }

            // Botón 5: Ayuda/Información (El botón extra a la derecha)
            IconButton(onClick = onHelpClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_help),
                    contentDescription = "Ayuda",
                    tint = Color.White
                )
            }
        }
    }
}