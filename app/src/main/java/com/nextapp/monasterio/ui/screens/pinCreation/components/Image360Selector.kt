package com.nextapp.monasterio.ui.screens.pinCreation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Image360Selector(
    label: String,
    uri: Uri?,
    onPick: (Uri) -> Unit,
    onRemove: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { newUri ->
        newUri?.let { onPick(it) }
    }

    Text(
        text = label,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        AddImageButton(
            text = if (uri == null) "Añadir" else "Cambiar"
        ) {
            launcher.launch("image/*")
        }


        uri?.let {
            ImagePreviewCard(
                uri = it,
                onRemove = { onRemove() }
            )
        }
    }
}
