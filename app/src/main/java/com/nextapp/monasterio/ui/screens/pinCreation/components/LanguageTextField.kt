package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun LanguageTextField(
    label: String,
    text: String,
    onChange: (String) -> Unit,
    mandatory: Boolean,
    singleLine: Boolean
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label)

        OutlinedTextField(
            value = text,
            onValueChange = onChange,
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth()
        )

        if (mandatory && text.isBlank()) {
            Text("Campo obligatorio", color = Color.Red)
        }
    }
}
