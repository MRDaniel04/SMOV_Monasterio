package com.nextapp.monasterio.ui.screens.pinCreation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextapp.monasterio.models.ImageTag
import com.nextapp.monasterio.models.ImagenData
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.repository.PinRepository
import com.nextapp.monasterio.ui.screens.pinCreation.state.*
import kotlinx.coroutines.launch


// --- NUEVO DATA CLASS PARA GESTIÓN DE TRADUCCIONES MANUALES DEL TÍTULO (Ubicación) ---
data class PinTitleManualTrads(
    var en: String = "",
    var de: String = "",
    var fr: String = ""
)

data class PinImageCanonical(
    val id: String,
    val url: String,
    val tipo: String,
    val titulo_es: String,
    val titulo_en: String,
    val titulo_de: String,
    val titulo_fr: String
)

class CreacionPinSharedViewModel : ViewModel() {

    private val pinRepository = PinRepository
    private var isLoadingInitialData = false

    val descripcion = DescripcionState(onChanged = { checkIfModified() })
    val imagenes = ImagenesState()
    private var originalAudioUrls: Map<String, String?> = emptyMap()

    private var _imagen360 by mutableStateOf<Uri?>(null)
    var imagen360: Uri?
        get() = _imagen360
        set(value) {
            _imagen360 = value
            if (!isLoadingInitialData) checkIfModified()
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

    var pinTitleManualTrads by mutableStateOf(PinTitleManualTrads())
        private set // ⚠️ Hacemos el setter privado para forzar el uso de una función.

    private var _area_es_internal by mutableStateOf("")

    var area_es: String // Campo principal obligatorio
        get() = _area_es_internal
        set(value) {
            _area_es_internal = value
            updatePinArea(value) // ⬅️ Dispara la traducción automática.
            if (!isLoadingInitialData) checkIfModified()
        }

    private var area_traducciones_automaticas: Triple<String?, String?, String?> = Triple(null, null, null)

    // 🆕 Exponemos las traducciones automáticas del Área (para uso en la UI/Repositorio)
    val area_en: String?
        get() = area_traducciones_automaticas.first

    val area_de: String?
        get() = area_traducciones_automaticas.second

    val area_fr: String?
        get() = area_traducciones_automaticas.third

    companion object {
        // Traducciones de las opciones fijas para el ÁREA
        private val AREA_TRADUCCIONES = mapOf(
            "Iglesia" to Triple("Church", "Kirche", "Église"),
            "Monasterio" to Triple("Monastery", "Kloster", "Monastère")
        )

        // Opciones del desplegable fijo (para ayudar en loadPinForEditing)
        val ubicacionDetalladaOptionsFijas = listOf("Crucero", "Lado de la Epistola", "Trascoro", "Coro", "Capillad del nacimiento")
    }

    private fun updatePinArea(newArea: String) {
        val translations = AREA_TRADUCCIONES[newArea]
        if (translations != null) {
            area_traducciones_automaticas = Triple(translations.first, translations.second, translations.third)
        } else {
            // Si es un área no predefinida (Ej: texto manual en un futuro), límpialas.
            area_traducciones_automaticas = Triple(null, null, null)
        }
    }

    fun updateTitleManualTrads(en: String = pinTitleManualTrads.en, de: String = pinTitleManualTrads.de, fr: String = pinTitleManualTrads.fr) {
        pinTitleManualTrads = PinTitleManualTrads(en, de, fr)
        if (!isLoadingInitialData) checkIfModified()
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
        pinTitleManualTrads = PinTitleManualTrads()

        _area_es_internal = ""
        area_traducciones_automaticas = Triple(null, null, null)

        descripcion.reset() // Asumiendo que DescripcionState tiene un reset()

        imagenes.images = emptyList()
        imagen360 = null

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

            val isManualEntry = !ubicacionDetalladaOptionsFijas.contains(pin.ubicacion_es)

            if (isManualEntry) {
                // Carga las traducciones manuales desde PinData
                pinTitleManualTrads = PinTitleManualTrads(
                    en = pin.ubicacion_en.orEmpty(),
                    de = pin.ubicacion_de.orEmpty(),
                    fr = pin.ubicacion_fr.orEmpty()
                )
            } else {
                // Si es una opción fija, limpiamos las traducciones manuales
                pinTitleManualTrads = PinTitleManualTrads()
            }

            // 🟦 DESCRIPCIÓN
            descripcion.updateEs(pin.descripcion_es ?: "")
            descripcion.updateEn(pin.descripcion_en ?: "")
            descripcion.updateDe(pin.descripcion_de ?: "")
            descripcion.updateFr(pin.descripcion_fr ?: "")


            // 🟦 ÁREA (Simple)
            area_es = pin.area_es ?: ""
            originalAudioUrls = mapOf(
                "es" to pin.audioUrl_es,
                "en" to pin.audioUrl_en,
                "de" to pin.audioUrl_de,
                "fr" to pin.audioUrl_fr
            )

            imagenes.images = when {
                pin.imagenesDetalladas.isNotEmpty() -> {
                    pin.imagenesDetalladas.mapNotNull { img ->
                        try {
                            val tagEnum = ImageTag.fromFirestoreString(img.tipo)

                            PinImage( // ⚠️ Usando la nueva estructura PinImage
                                id = img.id, // 🆕 Cargamos el ID original
                                uri = Uri.parse(img.url),
                                tag = tagEnum,
                                titulo_es = img.titulo ?: "",
                                titulo_en = img.tituloIngles ?: "",
                                titulo_de = img.tituloAleman ?: "",
                                titulo_fr = img.tituloFrances ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                else -> emptyList()
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

        val id = editingPinId ?: return
        val original = originalPin!!

        formSubmitted = true
        isUploading = true
        uploadMessage = "Actualizando pin..."

        val imagenesParaGuardar = imagenes.images.map { pinImage ->
            ImagenData(
                id = "", // Generado en el repositorio o irrelevante para actualización
                url = pinImage.uri.toString(), // URL de la imagen
                tipo = pinImage.tag?.toFirestoreString() ?: "", // Tipo/Tag
                titulo = pinImage.titulo_es, // Título en español
                tituloIngles = pinImage.titulo_en, // Título en inglés
                tituloAleman = pinImage.titulo_de, // Título en alemán
                tituloFrances = pinImage.titulo_fr, // Título en francés
                foco = 0f, // Mantenemos el valor por defecto
                etiqueta = "" // Mantenemos vacío si se va a eliminar
            )
        }

        val imagen360Url = imagen360?.toString()

        val (area_en_auto, area_de_auto, area_fr_auto) = area_traducciones_automaticas

        // TRADUCCIONES DEL TÍTULO (Manuales si es "Otra", o nulas)
        val ubicacion_en_manual = if (pinTitleManualTrads.en.isNotBlank()) pinTitleManualTrads.en else null
        val ubicacion_de_manual = if (pinTitleManualTrads.de.isNotBlank()) pinTitleManualTrads.de else null
        val ubicacion_fr_manual = if (pinTitleManualTrads.fr.isNotBlank()) pinTitleManualTrads.fr else null

        val originalRadius = originalPin!!.tapRadius ?: 0.06f // Si no se cargó, toma el valor original

        val audioUrl_es_final = if (descripcion.es != original.descripcion_es) null else original.audioUrl_es
        val audioUrl_en_final = if (descripcion.en != original.descripcion_en) null else original.audioUrl_en
        val audioUrl_de_final = if (descripcion.de != original.descripcion_de) null else original.audioUrl_de
        val audioUrl_fr_final = if (descripcion.fr != original.descripcion_fr) null else original.audioUrl_fr

        viewModelScope.launch {
            try {
                pinRepository.updatePin(
                    pinId = id,

                    // --- UBICACIONES (Compleja) ---
                    ubicacion_es = ubicacion_es,
                    ubicacion_en = ubicacion_en_manual, // Usamos el valor manual si existe
                    ubicacion_de = ubicacion_de_manual, // Usamos el valor manual si existe
                    ubicacion_fr = ubicacion_fr_manual, // Usamos el valor manual si existe

                    // --- ÁREAS (Simple) ---
                    area_es = area_es,
                    area_en = area_en_auto, // Usamos el valor automático
                    area_de = area_de_auto, // Usamos el valor automático
                    area_fr = area_fr_auto, // Usamos el valor automático

                    // --- DESCRIPCIONES ---
                    descripcion_es = descripcion.es, // ⬅️ Nuevo nombre
                    descripcion_en = descripcion.en,
                    descripcion_de = descripcion.de,
                    descripcion_fr = descripcion.fr,

                    // --- AUDIO ---
                    audioUrl_es = audioUrl_es_final,
                    audioUrl_en = audioUrl_en_final,
                    audioUrl_de = audioUrl_de_final,
                    audioUrl_fr = audioUrl_fr_final,

                    // --- RADIO e IMÁGENES ---
                    tapRadius = originalRadius,
                    imagenes = imagenesParaGuardar,
                    imagen360 = imagen360Url
                )

                // Éxito
                uploadMessage = "Pin actualizado con éxito."
                updateRequested = true
                formSubmitted = false

                isModified = false

            } catch (e: Exception) {
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



    /**
     * Actualiza la ubicación principal y asigna automáticamente las traducciones.
     */


    fun checkIfModified() {

        if (isLoadingInitialData || !isEditing || originalPin == null) {
            isModified = false
            return
        }


        val original = originalPin!!
        val wasModifiedBefore = isModified

        // ⚠️ UBICACIÓN (Compleja - Título)
        // Comparamos ES y las 3 traducciones manuales (EN/DE/FR)
        val isUbicacionModified = ubicacion_es != original.ubicacion_es ||
                pinTitleManualTrads.en != original.ubicacion_en.orEmpty() ||
                pinTitleManualTrads.de != original.ubicacion_de.orEmpty() ||
                pinTitleManualTrads.fr != original.ubicacion_fr.orEmpty()

        // ⚠️ ÁREA (Simple)
        // SOLO necesitamos comparar area_es, ya que las traducciones EN/DE/FR son AUTOMÁTICAS.
        val isAreaModified = area_es != original.area_es


        // ⚠️ DESCRIPCIÓN
        val isDescModified = descripcion.es != original.descripcion_es ||
                descripcion.en != original.descripcion_en ||
                descripcion.de != original.descripcion_de ||
                descripcion.fr != original.descripcion_fr

        val originalImageCanonical = original.imagenesDetalladas
            .map { img ->
                val normUrl = try { Uri.parse(img.url).toString() } catch (_: Exception) { img.url }

                PinImageCanonical(
                    id = img.id,
                    url = normUrl,
                    tipo = img.tipo.orEmpty(),
                    titulo_es = img.titulo.orEmpty(),
                    titulo_en = img.tituloIngles.orEmpty(),
                    titulo_de = img.tituloAleman.orEmpty(),
                    titulo_fr = img.tituloFrances.orEmpty()
                )
            }
            .sortedBy { it.url } // Ordenamos por URL

        val currentImageCanonical = imagenes.images
            .map { img ->
                val normUrl = try { img.uri.toString() } catch (_: Exception) { "" }
                PinImageCanonical(
                    id = img.id,
                    url = normUrl,
                    tipo = img.tag?.toFirestoreString().orEmpty(),
                    titulo_es = img.titulo_es,
                    titulo_en = img.titulo_en,
                    titulo_de = img.titulo_de,
                    titulo_fr = img.titulo_fr
                )
            }
            .sortedBy { it.url } // Ordenamos por URL


        val isImagesModified = originalImageCanonical != currentImageCanonical ||
                (imagen360?.toString() ?: "") != (original.vista360Url ?: "")


        // Si cualquier campo es diferente, el pin está modificado
        isModified = isUbicacionModified ||
                isAreaModified ||
                isDescModified ||
                isImagesModified

    }


}
