package com.nextapp.monasterio.ui.screens.pinCreation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.nextapp.monasterio.models.ImageTag
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.ui.screens.pinCreation.state.*


class CreacionPinSharedViewModel : ViewModel() {

    val descripcion = DescripcionState()
    val imagenes = ImagenesState()
    var imagen360 by mutableStateOf<Uri?>(null)
    var pinTitle by mutableStateOf("")     // ✅ Sustituye a ubicacionDetallada/Manual
    var pinUbicacion by mutableStateOf("")  // ✅ Sustituye a areaPrincipal


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

        descripcion.es = ""; descripcion.en = ""; descripcion.de = ""

        imagenes.images = emptyList()
        imagen360 = null

        pinTitle = ""
        pinUbicacion = ""

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

        pinTitle = pin.titulo ?: ""

        // Descripciones
        descripcion.es = pin.descripcion ?: ""
        descripcion.en = pin.descripcionIngles ?: ""
        descripcion.de = pin.descripcionAleman ?: ""

        pinUbicacion = pin.ubicacion?.name ?: ""


        imagenes.images = when {
            pin.imagenesDetalladas.isNotEmpty() -> {
                pin.imagenesDetalladas.mapNotNull { img ->

                    // ⭐ NUEVO LOG: Muestra el objeto ImagenData COMPLETO ⭐
                    Log.d("PinEdit-IMG", "ImagenData COMPLETO: $img")
                    Log.d("PinEdit-TAG", "Procesando imagen, Tipo desde DB: '${img.tipo}'")
                    try {
                        // 1. Convertir el String de Firebase (img.tipo) a Enum (ImageTag)
                        val tagEnum = ImageTag.fromFirestoreString(img.tipo)

                        // 2. Crear el objeto de estado PinImage
                        Log.d("PinEdit-TAG", "Procesando imagen, Tipo usado: '${img.tipo}'")
                        PinImage(
                            uri = Uri.parse(img.url),
                            tag = tagEnum
                        )
                    } catch (e: Exception) {
                        Log.e("PinEdit", "Error al cargar tag ${img.tipo}: ${e.message}")
                        null
                    }
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
