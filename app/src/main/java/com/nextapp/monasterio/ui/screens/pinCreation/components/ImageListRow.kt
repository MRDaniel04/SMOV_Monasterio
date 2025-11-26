package com.nextapp.monasterio.ui.screens.pinCreation.components

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun ImageListRow(
    uris: List<Uri>,
    onRemove: (Uri) -> Unit
) {
    LazyRow(contentPadding = PaddingValues(end = 8.dp)) {
        items(uris) { uri ->
            ImagePreviewCard(
                uri = uri,
                onRemove = { onRemove(uri) }
            )
        }
    }
}
