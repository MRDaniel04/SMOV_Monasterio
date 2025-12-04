
package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.models.UbicacionDetalladaTag

// Lista de opciones se mantiene

const val OTRA_UBICACION_DETALLADA = "Otra" // ✅ AÑADIDO: Definimos "Otra" aquí

val ubicacionDetalladaOptions = UbicacionDetalladaTag.entries.map { it.displayName }

fun getAreaPrincipalForLocation(location: String): String? {
    return when (location) {
        UbicacionDetalladaTag.CRUCERO.displayName, UbicacionDetalladaTag.LADO_EPISTOLA.displayName -> "Iglesia"
        UbicacionDetalladaTag.TRASCORO.displayName, UbicacionDetalladaTag.CORO.displayName, UbicacionDetalladaTag.CAPILLA_NACIMIENTO.displayName -> "Monasterio"
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinLocationDropdown(

    currentTitle: String, // Valor actual del Título (para mostrar)
    currentUbicacion: String, // Valor actual de la Ubicación (para mostrar)
    onTitleChange: (String) -> Unit, // Callback para actualizar TÍTULO en el ViewModel
    onUbicacionChange: (String) -> Unit, // Callback para actualizar UBICACIÓN en el ViewModel
    label: String = "Ubicación Detallada (Título del Pin)" // Nombre actualizado

) {
    var expanded by remember { mutableStateOf(false) }

    var selectedDropdownLocation by remember {
        mutableStateOf(
            if (currentTitle.isNotBlank() && ubicacionDetalladaOptions.contains(currentTitle))
                currentTitle
            else
                ubicacionDetalladaOptions.firstOrNull() ?: ""
        )
    }

    var manualTitleText by remember { mutableStateOf(if (selectedDropdownLocation == OTRA_UBICACION_DETALLADA) currentTitle else "") }

    LaunchedEffect(currentTitle, currentUbicacion) {

        // --- SINCRONIZACIÓN AL EDITAR ---
        if (currentTitle.isNotBlank()) {
            if (ubicacionDetalladaOptions.contains(currentTitle)) {
                selectedDropdownLocation = currentTitle
                manualTitleText = ""
            } else {
                selectedDropdownLocation = OTRA_UBICACION_DETALLADA
                manualTitleText = currentTitle
            }

            onUbicacionChange(currentUbicacion)
        }

        // --- INICIALIZACIÓN CUANDO SE CARGA "nuevo" pin ---
        if (currentTitle.isBlank() && currentUbicacion.isBlank() && selectedDropdownLocation.isNotBlank()) {
            onTitleChange(selectedDropdownLocation)
            val areaPrincipal = getAreaPrincipalForLocation(selectedDropdownLocation)
            onUbicacionChange(areaPrincipal ?: "")
        }
    }

    val isManualEntry = selectedDropdownLocation == OTRA_UBICACION_DETALLADA

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(

                value = selectedDropdownLocation,
                onValueChange = { /* Solo cambia a través del DropdownMenuItem */ },
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                ubicacionDetalladaOptions.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location) },
                        onClick = {
                            selectedDropdownLocation = location // ✅ MODIFICADO: Actualizamos el estado local del Dropdown
                            expanded = false

                            if (location != OTRA_UBICACION_DETALLADA) {
                                onTitleChange(location)
                                manualTitleText = ""

                                // ✅ AÑADIR ESTAS DOS LÍNEAS PARA ASIGNAR EL ÁREA PRINCIPAL AUTOMÁTICAMENTE
                                val areaPrincipal = getAreaPrincipalForLocation(location)
                                onUbicacionChange(areaPrincipal ?: "") // <-- Llama al callback para actualizar vm.pinUbicacio
                            } else {

                                onTitleChange(manualTitleText)

                            }
                        }
                    )
                }
            }
        }


        AnimatedVisibility(visible = isManualEntry) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Escriba aquí la Ubicación Detallada (Título del Pin):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = manualTitleText, // ✅ MODIFICADO: Usa el campo Manual LOCAL
                    onValueChange = { newValue ->
                        manualTitleText = newValue // Actualiza el campo Manual LOCAL
                        onTitleChange(newValue) // ✅ CLAVE: El valor escrito se guarda en la variable 'titulo' del ViewModel
                    },
                    label = { Text("Ubicación Detallada (Manual)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Seleccione el Área Principal (Ubicación del Pin):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                // ✅ MODIFICADO: Pasamos el valor de ubicación directamente al selector
                AreaPrincipalSelector(
                    selectedArea = currentUbicacion,
                    onAreaSelected = onUbicacionChange // ✅ CLAVE: El valor seleccionado se guarda en la variable 'ubicacion' del ViewModel
                )
            }
        }
    }
}


@Composable
fun AreaPrincipalSelector(
    selectedArea: String,
    onAreaSelected: (String) -> Unit
) {

    val areas = listOf("Iglesia", "Monasterio")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        areas.forEach { area ->
            val isSelected = selectedArea == area
            AssistChip(
                onClick = { onAreaSelected(area) },
                label = { Text(area) },
                colors = if (isSelected) AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onPrimary
                ) else AssistChipDefaults.assistChipColors()
            )
        }
    }
}