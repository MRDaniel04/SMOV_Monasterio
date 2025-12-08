package com.nextapp.monasterio.ui.screens.pinCreation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nextapp.monasterio.ui.screens.pinCreation.components.TranslatableTextField
import com.nextapp.monasterio.ui.screens.pinCreation.state.DescripcionState
import com.nextapp.monasterio.R

@Composable
fun PinDescriptionFields(
    state: DescripcionState,
    isEditing: Boolean = false,
    onChanged: () -> Unit = {}
) {
    TranslatableTextField(
        label = stringResource(R.string.pin_description_label),
        state = state,
        singleLine = false,
        isEditing = isEditing,
        onChanged = onChanged
    )
}