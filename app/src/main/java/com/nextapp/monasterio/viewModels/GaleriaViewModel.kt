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

data class ObraAgrupada(
    val titulo:String,
    val portada: ImagenData,
    val fotos:List<ImagenData>
)


data class GaleriaUiState(
    val images: List<ImagenData> = emptyList(),
    val obrasFiltradas: List<ObraAgrupada> = emptyList(),
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
            "en" -> image.tituloIngles.ifEmpty { image.titulo }
            "de" -> image.tituloAleman.ifEmpty { image.titulo }
            "fr" -> image.tituloFrances.ifEmpty { image.titulo }
            else -> image.titulo
        }
    }

    private fun applyFilters() {
        _uiState.update { currentState ->

            val imagenesFiltradasPorTipo = if (currentState.selectedType == GaleriaType.ALL) {
                currentState.images
            } else {
                currentState.images.filter {
                    it.tipo.equals(currentState.selectedType.id, ignoreCase = true)
                }
            }

            val obrasAgrupadas = imagenesFiltradasPorTipo
                .groupBy { it.titulo } // Esto crea un mapa: "La Piedad" -> [Foto1, Foto2]
                .map { (titulo, listaFotos) ->
                    ObraAgrupada(
                        titulo = titulo,
                        portada = listaFotos.first(), // Usamos la primera como portada
                        fotos = listaFotos
                    )
                }

            val obrasOrdenadas = obrasAgrupadas.sortedWith { obra1, obra2 ->
                val title1 = getLocalizedTitle(obra1.portada).lowercase()
                val title2 = getLocalizedTitle(obra2.portada).lowercase()

                when {
                    title1.isEmpty() && title2.isNotEmpty() -> 1
                    title1.isNotEmpty() && title2.isEmpty() -> -1
                    else -> title1.compareTo(title2)
                }
            }

            currentState.copy(obrasFiltradas = obrasOrdenadas)
        }
    }
}
