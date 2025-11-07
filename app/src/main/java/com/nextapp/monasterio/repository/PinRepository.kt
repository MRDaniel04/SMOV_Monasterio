package com.nextapp.monasterio.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.models.Tema
import com.nextapp.monasterio.models.Ubicacion
import kotlinx.coroutines.tasks.await

object PinRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pines")

    // -----------------------
    // CREATE (auto id)
    // -----------------------
    suspend fun createPinAutoId(pinPayload: Map<String, Any?>): String {
        val docRef = collection.add(pinPayload).await()
        val generatedId = docRef.id
        docRef.set(mapOf("id" to generatedId), SetOptions.merge()).await()
        return generatedId
    }

    // Crear desde un PinData
    suspend fun createPin(pin: PinData): String {
        val payload = mapOf(
            "titulo" to pin.titulo,
            "ubicacion" to pin.ubicacion?.name,
            "x" to pin.x.toDouble(),
            "y" to pin.y.toDouble(),
            "tema" to pin.tema.name,
            "color" to null,
            "imagenes" to pin.imagenes, // ✅ URLs Cloudinary
            "descripcion" to pin.descripcion,
            "tapRadius" to pin.tapRadius.toDouble()
        )
        return createPinAutoId(payload)
    }

    // -----------------------
    // READ: todos los pines
    // -----------------------
    suspend fun getAllPins(): List<PinData> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc -> mapDocToPinData(doc.id, doc.data) }
    }

    // -----------------------
    // READ: por id
    // -----------------------
    suspend fun getPinById(id: String): PinData? {
        val doc = collection.document(id).get().await()
        if (!doc.exists()) return null
        return mapDocToPinData(doc.id, doc.data)
    }

    // -----------------------
    // Helper: mapear doc -> PinData
    // -----------------------
    private fun mapDocToPinData(docId: String, data: Map<String, Any>?): PinData? {
        if (data == null) return null
        return try {
            val titulo = data["titulo"] as? String ?: ""
            val ubicacionStr = data["ubicacion"] as? String
            val ubicacion = ubicacionStr?.let { safeUbicacionOf(it) }

            val x = (data["x"] as? Number)?.toFloat() ?: 0f
            val y = (data["y"] as? Number)?.toFloat() ?: 0f

            val temaStr = data["tema"] as? String
            val tema = temaStr?.let { safeTemaOf(it) } ?: Tema.PINTURA_Y_ARTE_VISUAL

            val imagenes = (data["imagenes"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val descripcion = data["descripcion"] as? String
            val tapRadius = (data["tapRadius"] as? Number)?.toFloat() ?: 0.04f

            PinData(
                id = docId,
                titulo = titulo,
                ubicacion = ubicacion,
                x = x,
                y = y,
                tema = tema,
                color = null,
                iconRes = null,
                imagenes = imagenes, // ✅ URLs Cloudinary
                descripcion = descripcion,
                destino = com.nextapp.monasterio.models.DestinoPin.Detalle(docId),
                tapRadius = tapRadius
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun safeTemaOf(name: String): Tema {
        return try { Tema.valueOf(name) } catch (_: Exception) { Tema.PINTURA_Y_ARTE_VISUAL }
    }

    private fun safeUbicacionOf(name: String): Ubicacion? {
        return try { Ubicacion.valueOf(name) } catch (_: Exception) { null }
    }
}
