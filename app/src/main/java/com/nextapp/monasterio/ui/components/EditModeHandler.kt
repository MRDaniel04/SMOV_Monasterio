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

    val currentHasChanges by rememberUpdatedState(hasChanges)
    val currentIsDiscarding by rememberUpdatedState(isDiscarding)
    val currentOnDiscard by rememberUpdatedState(onDiscard)

    // Detectar transición de editing -> not editing de forma síncrona para retorno inmediato
    val justStoppedEditing = previousIsEditing.value && !isEditing
    val shouldPromptSave = justStoppedEditing && currentHasChanges && !currentIsDiscarding
    val shouldDiscard = justStoppedEditing && !currentHasChanges && !currentIsDiscarding // Or if discarding explicitly? No, if discarding, value is ignored in parent usually, but here we handle it.

    // Persistir el estado del diálogo
    if (shouldPromptSave) {
        SideEffect { showSaveDialog = true }
    }
    
    // Manejar descarte automático
    if (shouldDiscard || (justStoppedEditing && currentIsDiscarding)) {
        SideEffect { currentOnDiscard() }
    }

    // Actualizar estado previo
    LaunchedEffect(isEditing) {
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
    
    return showSaveDialog || shouldPromptSave
}
