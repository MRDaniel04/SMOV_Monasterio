package com.nextapp.monasterio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nextapp.monasterio.R

@Composable
fun EditableImageGallery(
    imageUrls: List<String>,
    isEditing: Boolean,
    isUploading: Boolean = false,
    onAddImage: () -> Unit,
    onDeleteImage: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    var imageToDelete by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Título opcional
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Galería horizontal
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Botón para añadir imagen (solo en modo edición)
            if (isEditing) {
                item {
                    AddImageButton(
                        onClick = onAddImage,
                        isUploading = isUploading
                    )
                }
            }

            // Imágenes existentes
            items(imageUrls) { imageUrl ->
                ImageItem(
                    imageUrl = imageUrl,
                    isEditing = isEditing,
                    onDelete = { imageToDelete = imageUrl }
                )
            }
        }

        // Indicador de carga
        if (isUploading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.image_uploading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    // Diálogo de confirmación para eliminar
    ConfirmDeleteImageDialog(
        showDialog = imageToDelete != null,
        onDismiss = { imageToDelete = null },
        onConfirm = {
            imageToDelete?.let { onDeleteImage(it) }
            imageToDelete = null
        }
    )
}

/**
 * Botón para añadir nueva imagen
 */
@Composable
private fun AddImageButton(
    onClick: () -> Unit,
    isUploading: Boolean
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = !isUploading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_24),
                    contentDescription = stringResource(id = R.string.image_add),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.image_add),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Item individual de imagen
 */
@Composable
private fun ImageItem(
    imageUrl: String,
    isEditing: Boolean,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Imagen
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Botón de eliminar (solo en modo edición)
        if (isEditing) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close_24),
                    contentDescription = stringResource(id = R.string.image_delete),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
