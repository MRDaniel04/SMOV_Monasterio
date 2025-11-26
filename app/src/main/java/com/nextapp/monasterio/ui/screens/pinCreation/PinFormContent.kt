package com.nextapp.monasterio.ui.screens.pinCreation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import com.nextapp.monasterio.ui.screens.pinCreation.state.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PinFormContent(
    padding: PaddingValues,
    tituloState: TituloState,
    descripcionState: DescripcionState,
    imagenesState: ImagenesState,
    imagenes360State: ImagenesState,
    ubicacionState: UbicacionState
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {


        PinTitleFields(state = tituloState)

        PinImageSelector(
            label = "Imágenes del Pin",
            state = imagenesState,
            mandatory = true
        )

        PinDescriptionFields(state = descripcionState)

        PinLocationField(state = ubicacionState)

        PinImageSelector(
            label = "Imágenes 360",
            state = imagenes360State,
            mandatory = false
        )
    }
}
