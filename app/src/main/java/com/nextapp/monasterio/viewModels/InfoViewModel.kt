package com.nextapp.monasterio.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nextapp.monasterio.models.InfoModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class InfoViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val infoCollection = firestore.collection("info")
    private val documentId = "general"

    private val _infoState = MutableStateFlow(InfoModel())
    val infoState: StateFlow<InfoModel> = _infoState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    companion object {
        private const val TAG = "InfoViewModel"
    }

    init {
        loadInfo()
    }

    fun loadInfo() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val doc = infoCollection.document(documentId).get().await()
                if (doc.exists()) {
                    val info = doc.toObject(InfoModel::class.java)
                    if (info != null) {
                        _infoState.value = info
                    }
                } else {
                    // Si no existe, creamos uno por defecto
                    val defaultInfo = InfoModel()
                    infoCollection.document(documentId).set(defaultInfo).await()
                    _infoState.value = defaultInfo
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading info", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMainContent(newContent: Map<String, String>) {
        updateField("mainContent", newContent) { current ->
            current.copy(mainContent = newContent)
        }
    }

    fun updateLocation(newLocation: Map<String, String>) {
        updateField("location", newLocation) { current ->
            current.copy(location = newLocation)
        }
    }

    fun updateHours(newHours: Map<String, String>) {
        updateField("hours", newHours) { current ->
            current.copy(hours = newHours)
        }
    }

    private fun <T> updateField(fieldName: String, value: T, updateLocal: (InfoModel) -> InfoModel) {
        viewModelScope.launch {
            try {
                infoCollection.document(documentId)
                    .set(mapOf(fieldName to value), SetOptions.merge())
                    .await()
                
                _infoState.value = updateLocal(_infoState.value)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error updating $fieldName", e)
                _error.value = e.message
            }
        }
    }
}
