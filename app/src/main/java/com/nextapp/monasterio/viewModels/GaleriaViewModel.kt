package com.nextapp.monasterio.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextapp.monasterio.repository.ImagenRepository
import com.nextapp.monasterio.models.ImagenData
import com.nextapp.monasterio.utils.GaleriaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GaleriaUiState(
    val images: List<ImagenData> = emptyList(),
    val filteredImages: List<ImagenData> = emptyList(),
    val selectedType: GaleriaType = GaleriaType.ALL,
    val isLoading: Boolean = false
)

class GaleriaViewModel(
    private val imagenRepository: ImagenRepository = ImagenRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GaleriaUiState())
    val uiState: StateFlow<GaleriaUiState> = _uiState.asStateFlow()

    init {
        loadImages()
    }

    private fun loadImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val allImages = imagenRepository.getAllImages().filter { it.tipo.isNotBlank() }
            _uiState.update { 
                it.copy(
                    images = allImages,
                    filteredImages = allImages,
                    isLoading = false
                ) 
            }
        }
    }

    fun onTypeSelected(type: GaleriaType) {
        _uiState.update { currentState ->
            val filtered = if (type == GaleriaType.ALL) {
                currentState.images
            } else {
                currentState.images.filter { 
                    // Normalize strings for comparison (optional but good practice)
                    it.tipo.equals(type.id, ignoreCase = true)
                }
            }
            currentState.copy(selectedType = type, filteredImages = filtered)
        }
    }
}
