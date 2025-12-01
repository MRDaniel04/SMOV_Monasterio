package com.nextapp.monasterio.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.nextapp.monasterio.models.FiguraData
import kotlinx.coroutines.tasks.await

object FiguraRepository {

    private const val TAG = "FiguraRepository"

    suspend fun getAllFiguras(): List<FiguraData> {
        val db = FirebaseFirestore.getInstance()

        Log.d(TAG, "Iniciando consulta a la colección 'figuras'...")

        try {
            val snapshot = db.collection("figuras").get().await()

            if (snapshot.isEmpty) {
                Log.d(TAG, "Consulta exitosa, pero la colección 'figuras' está vacía.")
                return emptyList()
            }

            Log.d(TAG, "Consulta exitosa. Documentos encontrados: ${snapshot.size()}")

            // ✅ CORRECCIÓN CRÍTICA: Mapear el ID del documento
            val figurasList = snapshot.documents.mapNotNull { document ->
                // document.id contiene el ID de Firebase (ej. "arco_mudejar")
                // Se deserializa el objeto y se le añade el ID con .copy()
                document.toObject(FiguraData::class.java)?.copy(id = document.id)
            }

            Log.d(TAG, "Resultado final de FiguraRepository.getAllFiguras() (con ID): ${figurasList.size} figuras.")

            // Opcional: Log para verificar que el ID se mapeó correctamente
            figurasList.take(2).forEach {
                Log.d(TAG, "Verificación ID: ${it.nombre} -> ID: ${it.id}")
            }

            return figurasList

        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener las figuras de Firestore", e)
            return emptyList()
        }
    }

    suspend fun getFiguraById(id: String): FiguraData? {
        val db = FirebaseFirestore.getInstance()
        return try {
            val doc = db.collection("figuras").document(id).get().await()
            if (doc.exists()) {
                doc.toObject(FiguraData::class.java)?.copy(id = doc.id)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener la figura $id de Firestore", e)
            null
        }
    }
}