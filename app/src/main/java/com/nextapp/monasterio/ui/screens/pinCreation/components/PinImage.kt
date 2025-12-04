package com.nextapp.monasterio.ui.screens.pinCreation.state

import android.net.Uri
import android.util.Log
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
        Log.d("FLUJO_PIN_IMAGES", "Imagen añadida: ${pinImage.uri}. now size=${images.size}")
    }

    fun remove(uriString: String) {
        images = images.filter { it.uri.toString() != uriString }
        Log.d("FLUJO_PIN_IMAGES", "Imagen eliminada: $uriString. now size=${images.size}")
    }

    fun updateTag(uri: Uri, newTag: ImageTag) {
        images = images.map { pinImage ->
            if (pinImage.uri == uri) {
                pinImage.copy(tag = newTag)
            } else pinImage
        }
        Log.d("FLUJO_PIN_IMAGES", "Imagen tag actualizada: ${uri} -> $newTag. canonical now: ${images.map { it.uri.toString() to it.tag }}")
    }


    val allImagesTagged: Boolean
        get() = images.all { it.tag != null }
}