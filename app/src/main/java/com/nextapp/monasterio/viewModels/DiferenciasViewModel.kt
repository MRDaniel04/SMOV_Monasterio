package com.nextapp.monasterio.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextapp.monasterio.models.NivelJuego
import com.nextapp.monasterio.models.obtenerPares
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.nextapp.monasterio.models.Diferencia
import com.nextapp.monasterio.repository.UserPreferencesRepository
import kotlinx.coroutines.launch

class DiferenciasViewModel(
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {
    private val todosLosPares = obtenerPares()

    val showInstructionsDialog : StateFlow<Boolean> = prefsRepository.isInstructionsDifferencesDismissed
        .map { isDismissed -> !isDismissed }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private fun seleccionarParAlAzar(): NivelJuego{
        val parSeleccionado = todosLosPares.random()

        val diferencias = parSeleccionado.diferencias.map { diff ->
            Diferencia(
                diff.id,
                diff.rectX,
                diff.rectY,
                diff.width,
                diff.height,
                _encontrada = false
            )
        }

        return parSeleccionado.copy(
            diferencias = mutableStateListOf(*diferencias.toTypedArray())
        )
    }

    private val _juegoActual = MutableStateFlow(seleccionarParAlAzar())
    val juegoActual: StateFlow<NivelJuego> = _juegoActual.asStateFlow()

    private val _diferenciasEncontradas = MutableStateFlow(0)
    val diferenciasEncontradas: StateFlow<Int> = _diferenciasEncontradas.asStateFlow()

    fun markInstructionsAsShown() {
        viewModelScope.launch {
            prefsRepository.dismissInstructionsDifferences()
        }
    }


    fun onTouch(touchX: Float, touchY: Float) {
        val juego = _juegoActual.value
        if (diferenciasEncontradas.value >= juego.diferencias.size) return

        val diferenciaEncontrada = juego.diferencias.firstOrNull {
            it.tocoDiferencia(touchX, touchY) && !it.encontrada
        }

        if (diferenciaEncontrada != null) {
            val nuevaDiferencia = diferenciaEncontrada.copy(_encontrada = true)

            val nuevaListaDiferencias = juego.diferencias.toMutableList().apply {
                val index = indexOf(diferenciaEncontrada) // O encontrar el índice
                if (index != -1) {
                    this[index] = nuevaDiferencia
                }
            }.toList()

            _juegoActual.value = juego.copy(diferencias = nuevaListaDiferencias)
            _diferenciasEncontradas.value++
        }
    }

    fun reiniciarJuego() {
        _juegoActual.value = seleccionarParAlAzar()
    }

    companion object {
        fun Factory(prefsRepository: UserPreferencesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    return DiferenciasViewModel(prefsRepository) as T
                }
            }
    }
}
