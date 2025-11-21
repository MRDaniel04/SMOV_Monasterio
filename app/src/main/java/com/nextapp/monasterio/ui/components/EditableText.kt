package com.nextapp.monasterio.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * Componente reutilizable para texto editable
 * Muestra texto normal cuando no está en modo edición y un campo editable cuando sí lo está
 * 
 * @param text Texto actual
 * @param isEditing Si está en modo edición
 * @param onTextChange Callback cuando cambia el texto
 * @param modifier Modificador de Compose
 * @param label Etiqueta del campo (opcional)
 * @param singleLine Si el campo debe ser de una sola línea (por defecto true)
 * @param readOnlyStyle Estilo del texto cuando no está en edición
 */
@Composable
fun EditableText(
    text: String,
    isEditing: Boolean,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = true,
    readOnlyStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    if (isEditing) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = modifier.fillMaxWidth(),
            singleLine = singleLine,
            label = label?.let { { Text(it) } },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    } else {
        // Mostrar texto normal con estilo cuando no está editando
        Text(
            text = if (text.isEmpty()) "—" else text,
            modifier = modifier,
            style = readOnlyStyle,
            fontWeight = if (text.isEmpty()) FontWeight.Light else FontWeight.Normal,
            color = if (text.isEmpty()) 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
            else 
                MaterialTheme.colorScheme.onSurface
        )
    }
}
