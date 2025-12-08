package com.nextapp.monasterio.ui.screens.pinCreation.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.screens.pinCreation.state.PinImage

@Composable
fun ImagePreviewCard(
    pinImage: PinImage,
    onRemove: () -> Unit,
    onEditDetails: (PinImage) -> Unit // 🆕 Nuevo callback
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.size(120.dp)
    ) {
        Box(Modifier.size(120.dp)) {

            Image(
                painter = rememberAsyncImagePainter(pinImage.uri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Botón 'X' para eliminar (Top End)
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                    .size(24.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_close_24),
                    contentDescription = stringResource(R.string.img_edit_desc),
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { onEditDetails(pinImage) }, // Disparamos el diálogo
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                    .size(24.dp)
            ) {
                Icon(
                    painterResource(R.drawable.lapiz),
                    contentDescription = stringResource(R.string.img_edit_desc),
                    tint = Color.White
                )
            }

            // CHIP DE ETIQUETA (Bottom End)
            pinImage.tag?.let { tag ->
                AssistChip(
                    onClick = { onEditDetails(pinImage) }, // 🆕 Clic en el chip también edita
                    label = { Text(
                        text = tag.displayName,
                        style = MaterialTheme.typography.labelSmall
                    ) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                )

            }
        }
    }
}