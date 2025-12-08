package com.nextapp.monasterio.ui.screens.pinEdition.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column // ⬅️ Añadir este import
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.R

@Composable
fun PinEditionToolbar(
    onPinAddClick: () -> Unit,
    onCrosshairClick: () -> Unit,
    onCancelEditClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPortrait: Boolean // ⬅️ NUEVO: Indicador de orientación
) {
    val context = LocalContext.current
    val activity = context.getActivity() as AppCompatActivity

    Box(
        modifier = modifier // El alineamiento (e.g., Alignment.CenterStart) se aplica aquí
            .padding(12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xBB000000)) // Fondo semi-transparente
    ) {
        // Definimos el contenido de la Toolbar para evitar repetirlo
        val toolbarContent = @Composable {
            // Botón 2: Añadir Pin

            MainLanguageSelector(activity)

            IconButton(onClick = onPinAddClick) {
                Icon(
                    painter = painterResource(id = R.drawable.pin),
                    contentDescription = "Añadir Pin",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
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

        if (isPortrait) {
            // MODO VERTICAL (Portrait): Row (Horizontal)
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                toolbarContent()
            }
        } else {
            // MODO HORIZONTAL (Landscape): Column (Vertical)
            Column(
                modifier = Modifier.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally, // Centra los iconos
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                toolbarContent()
            }
        }
    }
}