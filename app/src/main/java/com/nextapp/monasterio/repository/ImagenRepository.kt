package com.nextapp.monasterio.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.nextapp.monasterio.models.ImagenData
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
class ImagenRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val collection = firestore.collection("imagenes")

    /**
     * Obtiene todas las imágenes de la colección "imagenes".
     */
    suspend fun getAllImages(): List<ImagenData> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc -> mapDocToImagenData(doc) }
        } catch (e: Exception) {
            Log.e("ImagenRepository", "Error getting all images", e)
            emptyList()
        }
    }

    /**
     * Obtiene una imagen concreta por su ID.
     */
    suspend fun getImageById(id: String): ImagenData? {
        return try {
            val doc = collection.document(id).get().await()
            if (!doc.exists()) return null
            mapDocToImagenData(doc)
        } catch (e: Exception) {
            null
        }
    }
    private fun mapDocToImagenData(doc: DocumentSnapshot): ImagenData? {
        val data = doc.data ?: return null

        return ImagenData(
            id = doc.id,
            url = data["url"] as? String ?: "",
            etiqueta = data["etiqueta"] as? String ?: "",
            titulo = data["titulo"] as? String ?: "",
            tituloIngles = data["tituloIngles"] as? String ?: "",
            tituloAleman = data["tituloAleman"] as? String ?: "",
            tituloFrances = data["tituloFrances"] as? String ?: "",
            foco = (data["foco"] as? Number)?.toFloat() ?: 0f,
            tipo = data["tipo"] as? String ?: ""
        )
    }

    /**
     * Obtiene la imagen usada como fondo de inicio.
     * El documento en Firestore debe llamarse: "imagen_fondo_inicio"
     */
    suspend fun getImagenFondoInicio(): ImagenData? {
        return try {
            val doc = collection.document("imagen_fondo_inicio").get().await()
            if (!doc.exists()) return null
            // Uso del helper
            mapDocToImagenData(doc)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Guarda o actualiza una imagen en Firestore.
     * Útil por si en el futuro quieres permitir editar textos o cambiar una imagen ya existente.
     */
    suspend fun saveImage(image: ImagenData): Boolean {
        return try {
            collection.document(image.id)
                .set(image.copy(id = image.id))
                .await()
            true
        } catch (e: Exception) {
            Log.e("ImagenRepository", "❌ Error al guardar/actualizar imagen ${image.id}", e)
            false
        }
    }

    suspend fun updateImagenFondoInicio(nuevaUrl: String) {
        try {
            collection
                .document("imagen_fondo_inicio")
                .update("url", nuevaUrl)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getImagesByIds(ids: List<String>): List<ImagenData> {
        if (ids.isEmpty()) return emptyList()

        val chunkedIds = ids.chunked(10)

        val allImages = mutableListOf<ImagenData>()

        for (idChunk in chunkedIds) {

            val snapshot = collection
                .whereIn(FieldPath.documentId(), idChunk)
                .get()
                .await()

            val chunkImages = snapshot.documents.mapNotNull { doc ->
                mapDocToImagenData(doc)
            }

            allImages.addAll(chunkImages)
        }

        return allImages
    }

}
