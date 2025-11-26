package com.nextapp.monasterio.ui.screens.pinCreation.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class UbicacionState(
    displayName: String = ""
) {
    var displayName by mutableStateOf(displayName)
}
