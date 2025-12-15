package com.nextapp.monasterio.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextapp.monasterio.repository.UserPreferencesRepository // 1. Importar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AjustesViewModel : ViewModel() {
    // 2. Obtener la instancia del repositorio
    private val prefsRepository = UserPreferencesRepository.instance

    // --- Lógica existente de botones ---
    private val _botonesVisibles = MutableStateFlow(true)
    val botonesVisibles: StateFlow<Boolean> = _botonesVisibles
    fun setBotonesVisibles(valor: Boolean) {
        _botonesVisibles.value = valor
    }

    val isMainMapDismissed = prefsRepository.isMainMapTutorialDismissed
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isSubMapDismissed = prefsRepository.isSubMapTutorialDismissed
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isPinDismissed = prefsRepository.isPinTutorialDismissed
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // --- FUNCIONES PARA OCULTAR ---

    fun dismissMainMap() = viewModelScope.launch { prefsRepository.dismissAllTutorials() }
    fun dismissSubMap() = viewModelScope.launch { prefsRepository.dismissAllTutorials() }
    fun dismissPin() = viewModelScope.launch { prefsRepository.dismissAllTutorials() }}