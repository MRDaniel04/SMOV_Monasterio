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

    // CAMPO IMAGEN 360
// Implementación con setter explícito para forzar checkIfModified()
    private var _imagen360 by mutableStateOf<Uri?>(null)
    var imagen360: Uri?
        get() = _imagen360
        set(value) {
            _imagen360 = value
            checkIfModified() // ✅ LLAMADA AGREGADA
        }

    var _pinTitle by mutableStateOf("")

    // CAMPO TÍTULO (Implementación ya correcta)
    var pinTitle: String
        get() = _pinTitle
        set(value) {
            _pinTitle = value
            checkIfModified() // ✅ Llama a la detección de cambios
        }

    var _pinUbicacion by mutableStateOf("")

    // CAMPO UBICACIÓN (Implementación ya correcta)
    var pinUbicacion: String
        get() = _pinUbicacion
        set(value) {
            _pinUbicacion = value
            updatePinUbicacion(value) // Llama a la lógica de traducción
            checkIfModified() // ✅ Llama a la detección de cambios
        }

    // CAMPOS DE TRADUCCIÓN (Se mantienen igual, asumiendo que son actualizados por funciones que ya llaman a checkIfModified)
    var pinTitleIngles by mutableStateOf("")
    var pinTitleAleman by mutableStateOf("")
    var pinTitleFrances by mutableStateOf("")
    var pinUbicacionIngles by mutableStateOf("")
    var pinUbicacionAleman by mutableStateOf("")
    var pinUbicacionFrances by mutableStateOf("")

    // --- CAMPOS DE AUDIO ---
// CORRECCIÓN: Aplicar setter explícito para audioUrl_es
    private var _audioUrl_es by mutableStateOf<String?>(null)
    var audioUrl_es: String?
        get() = _audioUrl_es
        set(value) {
            _audioUrl_es = value
            checkIfModified() // ✅ LLAMADA AGREGADA
        }

    // CORRECCIÓN: Aplicar setter explícito para audioUrl_en
    private var _audioUrl_en by mutableStateOf<String?>(null)
    var audioUrl_en: String?
        get() = _audioUrl_en
        set(value) {
            _audioUrl_en = value
            checkIfModified() // ✅ LLAMADA AGREGADA
        }

    // CORRECCIÓN: Aplicar setter explícito para audioUrl_de
    private var _audioUrl_de by mutableStateOf<String?>(null)
    var audioUrl_de: String?
        get() = _audioUrl_de
        set(value) {
            _audioUrl_de = value
            checkIfModified() // ✅ LLAMADA AGREGADA
        }

    // CORRECCIÓN: Aplicar setter explícito para audioUrl_fr
    private var _audioUrl_fr by mutableStateOf<String?>(null)
    var audioUrl_fr: String?
        get() = _audioUrl_fr
        set(value) {
            _audioUrl_fr = value
            checkIfModified() // ✅ LLAMADA AGREGADA
        }


    // --- RADIO ---
