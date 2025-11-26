package com.nextapp.monasterio.ui.screens.pinCreation

import androidx.compose.runtime.Composable
import com.nextapp.monasterio.ui.screens.pinCreation.components.TranslatableTextField
import com.nextapp.monasterio.ui.screens.pinCreation.state.DescripcionState

@Composable
fun PinDescriptionFields(state: DescripcionState) {
    TranslatableTextField(
        label = "Descripción del Pin",
        state = state,
        singleLine = false
    )
}
