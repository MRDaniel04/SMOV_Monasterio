package com.nextapp.monasterio.ui.screens.pinCreation

import android.util.Log
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nextapp.monasterio.ui.screens.pinCreation.components.PinTopBar
import com.nextapp.monasterio.ui.screens.pinCreation.state.*


@Composable
fun CreacionPinesScreen(
    navController: NavController
) {

    // 1. Buscamos la entrada padre de forma segura. Si falla, es null.
    val parentEntry = remember(navController.currentBackStackEntry) {
        try {
            // Intentamos obtener la entrada del grafo padre ("pins_graph")
            navController.getBackStackEntry("pins_graph")
        } catch (e: Exception) {
            // Logeamos el error esperado, pero el código continúa con null
            null
        }
    }

    // 2. Obtenemos el ViewModel. Si parentEntry es NO nulo, lo usamos como scope.
    // Si es nulo (por la excepción), se usará el scope local del Composable actual.
    val vm = if (parentEntry != null)
        viewModel<CreacionPinSharedViewModel>(viewModelStoreOwner = parentEntry)
    else
        viewModel<CreacionPinSharedViewModel>()
    // -------------------------
    // ESTADOS
    // -------------------------
    val tituloState = vm.titulo
    val descripcionState = vm.descripcion
    val imagenesState = vm.imagenes
    val imagenes360State = vm.imagenes360
    val ubicacionState = vm.ubicacion


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
                    vm.formSubmitted = true
                    navController.popBackStack()

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
