package com.nextapp.monasterio.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nextapp.monasterio.models.GridPosicion
import com.nextapp.monasterio.models.PuzzleManager
import com.nextapp.monasterio.models.PiezaPuzzle
import com.nextapp.monasterio.models.PuzzleSize
import com.nextapp.monasterio.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class puzzleUiState(
    val piezas : List<PiezaPuzzle> = emptyList(),
    val size: PuzzleSize,
    val imagenCompleta : Int,
    val solucionado: Boolean = false,
)

class PuzzleViewModel(
    private val tamañoPuzzle: PuzzleSize,
    private val imagenId: List<Int>,
    private val prefsRepository: UserPreferencesRepository,
    private val imagenCompleta: Int) : ViewModel() {

    private val _uiState = MutableStateFlow(puzzleUiState(size=tamañoPuzzle, imagenCompleta = imagenCompleta))
    val uiState: StateFlow<puzzleUiState> = _uiState

    val showInstructionsDialog : StateFlow<Boolean> = prefsRepository.isInstructionsPuzzleDismissed
        .map { isDismissed -> !isDismissed }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    private val gameManager = PuzzleManager(tamañoPuzzle)

    init{
        resetGame()
    }

    fun markInstructionsAsShown() {
        viewModelScope.launch {
            prefsRepository.dismissInstructionsPuzzle()
        }
    }

    private fun resetGame(){
        val piezasIniciales = gameManager.inicializarPiezas(imagenId)

        _uiState.value = puzzleUiState(
            piezas = piezasIniciales,
            size = tamañoPuzzle,
            imagenCompleta = imagenCompleta,
            solucionado = false,
        )
    }

    fun soltarPieza(piezaId: Int, newGridPosicion: GridPosicion){
        _uiState.update{currentUiState->
            val piezasActualizadas = currentUiState.piezas.map{pieza->
                if(pieza.id == piezaId){
                    val esCorrecta = newGridPosicion == pieza.posicionCorrecta
                    val posicionFinal = if (esCorrecta) {
                        newGridPosicion
                    }
                    else {
                        pieza.posicionActual
                    }
                    pieza.copy(
                        posicionActual = posicionFinal,
                        encajada = esCorrecta
                    )
                }else{
                    pieza
                }
            }
            val solved = piezasActualizadas.all{it.encajada}
            currentUiState.copy(
                piezas=piezasActualizadas,
                solucionado = solved
            )
        }
    }

}

class PuzzleViewModelFactory(
    private val tamañoPuzzle: PuzzleSize,
    private val imagenesIds: List<Int>,
    private val prefsRepository: UserPreferencesRepository,
    private val imagenCompleta : Int
) : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PuzzleViewModel::class.java)) {
            return PuzzleViewModel(tamañoPuzzle, imagenesIds,prefsRepository,imagenCompleta) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
