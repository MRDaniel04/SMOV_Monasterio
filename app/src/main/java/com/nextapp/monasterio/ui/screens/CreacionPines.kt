package com.nextapp.monasterio.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nextapp.monasterio.R
import androidx.compose.ui.graphics.RectangleShape

// Definición de un color Verde para el botón "Guardar Pin"
val GreenPinSave = Color(0xFF4CAF50) // Verde 500

/**
 * Componente que muestra el campo de texto en Español (Obligatorio)
 * y permite desplegar los campos opcionales para Inglés y Alemán.
 */
@Composable
fun RequiredTranslatableTextField(
    label: String,
    textEs: String,
    onTextChangeEs: (String) -> Unit,
    textEn: String,
    onTextChangeEn: (String) -> Unit,
    textDe: String,
    onTextChangeDe: (String) -> Unit,
    isMandatory: Boolean = true,
    singleLine: Boolean = false
) {
    var showOptionalLangs by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {

        // --- CAMPO OBLIGATORIO (ESPAÑOL) ---
        Text(
            text = "$label (ES)",
            // ⭐ Título del Box: 14.sp
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
        OutlinedTextField(
            value = textEs,
            onValueChange = onTextChangeEs,
            // ⭐ Label del TextField: 10.sp
            label = { Text("Introduce el texto en español", fontSize = 10.sp) },
            isError = isMandatory && textEs.isBlank(),
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            minLines = if (!singleLine) 3 else 1
        )
        if (isMandatory && textEs.isBlank()) {
            Text(
                text = "Campo obligatorio",
                color = MaterialTheme.colorScheme.error,
                // ⭐ Error: 10.sp
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- BOTÓN DE TRADUCCIONES OPCIONALES ---
        OutlinedButton(
            onClick = { showOptionalLangs = !showOptionalLangs },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = if (showOptionalLangs) 0.dp else 8.dp,
                bottomEnd = if (showOptionalLangs) 0.dp else 8.dp
            ),
            contentPadding = PaddingValues(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        ) {
            val iconRes = if (showOptionalLangs) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
            Text(
                text = if (showOptionalLangs) "Ocultar traducciones" else "Añadir traducciones opcionales",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                // ⭐ Botón de Traducciones: 12.sp
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        // --- CAMPOS OPCIONALES (INGLÉS Y ALEMÁN) ---
        if (showOptionalLangs) {
            Surface(
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp, topStart = 0.dp, topEnd = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-1).dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(top = 8.dp)
                ) {
                    // Campo INGLÉS
                    Text(
                        text = "$label (EN) - (Opcional)",
                        // ⭐ Título del Box: 14.sp
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp)
                    )
                    OutlinedTextField(
                        value = textEn,
                        onValueChange = onTextChangeEn,
                        // ⭐ Label del TextField: 10.sp
                        label = { Text("Texto opcional en inglés", fontSize = 10.sp) },
                        singleLine = singleLine,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        minLines = if (!singleLine) 3 else 1
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo ALEMÁN
                    Text(
                        text = "$label (DE) - (Opcional)",
                        // ⭐ Título del Box: 14.sp
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp)
                    )
                    OutlinedTextField(
                        value = textDe,
                        onValueChange = onTextChangeDe,
                        // ⭐ Label del TextField: 10.sp
                        label = { Text("Texto opcional en alemán", fontSize = 10.sp) },
                        singleLine = singleLine,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        minLines = if (!singleLine) 3 else 1
                    )
                }
            }
        }
    }
}

// =========================================================================
// 2. PANTALLA PRINCIPAL DE CREACIÓN DEL PIN
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreacionPines(
    navController: NavController,
    rootNavController: NavController? = null
) {
    // --- ESTADOS LOCALES (Omitidos para brevedad) ---
    var tituloEs by remember { mutableStateOf("") }
    var tituloEn by remember { mutableStateOf("") }
    var tituloDe by remember { mutableStateOf("") }
    var descripcionEs by remember { mutableStateOf("") }
    var descripcionEn by remember { mutableStateOf("") }
    var descripcionDe by remember { mutableStateOf("") }
    var ubicacionDisplayName by remember { mutableStateOf("") }
    var vista360Url by remember { mutableStateOf("") }
    var audioUrlEs by remember { mutableStateOf("") }
    var audioUrlEn by remember { mutableStateOf("") }
    var audioUrlDe by remember { mutableStateOf("") }

    // Validación mínima para activar el botón
    val allMandatoryFieldsFilled = tituloEs.isNotBlank() && descripcionEs.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                // 1. Título Centrado
                title = {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Crear Nuevo Pin",
                            // ⭐ Título principal: 16.sp
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    // 2. Botón de Atrás (Integrado)
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Volver",
                        )
                    }
                },

                actions = {
                    // 3. Botón Guardar Pin (SIN FONDO, solo el icono)
                    IconButton(
                        onClick = {
                        },
                        // La propiedad 'enabled' gestiona la capacidad de pulsación
                        enabled = allMandatoryFieldsFilled,
                        // No se usa ningún modificador de fondo o forma.
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_save),
                            contentDescription = "Guardar Pin",
                            // El color del icono cambia según el estado: Verde si se puede guardar, Gris si no.
                            tint = if (allMandatoryFieldsFilled) GreenPinSave else Color.Gray,
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. TÍTULO ---
            RequiredTranslatableTextField(
                label = "Título del Pin",
                textEs = tituloEs,
                onTextChangeEs = { tituloEs = it },
                textEn = tituloEn,
                onTextChangeEn = { tituloEn = it },
                textDe = tituloDe,
                onTextChangeDe = { tituloDe = it },
                singleLine = true
            )

            // --- 2. DESCRIPCIÓN ---
            RequiredTranslatableTextField(
                label = "Descripción del Pin",
                textEs = descripcionEs,
                onTextChangeEs = { descripcionEs = it },
                textEn = descripcionEn,
                onTextChangeEn = { descripcionEn = it },
                textDe = descripcionDe,
                onTextChangeDe = { descripcionDe = it },
                singleLine = false
            )

            // --- 3. UBICACIÓN (Display Name) ---
            Text(
                text = "Ubicación",
                // ⭐ Título de Sección: 14.sp
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OutlinedTextField(
                value = ubicacionDisplayName,
                onValueChange = { ubicacionDisplayName = it },
                // ⭐ Label del TextField: 10.sp
                label = { Text("Nombre de la Ubicación", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
                singleLine = true
            )

            // --- 4. URL 360 (Opcional) ---
            Text(
                text = "Vista 360 (Opcional)",
                // ⭐ Título de Sección: 14.sp
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OutlinedTextField(
                value = vista360Url,
                onValueChange = { vista360Url = it },
                // ⭐ Label del TextField: 10.sp
                label = { Text("URL de la Vista 360 - Opcional", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
                singleLine = true
            )

            // --- 5. AUDIO URLS ---
            Text(
                text = "URLs de Audio",
                // ⭐ Título de Sección: 14.sp
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            OutlinedTextField(
                value = audioUrlEs,
                onValueChange = { audioUrlEs = it },
                // ⭐ Label del TextField: 10.sp
                label = { Text("URL de Audio (ES) - Opcional", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = audioUrlEn,
                onValueChange = { audioUrlEn = it },
                // ⭐ Label del TextField: 10.sp
                label = { Text("URL de Audio (EN) - Opcional", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = audioUrlDe,
                onValueChange = { audioUrlDe = it },
                // ⭐ Label del TextField: 10.sp
                label = { Text("URL de Audio (DE) - Opcional", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}