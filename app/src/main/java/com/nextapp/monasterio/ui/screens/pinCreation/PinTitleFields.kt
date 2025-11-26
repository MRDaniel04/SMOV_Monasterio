package com.nextapp.monasterio.ui.screens.pinCreation

import androidx.compose.runtime.Composable
import com.nextapp.monasterio.ui.screens.pinCreation.components.TranslatableTextField
import com.nextapp.monasterio.ui.screens.pinCreation.state.TituloState

@Composable
fun PinTitleFields(state: TituloState) {
    TranslatableTextField(
        label = "Título del Pin",
        state = state,
        singleLine = true
    )
}

