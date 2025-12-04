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

    val isEditing = vm.isEditing
    val isModified = vm.isModified

    val descripcionState = vm.descripcion
    val imagenesState = vm.imagenes
    val imagen360 = vm.imagen360

    val isFormValid = remember(

        descripcionState.es,
        imagenesState.images,
        imagenesState.allImagesTagged,
        vm.pinTitle,
        vm.pinUbicacion
    ) {
        descripcionState.es.isNotBlank() &&
        imagenesState.images.isNotEmpty() &&
        imagenesState.allImagesTagged &&
        vm.pinTitle.isNotBlank() &&
        vm.pinUbicacion.isNotBlank()
    }

    Scaffold(
        topBar = {
            val isSaveEnabled = if (isEditing) {
                isFormValid && isModified
            } else {
                isFormValid
            }
            PinTopBar(
                enabled = isSaveEnabled,
                isEditing = isEditing,
                onSave = {
                    if (isEditing) {
                        Log.d("FLUJO_PIN", "CreacionPinesScreen: Guardar en modo EDICIÓN.")
                        vm.onSaveClicked()
                        navController.popBackStack()
                    } else {
                        Log.d("FLUJO_PIN", "CreacionPinesScreen: Botón CREAR pulsado. Llamando a vm.onCreateClicked().")
                        vm.onCreateClicked {
                            navController.popBackStack()
                        }
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
            Text(
                text = "Ubicación del Pin", // Título unificado
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            PinLocationDropdown(
                currentTitle = vm.pinTitle, // Pasamos el valor actual para mostrar
                currentUbicacion = vm.pinUbicacion, // Pasamos el valor actual para mostrar
                onTitleChange = { newTitle -> vm.pinTitle = newTitle }, // Callback: actualiza el TÍTULO
                onUbicacionChange = { newUbicacion -> vm.pinUbicacion = newUbicacion } // Callback: actualiza la UBICACIÓN
            )

            Spacer(Modifier.height(24.dp))
            PinDescriptionFields(
                state = descripcionState,
                isEditing = isEditing, // Usamos la variable local 'isEditing'
            )


            Spacer(Modifier.height(24.dp))
            PinImageSelector(
                label = "Imágenes del Pin",
                state = imagenesState,
                mandatory = true,
                onChanged = { vm.checkIfModified() } // ⬅️ AGREGADO: Notifica al VM si la lista de imágenes cambia.
            )

            Spacer(Modifier.height(24.dp))

            Image360Selector(
                label = "Imagen 360 (opcional)",
                uri = imagen360,
                onPick = { uri -> vm.imagen360 = uri },
                onRemove = { vm.imagen360 = null }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

