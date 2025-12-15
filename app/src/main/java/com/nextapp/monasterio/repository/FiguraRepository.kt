package com.nextapp.monasterio.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.nextapp.monasterio.models.FiguraData
import com.nextapp.monasterio.models.Punto
import kotlinx.coroutines.tasks.await

object FiguraRepository {

    suspend fun getAllFiguras(): List<FiguraData> {
        val db = FirebaseFirestore.getInstance()
        return try {
            val snapshot = db.collection("figuras").get().await()
            snapshot.documents.mapNotNull { doc ->
                mapDocumentToFigura(doc.id, doc.data)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFiguraByNombre(nombre: String): FiguraData? {
        val db = FirebaseFirestore.getInstance()
        return try {
            val snapshot = db.collection("figuras")
                .whereEqualTo("nombre", nombre)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                mapDocumentToFigura(doc.id, doc.data)
            } else null
        } catch (e:Exception) {
            null
        }
    }

    suspend fun getFiguraById(id: String): FiguraData? {
        val db = FirebaseFirestore.getInstance()
        return try {
            val doc = db.collection("figuras").document(id).get().await()
            if (doc.exists()) {
                mapDocumentToFigura(doc.id, doc.data)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // 👇 FUNCIÓN DE MAPEO MANUAL PARA EVITAR ERRORES DE TIPO 👇
    private fun mapDocumentToFigura(id: String, data: Map<String, Any>?): FiguraData? {
        if (data == null) return null
        try {
            // 1. Tratamiento especial para IMÁGENES (Reference -> String)
            val imagenesRaw = data["imagenes"]
            val imagenesList = if (imagenesRaw is List<*>) {
                imagenesRaw.mapNotNull { item ->
                    if (item is DocumentReference) item.id else item.toString()
                }
            } else emptyList()

            // 2. Tratamiento especial para PATH (List<HashMap> -> List<Punto>)
            val pathRaw = data["path"] as? List<Map<String, Number>>
            val pathList = pathRaw?.map {
                Punto((it["x"]?.toFloat() ?: 0f), (it["y"]?.toFloat() ?: 0f))
            } ?: emptyList()

            return FiguraData(
                id = id,
                nombre = data["nombre"] as? String ?: "",
                nombre_en = data["nombre_en"] as? String ?: "",
                nombre_de = data["nombre_de"] as? String ?: "",
                nombre_fr = data["nombre_fr"] as? String ?: "",
                descripcion = data["descripcion"] as? String ?: "",
                imagenes = imagenesList, // Ahora es List<String>

                info_es = data["info_es"] as? String ?: "",
                info_en = data["info_en"] as? String ?: "",
                info_de = data["info_de"] as? String ?: "",
                info_fr = data["info_fr"] as? String ?: "",

                path = pathList,
                colorResaltado = (data["colorResaltado"] as? Number)?.toLong() ?: 0xFFFFFFFF,
                scale = (data["scale"] as? Number)?.toFloat() ?: 1f,
                offsetX = (data["offsetX"] as? Number)?.toFloat() ?: 0f,
                offsetY = (data["offsetY"] as? Number)?.toFloat() ?: 0f,

                tipoDestino = data["tipoDestino"] as? String ?: "",
                valorDestino = data["valorDestino"] as? String ?: "",

                audioUrl_es = data["audioUrl_es"] as? String,
                audioUrl_en = data["audioUrl_en"] as? String,
                audioUrl_de = data["audioUrl_de"] as? String ?: data["audioUrl_ge"] as? String, // Soporte doble
                audioUrl_fr = data["audioUrl_fr"] as? String,

                vista360Url = data["vista360Url"] as? String
            )
        } catch (e: Exception) {
            return null
        }
    }
}