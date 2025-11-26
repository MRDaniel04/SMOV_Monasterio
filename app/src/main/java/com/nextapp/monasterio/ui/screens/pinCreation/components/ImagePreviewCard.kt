package com.nextapp.monasterio.ui.screens.pinCreation.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.nextapp.monasterio.R

@Composable
fun ImagePreviewCard(
    uri: Uri,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.size(120.dp)
    ) {
        Box(Modifier.size(120.dp)) {

            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .size(24.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_close_24),
                    contentDescription = "Eliminar",
                    tint = Color.White
                )
            }
        }
    }
}
