package com.nextapp.monasterio.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nextapp.monasterio.models.GridPosicion
import com.nextapp.monasterio.models.PuzzleManager
import com.nextapp.monasterio.models.PiezaPuzzle
import com.nextapp.monasterio.models.PuzzleSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update



data class puzzleUiState(
    val piezas : List<PiezaPuzzle> = emptyList(),
    val size: PuzzleSize,
    val solucionado: Boolean = false,
)

class PuzzleViewModel(
    private val tamañoPuzzle: PuzzleSize,
    private val imagenId: List<Int>) : ViewModel() {

    private val piezas = List(25){it}

    private val _uiState = MutableStateFlow(puzzleUiState(size=tamañoPuzzle))
    val uiState: StateFlow<puzzleUiState> = _uiState
    private val gameManager = PuzzleManager(tamañoPuzzle)

    init{
        resetGame()
    }

    private fun resetGame(){
        val piezasIniciales = gameManager.inicializarPiezas(imagenId)
        if (piezasIniciales.isNotEmpty()) {
            println("DEBUG: Pieza 0 posición inicial: R${piezasIniciales[0].posicionActual.row} C${piezasIniciales[0].posicionActual.column}")
        }
        _uiState.value = puzzleUiState(
            piezas = piezasIniciales,
            size = tamañoPuzzle,
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
    private val imagenesIds: List<Int>
) : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PuzzleViewModel::class.java)) {
            return PuzzleViewModel(tamañoPuzzle, imagenesIds) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
