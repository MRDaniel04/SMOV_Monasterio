package com.nextapp.monasterio.ui.screens.pinCreation

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nextapp.monasterio.ui.screens.pinCreation.components.Image360Selector
import com.nextapp.monasterio.ui.screens.pinCreation.components.PinLocationDropdown
import com.nextapp.monasterio.ui.screens.pinCreation.components.PinTopBar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.nextapp.monasterio.ui.screens.pinCreation.components.getAreaPrincipalForLocation
import com.nextapp.monasterio.R


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
                vm.ubicacion_es.isNotBlank() &&
                vm.area_es.isNotBlank()
    var showExitDialog by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            val isSaveEnabled = if (isEditing) {
                val enabled = isFormValid && isModified
                enabled
            } else {
                isFormValid.also { Log.d("FLUJO_PIN_UI", "UI: CREACIÓN isFormValid=$it") }
            }

            PinTopBar(
                enabled = isSaveEnabled,
                isEditing = isEditing,
                onSave = {
                    if (isEditing) {
                        vm.onSaveClicked(context)
                        navController.popBackStack()
                    } else {
                        vm.onCreateClicked(context) {
                             navController.popBackStack()
                        }
                    }
                },
                onBack = {
                    if (isEditing) {

                        if (vm.isModified) {
                            showExitDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    } else {
                        // CREAR → mostrar diálogo si rellenó algún campo
                        val hasData =
                            vm.ubicacion_es.isNotBlank() ||
                                    vm.area_es.isNotBlank() ||
                                    vm.descripcion.es.isNotBlank() ||
                                    vm.imagenes.images.isNotEmpty() ||
                                    vm.imagen360 != null

                        if (hasData) {
                            showExitDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }
                }

            )

        }
    ) { paddingValues ->
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = {
                    Text(
                        stringResource(
                            id = if (isEditing)
                                R.string.alert_edit_title
                            else
                                R.string.alert_create_title
                        )
                    )
                },
                text = {
                    Text(
                        stringResource(
                            id = if (isEditing)
                                R.string.alert_edit_message
                            else
                                R.string.alert_create_message
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitDialog = false
                            navController.popBackStack()
                        }
                    ) {
                        Text(stringResource(R.string.alert_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text(stringResource(R.string.alert_cancel))
                    }

                }
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.pin_location_title),
                style = MaterialTheme.typography.titleMedium
            )


            Spacer(Modifier.height(8.dp))

            PinLocationDropdown(
                currentTitle = vm.ubicacion_es,
                currentUbicacion = vm.area_es,

                onTitleChange = { newUbicacion ->
                    vm.updateUbicacionConAutoTraduccion(newUbicacion, ::getAreaPrincipalForLocation)
                },

                onUbicacionChange = { newArea -> vm.area_es = newArea },
                titleManualTrads = vm.pinTitleManualTrads,
                onTitleManualTradsUpdate = { en, de, fr -> vm.updateTitleManualTrads(en, de, fr) }
            )

            Spacer(Modifier.height(24.dp))
            PinDescriptionFields(
                state = descripcionState,
                isEditing = isEditing,
                onChanged = {
                    vm.checkIfModified()
                }
            )

            Spacer(Modifier.height(24.dp))
            PinImageSelector(
                label = stringResource(R.string.pin_images_label),
                state = imagenesState,
                mandatory = true,
                onChanged = {
                    vm.checkIfModified()
                }

            )

            Spacer(Modifier.height(24.dp))

            Image360Selector(
                label = stringResource(R.string.pin_360_label),
                uri = imagen360,
                onPick = { uri -> vm.imagen360 = uri },
                onRemove = { vm.imagen360 = null }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

