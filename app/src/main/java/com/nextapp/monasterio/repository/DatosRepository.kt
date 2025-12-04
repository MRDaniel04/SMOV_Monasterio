package com.nextapp.monasterio.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.nextapp.monasterio.models.DatosData
import kotlinx.coroutines.tasks.await

object DatosRepository {

    private const val TAG = "DatosRepository"

    suspend fun getDatos(): List<DatosData> {
        val db = FirebaseFirestore.getInstance()

        try {
            val snapshot = db.collection("datos").get().await()

            if (snapshot.isEmpty) {
                return emptyList()
            }

            // ✅ CORRECCIÓN CRÍTICA: Mapear el ID del documento
            val datos = snapshot.documents.mapNotNull { document ->
                document.toObject(  DatosData::class.java)?.copy(id = document.id)
            }

            return datos

        } catch (e: Exception) {
            return emptyList()
        }
    }
}
