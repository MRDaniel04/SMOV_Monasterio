package com.nextapp.monasterio.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.R

@Composable
fun EditableContent(
    isEditing: Boolean,
    hasChanges: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Contenido editable
        content()

        // Botones de acción cuando está en modo edición y hay cambios
        if (isEditing && hasChanges) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Cancelar
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(stringResource(id = R.string.edit_cancel_changes))
                }

                // Botón Guardar
                Button(
                    onClick = { showConfirmDialog = true }
                ) {
                    Text(stringResource(id = R.string.edit_save_changes))
                }
            }
        }
    }

    // Diálogo de confirmación
    ConfirmSaveDialog(
        showDialog = showConfirmDialog,
        onDismiss = { showConfirmDialog = false },
        onConfirm = onSave
    )
}