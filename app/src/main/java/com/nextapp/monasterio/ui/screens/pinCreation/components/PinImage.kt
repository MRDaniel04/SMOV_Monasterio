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
    // La lista ahora almacena PinImage
    var images by mutableStateOf(images)

    // Funciones actualizadas para usar PinImage
    val uris: List<Uri>
        get() = images.map { it.uri }

    // Función original (mantenemos por si el código antiguo la usaba, pero el nuevo flujo no la usa)
    fun addImages(newUris: List<Uri>) {
        images = images + newUris.map { PinImage(uri = it) }
    }


    // NUEVO: Función para añadir una imagen que YA HA SIDO ETIQUETADA
    fun addTaggedImage(pinImage: PinImage) {
        images = images + pinImage
    }

    fun remove(uriString: String) {
        images = images.filter { it.uri.toString() != uriString }
    }


    // Nueva función para actualizar la etiqueta de una imagen específica
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

    // Propiedad calculada para la validación final
    val allImagesTagged: Boolean
        get() = images.all { it.tag != null }
}