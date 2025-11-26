package com.nextapp.monasterio.ui.screens.pinCreation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextapp.monasterio.ui.screens.pinCreation.state.UbicacionState

@Composable
fun PinLocationField(state: UbicacionState) {
    Text(
        text = "Ubicación",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        ),
        modifier = Modifier.padding(bottom = 8.dp)
    )

    OutlinedTextField(
        value = state.displayName,
        onValueChange = { state.displayName = it },
        placeholder = { Text("Introduce la ubicación…", fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )


}

