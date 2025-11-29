package com.nextapp.monasterio.repository

import android.util.Log
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nextapp.monasterio.models.*
import kotlinx.coroutines.tasks.await

object PinRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pines")

    // -----------------------
    // CREATE
    // -----------------------
    suspend fun createPinAutoId(pinPayload: Map<String, Any?>): String {
        val docRef = collection.add(pinPayload).await()
        val generatedId = docRef.id
        docRef.set(mapOf("id" to generatedId), SetOptions.merge()).await()
        return generatedId
    }




    suspend fun createPin(pin: PinData): String {

        val imagenesRefs = pin.imagenes.map { url ->
            createImagenDocument(url)
        }

        val payload = mapOf(
            "titulo" to pin.titulo,
            "tituloIngles" to pin.tituloIngles,
            "tituloAleman" to pin.tituloAleman,
            "tituloFrances" to pin.tituloFrances,
            "ubicacion" to pin.ubicacion?.name,
            "x" to pin.x.toDouble(),
            "y" to pin.y.toDouble(),
            "imagenes" to imagenesRefs,
            "descripcion" to pin.descripcion,
            "descripcionIngles" to pin.descripcionIngles,
            "descripcionAleman" to pin.descripcionAleman,
            "descripcionFrances" to pin.descripcionFrances,
            "vista360Url" to pin.vista360Url,
            "audioUrl_es" to pin.audioUrl_es,
            "audioUrl_en" to pin.audioUrl_en,
            "audioUrl_ge" to pin.audioUrl_ge,
            "audioUrl_fr" to pin.audioUrl_fr,
            "tipoDestino" to "detalle",
            "valorDestino" to "auto"
        )

        return createPinAutoId(payload)
    }



    suspend fun createPinFromForm(
        titulo: String,
        descripcion: String?,
        tituloIngles: String? = null,
        tituloAleman: String? = null,
        tituloFrances: String? = null,
        descripcionIngles: String? = null,
        descripcionAleman: String? = null,
        descripcionFrances: String? = null,
        ubicacion: String?,
        imagenes: List<String>,   // URLs de Cloudinary
        imagen360: String?,       // URL de la imagen 360 (opcional)
        x: Float,
        y: Float
    ): String {

        // 1️⃣ Crear documentos en la colección /imagenes y obtener REFERENCIAS
        val imagenesRefs: List<DocumentReference> = imagenes.map { url ->
            createImagenDocument(url) // crea doc y retorna DocumentReference
        }

        val payload = mapOf(
            "titulo" to titulo,
            "tituloIngles" to tituloIngles.orEmpty(),
            "tituloAleman" to tituloAleman.orEmpty(),
            "tituloFrances" to tituloFrances.orEmpty(),

            "descripcion" to descripcion,
            "descripcionIngles" to descripcionIngles,
            "descripcionAleman" to descripcionAleman,
            "descripcionFrances" to descripcionFrances,
            "ubicacion" to ubicacion,
            "x" to x.toDouble(),
            "y" to y.toDouble(),
            "imagenes" to imagenesRefs,
            "vista360Url" to imagen360,
            "audioUrl_es" to null,
            "audioUrl_en" to null,
            "audioUrl_ge" to null,
            "audioUrl_fr" to null,
            "tipoDestino" to "detalle",
            "valorDestino" to "auto"
        )

        // 3️⃣ Crear el documento del pin
        return createPinAutoId(payload)
    }



    private fun safeUbicacionOf(name: String): Ubicacion? {
        return try {
            Ubicacion.valueOf(name)
        } catch (_: Exception) {
            null
        }
    }
    // -----------------------
    // READ: todos los pines
    // -----------------------
    suspend fun getAllPins(): List<PinData> {
        val snapshot = collection.get().await()
        Log.d("PinRepository", "📦 getAllPins() → ${snapshot.size()} documentos")
        return snapshot.documents.mapNotNull { doc ->
            mapDocToPinData(doc.id, doc.data)
        }
    }

    // -----------------------
    // READ: pin individual
    // -----------------------
    suspend fun getPinById(id: String): PinData? {
        val doc = collection.document(id).get().await()
        if (!doc.exists()) return null

        val basePin = mapDocToPinData(doc.id, doc.data) ?: return null

        // 🔥 Cargar cada imagen desde /imagenes/
        val imagenesDetalladas = basePin.imagenes.map { imagenId ->
            val imgDoc = firestore.collection("imagenes").document(imagenId).get().await()
            val focoDouble = imgDoc.getDouble("foco") ?: 0.0
            ImagenData(
                url = imgDoc.getString("url") ?: "",
                etiqueta = imgDoc.getString("etiqueta") ?: "",
                titulo = imgDoc.getString("titulo") ?: "",
                tituloIngles = imgDoc.getString("tituloIngles") ?: "",
                tituloAleman = imgDoc.getString("tituloAleman") ?: "",
                tituloFrances = imgDoc.getString("tituloFrances") ?: "",
                foco = focoDouble.toFloat()
            )
        }

        return basePin.copy(
            imagenesDetalladas = imagenesDetalladas
        )
    }


    suspend fun deletePin(pinId: String): Boolean {
        return try {
            collection.document(pinId)
                .delete()
                .await()
            Log.d("PinRepository", "✅ Pin '$pinId' eliminado correctamente.")
            true
        } catch (e: Exception) {
            Log.e("PinRepository", "❌ Error al eliminar el pin '$pinId'", e)
            false
        }
    }

    suspend fun updatePinPosition(pinId: String, newX: Float, newY: Float) {
        val payload = mapOf(
            "x" to newX.toDouble(), // Firebase usa Double para números
            "y" to newY.toDouble()
        )
        try {
            collection.document(pinId)
                .update(payload)
                .await()

        } catch (e: Exception) {

            throw e // Propagar el error para manejo en la UI
        }
    }

    // -----------------------
    // Helper: mapear doc -> PinData
    // -----------------------
    private fun mapDocToPinData(docId: String, data: Map<String, Any>?): PinData? {
        if (data == null) return null
        if (docId == "ID_DEL_PIN_QUE_FALLA" || true) { // El "|| true" es para que salga en todos por ahora
            Log.d("GHOST_BUSTER", "========================================")
            Log.d("GHOST_BUSTER", "👻 Analizando claves del documento: $docId")

            // Recorremos TODAS las claves que vienen de Firebase
            data.keys.forEach { key ->
                // Filtramos solo las que tengan "audio" para no llenar el log
                if (key.contains("audio", ignoreCase = true)) {
                    // Imprimimos la clave entre corchetes [] para ver si hay espacios
                    Log.d("GHOST_BUSTER", "🔑 Clave encontrada: ['$key'] -> Valor: '${data[key]}'")
                }
            }
            Log.d("GHOST_BUSTER", "========================================")
        }
        return try {

            val titulo = data["titulo"] as? String ?: ""
            val tituloIngles = data["tituloIngles"] as? String ?: ""
            val tituloAleman = data["tituloAleman"] as? String ?: ""
            val tituloFrances = data["tituloFrances"] as? String ?: ""

            val ubicacion = (data["ubicacion"] as? String)?.let { safeUbicacionOf(it) }
            val ubicacionIngles = (data["ubicacionIngles"] as? String)?.let { safeUbicacionOf(it) }
            val ubicacionAleman = (data["ubicacionAleman"] as? String)?.let { safeUbicacionOf(it) }
            val ubicacionFrances = (data["ubicacionFrances"] as? String)?.let { safeUbicacionOf(it) }


            val x = (data["x"] as? Number)?.toFloat() ?: 0f
            val y = (data["y"] as? Number)?.toFloat() ?: 0f

            // 🟦 Cargamos SOLO los IDs de referencias a imágenes
            val imagenes: List<String> = when (val raw = data["imagenes"]) {
                is List<*> -> raw.mapNotNull { ref ->
                    when (ref) {
                        is DocumentReference -> ref.id
                        else -> null
                    }
                }
                else -> emptyList()
            }

            val descripcion = data["descripcion"] as? String
            val descripcionIngles = data["descripcionIngles"] as? String
            val descripcionAleman = data["descripcionAleman"] as? String
            val descripcionFrances = data["descripcionFrances"] as? String
            val vista360Url = data["vista360Url"] as? String

            val audioUrl_es = data["audioUrl_es"] as? String
            val audioUrl_en = data["audioUrl_en"] as? String
            val audioUrl_ge = data["audioUrl_ge"] as? String
            val audioUrl_fr = data["audioUrl_fr"] as? String


            val tapRadius = (data["tapRadius"] as? Number)?.toFloat() ?: 0.04f
            val tipoDestino = data["tipoDestino"] as? String
            val valorDestino = data["valorDestino"] as? String

            val destino = when (tipoDestino?.lowercase()) {
                "ruta" -> DestinoPin.Ruta(valorDestino ?: "")
                "detalle" -> DestinoPin.Detalle(valorDestino ?: docId)
                "popup" -> DestinoPin.Popup(valorDestino ?: "Sin mensaje")
                else -> DestinoPin.Detalle(docId)
            }

            PinData(
                id = docId,
                titulo = titulo,
                tituloIngles = tituloIngles,
                tituloAleman = tituloAleman,
                tituloFrances = tituloFrances,
                ubicacion = ubicacion,
                ubicacionIngles = ubicacionIngles,
                ubicacionAleman = ubicacionAleman,
                ubicacionFrances = ubicacionFrances,
                x = x,
                y = y,
                color = null,
                iconRes = null,
                imagenes = imagenes,
                imagenesDetalladas = emptyList(),   // ✔ CORRECTO
                descripcion = descripcion,
                descripcionIngles = descripcionIngles,
                descripcionAleman = descripcionAleman,
                descripcionFrances = descripcionFrances,
                tipoDestino = tipoDestino,
                valorDestino = valorDestino,
                destino = destino,
                tapRadius = tapRadius,
                vista360Url = vista360Url,
                audioUrl_es = audioUrl_es,
                audioUrl_en = audioUrl_en,
                audioUrl_ge = audioUrl_ge,
                audioUrl_fr = audioUrl_fr
            )


        } catch (e: Exception) {
            Log.e("PinRepository", "❌ Error mapeando pin $docId", e)
            null
        }
    }


    private suspend fun createImagenDocument(url: String): DocumentReference {
        val imagenesCollection = firestore.collection("imagenes")

        val payload = mapOf(
            "url" to url,
            "etiqueta" to "",
            "titulo" to "",
            "tituloIngles" to "",
            "tituloAleman" to "",
            "tituloFrances" to "",
            "foco" to 0
        )

        val docRef = imagenesCollection.add(payload).await()
        return docRef
    }

    suspend fun deletePinAndImages(pinId: String): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()

            val pinDoc = firestore.collection("pines").document(pinId).get().await()
            if (!pinDoc.exists()) return false

            val imagenesRefs = pinDoc.get("imagenes") as? List<DocumentReference> ?: emptyList()

            imagenesRefs.forEach { ref ->
                try {
                    ref.delete().await()
                    Log.d("PinRepository", "🗑 Imagen eliminada: ${ref.id}")
                } catch (e: Exception) {
                    Log.e("PinRepository", "❌ Error eliminando imagen ${ref.id}", e)
                }
            }

            firestore.collection("pines").document(pinId).delete().await()

            Log.d("PinRepository", "🗑 Pin eliminado correctamente: $pinId")
            true

        } catch (e: Exception) {
            Log.e("PinRepository", "❌ Error eliminando pin completo $pinId", e)
            false
        }
    }

    suspend fun updatePin(
        pinId: String,
        titulo: String,
        descripcion: String?,
        tituloIngles: String?,
        descripcionIngles: String?,
        tituloAleman: String?,
        descripcionAleman: String?,
        ubicacion: String?,
        imagenes: List<String>, // URLs de cloudinary
        imagen360: String? // URL 360
    ) {
        // 1) Crear docs en /imagenes para cada URL (si procede)
        val imagenesRefs: List<DocumentReference> = imagenes.map { url ->
            createImagenDocument(url)
        }

        // 2) Preparar payload de actualización
        val updates = mapOf<String, Any?>(
            "titulo" to titulo,
            "descripcion" to descripcion,
            "tituloIngles" to tituloIngles,
            "descripcionIngles" to descripcionIngles,
            "tituloAleman" to tituloAleman,
            "descripcionAleman" to descripcionAleman,
            "ubicacion" to ubicacion,
            "imagenes" to imagenesRefs,
            "vista360Url" to imagen360,
            "tipoDestino" to "detalle",
            "valorDestino" to "auto"
        )

        // 3) Ejecutar update
        collection.document(pinId)
            .set(updates, SetOptions.merge())
            .await()
    }

}
