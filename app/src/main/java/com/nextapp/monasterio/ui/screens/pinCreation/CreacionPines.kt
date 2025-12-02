package com.nextapp.monasterio.ui.screens.pinCreation

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nextapp.monasterio.ui.screens.pinCreation.components.Image360Selector
import com.nextapp.monasterio.ui.screens.pinCreation.components.PinLocationDropdown
import com.nextapp.monasterio.ui.screens.pinCreation.components.PinTopBar


@Composable
fun CreacionPinesScreen(
    navController: NavController
) {

    val parentEntry = remember(navController.currentBackStackEntry) {
        try {
            navController.getBackStackEntry("pins_graph")
        } catch (_: Exception) { null }
    }

    val vm = if (parentEntry != null)
        viewModel<CreacionPinSharedViewModel>(parentEntry)
    else
        viewModel<CreacionPinSharedViewModel>()

    val tituloState = vm.titulo
    val descripcionState = vm.descripcion
    val imagenesState = vm.imagenes
    val ubicacionState = vm.ubicacion
    val imagen360 = vm.imagen360

    val isFormValid = remember(
        tituloState.es,
        descripcionState.es,
        imagenesState.images,
        imagenesState.allImagesTagged

    ) {
        tituloState.es.isNotBlank() &&
                descripcionState.es.isNotBlank() &&
                imagenesState.images.isNotEmpty() &&
                imagenesState.allImagesTagged
    }

    Scaffold(
        topBar = {
            PinTopBar(
                enabled = isFormValid,
                isEditing = vm.isEditing,   // ← Añadido
                onSave = {
                    if (vm.isEditing) {
                        vm.updateRequested = true
                        navController.popBackStack()
                    } else {
                        vm.formSubmitted = true
                        navController.popBackStack()
                    }

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

            Spacer(Modifier.height(12.dp))
            PinTitleFields(state = tituloState, isEditing = vm.isEditing)
            Spacer(Modifier.height(24.dp))
            PinDescriptionFields(state = descripcionState, isEditing = vm.isEditing)

            Spacer(Modifier.height(24.dp))

            PinImageSelector(
                label = "Imágenes del Pin",
                state = imagenesState,
                mandatory = true
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Ubicación del Pin", // Título unificado
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            PinLocationDropdown(state = ubicacionState) // Ya incluye la lógica condicional

            Spacer(Modifier.height(24.dp))


            Image360Selector(
                label = "Imagen 360 (opcional)",
                uri = imagen360,
                onPick = { uri -> vm.imagen360 = uri },
                onRemove = { vm.imagen360 = null }
            )
        }
    }
}

