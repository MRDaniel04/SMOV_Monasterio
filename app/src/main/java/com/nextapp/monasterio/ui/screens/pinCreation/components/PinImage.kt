package com.nextapp.monasterio.ui.screens.pinCreation.state

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nextapp.monasterio.models.ImageTag

// Clase que representa una imagen con su URI y su etiqueta
data class PinImage(
    val id: String = java.util.UUID.randomUUID().toString(), // <--- NUEVO
    val uri: Uri,
    var tag: ImageTag? = null
)

class ImagenesState(
    images: List<PinImage> = emptyList()
) {

    var images by mutableStateOf(images)

    val uris: List<Uri>
        get() = images.map { it.uri }

    fun addTaggedImage(pinImage: PinImage) {
        images = images + pinImage
    }

    fun remove(uriString: String) {
        images = images.filter { it.uri.toString() != uriString }
    }

    fun updateTag(uri: Uri, newTag: ImageTag) {
        images = images.map { pinImage ->
            if (pinImage.uri == uri) {
                // Usamos copy() para que Compose detecte el cambio de estado en la lista inmutable
                pinImage.copy(tag = newTag)
            } else {
                pinImage
            }
        }
    }


    val allImagesTagged: Boolean
        get() = images.all { it.tag != null }
}