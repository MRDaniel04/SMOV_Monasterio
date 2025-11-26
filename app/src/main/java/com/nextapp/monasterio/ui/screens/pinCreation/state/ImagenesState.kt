package com.nextapp.monasterio.ui.screens.pinCreation.state


import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class ImagenesState(
    uris: List<Uri> = emptyList()
) {
    var uris by mutableStateOf(uris)

    fun addImages(newUris: List<Uri>) {
        uris = uris + newUris
    }

    fun remove(uri: Uri) {
        uris = uris - uri
    }
}
