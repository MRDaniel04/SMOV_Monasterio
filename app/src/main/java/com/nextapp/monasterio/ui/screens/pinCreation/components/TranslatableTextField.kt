// TranslatableTextField.kt (MODIFICADO)
package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.screens.pinCreation.state.TranslationFieldState

@Composable
fun TranslatableTextField(
    label: String,
    state: TranslationFieldState,
    singleLine: Boolean,
    isEditing: Boolean = false,
    onChanged: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {

        Text(text = "$label (ES)", style = MaterialTheme.typography.titleSmall)

        OutlinedTextField(
            value = state.es.value,   // ← ← ← CORREGIDO
            onValueChange = {
                state.updateEs(it)    // ← ← ← CORREGIDO
                onChanged()
            },
            isError = state.es.value.isBlank(),
            label = { Text("Introduce el texto en español", fontSize = 10.sp) },
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            minLines = if (!singleLine) 3 else 1
        )

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) { /* … */ }

        if (expanded) {
            Surface {
                Column(Modifier.padding(16.dp)) {

                    OutlinedTextField(
                        value = state.en.value,     // ← ← ← CORREGIDO
                        onValueChange = {
                            state.updateEn(it)      // ← ← ← CORREGIDO
                            onChanged()
                        },
                        label = { Text("Texto opcional en inglés") }
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.de.value,
                        onValueChange = {
                            state.updateDe(it)
                            onChanged()
                        },
                        label = { Text("Texto opcional en alemán") }
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.fr.value,
                        onValueChange = {
                            state.updateFr(it)
                            onChanged()
                        },
                        label = { Text("Texto opcional en francés") }
                    )
                }
            }
        }
    }
}
