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
import androidx.compose.ui.platform.LocalContext // ⬅️ IMPORTANTE: Añadir este import
import com.nextapp.monasterio.ui.screens.pinCreation.components.getAreaPrincipalForLocation


@Composable
fun CreacionPinesScreen(
    navController: NavController
) {

    val context = LocalContext.current
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

    val isFormValid =
        descripcionState.es.isNotBlank() &&
                imagenesState.images.isNotEmpty() &&
                imagenesState.allImagesTagged &&
                vm.ubicacion_es.isNotBlank() && // Campo complejo (antes pinTitle)
                vm.area_es.isNotBlank() // Campo simple (antes pinUbicacion)


    Scaffold(
        topBar = {
            Log.d("FLUJO_PIN_UI", "UI: pinUbicacion='${vm.area_es}'")
            val isSaveEnabled = if (isEditing) {
                Log.d("FLUJO_PIN_UI", "UI: pinUbicacion='${vm.area_es}'")
                val enabled = isFormValid && isModified
                Log.d("FLUJO_PIN_UI", "UI: isFormValid=$isFormValid, isModified=$isModified, isSaveEnabled=$enabled, ubicacion='${vm.ubicacion_es}', descripcion_es_len=${vm.descripcion.es.length}, imagenes_count=${vm.imagenes.images.size}, allTagged=${vm.imagenes.allImagesTagged}")
                enabled
            } else {
                isFormValid.also { Log.d("FLUJO_PIN_UI", "UI: CREACIÓN isFormValid=$it") }
            }

            PinTopBar(
                enabled = isSaveEnabled,
                isEditing = isEditing,
                onSave = {
                    if (isEditing) {
                        Log.d("FLUJO_PIN", "CreacionPinesScreen: Guardar en modo EDICIÓN.")
                        vm.onSaveClicked(context)
                        navController.popBackStack()
                    } else {
                        Log.d("FLUJO_PIN", "CreacionPinesScreen: Botón CREAR pulsado. Llamando a vm.onCreateClicked().")
                        vm.onCreateClicked(context) {
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
                currentTitle = vm.ubicacion_es,
                currentUbicacion = vm.area_es,

                onTitleChange = { newUbicacion ->
                    vm.updateUbicacionConAutoTraduccion(newUbicacion, ::getAreaPrincipalForLocation)
                },
                // El cambio manual de área sigue usando el setter normal
                onUbicacionChange = { newArea -> vm.area_es = newArea },

                // Pasamos el estado de las traducciones manuales y el callback
                titleManualTrads = vm.pinTitleManualTrads,
                onTitleManualTradsUpdate = { en, de, fr -> vm.updateTitleManualTrads(en, de, fr) }
            )

            Spacer(Modifier.height(24.dp))
            PinDescriptionFields(
                state = descripcionState,
                isEditing = isEditing,
                onChanged = {
                    Log.d("FLUJO_PIN_UI", "UI: Descripción onChanged() → descripcion.es='${vm.descripcion.es.take(60)}'")
                    vm.checkIfModified()
                    Log.d("FLUJO_PIN_UI", "UI: tras check: isModified=${vm.isModified}")
                }
            )

            Spacer(Modifier.height(24.dp))
            PinImageSelector(
                label = "Imágenes del Pin",
                state = imagenesState,
                mandatory = true,
                onChanged = {
                    Log.d("FLUJO_PIN_UI", "UI: PinImageSelector.onChanged() llamado. imagenes.size=${vm.imagenes.images.size}, allTagged=${vm.imagenes.allImagesTagged}")
                    vm.checkIfModified()
                    Log.d("FLUJO_PIN_UI", "UI: tras check: isModified=${vm.isModified}")
                }

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

