package com.nextapp.monasterio.ui.screens.pinCreation.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.ImageTag
import com.nextapp.monasterio.ui.screens.pinCreation.state.PinImage

@Composable
fun ImagePreviewCard(
    pinImage: PinImage,
    onRemove: () -> Unit,
    onTagSelected: (ImageTag) -> Unit
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
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                    .size(24.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_close_24),
                    contentDescription = "Eliminar imagen",
                    tint = Color.White
                )
            }

            // CHIP DE ETIQUETA (Bottom End)
            pinImage.tag?.let { tag ->
                AssistChip(
                    onClick = { showMenu = true },
                    label = { Text(tag.displayName) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )

                // MINI MENÚ DESPLEGABLE (Para Reedición)
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // ¡ELIMINADO: Text("Cambiar a:") y Divider!
                    ImageTag.entries.forEach { newTag ->
                        DropdownMenuItem(
                            text = { Text(newTag.displayName) },
                            onClick = {
                                onTagSelected(newTag)
                                showMenu = false
                            },
                            enabled = newTag != tag
                        )
                    }
                }
            }
        }
    }
}