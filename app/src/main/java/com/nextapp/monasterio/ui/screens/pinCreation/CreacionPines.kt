package com.nextapp.monasterio.ui.screens.pinCreation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextapp.monasterio.ui.screens.pinCreation.components.PinTopBar
import com.nextapp.monasterio.ui.screens.pinCreation.state.*


@Composable
fun CreacionPinesScreen(
    navController: NavController
) {
    // -------------------------
    // ESTADOS
    // -------------------------
    val tituloState = remember { TituloState() }
    val descripcionState = remember { DescripcionState() }
    val imagenesState = remember { ImagenesState() }
    val imagenes360State = remember { ImagenesState() }
    val ubicacionState = remember { UbicacionState() }

    val isFormValid = remember(
        tituloState.es,
        descripcionState.es,
        imagenesState.uris
    ) {
        tituloState.es.isNotBlank() &&
                descripcionState.es.isNotBlank() &&
                imagenesState.uris.isNotEmpty()
    }


    Scaffold(
        topBar = {
            PinTopBar(
                enabled = isFormValid,
                onSave = {
                    // TODO guardar pin
                },
                onBack = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            PinTitleFields(state = tituloState)

            Spacer(Modifier.height(24.dp))   // ⬅ SEPARACIÓN ENTRE BLOQUES

            PinImageSelector(
                label = "Imágenes del Pin",
                state = imagenesState,
                mandatory = true
            )

            Spacer(Modifier.height(24.dp))

            PinDescriptionFields(state = descripcionState)

            Spacer(Modifier.height(24.dp))

            PinLocationField(state = ubicacionState)

            Spacer(Modifier.height(24.dp))

            PinImageSelector(
                label = "Imágenes 360",
                state = imagenes360State,
                mandatory = false
            )
        }

    }
}
