package com.nextapp.monasterio.viewModels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nextapp.monasterio.models.HistoriaPeriod
import com.nextapp.monasterio.services.CloudinaryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel para gestionar períodos históricos y sus imágenes
 */
class HistoriaViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val historyCollection = firestore.collection("historia")

    private val _historyPeriods = MutableStateFlow<List<HistoriaPeriod>>(emptyList())
    val historyPeriods: StateFlow<List<HistoriaPeriod>> = _historyPeriods.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadingPeriodId = MutableStateFlow<String?>(null)
    val uploadingPeriodId: StateFlow<String?> = _uploadingPeriodId.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    companion object {
        private const val TAG = "HistoryViewModel"
    }

    init {
        loadHistoryPeriods()
    }

    /**
     * Carga todos los períodos históricos desde Firebase
     */
    fun loadHistoryPeriods() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando períodos históricos...")

                val snapshot = historyCollection
                    .orderBy("order")
                    .get()
                    .await()

                val periods = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Parsear el contenido: puede ser un String (legacy) o un Map (nuevo)
                        val rawContent = doc.get("content")
                        val contentMap = when (rawContent) {
                            is Map<*, *> -> rawContent.mapKeys { it.key.toString() }.mapValues { it.value.toString() }
                            is String -> mapOf("es" to rawContent) // Migración al vuelo para datos antiguos
                            else -> emptyMap()
                        }

                        // Parsear el título: puede ser un String (legacy) o un Map (nuevo)
                        val rawTitle = doc.get("title")
                        val titleMap = when (rawTitle) {
                            is Map<*, *> -> rawTitle.mapKeys { it.key.toString() }.mapValues { it.value.toString() }
                            is String -> mapOf("es" to rawTitle)
                            else -> emptyMap()
                        }

                        HistoriaPeriod(
                            id = doc.id,
                            title = titleMap,
                            content = contentMap,
                            imageUrls = (doc.get("imageUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            order = doc.getLong("order")?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al parsear período ${doc.id}", e)
                        null
                    }
                }

                _historyPeriods.value = periods
                Log.d(TAG, "Períodos cargados: ${periods.size}")

            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar períodos históricos", e)
                _error.value = e.message
            }
        }
    }

    /**
     * Actualiza el contenido (textos) de un período histórico
     */
    fun updatePeriodContent(periodId: String, newContent: Map<String, String>) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Actualizando contenido para período: $periodId")
                
                historyCollection.document(periodId)
                    .update("content", newContent)
                    .await()

                // Actualizar estado local optimísticamente o recargar
                _historyPeriods.value = _historyPeriods.value.map { 
                    if (it.id == periodId) it.copy(content = newContent) else it 
                }
                
                Log.d(TAG, "Contenido actualizado exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar contenido", e)
                _error.value = e.message
            }
        }
    }

    /**
     * Sube una imagen a Cloudinary y la añade al array de imágenes en Firebase
     * @param uri URI de la imagen seleccionada
     * @param context Contexto de la aplicación
     * @param periodId ID del período histórico
     */
    fun uploadImage(uri: Uri, context: Context, periodId: String) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                _uploadingPeriodId.value = periodId
                _error.value = null
                Log.d(TAG, "Subiendo imagen para período: $periodId")

                // 1. Subir a Cloudinary
                val uploadResult = CloudinaryService.uploadImage(uri, context)

                if (uploadResult.isFailure) {
                    throw uploadResult.exceptionOrNull() ?: Exception("Error desconocido al subir imagen")
                }

                val imageUrl = uploadResult.getOrThrow()
                Log.d(TAG, "Imagen subida a Cloudinary: $imageUrl")

                // 2. Añadir al array de imageUrls en Firebase
                historyCollection
                    .document(periodId)
                    .update("imageUrls", FieldValue.arrayUnion(imageUrl))
                    .await()

                Log.d(TAG, "URL de imagen añadida al array en Firebase")

                // 3. Recargar períodos
                loadHistoryPeriods()

            } catch (e: Exception) {
                Log.e(TAG, "Error al subir imagen", e)
                _error.value = e.message
            } finally {
                _isUploading.value = false
                _uploadingPeriodId.value = null
            }
        }
    }

    /**
     * Elimina una imagen específica del array de imágenes de un período histórico
     * @param periodId ID del período histórico
     * @param imageUrl URL de la imagen a eliminar
     */
    fun deleteImage(periodId: String, imageUrl: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Eliminando imagen del período: $periodId")

                // Eliminar URL específica del array imageUrls en Firebase
                historyCollection
                    .document(periodId)
                    .update("imageUrls", FieldValue.arrayRemove(imageUrl))
                    .await()

                Log.d(TAG, "Imagen eliminada exitosamente")

                // Recargar períodos
                loadHistoryPeriods()

            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar imagen", e)
                _error.value = e.message
            }
        }
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _error.value = null
    }
}
