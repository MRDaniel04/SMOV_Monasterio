package com.nextapp.monasterio.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AjustesViewModel : ViewModel(){
    private val _botonesVisibles = MutableStateFlow(true)

    val botonesVisibles: StateFlow<Boolean> = _botonesVisibles

    fun setBotonesVisibles(valor: Boolean){
        _botonesVisibles.value = valor
    }
}