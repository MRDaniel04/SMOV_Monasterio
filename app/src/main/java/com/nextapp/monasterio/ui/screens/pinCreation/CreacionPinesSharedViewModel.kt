package com.nextapp.monasterio.ui.screens.pinCreation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.ui.screens.pinCreation.state.*

class CreacionPinSharedViewModel : ViewModel() {

    val titulo = TituloState()
    val descripcion = DescripcionState()
    val imagenes = ImagenesState()
    var imagen360 by mutableStateOf<Uri?>(null)
    val ubicacion = UbicacionState()

    // --- EDICIÓN ---
    var isEditing by mutableStateOf(false)
    var updateRequested by mutableStateOf(false)
    var editingPinId: String? = null
    var isUploading by mutableStateOf(false)
    var uploadMessage by mutableStateOf("")


    var modoMoverPin: Boolean = false
    var formSubmitted: Boolean = false
    var coordenadasFinales: Pair<Float, Float>? = null

    fun reset() {
        titulo.es = ""; titulo.en = ""; titulo.de = ""
        descripcion.es = ""; descripcion.en = ""; descripcion.de = ""

        imagenes.uris = emptyList()
        imagen360 = null

        ubicacion.displayName = ""
        modoMoverPin = false
        coordenadasFinales = null
        formSubmitted = false

        // limpiar modo edición
        isEditing = false
        editingPinId = null
    }

    /**
     * Carga un PinData en el ViewModel para editar.
     * Convierte las URLs a Uri.parse(...) para poder reutilizar el selector de imágenes.
     */
    fun loadPinForEditing(pin: PinData) {
        isEditing = true
        editingPinId = pin.id

        // Títulos
        titulo.es = pin.titulo ?: ""
        titulo.en = pin.tituloIngles ?: ""
        titulo.de = pin.tituloAleman ?: ""

        // Descripciones
        descripcion.es = pin.descripcion ?: ""
        descripcion.en = pin.descripcionIngles ?: ""
        descripcion.de = pin.descripcionAleman ?: ""

        // Ubicación
        ubicacion.displayName = pin.ubicacion?.name ?: ""


        imagenes.uris = when {
            pin.imagenesDetalladas.isNotEmpty() -> {
                pin.imagenesDetalladas.mapNotNull { img ->
                    try { Uri.parse(img.url) } catch (_: Exception) { null }
                }
            }

            pin.imagenes.any { it.startsWith("http://") || it.startsWith("https://") } -> {
                pin.imagenes.mapNotNull { urlStr ->
                    try {
                        if (urlStr.startsWith("http://") || urlStr.startsWith("https://"))
                            Uri.parse(urlStr) else null
                    } catch (_: Exception) { null }
                }
            }
            else -> {
                emptyList()
            }
        }


        // Imagen 360
        imagen360 = pin.vista360Url?.let { Uri.parse(it) }
    }
}
