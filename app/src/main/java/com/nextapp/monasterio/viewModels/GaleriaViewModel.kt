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

    private var currentLanguage = "es"

    init {
        loadImages()
    }

    fun setLanguage(language: String) {
        if (currentLanguage != language) {
            currentLanguage = language
            applyFilters()
        }
    }

    private fun loadImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val allImages = imagenRepository.getAllImages().filter { it.tipo.isNotBlank() }
            _uiState.update { 
                it.copy(
                    images = allImages,
                    isLoading = false
                ) 
            }
            applyFilters()
        }
    }

    fun onTypeSelected(type: GaleriaType) {
        _uiState.update { it.copy(selectedType = type) }
        applyFilters()
    }

    private fun getLocalizedTitle(image: ImagenData): String {
        return when (currentLanguage) {
            "en" -> if (image.tituloIngles.isNotEmpty()) image.tituloIngles else image.titulo
            "de" -> if (image.tituloAleman.isNotEmpty()) image.tituloAleman else image.titulo
            "fr" -> if (image.tituloFrances.isNotEmpty()) image.tituloFrances else image.titulo
            else -> image.titulo
        }
    }

    private fun applyFilters() {
        _uiState.update { currentState ->
            val sortedImages = currentState.images.sortedWith { image1, image2 ->
                val title1 = getLocalizedTitle(image1).lowercase()
                val title2 = getLocalizedTitle(image2).lowercase()

                when {
                    title1.isEmpty() && title2.isNotEmpty() -> 1
                    title1.isNotEmpty() && title2.isEmpty() -> -1
                    else -> title1.compareTo(title2)
                }
            }

            val filtered = if (currentState.selectedType == GaleriaType.ALL) {
                sortedImages
            } else {
                sortedImages.filter { 
                    it.tipo.equals(currentState.selectedType.id, ignoreCase = true)
                }
            }
            currentState.copy(images = sortedImages, filteredImages = filtered)
        }
    }
}
