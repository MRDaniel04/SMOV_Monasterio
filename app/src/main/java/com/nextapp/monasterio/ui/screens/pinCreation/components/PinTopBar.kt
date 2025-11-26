package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import com.nextapp.monasterio.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinTopBar(
    enabled: Boolean,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    TopAppBar(
        title = { Text("Crear Pin") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Volver"
                )
            }
                         },
        actions = {
            Button(onClick = onSave, enabled = enabled) {
                Text("Crear")
            }
        }
    )
}
