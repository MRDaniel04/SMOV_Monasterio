package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.ui.screens.pinCreation.state.UbicacionState

// Lista de opciones (la misma)
val locations = listOf(
    "Crucero",
    "Lado de la epistola",
    "Trascoro",
    "Coro",
    "Capilla del nacimiento",
    "(Otra)"
)

// Define la lógica de mapeo (la movemos aquí para que esté disponible)
fun getAreaPrincipalForLocation(location: String): String? {
    return when (location) {
        "Crucero", "Lado de la epistola" -> "Iglesia"
        "Trascoro", "Coro", "Capilla del nacimiento" -> "Monasterio"
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinLocationDropdown(
    state: UbicacionState,
    label: String = "Ubicación Detallada" // Nombre actualizado
) {
    var expanded by remember { mutableStateOf(false) }


    val initialText = state.ubicacionDetallada.ifBlank { "" }

    val isInitialOther = initialText.isNotBlank() && !locations.contains(initialText) && getAreaPrincipalForLocation(initialText) == null

    var selectedLocationText by remember {
        mutableStateOf(
            when {
                isInitialOther -> "(Otra)"
                locations.contains(initialText) -> initialText
                else -> "" // Si está vacío o es un valor no reconocido al inicio
            }
        )
    }

    // Bandera para mostrar el campo de texto y el selector de Área Principal
    val showManualInput = selectedLocationText == "(Otra)" || isInitialOther

    Column(modifier = Modifier.fillMaxWidth()) {

        // --- 1. Dropdown para la selección ---
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = if (showManualInput && state.ubicacionDetallada.isNotBlank()) {
                    // Mostrar el valor manual si existe
                    state.ubicacionDetallada
                } else {
                    // Mostrar la opción seleccionada (fija o "(Otra)")
                    selectedLocationText
                },
                onValueChange = { /* Solo selección */ },
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                locations.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedLocationText = selectionOption

                            if (selectionOption != "(Otra)") {
                                // 1. ASIGNAR UBICACIÓN DETALLADA
                                state.ubicacionDetallada = selectionOption

                                // 2. ASIGNACIÓN AUTOMÁTICA DEL ÁREA PRINCIPAL
                                val area = getAreaPrincipalForLocation(selectionOption)
                                if (area != null) {
                                    state.areaPrincipal = area // Asignación automática
                                }
                            } else {
                                // Si es "(Otra)", limpiamos la detallada y dejamos la principal vacía para que la elija
                                state.ubicacionDetallada = ""
                                state.areaPrincipal = ""
                            }

                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        // --- 2. Campo de texto CONDICIONAL para "(Otra)" ---
        AnimatedVisibility(visible = showManualInput) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                // --- Campo de texto manual ---
                Text(
                    text = "Escriba aquí la Ubicación Detallada:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.ubicacionDetallada,
                    onValueChange = { newValue -> state.ubicacionDetallada = newValue },
                    label = { Text("Ubicación Detallada (Manual)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Selector de Área Principal (Botones) ---
                Text(
                    text = "Seleccione el Área Principal:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                AreaPrincipalSelector(
                    selectedArea = state.areaPrincipal,
                    onAreaSelected = { newArea -> state.areaPrincipal = newArea }
                )
            }
        }
    }
}