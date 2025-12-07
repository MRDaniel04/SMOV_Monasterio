package com.nextapp.monasterio.ui.screens.pinCreation

import android.content.Context
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
import com.nextapp.monasterio.repository.PlanoRepository
import com.nextapp.monasterio.services.CloudinaryService
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

private val UBICACION_AUTO_TRADS = mapOf(
    "Coro" to mapOf("en" to "Choir", "de" to "Chor", "fr" to "Chœur"),
    "Crucero" to mapOf("en" to "Transept", "de" to "Querschiff", "fr" to "Transept"),
    "Lado de la epistola" to mapOf("en" to "Epistle Side", "de" to "Epistelseite", "fr" to "Côté épître"),
    "Trascoro" to mapOf("en" to "Retrochoir", "de" to "Hinterchor", "fr" to "Derrière le chœur"),
    "Capilla del nacimiento" to mapOf("en" to "Nativity Chapel", "de" to "Geburtskapelle", "fr" to "Chapelle de la Nativité")
    // ⚠️ Importante: Asegúrate de añadir aquí el resto de las ubicaciones predefinidas
)


class CreacionPinSharedViewModel : ViewModel() {

    private val pinRepository = PinRepository
    private var isLoadingInitialData = false

    val descripcion = DescripcionState(onChanged = { checkIfModified() })
    val imagenes = ImagenesState()


    var ubicacion_en_auto: String? by mutableStateOf(null)
    var ubicacion_de_auto: String? by mutableStateOf(null)
    var ubicacion_fr_auto: String? by mutableStateOf(null)

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

    val ubicacion_en: String?
        get() = pinTitleManualTrads.en.ifBlank { ubicacion_en_auto }

    val ubicacion_de: String?
        get() = pinTitleManualTrads.de.ifBlank { ubicacion_de_auto }

    val ubicacion_fr: String?
        get() = pinTitleManualTrads.fr.ifBlank { ubicacion_fr_auto }


    var newPinIdForPlacement by mutableStateOf<String?>(null)
        private set

    fun clearNewPinIdForPlacement() {
        newPinIdForPlacement = null
    }

