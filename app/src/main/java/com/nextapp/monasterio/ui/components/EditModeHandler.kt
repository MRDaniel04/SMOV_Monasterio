package com.nextapp.monasterio.ui.components

import androidx.compose.runtime.*

// Componente que permite controlar los cambios de una pagina cuando esta se edita
@Composable
fun EditModeHandler(
    isEditing: Boolean,
    hasChanges: Boolean,
    isDiscarding: Boolean = false,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onKeepEditing: () -> Unit = {}
): Boolean {
    var showSaveDialog by remember { mutableStateOf(false) }
    val previousIsEditing = remember { mutableStateOf(isEditing) }

    if (previousIsEditing.value && !isEditing) {
        if (isDiscarding) {
            // Si estamos descartando explícitamente, ignoramos cambios y procedemos
        } else if (hasChanges) {
             showSaveDialog = true
        }
    }

    // Efecto para actualizar el estado previo
    LaunchedEffect(isEditing, isDiscarding) {
        // Si volvemos a editar (cancelaron el diálogo), cerramos el diálogo
        if (isEditing && showSaveDialog) {
            showSaveDialog = false
        }

        if (previousIsEditing.value && !isEditing) {
             if (isDiscarding) {
                 onDiscard()
             } else if (!hasChanges) {
                 onDiscard()
             }
        }
        previousIsEditing.value = isEditing
    }

    // Dialogo de confirmación de cambios
    if (showSaveDialog) {
        ConfirmSaveDialog(
            showDialog = true,
            onDismiss = {
                onKeepEditing()
            },
            onConfirm = {
                showSaveDialog = false
                onSave()
            }
        )
    }
    
    return showSaveDialog
}
