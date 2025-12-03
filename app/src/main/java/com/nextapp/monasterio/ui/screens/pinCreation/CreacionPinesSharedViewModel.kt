package com.nextapp.monasterio.ui.screens.pinCreation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextapp.monasterio.models.ImageTag
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.repository.PinRepository
import com.nextapp.monasterio.ui.screens.pinCreation.state.*
import kotlinx.coroutines.launch


class CreacionPinSharedViewModel : ViewModel() {

    private val pinRepository = PinRepository
    val descripcion = DescripcionState()
    val imagenes = ImagenesState()
    var imagen360 by mutableStateOf<Uri?>(null)
    var pinTitle by mutableStateOf("")
    // ✅ La variable pinUbicacion ahora usa el getter/setter de una propiedad

    var _pinUbicacion by mutableStateOf("")
    var pinUbicacion: String
        get() = _pinUbicacion
        set(value) {
            _pinUbicacion = value
            updatePinUbicacion(value) // Llama a la lógica de traducción
        }

    var pinTitleIngles by mutableStateOf("")
    var pinTitleAleman by mutableStateOf("")
    var pinTitleFrances by mutableStateOf("")
    var pinUbicacionIngles by mutableStateOf("")
    var pinUbicacionAleman by mutableStateOf("")
    var pinUbicacionFrances by mutableStateOf("")

    // --- CAMPOS DE AUDIO ---
    var audioUrl_es by mutableStateOf<String?>(null)
    var audioUrl_en by mutableStateOf<String?>(null)
    var audioUrl_de by mutableStateOf<String?>(null)
    var audioUrl_fr by mutableStateOf<String?>(null)

    // --- RADIO ---
    var tapRadius by mutableStateOf<Float?>(0.06f)


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

        audioUrl_es = null
        audioUrl_en = null
        audioUrl_de = null
        audioUrl_fr = null

        modoMoverPin = false
        coordenadasFinales = null
        formSubmitted = false

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

        descripcion.es = pin.descripcion ?: ""
        descripcion.en = pin.descripcionIngles ?: ""
        descripcion.de = pin.descripcionAleman ?: ""

        _pinUbicacion = pin.ubicacion?.name ?: ""

        pinTitleIngles = pin.tituloIngles ?: ""
        pinTitleAleman = pin.tituloAleman ?: ""
        pinTitleFrances = pin.tituloFrances ?: ""

        descripcion.es = pin.descripcion ?: ""
        descripcion.en = pin.descripcionIngles ?: ""
        descripcion.de = pin.descripcionAleman ?: ""
        descripcion.fr = pin.descripcionFrances ?: ""

        pinUbicacionIngles = pin.ubicacionIngles?.name ?: ""
        pinUbicacionAleman = pin.ubicacionAleman?.name ?: ""
        pinUbicacionFrances = pin.ubicacionFrances?.name ?: ""

        audioUrl_es = pin.audioUrl_es
        audioUrl_en = pin.audioUrl_en
        audioUrl_de = pin.audioUrl_de
        audioUrl_fr = pin.audioUrl_fr

        tapRadius = pin.tapRadius

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


    /**
     * Se llama cuando el usuario pulsa el botón de Guardar en modo edición.
     */
    fun onSaveClicked() {
        // Solo permitimos guardar si estamos en modo edición y no estamos ya subiendo.
        if (!isEditing || isUploading || formSubmitted) return

        val id = editingPinId ?: run {
            Log.e("VM", "Error: Intento de actualizar sin editingPinId")
            return
        }

        // 1. Recopilar datos
        val imageUrls = imagenes.images.map { it.uri.toString() }
        val imagen360Url = imagen360?.toString()
        val finalTapRadius = tapRadius ?: 0.06f // Usar un valor por defecto si es nulo

        // 2. Estado de UI: Comenzar subida
        formSubmitted = true
        isUploading = true
        uploadMessage = "Actualizando pin..."

        viewModelScope.launch {
            try {
                pinRepository.updatePin(
                    pinId = id,
                    // --- TÍTULOS y DESCRIPCIONES ---
                    titulo = pinTitle,
                    descripcion = descripcion.es,
                    tituloIngles = pinTitleIngles,
                    descripcionIngles = descripcion.en,
                    tituloAleman = pinTitleAleman,
                    descripcionAleman = descripcion.de,
                    tituloFrances = pinTitleFrances,
                    descripcionFrances = descripcion.fr,

                    // --- UBICACIONES ---
                    ubicacion = pinUbicacion,
                    ubicacionIngles = pinUbicacionIngles,
                    ubicacionAleman = pinUbicacionAleman,
                    ubicacionFrances = pinUbicacionFrances,

                    // --- AUDIO ---
                    audioUrl_es = audioUrl_es,
                    audioUrl_en = audioUrl_en,
                    audioUrl_de = audioUrl_de,
                    audioUrl_fr = audioUrl_fr,

                    // --- RADIO e IMÁGENES ---
                    tapRadius = finalTapRadius,
                    imagenes = imageUrls,
                    imagen360 = imagen360Url
                )

                // Éxito
                uploadMessage = "Pin actualizado con éxito."
                updateRequested = true // Notifica a la UI que la actualización terminó
                formSubmitted = false

            } catch (e: Exception) {
                // Fracaso
                Log.e("VM", "Fallo al actualizar el pin: $e")
                uploadMessage = "Error: " + (e.message ?: "Fallo desconocido")
                formSubmitted = false

            } finally {
                isUploading = false
            }
        }
    }

    companion object {
        // Definimos las traducciones de las Áreas Principales (ES -> (EN, DE, FR))
        private val UBICACION_TRADUCCIONES = mapOf(
            "Iglesia" to Triple("Church", "Kirche", "Église"),
            "Monasterio" to Triple("Monastery", "Kloster", "Monastère")
        )
    }

    /**
     * Actualiza la ubicación principal y asigna automáticamente las traducciones.
     */
    fun updatePinUbicacion(newUbicacion: String) {
        // 1. Actualiza el valor principal
        _pinUbicacion = newUbicacion

        // 2. Busca las traducciones automáticas
        val translations = UBICACION_TRADUCCIONES[newUbicacion]

        if (translations != null) {
            // Asigna las traducciones
            pinUbicacionIngles = translations.first // "Church"
            pinUbicacionAleman = translations.second  // "Kirche"
            pinUbicacionFrances = translations.third // "Église"
        } else {
            // Si es una ubicación manual o no predefinida, límpialas (opcional: o déjalas como estaban)
            pinUbicacionIngles = ""
            pinUbicacionAleman = ""
            pinUbicacionFrances = ""
        }
    }


}