    companion object {
        // Traducciones de las opciones fijas para el ÁREA
        private val AREA_TRADUCCIONES = mapOf(
            "Iglesia" to Triple("Church", "Kirche", "Église"),
            "Monasterio" to Triple("Monastery", "Kloster", "Monastère")
        )
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

    fun updateUbicacionConAutoTraduccion(newTitleEs: String, getAreaFn: (String) -> String?) {

        Log.d("FLUJO_PIN_AUTO", "-> UPDATE INICIADO: newTitleEs='$newTitleEs'")

        // 1. Actualizar el campo principal (ES)
        _ubicacion_es = newTitleEs

        val autoTrads = UBICACION_AUTO_TRADS[newTitleEs]
        val isManualEntry = autoTrads == null

        if (!isManualEntry) {
            // ---------- UBICACIÓN PREDEFINIDA ----------
            ubicacion_en_auto = autoTrads["en"]
            ubicacion_de_auto = autoTrads["de"]
            ubicacion_fr_auto = autoTrads["fr"]

            // Y limpiamos las manuales
            pinTitleManualTrads = PinTitleManualTrads()
        } else {
            // ---------- UBICACIÓN MANUAL ----------
            // No toca automáticas
            ubicacion_en_auto = null
            ubicacion_de_auto = null
            ubicacion_fr_auto = null

            // Se usarán las manuales (si las escribe el usuario)
        }

        // 3. ÁREA AUTOMÁTICA (solo si es fija)
        if (!isManualEntry) {
            val newAreaEs = getAreaFn(newTitleEs)
            area_es = newAreaEs ?: ""
        } else {
            // manual → área vacía hasta que usuario elija
            area_es = ""
        }

        if (!isLoadingInitialData) checkIfModified()

        Log.d("FLUJO_PIN_AUTO", "<- UPDATE FINALIZADO: area_es='${area_es}', ubicacion_es='${_ubicacion_es}'")
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


    fun reset() {

        ubicacion_es = ""
        ubicacion_en_auto = null
        ubicacion_de_auto = null
        ubicacion_fr_auto = null
        pinTitleManualTrads = PinTitleManualTrads()
        _area_es_internal = ""
        area_traducciones_automaticas = Triple(null, null, null)
        descripcion.reset() // Asumiendo que DescripcionState tiene un reset()
        imagenes.images = emptyList()
        imagen360 = null
        modoMoverPin = false
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

        formSubmitted = false
        modoMoverPin = false
        newPinIdForPlacement = null // Limpiar el nuevo flag si se usara

        try {
            // 🟦 UBICACIÓN (Compleja)
            _ubicacion_es = pin.ubicacion_es ?: ""

            val auto = UBICACION_AUTO_TRADS[_ubicacion_es]

            if (auto != null) {
                // Ubicación fija → activar auto traducciones
                ubicacion_en_auto = auto["en"]
                ubicacion_de_auto = auto["de"]
                ubicacion_fr_auto = auto["fr"]

                // Limpiar manuales
                pinTitleManualTrads = PinTitleManualTrads()
            } else {
                // Ubicación manual → conservar las manuales cargadas
                ubicacion_en_auto = null
                ubicacion_de_auto = null
                ubicacion_fr_auto = null
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
    fun onSaveClicked(context: Context) {
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
                id = pinImage.id, // ⬅️ CORRECCIÓN: Usar el ID original
                url = pinImage.uri.toString(), // URL de la imagen
                tipo = pinImage.tag?.toFirestoreString() ?: "", // Tipo/Tag
                titulo = pinImage.titulo_es, // Título en español
                tituloIngles = pinImage.titulo_en, // Título en inglés
                tituloAleman = pinImage.titulo_de, // Título en alemán
                tituloFrances = pinImage.titulo_fr, // Título en francés
                foco = 0f // Mantenemos el valor por defecto
            )
        }

        val imagen360Url = imagen360?.toString()

        val (area_en_auto, area_de_auto, area_fr_auto) = area_traducciones_automaticas

        // TRADUCCIONES DEL TÍTULO (Manuales si es "Otra", o nulas)

        val descriptionTasks = mapOf(
            "es" to Pair(descripcion.es, original.descripcion_es),
            "en" to Pair(descripcion.en, original.descripcion_en),
            "de" to Pair(descripcion.de, original.descripcion_de),
            "fr" to Pair(descripcion.fr, original.descripcion_fr)
        )

        // 2. Inicializamos las URLs finales con las originales
        val audioUrlsFinal: MutableMap<String, String?> = mutableMapOf(
            "es" to original.audioUrl_es,
            "en" to original.audioUrl_en,
            "de" to original.audioUrl_de,
            "fr" to original.audioUrl_fr
        )

        viewModelScope.launch {
            try {

                for ((lang, texts) in descriptionTasks) {
                    val (currentText, originalText) = texts

                    val textModified = currentText != originalText
                    if (textModified) {
                        // El texto ha cambiado. Decidimos si subir o borrar.

                        if (currentText.isNotBlank()) {
                            // 1. TEXTO MODIFICADO Y NO VACÍO -> REGENERAR Y SUBIR
                            uploadMessage = "Generando y subiendo audio para ${lang.uppercase()}..."

                            // 🚨 LLAMADA CRÍTICA AL REPOSITORIO
                            val generatedUrl = pinRepository.generateAndUploadAudio(context, currentText, lang)

                            audioUrlsFinal[lang] = generatedUrl // Guarda la nueva URL
                        } else {
                            // 2. TEXTO MODIFICADO A VACÍO -> BORRAR
                            // El original tenía audio, pero el nuevo texto no. Borramos el link.
                            audioUrlsFinal[lang] = null
                        }
                    }
                }

                // 4. Obtener las URLs finales para la llamada al repositorio
                val audioUrl_es_final = audioUrlsFinal["es"]
                val audioUrl_en_final = audioUrlsFinal["en"]
                val audioUrl_de_final = audioUrlsFinal["de"]
                val audioUrl_fr_final = audioUrlsFinal["fr"]

                pinRepository.updatePin(
                    pinId = id,

                    // --- UBICACIONES (Compleja) ---
                    ubicacion_es = ubicacion_es,
                    ubicacion_en = ubicacion_en, // ⬅️ ¡Usando la nueva propiedad!
                    ubicacion_de = ubicacion_de, // ⬅️ ¡Usando la nueva propiedad!
                    ubicacion_fr = ubicacion_fr, // ⬅️ ¡Usando la nueva propiedad!

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
    fun onCreateConfirmed(context: Context, finalX: Float, finalY: Float, onSuccess: () -> Unit) {

        // Solo permitimos crear si NO estamos editando, NO estamos subiendo y tenemos coordenadas.
        if (isEditing || isUploading) {
            Log.e("FLUJO_PIN", "VM: onCreateConfirmed ignorado. Modo incorrecto o ya subiendo.")
            uploadMessage = "Error interno: Ya subiendo o en edición."
            isUploading = false
            return
        }

        isUploading = true
        uploadMessage = "Iniciando proceso de creación y subida de archivos..."

        // Mapeo de traducciones y áreas
        val (area_en_auto, area_de_auto, area_fr_auto) = area_traducciones_automaticas


        viewModelScope.launch {
            try {
                // --- 1. SUBIDA DE IMÁGENES NORMALES ---
                uploadMessage = "Subiendo imágenes normales..."
                val uploadedImageUrls = imagenes.uris.map { uri ->
                    CloudinaryService.uploadImage(uri, context).getOrThrow()
                }


                // --- 2. SUBIDA DE IMAGEN 360 ---
                uploadMessage = "Subiendo imagen 360 (si existe)..."
                val uploaded360Url: String? = imagen360?.let { uri ->
                    CloudinaryService.uploadImage(uri, context).getOrNull()
                }

                // Mapear URLs subidas con sus datos de título/tag
                val imagesWithData = imagenes.images.mapIndexed { index, pinImage ->
                    val uploadedUrl = uploadedImageUrls.getOrNull(index) ?: pinImage.uri.toString()
                    ImagenData(
                        id = "", url = uploadedUrl,
                        tipo = pinImage.tag?.toFirestoreString() ?: "", titulo = pinImage.titulo_es,
                        tituloIngles = pinImage.titulo_en, tituloAleman = pinImage.titulo_de,
                        tituloFrances = pinImage.titulo_fr, foco = 0f
                    )
                }


                // --- 3. GENERACIÓN Y SUBIDA DE AUDIOS (¡INTEGRADO!) ---
                val audioUrlsFinal: MutableMap<String, String?> = mutableMapOf()
                val creationDescriptions = mapOf(
                    "es" to descripcion.es, "en" to descripcion.en,
                    "de" to descripcion.de, "fr" to descripcion.fr
                )

                for ((lang, currentText) in creationDescriptions) {
                    if (currentText.isNotBlank()) {
                        uploadMessage = "Generando y subiendo audio para ${lang.uppercase()}..."
                        val generatedUrl = pinRepository.generateAndUploadAudio(context, currentText, lang)
                        audioUrlsFinal[lang] = generatedUrl
                    } else {
                        audioUrlsFinal[lang] = null
                    }
                }


                // --- 4. CREACIÓN DEL PIN FINAL EN EL REPOSITORIO ---
                uploadMessage = "Guardando Pin y asociando al plano..."

                val newPinId = pinRepository.createPinFromForm(
                    // UBICACIÓN (Compleja)
                    ubicacion_es = ubicacion_es,
                    ubicacion_en = ubicacion_en, // ⬅️ ¡Usando la nueva propiedad!
                    ubicacion_de = ubicacion_de, // ⬅️ ¡Usando la nueva propiedad!
                    ubicacion_fr = ubicacion_fr, // ⬅️ ¡Usando la nueva propiedad!
                    // DESCRIPCIONES
                    descripcion_es = descripcion.es.ifBlank { null }, descripcion_en = descripcion.en.ifBlank { null },
                    descripcion_de = descripcion.de.ifBlank { null }, descripcion_fr = descripcion.fr.ifBlank { null },

                    // ÁREA (Simple)
                    area_es = area_es, area_en = area_en_auto, area_de = area_de_auto, area_fr = area_fr_auto,

                    // AUDIO (USANDO LAS URLS GENERADAS)
                    audioUrl_es = audioUrlsFinal["es"], audioUrl_en = audioUrlsFinal["en"],
                    audioUrl_de = audioUrlsFinal["de"], audioUrl_fr = audioUrlsFinal["fr"],

                    imagenes = imagesWithData,
                    imagen360 = uploaded360Url,
                    x = finalX, y = finalY
                )

                PlanoRepository.addPinToPlano(planoId = "monasterio_interior", pinId = newPinId)

                // Éxito:
                uploadMessage = "Pin creado con éxito."
                formSubmitted = false // Resetear formSubmitted (si lo usa la UI)
                onSuccess() // Informar a EdicionPines para que recargue

            } catch (e: Exception) {
                Log.e("FLUJO_PIN", "❌ ERROR en proceso de creación: ${e.message}", e)
                uploadMessage = "Error: " + (e.message ?: "Fallo desconocido en el guardado.")

                formSubmitted = false // Resetear en caso de fallo
            } finally {
                isUploading = false
            }
        }
    }

    fun onCreateClicked(context: android.content.Context, onSuccess: () -> Unit) {
        // Aquí puedes añadir alguna validación de formulario si es crítica antes de pasar al mapa.
        // Por simplicidad, solo chequeamos el estado.
        if (isEditing || isUploading || formSubmitted) {
            Log.w("FLUJO_PIN", "VM: onCreateClicked ignorado. Formulario ya en proceso o en edición.")
            return
        }

        // 1. Marcar el estado del formulario como SUBMITTED
        // Esto es lo que activará el LaunchedEffect en EdicionPines (el mapa)
        formSubmitted = true
        Log.d("FLUJO_PIN", "VM: ✅ Formulario listo para posicionamiento. formSubmitted = true.")

        // 2. Llama al callback de navegación.
        onSuccess() // Esto llama a navController.popBackStack() para ir al mapa (EdicionPines)
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

        val isUbicacionModified =
            ubicacion_es != original.ubicacion_es ||
                    ubicacion_en.orEmpty() != original.ubicacion_en.orEmpty() ||
                    ubicacion_de.orEmpty() != original.ubicacion_de.orEmpty() ||
                    ubicacion_fr.orEmpty() != original.ubicacion_fr.orEmpty()


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
