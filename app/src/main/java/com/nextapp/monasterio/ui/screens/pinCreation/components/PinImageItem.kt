package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.models.ImageTag
import com.nextapp.monasterio.ui.screens.pinCreation.state.PinImage

@Composable
fun PinImageItem(
    pinImage: PinImage,
    onRemove: () -> Unit,
    onTagSelected: (ImageTag) -> Unit
) {
    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
        // 1. La tarjeta de previsualización (la antigua ImagePreviewCard)
        ImagePreviewCard(
            pinImage = pinImage,
            onRemove = onRemove,
            onTagSelected = onTagSelected // Pasamos la función para la reedición
        )


    }
}

