package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import com.nextapp.monasterio.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinTopBar(
    enabled: Boolean,
    isEditing: Boolean = false,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(text = if (isEditing) "Editar Pin" else "Crear Pin")
        },
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
                Text(text = if (isEditing) "Actualizar" else "Crear")
            }
        }
    )
}



