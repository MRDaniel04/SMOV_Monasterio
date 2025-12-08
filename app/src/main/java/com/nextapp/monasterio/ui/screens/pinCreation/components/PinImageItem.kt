package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
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
    val currentLanguage = LocalConfiguration.current.locales.get(0).language.lowercase()

    val titleToShow = when (currentLanguage) {
        "en" -> pinImage.titulo_en.ifBlank { pinImage.titulo_es }
        "de" -> pinImage.titulo_de.ifBlank { pinImage.titulo_es }
        "fr" -> pinImage.titulo_fr.ifBlank { pinImage.titulo_es }
        else -> pinImage.titulo_es // Por defecto o español
    }

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
                text = titleToShow,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                // Alineamos el texto dentro del ancho de la imagen
                modifier = Modifier.widthIn(max = 140.dp) // ⬅️ Ajustado al nuevo ancho de la tarjeta
            )
        } else if (pinImage.tag != null) {
            Text(
                text = stringResource(pinImage.tag!!.stringResId),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
