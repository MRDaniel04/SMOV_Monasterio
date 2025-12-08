
package com.nextapp.monasterio.ui.screens.pinCreation.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.models.UbicacionDetalladaTag
import com.nextapp.monasterio.ui.screens.pinCreation.PinTitleManualTrads
import com.nextapp.monasterio.R

// Lista de opciones se mantiene

const val OTRA_UBICACION_DETALLADA = "Otra"

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

    var showManualTrads by remember {
        // Inicializar a true si ya hay alguna traducción para el modo edición
        mutableStateOf(titleManualTrads.en.isNotBlank() || titleManualTrads.de.isNotBlank() || titleManualTrads.fr.isNotBlank())
    }

    val showManualTitleFields = currentUbicacion == OTRA_UBICACION_DETALLADA

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
    val selectedLocationText = remember(selectedDropdownLocation) {
        UbicacionDetalladaTag.fromDisplayName(selectedDropdownLocation)?.let { tag ->
            // Si encontramos el tag, usamos el stringResource. Si no (por ejemplo, al inicializar con una cadena vacía), devolvemos el valor en español.
            tag.stringResId
        }
    }?.let { stringResource(it) } ?: selectedDropdownLocation

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLocationText,
                onValueChange = { /* Solo cambia a través del DropdownMenuItem */ },
                readOnly = true,
                label = { Text(stringResource(R.string.dropdown_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                UbicacionDetalladaTag.entries.forEach { tag ->
                    DropdownMenuItem(
                        // 🚀 CAMBIO CLAVE: Usamos el stringResource para mostrar la traducción
                        text = { Text(stringResource(tag.stringResId)) },
                        onClick = {
                            val location = tag.displayName // El valor en español

                            selectedDropdownLocation = location
                            expanded = false

                            if (tag != UbicacionDetalladaTag.OTRA) {
                                // Devolvemos el valor en español (location)
                                onTitleChange(location)
                                manualTitleText = ""
                                onTitleManualTradsUpdate("", "", "")
                                val areaPrincipal = getAreaPrincipalForLocation(location)
                                onUbicacionChange(areaPrincipal ?: "")
                            } else {
                                // Para OTRA, el manualTitleText ya estará en español
                                onTitleChange(manualTitleText)
                                onUbicacionChange("")
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
                    text = stringResource(R.string.manual_title_helper),
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
                    label = { Text(stringResource(R.string.manual_title_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showManualTrads = !showManualTrads },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground), // Aseguramos el borde
                    shape = RoundedCornerShape(8.dp), // Forma consistente
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (showManualTrads) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down), // Mantenemos tus iconos de flecha
                        contentDescription = stringResource(
                            if (showManualTrads)
                                R.string.translations_hide
                            else
                                R.string.translations_show
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (showManualTrads) R.string.translations_hide else R.string.translations_show
                        ),
                        fontWeight = FontWeight.Medium
                    )
                }

                if (showManualTrads) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {

                        // Campo Inglés (EN)
                        OutlinedTextField(
                            value = titleManualTrads.en,
                            onValueChange = { newValue ->
                                onTitleManualTradsUpdate(newValue, titleManualTrads.de, titleManualTrads.fr)
                            },
                            label = { Text(stringResource(R.string.title_en)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Campo Alemán (DE)
                        OutlinedTextField(
                            value = titleManualTrads.de,
                            onValueChange = { newValue ->
                                onTitleManualTradsUpdate(titleManualTrads.en, newValue, titleManualTrads.fr)
                            },
                            label = { Text(stringResource(R.string.title_de)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Campo Francés (FR)
                        OutlinedTextField(
                            value = titleManualTrads.fr,
                            onValueChange = { newValue ->
                                onTitleManualTradsUpdate(titleManualTrads.en, titleManualTrads.de, newValue)
                            },
                            label = { Text(stringResource(R.string.title_fr)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 📌 SELECTOR DE ÁREA PRINCIPAL
                if(currentUbicacion.isBlank()) {
                    Text(
                        text = stringResource(R.string.area_select_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
    val areaMap = mapOf(
        "Iglesia" to R.string.area_church,
        "Monasterio" to R.string.area_monastery
    )

    // Iteramos sobre los valores en español ("Iglesia", "Monasterio")
    val areas = areaMap.keys.toList()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        areas.forEach { areaInternalValue -> // areaInternalValue es "Iglesia" o "Monasterio"
            val isSelected = selectedArea == areaInternalValue
            // Obtenemos el ID del string para el valor interno
            val areaStringId = areaMap[areaInternalValue]

            AssistChip(
                modifier = Modifier
                    .heightIn(min=48.dp),
                onClick = { onAreaSelected(areaInternalValue) },
                label = { Text(areaStringId?.let { stringResource(it) } ?: areaInternalValue) },
                colors = if (isSelected) AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onPrimary
                ) else AssistChipDefaults.assistChipColors()
            )
        }
    }
}