package com.nextapp.monasterio.ui.screens.pinCreation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextapp.monasterio.ui.screens.pinCreation.components.AddImageButton
import com.nextapp.monasterio.ui.screens.pinCreation.components.ImagePreviewCard
import com.nextapp.monasterio.ui.screens.pinCreation.state.ImagenesState

@Composable
fun PinImageSelector(
    label: String,
    state: ImagenesState,
    mandatory: Boolean
) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        state.addImages(uris)
    }

    Text(
        text = "$label (${state.uris.size} añadidas)",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        ),
    )

    Spacer(Modifier.height(10.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AddImageButton { launcher.launch("image/*") }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.uris) { uri ->
                ImagePreviewCard(
                    uri = uri,
                    onRemove = {
                         state.remove(uri)
                    }
                )
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    // ---------- MENSAJE DE ERROR ----------
    if (mandatory && state.uris.isEmpty()) {
        Text(
            text = "Debe añadir al menos una imagen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp)
        )
    }


}
