package com.nextapp.monasterio.ui.components

import androidx.compose.runtime.*

// Componente que permite controlar los cambios de una pagina cuando esta se edita
@Composable
fun EditModeHandler(
    isEditing: Boolean,
    hasChanges: Boolean,
    onSave: () -> Unit,
    onDiscard: () -> Unit
): Boolean {
    var showSaveDialog by remember { mutableStateOf(false) }
    val previousIsEditing = remember { mutableStateOf(isEditing) }

    if (previousIsEditing.value && !isEditing) {
        if (hasChanges) showSaveDialog = true
    }

    // Efecto para actualizar el estado previo
    LaunchedEffect(isEditing) {
        if (previousIsEditing.value && !isEditing && !hasChanges) {
            onDiscard()
        }
        previousIsEditing.value = isEditing
    }

    // Dialogo de confirmación de cambios
    if (showSaveDialog) {
        ConfirmSaveDialog(
            showDialog = true,
            onDismiss = {
                showSaveDialog = false
                onDiscard()
            },
            onConfirm = {
                showSaveDialog = false
                onSave()
            }
        )
    }
    
    return showSaveDialog
}
