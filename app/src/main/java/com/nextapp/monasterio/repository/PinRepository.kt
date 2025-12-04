package com.nextapp.monasterio.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nextapp.monasterio.models.*
import kotlinx.coroutines.tasks.await

object PinRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pines")
    private val imagenRepository = ImagenRepository(firestore)

    suspend fun getPinById(id: String): PinData? {
        val doc = collection.document(id).get().await()
        if (!doc.exists()) {
            Log.w("PinRepository", "⚠️ Pin $id no encontrado.")
            return null
        }

        val basePin = mapDocToPinData(doc.id, doc.data) ?: return null
        val imagenesDetalladas = imagenRepository.getImagesByIds(basePin.imagenes)

        return basePin.copy(
            imagenesDetalladas = imagenesDetalladas
        )
    }

    suspend fun getAllPins(): List<PinData> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            mapDocToPinData(doc.id, doc.data)
        }
    }

    private fun mapDocToPinData(docId: String, data: Map<String, Any>?): PinData? {
        if (data == null) return null

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
            val audioUrl_de = data["audioUrl_de"] as? String
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
                iconRes = null,
                imagenes = imagenes,
                imagenesDetalladas = emptyList(),
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
                audioUrl_de = audioUrl_de,
                audioUrl_fr = audioUrl_fr
            )

        } catch (e: Exception) {
            Log.e("PinRepository", "❌ Error mapeando pin $docId", e)
            null
        }
    }

    suspend fun createPinAutoId(pinPayload: Map<String, Any?>): String {
        val docRef = collection.document()
        val generatedId = docRef.id
        val finalPayload = pinPayload.toMutableMap()
        finalPayload["id"] = generatedId
        docRef.set(finalPayload).await()
        return generatedId
    }

    suspend fun createPinFromForm(
        titulo: String,
        tituloIngles: String? = null,
        tituloAleman: String? = null,
        tituloFrances: String? = null,
        descripcion: String?,
        descripcionIngles: String? = null,
        descripcionAleman: String? = null,
        descripcionFrances: String? = null,
        ubicacion: String?,
        ubicacionIngles: String?,
        ubicacionAleman: String?,
        ubicacionFrances: String?,
        imagenes: List<String>,   // URLs de Cloudinary
        imagen360: String?,       // URL de la imagen 360 (opcional)
        x: Float,
        y: Float,
        tapRadius: Float?,
        audioUrl_es: String? = null,
        audioUrl_en: String? = null,
        audioUrl_de: String? = null,
        audioUrl_fr: String? = null
    ): String {

        Log.d("REPO-DEBUG", "📌 Guardando PIN...")
        Log.d("REPO-DEBUG", "Título Francés a guardar: $tituloFrances")
        Log.d("REPO-DEBUG", "Descripción Francés a guardar: $descripcionFrances")
        val imagenesRefs: List<DocumentReference> = imagenes.map { url ->
            createImagenDocument(url)
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
            "ubicacion" to ubicacionIngles,
            "ubicacionAleman" to ubicacionAleman,
            "ubicacionFrances" to ubicacionFrances,
            "x" to x.toDouble(),
            "y" to y.toDouble(),
            "tapRadius" to tapRadius,
            "imagenes" to imagenesRefs,
            "vista360Url" to imagen360,
            "audioUrl_es" to audioUrl_es,
            "audioUrl_en" to audioUrl_en,
            "audioUrl_de" to audioUrl_de,
            "audioUrl_fr" to audioUrl_fr,
            "tipoDestino" to "detalle",
            "valorDestino" to "auto"
        )

        return createPinAutoId(payload)
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
            "foco" to 0,
            "tipo" to ""
        )

        val docRef = imagenesCollection.add(payload).await()
        return docRef
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
            Log.e("PinRepository", "❌ Error al actualizar posición del pin $pinId", e)
            throw e // Propagar el error para manejo en la UI
        }
    }



    private fun safeUbicacionOf(name: String): Ubicacion? {
        return try {
            Ubicacion.valueOf(name)
        } catch (_: Exception) {
            null
        }
    }



    suspend fun deletePinAndImages(pinId: String): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()

            // 1) Obtener documento del pin
            val pinDoc = firestore.collection("pines").document(pinId).get().await()
            if (!pinDoc.exists()) {
                Log.w("PinRepository", "⚠️ deletePinAndImages: pin $pinId no existe")
                return false
            }

            // 2) Eliminar documentos de la colección "imagenes" referenciados en el pin
            val imagenesRefs = pinDoc.get("imagenes") as? List<DocumentReference> ?: emptyList()
            imagenesRefs.forEach { ref ->
                try {
                    ref.delete().await()
                    Log.d("PinRepository", "🗑 Imagen eliminada: ${ref.path}")
                } catch (e: Exception) {
                    Log.e("PinRepository", "❌ Error eliminando imagen ${ref.path}", e)
                }
            }


            try {
                val pinRef = firestore.collection("pines").document(pinId)
                firestore.collection("planos")
                    .document("monasterio_interior")
                    .update("pines", FieldValue.arrayRemove(pinRef))
                    .await()
                Log.d("PinRepository", "🗑 Referencia del pin $pinId eliminada en plano monastery_interior")
            } catch (e: Exception) {
                Log.e("PinRepository", "❌ Error eliminando referencia en plano para pin $pinId", e)
                // no abortamos: seguimos intentando borrar el documento del pin
            }

            // 4) Eliminar el propio documento del pin
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
        tituloFrances: String?,
        descripcionFrances: String?,
        ubicacion: String?,
        ubicacionIngles: String?,
        ubicacionAleman: String?,
        ubicacionFrances: String?,
        audioUrl_es: String?,
        audioUrl_en: String?,
        audioUrl_de: String?,
        audioUrl_fr: String?,
        tapRadius: Float?,
        // El resto
        imagenes: List<String>,
        imagen360: String?
    ) {
        try {
            val pinRef = collection.document(pinId)
            val pinDoc = pinRef.get().await()

            if (pinDoc.exists()) {
                val imagenesRefsAntiguas = pinDoc.get("imagenes") as? List<DocumentReference> ?: emptyList()
                imagenesRefsAntiguas.forEach { ref ->
                    try { ref.delete().await() }
                    catch (e: Exception) { Log.e("PinRepository", "❌ Advertencia: No se pudo eliminar la imagen antigua ${ref.id}. Posible orfandad.", e) }
                }
            }

            val imagenesRefsNuevas: List<DocumentReference> = imagenes.map { url ->
                createImagenDocument(url)
            }

            val updates = mapOf<String, Any?>(
                "titulo" to titulo,
                "descripcion" to descripcion,
                "ubicacion" to ubicacion,
                "imagenes" to imagenesRefsNuevas,
                "vista360Url" to imagen360,

                "tituloIngles" to tituloIngles,
                "descripcionIngles" to descripcionIngles,
                "tituloAleman" to tituloAleman,
                "descripcionAleman" to descripcionAleman,
                "tituloFrances" to tituloFrances,
                "descripcionFrances" to descripcionFrances,
                "ubicacionIngles" to ubicacionIngles,
                "ubicacionAleman" to ubicacionAleman,
                "ubicacionFrances" to ubicacionFrances,
                "audioUrl_es" to audioUrl_es,
                "audioUrl_en" to audioUrl_en,
                "audioUrl_de" to audioUrl_de,
                "audioUrl_fr" to audioUrl_fr,
                "tapRadius" to tapRadius?.toDouble(),
                "tipoDestino" to "detalle",
                "valorDestino" to "auto"
            )

            pinRef
                .set(updates, SetOptions.merge())
                .await()

            Log.d("PinRepository", "✅ Pin $pinId actualizado correctamente.")

        } catch (e: Exception) {
            Log.e("PinRepository", "❌ Error al actualizar pin $pinId", e)
            throw e
        }
    }

}
