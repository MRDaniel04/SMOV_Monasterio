package com.nextapp.monasterio.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

// Componente reutilizable para el texte editable
@Composable
fun EditableText(
    text: String,
    isEditing: Boolean,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    if (isEditing) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = modifier,
            singleLine = true,
            label = label?.let { { Text(it) } }
        )
    } else {
        Text(
            text = text.ifEmpty { "—" },
            modifier = modifier,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
