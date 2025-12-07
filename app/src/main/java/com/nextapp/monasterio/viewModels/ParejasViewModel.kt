package com.nextapp.monasterio.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nextapp.monasterio.models.ParejasManager
import com.nextapp.monasterio.models.ParejasPieza
import com.nextapp.monasterio.models.ParejasSize
import com.nextapp.monasterio.repository.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class parejasUiState(
    val piezas : List<ParejasPieza> = emptyList(),
    val size : ParejasSize,
    val solucionado : Boolean = false,
    val mostradoInicial : Boolean = true,
    val esperandoBoton : Boolean = true,
    val verificandoPareja: Boolean = false,
    val parejas : Int = (size.columns* size.rows)/2
)

class ParejasViewModel (
    val size: ParejasSize,
    val imagenesIds: List<Int>,
    private val prefsRepository: UserPreferencesRepository
): ViewModel(){

    private val _uiState = MutableStateFlow(parejasUiState(size=size))

    val uiState: StateFlow<parejasUiState> = _uiState

    val showInstructionsDialog : StateFlow<Boolean?> = prefsRepository.isInstructionsPairsDismissed
        .map { isDismissed -> !isDismissed }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


    val piezasManager = ParejasManager(size)

    init{
        resetGame()
    }

    fun markInstructionsAsShown() {
        viewModelScope.launch {
            prefsRepository.dismissInstructionsPairs()
        }
    }

    fun resetGame(){
        val piezasIniciales = piezasManager.inicializarParejaPiezas(imagenesIds)
        _uiState.value = parejasUiState(
            piezas = piezasIniciales,
            size = size,
            solucionado = false,
            mostradoInicial =  true
        )
    }

    fun iniciarJuego(){
        _uiState.update{
            it.copy(
                esperandoBoton = false,
            )
        }

        // 3. Iniciar el delay de 2 segundos
        viewModelScope.launch {
            delay(2000)
            _uiState.update{
                it.copy(mostradoInicial = false) // 4. Las cartas se giran (FIN de la pausa)
            }
        }
    }

    private val piezasVolteadas = mutableListOf<ParejasPieza>()

    fun onClickPieza(id:Int){
        if(_uiState.value.verificandoPareja) return
        if(piezasVolteadas.size == 2) return
        val piezaClickeada = _uiState.value.piezas.find { it.id == id } ?: return

        if(piezaClickeada.estaVolteada || piezaClickeada.conPareja) return

        val nuevasPiezas = _uiState.value.piezas.toMutableList().apply {
            val index = indexOfFirst { it.id == id }
            this[index] = piezaClickeada.copy(estaVolteada = true)
        }
        _uiState.update { it.copy(piezas = nuevasPiezas) }

        val nuevaPiezaVolteada = nuevasPiezas.find { it.id == id }!!
        piezasVolteadas.add(nuevaPiezaVolteada)

        // 2. Si ya hay dos piezas volteadas, chequeamos la pareja
        if (piezasVolteadas.size == 2) {
            chequearPareja(piezasVolteadas)
        }

    }

    private fun chequearPareja(piezas : List<ParejasPieza>,){
        val pieza1 = piezas[0]
        val pieza2 = piezas[1]

        viewModelScope.launch {
            _uiState.update { it.copy(verificandoPareja = true) }
            delay(1000)
            var piezasCompletas = _uiState.value.piezas
            var parejasRestantes = _uiState.value.parejas

            if(pieza1.imagen == pieza2.imagen){
                piezasCompletas = piezasCompletas.map { pieza ->
                    if (pieza.id == pieza1.id || pieza.id == pieza2.id) {
                        // Crea una nueva pieza, resuelta y no volteada
                        pieza.copy(conPareja = true, estaVolteada = false).also{
                        }
                    } else {
                        pieza
                    }
                }
                parejasRestantes -= 1
            }
            else{
                piezasCompletas = piezasCompletas.map { pieza ->
                    if (pieza.id == pieza1.id || pieza.id == pieza2.id) {
                        // Crea una nueva pieza, des-volteada
                        pieza.copy(estaVolteada = false).also{
                        }
                    } else {
                        pieza
                    }
                }
            }
            this@ParejasViewModel.piezasVolteadas.clear()
            _uiState.update { it.copy(piezas = piezasCompletas, verificandoPareja = false, parejas=parejasRestantes) }
            val todasPiezasConPareja = estaTerminado()
            _uiState.update { it.copy(solucionado = estaTerminado()) }
        }
    }

    private fun estaTerminado(): Boolean{
        val piezasActuales = _uiState.value.piezas
        val todasPiezasConPareja = piezasActuales.all{pieza ->
            pieza.conPareja
        }
        return todasPiezasConPareja
    }


}

class ParejasViewModelFactory(
    private val size: ParejasSize,
    private val imagenesIds: List<Int>,
    private val prefsRepository: UserPreferencesRepository
) : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParejasViewModel::class.java)) {
            return ParejasViewModel(size, imagenesIds,prefsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}