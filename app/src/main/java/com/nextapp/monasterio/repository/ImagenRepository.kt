package com.nextapp.monasterio.data

import com.google.firebase.firestore.FirebaseFirestore
import com.nextapp.monasterio.models.ImagenData
import kotlinx.coroutines.tasks.await

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
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ImagenData::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene una imagen concreta por su ID.
     */
    suspend fun getImageById(id: String): ImagenData? {
        return try {
            val doc = collection.document(id).get().await()
            doc.toObject(ImagenData::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene la imagen usada como fondo de inicio.
     * El documento en Firestore debe llamarse: "imagen_fondo_inicio"
     */
    suspend fun getImagenFondoInicio(): ImagenData? {
        return try {
            val doc = collection.document("imagen_fondo_inicio").get().await()
            if (doc.exists()) {
                doc.toObject(ImagenData::class.java)?.copy(id = doc.id)
            } else null
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
            false
        }
    }
}
