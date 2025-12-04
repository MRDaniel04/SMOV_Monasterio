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
    private var isLoadingInitialData = false

    val descripcion = DescripcionState(onChanged = { checkIfModified() })
    val imagenes = ImagenesState()

    private var _imagen360 by mutableStateOf<Uri?>(null)
    var imagen360: Uri?
        get() = _imagen360
        set(value) {
            _imagen360 = value
            checkIfModified()
        }

    // ===========================================
    // 🆕 UBICACIÓN (Compleja, antes 'pinTitle')
    // ===========================================
    var _ubicacion_es by mutableStateOf("")

    var ubicacion_es: String
        get() = _ubicacion_es
        set(value) {
            _ubicacion_es = value
            if (!isLoadingInitialData) checkIfModified()
        }

    var ubicacion_en by mutableStateOf("") // Antes pinTitleIngles
    var ubicacion_de by mutableStateOf("") // Antes pinTitleAleman
    var ubicacion_fr by mutableStateOf("") // Antes pinTitleFrances

    // ===========================================
    // 🆕 ÁREA (Simple, antes 'pinUbicacion')
    // ===========================================
    var _area_es by mutableStateOf("")

    var area_es: String // Antes pinUbicacion
        get() = _area_es
        set(value) {
            _area_es = value
            updatePinArea(value) // ⬅️ Renombrada
            checkIfModified()
        }

    var area_en by mutableStateOf("") // Antes pinUbicacionIngles
    var area_de by mutableStateOf("") // Antes pinUbicacionAleman
    var area_fr by mutableStateOf("") // Antes pinUbicacionFrances


    // --- CAMPOS DE AUDIO (Se mantienen, ya usan _idioma) ---
    private var _audioUrl_es by mutableStateOf<String?>(null)
    var audioUrl_es: String?
        get() = _audioUrl_es
        set(value) {
            _audioUrl_es = value
            checkIfModified()
        }

    private var _audioUrl_en by mutableStateOf<String?>(null)
    var audioUrl_en: String?
        get() = _audioUrl_en
        set(value) {
            _audioUrl_en = value
            checkIfModified()
        }

    private var _audioUrl_de by mutableStateOf<String?>(null)
    var audioUrl_de: String?
        get() = _audioUrl_de
        set(value) {
            _audioUrl_de = value
            checkIfModified()
        }

    private var _audioUrl_fr by mutableStateOf<String?>(null)
    var audioUrl_fr: String?
        get() = _audioUrl_fr
        set(value) {
            _audioUrl_fr = value
            checkIfModified()
        }


    // --- RADIO ---
    private var _tapRadius by mutableStateOf<Float?>(0.06f)
    var tapRadius: Float?
        get() = _tapRadius
        set(value) {
            _tapRadius = value
            checkIfModified()
        }


    // --- EDICIÓN ---
    private var originalPin: PinData? = null


    var isEditing by mutableStateOf(false)
    var isModified by mutableStateOf(false)
    var updateRequested by mutableStateOf(false)
    var editingPinId: String? = null
    var isUploading by mutableStateOf(false)
    var uploadMessage by mutableStateOf("")


    var modoMoverPin: Boolean = false
    var formSubmitted: Boolean = false
    var coordenadasFinales: Pair<Float, Float>? = null

    fun reset() {

        ubicacion_es = ""
        ubicacion_en = ""
        ubicacion_de = ""
        ubicacion_fr = ""

        descripcion.es.value = ""; descripcion.en.value = ""; descripcion.de.value = ""; descripcion.fr.value = "";

        imagenes.images = emptyList()
        imagen360 = null

        area_es = ""
        area_en = ""
        area_de = ""
        area_fr = ""

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
        isLoadingInitialData = true


        try {
            // 🟦 UBICACIÓN (Compleja)
            _ubicacion_es = pin.ubicacion_es ?: ""
            ubicacion_en = pin.ubicacion_en ?: ""
            ubicacion_de = pin.ubicacion_de ?: ""
            ubicacion_fr = pin.ubicacion_fr ?: ""

            // 🟦 DESCRIPCIÓN
            descripcion.es.value = pin.descripcion_es ?: ""
            descripcion.en.value = pin.descripcion_en ?: ""
            descripcion.de.value = pin.descripcion_de ?: ""
            descripcion.fr.value = pin.descripcion_fr ?: ""


            // 🟦 ÁREA (Simple)
            _area_es = pin.area_es ?: ""
            Log.d("VALORES", "Valor Area: $_area_es")

            area_en = pin.area_en ?: ""
            area_de = pin.area_de ?: ""
            area_fr = pin.area_fr ?: ""

            _audioUrl_es = pin.audioUrl_es
            _audioUrl_en = pin.audioUrl_en
            _audioUrl_de = pin.audioUrl_de
            _audioUrl_fr = pin.audioUrl_fr

            _tapRadius = pin.tapRadius

            imagenes.images = when {
                pin.imagenesDetalladas.isNotEmpty() -> {
                    pin.imagenesDetalladas.mapNotNull { img ->

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
            _imagen360 = pin.vista360Url?.let { Uri.parse(it) }
            originalPin = pin.copy()
        } finally {
            isLoadingInitialData = false
        }

        checkIfModified()
    }


    /**
     * Se llama cuando el usuario pulsa el botón de Guardar en modo edición.
     */
    fun onSaveClicked() {
        // Solo permitimos guardar si estamos en modo edición y no estamos ya subiendo.
        if (!isEditing || isUploading || formSubmitted) {
            Log.d("FLUJO_PIN", "VM: onSaveClicked ignorado. isEditing=$isEditing, isUploading=$isUploading, formSubmitted=$formSubmitted")
            return
        }

        val id = editingPinId ?: run {
            Log.e("VM", "Error: Intento de actualizar sin editingPinId")
            return
        }

        // CAMBIO: Log de inicio de guardado
        Log.d("FLUJO_PIN", "VM: 🚀 Iniciando subida de actualización para Pin ID: $id")


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

                    // --- UBICACIONES (Compleja) ---
                    ubicacion_es = ubicacion_es, // ⬅️ Nuevo nombre
                    ubicacion_en = ubicacion_en,
                    ubicacion_de = ubicacion_de,
                    ubicacion_fr = ubicacion_fr,

                    // --- DESCRIPCIONES ---
                    descripcion_es = descripcion.es.value, // ⬅️ Nuevo nombre
                    descripcion_en = descripcion.en.value,
                    descripcion_de = descripcion.de.value,
                    descripcion_fr = descripcion.fr.value,

                    // --- ÁREAS (Simple) ---
                    area_es = area_es, // ⬅️ Nuevo nombre
                    area_en = area_en,
                    area_de = area_de,
                    area_fr = area_fr,

                    // --- AUDIO ---
                    audioUrl_es = audioUrl_es,
                    audioUrl_en = audioUrl_en,
                    audioUrl_de = audioUrl_de,
                    audioUrl_fr = audioUrl_fr,

                    // --- RADIO e IMÁGENES ---
                    tapRadius = tapRadius ?: 0.06f,
                    imagenes = imageUrls,
                    imagen360 = imagen360Url
                )

                // Éxito
                uploadMessage = "Pin actualizado con éxito."
                updateRequested = true
                formSubmitted = false

                isModified = false

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


    /**
     * Se llama cuando el usuario pulsa el botón de Guardar en modo CREACIÓN.
     */
    fun onCreateClicked(onSuccess: () -> Unit) {
        // Solo permitimos crear si NO estamos en modo edición y no estamos ya subiendo.
        if (isEditing || isUploading || formSubmitted) {
            Log.d("FLUJO_PIN", "VM: onCreateClicked ignorado. isEditing=$isEditing, isUploading=$isUploading, formSubmitted=$formSubmitted")
            return
        }

        // 1. Marcar el estado del formulario como SUBMITTED
        formSubmitted = true
        Log.d("FLUJO_PIN", "VM: ✅ Formulario listo. Establecido formSubmitted = true.")

        onSuccess()

    }

    companion object {
        private val AREA_TRADUCCIONES = mapOf(
            "Iglesia" to Triple("Church", "Kirche", "Église"),
            "Monasterio" to Triple("Monastery", "Kloster", "Monastère")
        )
    }

    /**
     * Actualiza la ubicación principal y asigna automáticamente las traducciones.
     */
    fun updatePinArea(newArea: String) {
        // 1. Actualiza el valor principal
        _area_es = newArea // ⬅️ Antes _pinUbicacion

        // 2. Busca las traducciones automáticas
        val translations = AREA_TRADUCCIONES[newArea]

        if (translations != null) {
            // Asigna las traducciones
            area_en = translations.first // ⬅️ Antes pinUbicacionIngles
            area_de = translations.second  // ⬅️ Antes pinUbicacionAleman
            area_fr = translations.third // ⬅️ Antes pinUbicacionFrances
        } else {
            // Si es un área manual o no predefinida, límpialas
            area_en = ""
            area_de = ""
            area_fr = ""
        }
    }

    fun checkIfModified() {

        if (isLoadingInitialData) {
            Log.d("FLUJO_PIN", "VM: checkIfModified() cancelado por carga inicial")
            return
        }

        Log.d("FLUJO_PIN", "VM: -> Ejecutando checkIfModified(). isEditing=$isEditing, originalPinNull=${originalPin==null}")

        if (!isEditing || originalPin == null) {
            Log.d("FLUJO_PIN", "VM: Salida temprana: isEditing=$isEditing, originalPinNull=${originalPin==null}")
            isModified = false
            return
        }


        val original = originalPin!!
        val wasModifiedBefore = isModified

        // Comparación de campos
        // ⚠️ UBICACIÓN (Compleja)
        val isUbicacionModified = ubicacion_es != original.ubicacion_es
        val isUbicacionTradsModified = ubicacion_en != original.ubicacion_en ||
                ubicacion_de != original.ubicacion_de ||
                ubicacion_fr != original.ubicacion_fr

        // ⚠️ ÁREA (Simple)
        val isAreaModified = _area_es != original.area_es
        val isAreaTradsModified = area_en != original.area_en ||
                area_de != original.area_de ||
                area_fr != original.area_fr

        // ⚠️ DESCRIPCIÓN
        val isDescModified = descripcion.es.value != original.descripcion_es ||
                descripcion.en.value != original.descripcion_en ||
                descripcion.de.value != original.descripcion_de ||
                descripcion.fr.value != original.descripcion_fr

        // Comparación de audio y radio
        val isAudioModified = audioUrl_es != original.audioUrl_es ||
                audioUrl_en != original.audioUrl_en ||
                audioUrl_de != original.audioUrl_de ||
                audioUrl_fr != original.audioUrl_fr

        val isRadiusModified = tapRadius != original.tapRadius

        // 1. Crear una lista canónica (URL, Tag) del Pin Original, ordenada por URL.
        // --- NORMALIZACIÓN DE IMÁGENES ---
        val originalImageCanonical = original.imagenesDetalladas
            .map { img ->
                val normUrl = try { Uri.parse(img.url).toString() } catch (_: Exception) { img.url }
                val normTag = img.tipo ?: ""
                normUrl to normTag
            }
            .sortedBy { it.first }

        val currentImageCanonical = imagenes.images
            .map { img ->
                val normUrl = try { img.uri.toString() } catch (_: Exception) { "" }
                val normTag = img.tag?.toFirestoreString() ?: ""
                normUrl to normTag
            }
            .sortedBy { it.first }


        Log.d("FLUJO_PIN", "VM: originalImageCanonical = $originalImageCanonical")
        Log.d("FLUJO_PIN", "VM: currentImageCanonical  = $currentImageCanonical")


        // 3. Comparar ambas listas canónicas. Si difieren en URLs o Tags, es modificado.
        val isImagesModified = originalImageCanonical != currentImageCanonical ||
                (imagen360?.toString() ?: "") != (original.vista360Url ?: "")


        // Si cualquier campo es diferente, el pin está modificado
        isModified = isUbicacionModified ||
                isAreaModified ||
                isUbicacionTradsModified ||
                isAreaTradsModified ||
                isDescModified ||
                isAudioModified ||
                isRadiusModified ||
                isImagesModified

        Log.d("FLUJO_PIN", "VM: Resultado check -> isUbicacionModified=$isUbicacionModified, isAreaModified=$isAreaModified, isDescModified=$isDescModified, isImagesModified=$isImagesModified, finalIsModified=$isModified (antes=$wasModifiedBefore)")


    }


}
