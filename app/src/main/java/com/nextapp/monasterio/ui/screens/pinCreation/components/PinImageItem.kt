package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.models.ImageTag
import com.nextapp.monasterio.ui.screens.pinCreation.state.PinImage

// PinImageItem.kt

@Composable
fun PinImageItem(
    pinImage: PinImage,
    onRemove: () -> Unit,
    onEditDetails: (PinImage) -> Unit // 🆕 Nuevo Callback para la edición
) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally // Centramos la columna
    ) {
        // 1. La tarjeta de previsualización
        ImagePreviewCard(
            pinImage = pinImage,
            onRemove = onRemove,
            onEditDetails = onEditDetails // 🆕 Pasamos el nuevo callback
        )

        // 2. Título en español visible
        if (pinImage.titulo_es.isNotBlank()) {
            Text(
                text = pinImage.titulo_es,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                // Alineamos el texto dentro del ancho de la imagen
                modifier = Modifier.widthIn(max = 120.dp)
            )
        } else if (pinImage.tag != null) {
            Text(
                text = pinImage.tag!!.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
