package com.nextapp.monasterio.viewModels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextapp.monasterio.models.DatosData
import com.nextapp.monasterio.repository.DatosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


class DatosViewModel(
) : ViewModel() {

    private val _datos = MutableStateFlow<List<DatosData>>(emptyList())
    val datos: StateFlow<List<DatosData>> = _datos

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init{
        loadDatos()
    }

    private fun loadDatos() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = DatosRepository.getDatos()
            _datos.value = result
            _isLoading.value = false
        }
    }
}