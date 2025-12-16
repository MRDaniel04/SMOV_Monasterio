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
    var tag: ImageTag? = null,
    val titulo_es: String = "",
    val titulo_en: String = "",
    val titulo_de: String = "",
    val titulo_fr: String = ""
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

    // ⚠️ Esta función (updateTag) ya no es la principal, pero la dejamos por compatibilidad si no se elimina.

    fun updateTag(uri: Uri, newTag: ImageTag) {
        images = images.map { pinImage ->
            if (pinImage.uri == uri) {
                pinImage.copy(tag = newTag)
            } else pinImage
        }
    }

    // 🆕 FUNCIÓN CLAVE: Actualiza la etiqueta y los 4 títulos de la imagen.
    fun updateImageDetails(
        targetUri: Uri,
        newTag: ImageTag?,
        titulo_es: String,
        titulo_en: String,
        titulo_de: String,
        titulo_fr: String
    ) {
        images = images.map { pinImage ->
            if (pinImage.uri == targetUri) {
                pinImage.copy(
                    tag = newTag,
                    titulo_es = titulo_es.trim(),
                    titulo_en = titulo_en.trim(),
                    titulo_de = titulo_de.trim(),
                    titulo_fr = titulo_fr.trim()
                )
            } else pinImage
        }
    }


    // ⚠️ VALIDACIÓN ACTUALIZADA: Ahora requiere Tag y Título en Español (ES)
    val allImagesTagged: Boolean
        get() = images.all { it.tag != null && it.titulo_es.isNotBlank() }
}