// CORRECCIÓN: Aplicar setter explícito para tapRadius
    private var _tapRadius by mutableStateOf<Float?>(0.06f)
    var tapRadius: Float?
        get() = _tapRadius
        set(value) {
            _tapRadius = value
            checkIfModified() // ✅ LLAMADA AGREGADA
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

        pinTitle = ""
        pinTitleIngles = ""
        pinTitleAleman = ""
        pinTitleFrances = ""
        descripcion.es = ""; descripcion.en = ""; descripcion.de = ""; descripcion.fr = "";

        imagenes.images = emptyList()
        imagen360 = null

        pinUbicacion = ""
        pinUbicacionIngles = ""
        pinUbicacionAleman = ""
        pinUbicacionFrances = ""

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
        pinTitleIngles = pin.tituloIngles ?: ""
        pinTitleAleman = pin.tituloAleman ?: ""
        pinTitleFrances = pin.tituloFrances ?: ""

        descripcion.es = pin.descripcion ?: ""
        descripcion.en = pin.descripcionIngles ?: ""
        descripcion.de = pin.descripcionAleman ?: ""
        descripcion.fr = pin.descripcionFrances ?: ""

        pinUbicacion = pin.ubicacion?.name ?: ""
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
        originalPin = pin.copy()
        checkIfModified()
        Log.d("FLUJO_PIN", "VM: ✅ Pin Original cargado. ID: ${editingPinId}. isModified=false.")
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
                    tapRadius = tapRadius ?: 0.06f,
                    imagenes = imageUrls,
                    imagen360 = imagen360Url
                )

                // Éxito
                uploadMessage = "Pin actualizado con éxito."
                updateRequested = true // Notifica a la UI que la actualización terminó
                formSubmitted = false

                // CAMBIO: Resetear isModified después de guardar con éxito
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

    fun checkIfModified() {
        // CAMBIO: Log de inicio
        Log.d("FLUJO_PIN", "VM: -> Ejecutando checkIfModified().")

        // Si no estamos editando o no hay pin original, no hay cambios que rastrear.
        if (!isEditing || originalPin == null) {
            // CAMBIO: Log de salida temprana
            Log.d("FLUJO_PIN", "VM: ❌ Salida temprana. isEditing: $isEditing, originalPin is null: ${originalPin == null}")
            isModified = false
            return
        }

        val original = originalPin!!

        // Comparación de campos simples (títulos, ubicaciones, descripciones)
        val isTitleModified = pinTitle != original.titulo
        val isUbicacionModified = _pinUbicacion != original.ubicacion?.name

        // Comparación de traducciones
        val isTitleTradsModified = pinTitleIngles != original.tituloIngles ||
                pinTitleAleman != original.tituloAleman ||
                pinTitleFrances != original.tituloFrances

        val isUbicacionTradsModified = pinUbicacionIngles != original.ubicacionIngles?.name ||
                pinUbicacionAleman != original.ubicacionAleman?.name ||
                pinUbicacionFrances != original.ubicacionFrances?.name

        val isDescModified = descripcion.es != original.descripcion ||
                descripcion.en != original.descripcionIngles ||
                descripcion.de != original.descripcionAleman ||
                descripcion.fr != original.descripcionFrances

        // Comparación de audio y radio
        val isAudioModified = audioUrl_es != original.audioUrl_es ||
                audioUrl_en != original.audioUrl_en ||
                audioUrl_de != original.audioUrl_de ||
                audioUrl_fr != original.audioUrl_fr

        val isRadiusModified = tapRadius != original.tapRadius

        // 1. Crear una lista canónica (URL, Tag) del Pin Original, ordenada por URL.
        val originalImageCanonical = original.imagenesDetalladas
            .map { it.url to it.tipo } // Pair<String, String> (URL, TagString de Firebase)
            .sortedBy { it.first }

        // 2. Crear una lista canónica (URL, Tag) del estado actual, ordenada por URL.
        // Asumimos que PinImage.tag tiene un método toFirestoreString()
        val currentImageCanonical = imagenes.images
            .map { it.uri.toString() to it.tag?.toFirestoreString() } // PinImage -> Pair<String, String>
            .sortedBy { it.first }

        // 3. Comparar ambas listas canónicas. Si difieren en URLs o Tags, es modificado.
        val isImagesModified = originalImageCanonical != currentImageCanonical ||
                imagen360?.toString() != original.vista360Url

        // Si cualquier campo es diferente, el pin está modificado
        isModified = isTitleModified ||
                isUbicacionModified ||
                isTitleTradsModified ||
                isUbicacionTradsModified ||
                isDescModified ||
                isAudioModified ||
                isRadiusModified ||
                isImagesModified

        // CAMBIO: Log de resultado
        Log.d("FLUJO_PIN", "VM: Resultado de checkIfModified:")
        Log.d("FLUJO_PIN", "   - Titulo/Ubicacion Modificado: $isTitleModified / $isUbicacionModified")
        Log.d("FLUJO_PIN", "   - Descripciones Modificado: $isDescModified")
        Log.d("FLUJO_PIN", "   - isModified (FINAL): $isModified")
    }


}
