package com.nextapp.monasterio.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nextapp.monasterio.models.PlanoData
import kotlinx.coroutines.tasks.await

object PlanoRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("planos")

    // 🔹 Obtener todos los planos
    suspend fun getAllPlanos(): List<PlanoData> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                mapDocToPlanoData(doc.id, data)
            }
        } catch (e: Exception) {
            Log.e("PlanoRepository", "Error obteniendo planos", e)
            emptyList()
        }
    }

    // 🔹 Obtener un plano por ID
    suspend fun getPlanoById(id: String): PlanoData? {
        return try {
            val doc = collection.document(id).get().await()
            if (!doc.exists()) return null
            val plano = mapDocToPlanoData(doc.id, doc.data ?: emptyMap())

            // Log de depuración
            Log.d("PlanoRepository", "📄 Plano cargado: ${plano.nombre}")
            Log.d("PlanoRepository", "📌 Figuras: ${plano.figuras}")
            Log.d("PlanoRepository", "📍 Pines: ${plano.pines}")

            plano
        } catch (e: Exception) {
            Log.e("PlanoRepository", "Error obteniendo plano con id=$id", e)
            null
        }
    }

    // 🔹 Crear o actualizar un plano
    suspend fun createOrUpdatePlano(plano: PlanoData) {
        try {
            val payload = mapOf(
                "nombre" to plano.nombre,
                "plano" to plano.plano,
                "figuras" to plano.figuras,
                "pines" to plano.pines
            )
            collection.document(plano.id.ifEmpty { collection.document().id }).set(payload).await()
        } catch (e: Exception) {
            Log.e("PlanoRepository", "Error guardando plano", e)
        }
    }

    // 🔹 Eliminar un plano
    suspend fun deletePlano(id: String) {
        try {
            collection.document(id).delete().await()
        } catch (e: Exception) {
            Log.e("PlanoRepository", "Error eliminando plano con id=$id", e)
        }
    }

    // 🔹 Mapeo documento → PlanoData (con soporte para referencias Firebase)
    private fun mapDocToPlanoData(id: String, data: Map<String, Any>): PlanoData {
        val figuras = (data["figuras"] as? List<*>)?.mapNotNull { ref ->
            when (ref) {
                is String -> ref // caso raro, si fuera string
                is DocumentReference -> ref.path // caso normal: "/figuras/claustro"
                else -> null
            }
        } ?: emptyList()

        val pines = (data["pines"] as? List<*>)?.mapNotNull { ref ->
            when (ref) {
                is String -> ref
                is DocumentReference -> ref.path
                else -> null
            }
        } ?: emptyList()

        return PlanoData(
            id = id,
            nombre = data["nombre"] as? String ?: "",
            plano = data["plano"] as? String ?: "",
            figuras = figuras,
            pines = pines
        )
    }

    suspend fun addPinToPlano(planoId: String, pinId: String) {
        val pinRef = firestore.collection("pines").document(pinId)

        firestore.collection("planos")
            .document(planoId)
            .update("pines", FieldValue.arrayUnion(pinRef))
            .await()
    }


}
