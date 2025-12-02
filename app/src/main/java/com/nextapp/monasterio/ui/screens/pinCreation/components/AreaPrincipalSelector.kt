package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


val areaOptions = listOf("Monasterio", "Iglesia")

@Composable
fun AreaPrincipalSelector(
    selectedArea: String,
    onAreaSelected: (String) -> Unit
) {
    Row(
        // Puedes añadir aquí un Modifier para espaciar los botones
    ) {
        areaOptions.forEach { area ->
            val isSelected = selectedArea == area

            // Usamos un botón 'elevado' para la selección activa y 'outlined' para la inactiva.
            if (isSelected) {
                Button(onClick = { onAreaSelected(area) }) {
                    Text(area)
                }
            } else {
                OutlinedButton(onClick = { onAreaSelected(area) }) {
                    Text(area)
                }
            }
            Spacer(Modifier.width(8.dp)) // Espacio entre botones
        }
    }
}