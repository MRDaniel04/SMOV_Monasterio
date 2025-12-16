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

    var _ubicacion_es by mutableStateOf("")
    var ubicacion_es: String
        get() = _ubicacion_es
        set(value) {
            _ubicacion_es = value
            if (!isLoadingInitialData) checkIfModified()
        }

    var pinTitleManualTrads by mutableStateOf(PinTitleManualTrads())
        private set

    private var _area_es_internal by mutableStateOf("")

    var area_es: String
        get() = _area_es_internal
        set(value) {
            _area_es_internal = value
            updatePinArea(value)
            if (!isLoadingInitialData) checkIfModified()
        }

    private var area_traducciones_automaticas: Triple<String?, String?, String?> = Triple(null, null, null)

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
            area_traducciones_automaticas = Triple(null, null, null)
        }
    }

    fun updateTitleManualTrads(en: String = pinTitleManualTrads.en, de: String = pinTitleManualTrads.de, fr: String = pinTitleManualTrads.fr) {
        pinTitleManualTrads = PinTitleManualTrads(en, de, fr)
        if (!isLoadingInitialData) checkIfModified()
    }

    fun updateUbicacionConAutoTraduccion(newTitleEs: String, getAreaFn: (String) -> String?) {


        _ubicacion_es = newTitleEs

        val autoTrads = UBICACION_AUTO_TRADS[newTitleEs]
        val isManualEntry = autoTrads == null

        if (!isManualEntry) {
            ubicacion_en_auto = autoTrads["en"]
            ubicacion_de_auto = autoTrads["de"]
            ubicacion_fr_auto = autoTrads["fr"]
            pinTitleManualTrads = PinTitleManualTrads()

        } else {
            ubicacion_en_auto = null
            ubicacion_de_auto = null
            ubicacion_fr_auto = null
        }

        if (!isManualEntry) {
            val newAreaEs = getAreaFn(newTitleEs)
            area_es = newAreaEs ?: ""
        } else {
            area_es = ""
        }

        if (!isLoadingInitialData) checkIfModified()
    }

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
        descripcion.reset()
        imagenes.images = emptyList()
        imagen360 = null
        modoMoverPin = false
        formSubmitted = false
        isEditing = false
        editingPinId = null
    }


    fun loadPinForEditing(pin: PinData) {
        isEditing = true
        editingPinId = pin.id
        isLoadingInitialData = true
        formSubmitted = false
        modoMoverPin = false
        newPinIdForPlacement = null

        try {

            _ubicacion_es = pin.ubicacion_es ?: ""
            val auto = UBICACION_AUTO_TRADS[_ubicacion_es]

            if (auto != null) {
                ubicacion_en_auto = auto["en"]
                ubicacion_de_auto = auto["de"]
                ubicacion_fr_auto = auto["fr"]
                pinTitleManualTrads = PinTitleManualTrads()

            } else {
                ubicacion_en_auto = null
                ubicacion_de_auto = null
                ubicacion_fr_auto = null
            }

            descripcion.updateEs(pin.descripcion_es ?: "")
            descripcion.updateEn(pin.descripcion_en ?: "")
            descripcion.updateDe(pin.descripcion_de ?: "")
            descripcion.updateFr(pin.descripcion_fr ?: "")

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

                            PinImage(
                                id = img.id,
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

            _imagen360 = pin.vista360Url?.let { Uri.parse(it) }
            originalPin = pin.copy()
        } finally {
            isLoadingInitialData = false
        }

        checkIfModified()
    }


    fun onSaveClicked(context: Context) {
        if (!isEditing || isUploading || formSubmitted) {
            return
        }

        val id = editingPinId ?: return
        val original = originalPin!!

        formSubmitted = true
        isUploading = true
        uploadMessage = "Actualizando pin..."

        val imagenesParaGuardar = imagenes.images.map { pinImage ->
            ImagenData(
                id = pinImage.id,
                url = pinImage.uri.toString(),
                tipo = pinImage.tag?.toFirestoreString() ?: "",
                titulo = pinImage.titulo_es,
                tituloIngles = pinImage.titulo_en,
                tituloAleman = pinImage.titulo_de,
                tituloFrances = pinImage.titulo_fr,
                foco = 0f
            )
        }

        val imagen360Url = imagen360?.toString()
        val (area_en_auto, area_de_auto, area_fr_auto) = area_traducciones_automaticas


        val descriptionTasks = mapOf(
            "es" to Pair(descripcion.es, original.descripcion_es),
            "en" to Pair(descripcion.en, original.descripcion_en),
            "de" to Pair(descripcion.de, original.descripcion_de),
            "fr" to Pair(descripcion.fr, original.descripcion_fr)
        )

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

                        if (currentText.isNotBlank()) {

                            uploadMessage = "Generando y subiendo audio para ${lang.uppercase()}..."

                            val generatedUrl = pinRepository.generateAndUploadAudio(context, currentText, lang)

                            audioUrlsFinal[lang] = generatedUrl // Guarda la nueva URL
                        } else {
                            audioUrlsFinal[lang] = null
                        }
                    }
                }

                val audioUrl_es_final = audioUrlsFinal["es"]
                val audioUrl_en_final = audioUrlsFinal["en"]
                val audioUrl_de_final = audioUrlsFinal["de"]
                val audioUrl_fr_final = audioUrlsFinal["fr"]

                pinRepository.updatePin(
                    pinId = id,

                    ubicacion_es = ubicacion_es,
                    ubicacion_en = ubicacion_en,
                    ubicacion_de = ubicacion_de,
                    ubicacion_fr = ubicacion_fr,

                    area_es = area_es,
                    area_en = area_en_auto,
                    area_de = area_de_auto,
                    area_fr = area_fr_auto,

                    descripcion_es = descripcion.es,
                    descripcion_en = descripcion.en,
                    descripcion_de = descripcion.de,
                    descripcion_fr = descripcion.fr,

                    audioUrl_es = audioUrl_es_final,
                    audioUrl_en = audioUrl_en_final,
                    audioUrl_de = audioUrl_de_final,
                    audioUrl_fr = audioUrl_fr_final,

                    imagenes = imagenesParaGuardar,
                    imagen360 = imagen360Url
                )

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


    fun onCreateConfirmed(context: Context, finalX: Float, finalY: Float, onSuccess: () -> Unit) {

        if (isEditing || isUploading) {
            uploadMessage = "Error interno: Ya subiendo o en edición."
            isUploading = false
            return
        }

        isUploading = true
        uploadMessage = "Iniciando proceso de creación y subida de archivos..."

        val (area_en_auto, area_de_auto, area_fr_auto) = area_traducciones_automaticas


        viewModelScope.launch {
            try {
                uploadMessage = "Subiendo imágenes normales..."
                val uploadedImageUrls = imagenes.uris.map { uri ->
                    CloudinaryService.uploadImage(uri, context).getOrThrow()
                }

                uploadMessage = "Subiendo imagen 360 (si existe)..."
                val uploaded360Url: String? = imagen360?.let { uri ->
                    CloudinaryService.uploadImage(uri, context).getOrNull()
                }

                val imagesWithData = imagenes.images.mapIndexed { index, pinImage ->
                    val uploadedUrl = uploadedImageUrls.getOrNull(index) ?: pinImage.uri.toString()
                    ImagenData(
                        id = "", url = uploadedUrl,
                        tipo = pinImage.tag?.toFirestoreString() ?: "", titulo = pinImage.titulo_es,
                        tituloIngles = pinImage.titulo_en, tituloAleman = pinImage.titulo_de,
                        tituloFrances = pinImage.titulo_fr, foco = 0f
                    )
                }

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


                uploadMessage = "Guardando Pin y asociando al plano..."

                val newPinId = pinRepository.createPinFromForm(

                    ubicacion_es = ubicacion_es,
                    ubicacion_en = ubicacion_en,
                    ubicacion_de = ubicacion_de,
                    ubicacion_fr = ubicacion_fr,

                    descripcion_es = descripcion.es.ifBlank { null }, descripcion_en = descripcion.en.ifBlank { null },
                    descripcion_de = descripcion.de.ifBlank { null }, descripcion_fr = descripcion.fr.ifBlank { null },

                    area_es = area_es, area_en = area_en_auto, area_de = area_de_auto, area_fr = area_fr_auto,

                    audioUrl_es = audioUrlsFinal["es"], audioUrl_en = audioUrlsFinal["en"],
                    audioUrl_de = audioUrlsFinal["de"], audioUrl_fr = audioUrlsFinal["fr"],

                    imagenes = imagesWithData,
                    imagen360 = uploaded360Url,
                    x = finalX, y = finalY
                )

                PlanoRepository.addPinToPlano(planoId = "monasterio_interior", pinId = newPinId)

                uploadMessage = "Pin creado con éxito."
                formSubmitted = false
                onSuccess()

            } catch (e: Exception) {
                uploadMessage = "Error: " + (e.message ?: "Fallo desconocido en el guardado.")

                formSubmitted = false // Resetear en caso de fallo
            } finally {
                isUploading = false
            }
        }
    }

    fun onCreateClicked(context: android.content.Context, onSuccess: () -> Unit) {

        if (isEditing || isUploading || formSubmitted) {
            return
        }

        formSubmitted = true
        onSuccess()
    }


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


        val isAreaModified = area_es != original.area_es

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
            .sortedBy { it.url }

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
