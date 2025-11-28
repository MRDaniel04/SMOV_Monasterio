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
    isEditing: Boolean = false    // ⭐ SE AÑADE AQUÍ
) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {

        Text(
            text = "$label (ES)",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )

        OutlinedTextField(
            value = state.es,
            onValueChange = { state.es = it },
            isError = state.es.isBlank(),
            label = { Text("Introduce el texto en español", fontSize = 10.sp) },
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            minLines = if (!singleLine) 3 else 1
        )

        if (state.es.isBlank()) {
            Text(
                text = "Campo obligatorio",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // ⭐ BOTÓN EXPANDIBLE modificado según edición/creación
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = if (expanded) 0.dp else 8.dp,
                bottomEnd = if (expanded) 0.dp else 8.dp
            )
        ) {
            val buttonText = when {
                expanded -> "Ocultar traducciones"
                isEditing -> "Modificar traducciones opcionales"
                else -> "Añadir traducciones opcionales"
            }

            Text(
                text = buttonText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.width(8.dp))

            val icon = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
            Icon(painterResource(icon), contentDescription = null)
        }

        if (expanded) {
            Surface(
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        text = "$label (EN) - Opcional",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )

                    OutlinedTextField(
                        value = state.en,
                        onValueChange = { state.en = it },
                        label = { Text("Texto opcional en inglés", fontSize = 10.sp) },
                        singleLine = singleLine,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "$label (DE) - Opcional",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )

                    OutlinedTextField(
                        value = state.de,
                        onValueChange = { state.de = it },
                        label = { Text("Texto opcional en alemán", fontSize = 10.sp) },
                        singleLine = singleLine,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
