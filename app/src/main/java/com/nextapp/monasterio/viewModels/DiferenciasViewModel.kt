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
import com.nextapp.monasterio.models.Diferencia

class DiferenciasViewModel : ViewModel() {
    private val todosLosPares = obtenerPares()

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

    val diferenciasEncontradas: StateFlow<Int> = juegoActual.map { nivel ->
        nivel.diferencias.count { it.encontrada }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun onTouch(touchX: Float, touchY: Float) {
        val juego = _juegoActual.value
        if (diferenciasEncontradas.value >= juego.diferencias.size) return

        val diferenciaEncontrada = juego.diferencias.firstOrNull {
            it.tocoDiferencia(touchX, touchY) && !it.encontrada
        }

        if (diferenciaEncontrada != null) {
            Log.d("DEBUG_VIEWMODEL","id de la diferencia: ${diferenciaEncontrada.id}")
            diferenciaEncontrada.marcarEncontrada()
            juego.diferencias[juego.diferencias.indexOf(diferenciaEncontrada)] = diferenciaEncontrada
            _juegoActual.value = juego.copy(
                diferencias = juego.diferencias)
        }
    }

    fun reiniciarJuego() {
        _juegoActual.value = seleccionarParAlAzar()
    }
}
