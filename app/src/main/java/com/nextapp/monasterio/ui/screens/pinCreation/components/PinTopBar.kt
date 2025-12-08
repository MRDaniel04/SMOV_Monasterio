package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
            Text(
            text = stringResource(
                if (isEditing) R.string.edit_pin else R.string.create_pin
            ))
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.go_back)
                )
            }
        },
        actions = {
            Button(onClick = onSave, enabled = enabled) {
                Text(
                    text = stringResource(
                        if (isEditing) R.string.pin_update else R.string.pin_create
                    )
                )
            }
        }
    )
}



