package com.nextapp.monasterio.viewModels

import android.content.Context
import android.content.res.Resources
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
import com.nextapp.monasterio.repository.UserPreferencesRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.nextapp.monasterio.R

class DiferenciasViewModel(
    private val prefsRepository: UserPreferencesRepository,
    private val resources: Resources
) : ViewModel() {
    private val todosLosPares = obtenerPares()

    private var ultimoParID: Int = runBlocking {
        prefsRepository.getUltimoParID()
    }

    val showInstructionsDialog: StateFlow<Boolean> =
        prefsRepository.isInstructionsDifferencesDismissed
            .map { isDismissed -> !isDismissed }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )

    fun markInstructionsAsShown() {
        viewModelScope.launch {
            prefsRepository.dismissInstructionsDifferences()

        }
    }

    private fun seleccionarParAlAzar(): NivelJuego {
        var parSeleccionado = todosLosPares.random()

        while (parSeleccionado.id == ultimoParID) {
            parSeleccionado = todosLosPares.random()
        }

        ultimoParID = parSeleccionado.id

        viewModelScope.launch {
            prefsRepository.setUltimoParID(ultimoParID)
        }

        val diferenciasIniciales = parSeleccionado.diferencias.map { diff ->
            diff.copy(_encontrada = false)
        }

        val pistasIniciales = parSeleccionado.pistas

        return parSeleccionado.copy(
            diferencias = mutableStateListOf(*diferenciasIniciales.toTypedArray()),
            pistas = mutableStateListOf(*pistasIniciales.toTypedArray())
        )
    }

    private val _juegoActual = MutableStateFlow(seleccionarParAlAzar())
    val juegoActual: StateFlow<NivelJuego> = _juegoActual.asStateFlow()

    private val _diferenciasEncontradas = MutableStateFlow(0)
    val diferenciasEncontradas: StateFlow<Int> = _diferenciasEncontradas.asStateFlow()



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

            val nuevaListaPistas = juego.pistas.filter { pista ->
                pista.idDiferencia != diferenciaEncontrada.id
            }

            Log.d("DEPURACION_TOUCH", "Pistas después del filtro: ${nuevaListaPistas.map { it.idDiferencia }}")

            _juegoActual.value = juego.copy(
                diferencias = nuevaListaDiferencias,
                pistas = nuevaListaPistas
            )
            _diferenciasEncontradas.value++
        }
    }

    fun mostrarPistasAleatoria(): String{
        val juego = _juegoActual.value

        val idsDiferenciasNoEncontradas = juego.diferencias
            .filter { !it.encontrada }
            .map { it.id }


        val pistasDisponibles = juego.pistas.filter { pista ->
            idsDiferenciasNoEncontradas.contains(pista.idDiferencia)
        }

        Log.d("PISTAS_DEBUG", "Pistas disponibles para mostrar: $pistasDisponibles")

        if (pistasDisponibles.isEmpty()) {
            return ""
        }

        val pista = pistasDisponibles.random()
        val textoPista = resources.getStringArray(R.array.clues)[pista.idRecurso]

        return textoPista
    }


    companion object {
        fun Factory(prefsRepository: UserPreferencesRepository,context : Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    val resources = context.resources
                    return DiferenciasViewModel(prefsRepository,resources) as T
                }
            }
    }
}
