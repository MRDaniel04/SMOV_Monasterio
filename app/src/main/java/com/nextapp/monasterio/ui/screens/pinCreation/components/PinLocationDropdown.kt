
package com.nextapp.monasterio.ui.screens.pinCreation.components

import android.R.attr.label
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.models.UbicacionDetalladaTag
import com.nextapp.monasterio.ui.screens.pinCreation.CreacionPinSharedViewModel
import com.nextapp.monasterio.ui.screens.pinCreation.PinTitleManualTrads

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

    currentTitle: String,
    currentUbicacion: String,
    onTitleChange: (String) -> Unit,
    onUbicacionChange: (String) -> Unit,

    titleManualTrads: PinTitleManualTrads,
    onTitleManualTradsUpdate: (en: String, de: String, fr: String) -> Unit

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
                label = { Text("Ubicación Detallada (ES) o Fija") },
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
                            selectedDropdownLocation = location
                            expanded = false

                            if (location != OTRA_UBICACION_DETALLADA) {
                                onTitleChange(location)
                                manualTitleText = ""
                                // Limpiamos las traducciones manuales si se elige una opción fija
                                onTitleManualTradsUpdate("", "", "")

                                val areaPrincipal = getAreaPrincipalForLocation(location)
                                onUbicacionChange(areaPrincipal ?: "")
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

                // 📌 CAMPO 1: UBICACIÓN/TÍTULO EN ESPAÑOL (ES)
                Text(
                    text = "Escriba aquí la Ubicación Detallada (Título del Pin) - ESPAÑOL:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = manualTitleText,
                    onValueChange = { newValue ->
                        manualTitleText = newValue
                        onTitleChange(newValue) // Actualiza ubicacion_es en el ViewModel
                    },
                    label = { Text("Título (ES)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 📌 CAMPO 2: UBICACIÓN/TÍTULO EN INGLÉS (EN)
                OutlinedTextField(
                    value = titleManualTrads.en,
                    onValueChange = { newValue ->
                        onTitleManualTradsUpdate(newValue, titleManualTrads.de, titleManualTrads.fr)
                    },
                    label = { Text("Título (EN)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 📌 CAMPO 3: UBICACIÓN/TÍTULO EN ALEMÁN (DE)
                OutlinedTextField(
                    value = titleManualTrads.de,
                    onValueChange = { newValue ->
                        onTitleManualTradsUpdate(titleManualTrads.en, newValue, titleManualTrads.fr)
                    },
                    label = { Text("Título (DE)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 📌 CAMPO 4: UBICACIÓN/TÍTULO EN FRANCÉS (FR)
                OutlinedTextField(
                    value = titleManualTrads.fr,
                    onValueChange = { newValue ->
                        onTitleManualTradsUpdate(titleManualTrads.en, titleManualTrads.de, newValue)
                    },
                    label = { Text("Título (FR)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 📌 SELECTOR DE ÁREA PRINCIPAL
                Text(
                    text = "Seleccione el Área Principal (Ubicación del Pin):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                AreaPrincipalSelector(
                    selectedArea = currentUbicacion,
                    onAreaSelected = onUbicacionChange
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