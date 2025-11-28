package com.nextapp.monasterio.ui.screens.pinCreation

import androidx.compose.runtime.Composable
import com.nextapp.monasterio.ui.screens.pinCreation.components.TranslatableTextField
import com.nextapp.monasterio.ui.screens.pinCreation.state.DescripcionState
import com.nextapp.monasterio.ui.screens.pinCreation.state.TituloState

@Composable
fun PinTitleFields(
    state: TituloState,
    isEditing: Boolean = false
) {
    TranslatableTextField(
        label = "Descripción del Pin",
        state = state,
        singleLine = false,
        isEditing = isEditing    // ← **CLAVE**
    )
}
