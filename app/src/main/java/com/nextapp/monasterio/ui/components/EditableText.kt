package com.nextapp.monasterio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.nextapp.monasterio.R
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun EditableText(
    text: String,
    isEditing: Boolean,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = true,
    readOnlyStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    editTextColor: Color = Color.Unspecified
) {
    if (isEditing) {
        val colors = if (editTextColor != Color.Unspecified) {
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = editTextColor,
                unfocusedTextColor = editTextColor,
                cursorColor = editTextColor,
                focusedLabelColor = editTextColor,
                unfocusedLabelColor = editTextColor
            )
        } else {
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        }

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = modifier.fillMaxWidth(),
            singleLine = singleLine,
            label = label?.let { { Text(it) } },
            colors = colors
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

// EditableText que permite editar texto en varios idiomas
@Composable
fun EditableText(
    textMap: Map<String, String>,
    isEditing: Boolean,
    onTextMapChange: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    singleLine: Boolean = false,
    readOnlyStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    editTextColor: Color = Color.Unspecified
) {
    val currentSystemLanguage = java.util.Locale.getDefault().language
    // Mostrar el idioma del sistema, o español por defecto
    val displayLanguage = if (textMap.containsKey(currentSystemLanguage)) currentSystemLanguage else "es"
    val displayText = textMap[displayLanguage] ?: textMap["es"] ?: ""

    if (isEditing) {
        var editingLanguage by remember { mutableStateOf("es") } // Por defecto editar en español
        val currentEditingText = textMap[editingLanguage] ?: ""

        val colors = if (editTextColor != Color.Unspecified) {
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = editTextColor,
                unfocusedTextColor = editTextColor,
                cursorColor = editTextColor,
                focusedLabelColor = editTextColor,
                unfocusedLabelColor = editTextColor
            )
        } else {
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        }

        Column(modifier = modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = currentEditingText,
                onValueChange = { newText ->
                    val newMap = textMap.toMutableMap()
                    newMap[editingLanguage] = newText
                    onTextMapChange(newMap)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                label = label?.let { { Text("$it (${editingLanguage.uppercase()})") } },
                colors = colors
            )
            
            // Selector de idioma debajo del campo de texto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = androidx.compose.ui.Alignment.CenterEnd
            ) {
                LanguageFlagSelector(
                    currentLanguage = editingLanguage,
                    onLanguageSelected = { editingLanguage = it }
                )
            }
        }
    } else {
        Text(
            text = if (displayText.isEmpty()) "—" else displayText,
            modifier = modifier,
            style = readOnlyStyle,
            fontWeight = if (displayText.isEmpty()) FontWeight.Light else FontWeight.Normal,
            color = if (displayText.isEmpty()) 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
            else if (readOnlyStyle.color != Color.Unspecified)
                readOnlyStyle.color
            else 
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LanguageFlagSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val flagRes = when (currentLanguage) {
        "de" -> com.nextapp.monasterio.R.drawable.alemania
        "en" -> com.nextapp.monasterio.R.drawable.reinounido
        else -> com.nextapp.monasterio.R.drawable.espanya
    }

    Box {
        IconButton(onClick = { expanded = true }) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = flagRes),
                contentDescription = "Select Language",
                modifier = Modifier
                    .size(24.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Español") },
                leadingIcon = {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.nextapp.monasterio.R.drawable.espanya),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    )
                },
                onClick = {
                    onLanguageSelected("es")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("English") },
                leadingIcon = {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.nextapp.monasterio.R.drawable.reinounido),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    )
                },
                onClick = {
                    onLanguageSelected("en")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Deutsch") },
                leadingIcon = {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.nextapp.monasterio.R.drawable.alemania),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    )
                },
                onClick = {
                    onLanguageSelected("de")
                    expanded = false
                }
            )
        }
    }
}
