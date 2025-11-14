package com.nextapp.monasterio.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.nextapp.monasterio.models.FiguraData
import kotlinx.coroutines.tasks.await

object FiguraRepository {

    private const val TAG = "FiguraRepository"
    /*suspend fun getAllFiguras(): List<FiguraData> {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("figuras").get().await()
        return snapshot.toObjects(FiguraData::class.java)
    }*/
    suspend fun getAllFiguras(): List<FiguraData> {
        val db = FirebaseFirestore.getInstance()

        Log.d(TAG, "Iniciando consulta a la colección 'figuras'...")

        try {
            // 1. Obtener el QuerySnapshot
            val snapshot = db.collection("figuras").get().await()

            // 2. Verificar si la consulta fue exitosa y cuántos documentos hay
            if (snapshot.isEmpty) {
                Log.d(TAG, "Consulta exitosa, pero la colección 'figuras' está vacía o no hay documentos que coincidan.")
            } else {
                Log.d(TAG, "Consulta exitosa. Documentos encontrados: ${snapshot.size()}")

                // 3. Opcional: Imprimir los IDs y datos de cada documento
                for (document in snapshot.documents) {
                    Log.d(TAG, "Documento ID: ${document.id} => Datos: ${document.data}")
                }
            }

            // 4. Convertir a lista de FiguraData
            val figurasList = snapshot.toObjects(FiguraData::class.java)
            Log.d(TAG, "Resultado final de toObjects(): ${figurasList.size} figuras.")

            return figurasList

        } catch (e: Exception) {
            // 5. Capturar y registrar cualquier error de Firebase o red
            Log.e(TAG, "Error al obtener las figuras de Firestore", e)
            return emptyList() // Devuelve una lista vacía en caso de error
        }
    }
}
