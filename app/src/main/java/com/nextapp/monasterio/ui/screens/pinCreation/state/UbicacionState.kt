package com.nextapp.monasterio.ui.screens.pinCreation.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class UbicacionState(
    ubicacionDetallada: String = "", // Nombre de tu campo anterior
    areaPrincipal: String = ""       // Nuevo campo
) {
    // Ubicación Específica/Detallada (Ej: "Crucero", "Sacristía")
    var ubicacionDetallada by mutableStateOf(ubicacionDetallada)

    // Área Principal (Ej: "Iglesia", "Monasterio")
    var areaPrincipal by mutableStateOf(areaPrincipal)
}