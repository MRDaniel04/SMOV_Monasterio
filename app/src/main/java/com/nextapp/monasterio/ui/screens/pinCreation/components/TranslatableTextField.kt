package com.nextapp.monasterio.ui.screens.pinCreation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

        Text(
            text = "$label (ES)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold // Añadido para hacer el título más claro
        )

        // 1. CAMPO ESPAÑOL (MANDATORIO)
        OutlinedTextField(
            value = state.es,
            onValueChange = { newValue ->
                state.updateEs(newValue)
                onChanged()
            },
            isError = false,
            label = { Text(stringResource(R.string.hint_es_required), fontSize = 10.sp) },
            singleLine = singleLine,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            minLines = if (!singleLine) 3 else 1
        )

        if (state.es.isBlank()) {
            Text(
                text = stringResource(R.string.error_required_field),
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,               // ⬅ más pequeño
                modifier = Modifier.padding(top = 2.dp) // ⬅ más pegado al campo
            )
        }


        Spacer(Modifier.height(12.dp))

        // 2. BOTÓN DE TRADUCCIÓN (Para expandir/colapsar)
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground), // ⬅ borde negro
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            Icon(
                painter = painterResource(id = if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                contentDescription = stringResource(
                    if (expanded) R.string.translations_hide_cd
                    else R.string.translations_show_cd
                ),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onBackground // ⬅ icono negro
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (expanded) R.string.translations_hide
                    else R.string.translations_show
                ),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground // ⬅ texto negro
            )
        }

        // 3. CAMPOS DE TRADUCCIÓN (COLLAPSIBLE)
        if (expanded) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {

                // Campo Inglés
                OutlinedTextField(
                    value = state.en,
                    onValueChange = { newValue ->
                        state.updateEn(newValue)
                        onChanged()
                    },
                    label = { Text(stringResource(R.string.hint_en_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Campo Alemán
                OutlinedTextField(
                    value = state.de,
                    onValueChange = { newValue ->
                        state.updateDe(newValue)
                        onChanged()
                    },
                    label = { Text(stringResource(R.string.hint_de_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Campo Francés
                OutlinedTextField(
                    value = state.fr,
                    onValueChange = { newValue ->
                        state.updateFr(newValue)
                        onChanged()
                    },
                    label = { Text(stringResource(R.string.hint_fr_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}