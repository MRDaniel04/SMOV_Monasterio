package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.util.Patterns
import androidx.compose.ui.draw.shadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservaScreen(
    navController: NavController,
    topPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    // Detectar orientación
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { }
    }

    // --- ESTADOS DEL FORMULARIO ---
    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var hora by rememberSaveable { mutableStateOf("") }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var fechaError by remember { mutableStateOf<String?>(null) }
    var horaError by remember { mutableStateOf<String?>(null) }

    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    var mostrarSelectorHora by remember { mutableStateOf(false) }

    val formatoFecha = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    val horasDisponibles = listOf("17:00", "18:00", "19:00")

    // Lógica de validación de fechas
    val misFechasSeleccionables = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val hoy = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val calendario = Calendar.getInstance().apply { timeInMillis = utcTimeMillis }

            val esDespues = utcTimeMillis >= hoy.timeInMillis
            val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
            val esMiercolesOJueves = diaSemana == Calendar.WEDNESDAY || diaSemana == Calendar.THURSDAY

            return esDespues && esMiercolesOJueves
        }
        override fun isSelectableYear(year: Int) = year >= Calendar.getInstance().get(Calendar.YEAR)
    }

    val estadoSelectorFecha = rememberDatePickerState(selectableDates = misFechasSeleccionables)

    // Función de validación y navegación
    fun validarYConfirmar() {
        val nombreLimpio = nombre.trim()
        val nombreRegex = Regex("^[\\p{L} ]+\$")
        val contieneDigitosValidos = nombreRegex.matches(nombreLimpio)

        if (nombreLimpio.isEmpty()) nombreError = context.getString(R.string.compulsory_name)
        else if (!contieneDigitosValidos) nombreError = context.getString(R.string.valid_name)

        val emailLimpio = email.trim()
        if (emailLimpio.isEmpty()) emailError = context.getString(R.string.compulsory_email)
        else if (!Patterns.EMAIL_ADDRESS.matcher(emailLimpio).matches()) emailError = context.getString(R.string.valid_email)

        if (fecha == "") fechaError = context.getString(R.string.date_no_selected)
        if (hora == "") horaError = context.getString(R.string.hour_no_selected)

        if (emailError == null && nombreError == null && fecha != "" && hora != "") {
            navController.navigate(AppRoutes.CONFIRMACION_RESERVA + "/$nombre/$email/$fecha/$hora")
        }
    }

    // --- ESTRUCTURA PRINCIPAL ---
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. FONDO
        Image(
            painter = painterResource(R.drawable.fondo),
            contentDescription = "Fondo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. CONTENIDO
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding) // Respeta la barra superior
        ) {
            if (isLandscape) {
                // --- DISEÑO HORIZONTAL (2 Columnas) ---
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()), // Scroll global si es necesario
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // COLUMNA IZQUIERDA: Datos Personales
                    Column(modifier = Modifier.weight(1f)) {
                        ReservaTextField(
                            value = nombre,
                            labelRes = R.string.name_appointment,
                            placeholderRes = R.string.placeholdername_appointment,
                            error = nombreError,
                            onValueChange = { nombre = it; nombreError = null }
                        )
                        Spacer(Modifier.height(16.dp))
                        ReservaTextField(
                            value = email,
                            labelRes = R.string.email_appointment,
                            placeholderRes = R.string.placeholderemail_appointment,
                            error = emailError,
                            keyboardType = KeyboardType.Email,
                            onValueChange = { email = it; emailError = null }
                        )
                    }

                    // COLUMNA DERECHA: Fecha, Hora y Botón
                    Column(modifier = Modifier.weight(1f)) {
                        ReservaReadOnlyField(
                            value = fecha,
                            labelRes = R.string.date_appointment,
                            placeholderRes = R.string.placeholderdate_appointment,
                            iconRes = R.drawable.calendario,
                            error = fechaError,
                            onClick = { mostrarSelectorFecha = true }
                        )

                        Spacer(Modifier.height(16.dp))

                        // Selector de Hora
                        Box {
                            ReservaReadOnlyField(
                                value = hora,
                                labelRes = R.string.hour_appointment,
                                placeholderRes = R.string.placeholderhour_appointment,
                                iconRes = R.drawable.ic_time_24,
                                error = horaError,
                                onClick = { mostrarSelectorHora = true }
                            )
                            DropdownMenu(
                                expanded = mostrarSelectorHora,
                                onDismissRequest = { mostrarSelectorHora = false },
                                modifier = Modifier.fillMaxWidth(0.4f)
                            ) {
                                horasDisponibles.forEach { timeSlot ->
                                    DropdownMenuItem(
                                        text = { Text(timeSlot) },
                                        onClick = {
                                            horaError = null
                                            hora = timeSlot
                                            mostrarSelectorHora = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { validarYConfirmar() },
                            modifier = Modifier.fillMaxWidth().height(50.dp).shadow(8.dp, RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.MonasteryBlue))
                        ) {
                            Text(stringResource(R.string.confirm_button), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            } else {
                // --- DISEÑO VERTICAL (Original) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.height(24.dp)) // Margen superior extra

                    ReservaTextField(
                        value = nombre, labelRes = R.string.name_appointment, placeholderRes = R.string.placeholdername_appointment,
                        error = nombreError, onValueChange = { nombre = it; nombreError = null }
                    )
                    Spacer(Modifier.height(24.dp))

                    ReservaTextField(
                        value = email, labelRes = R.string.email_appointment, placeholderRes = R.string.placeholderemail_appointment,
                        error = emailError, keyboardType = KeyboardType.Email, onValueChange = { email = it; emailError = null }
                    )
                    Spacer(Modifier.height(24.dp))

                    ReservaReadOnlyField(
                        value = fecha, labelRes = R.string.date_appointment, placeholderRes = R.string.placeholderdate_appointment,
                        iconRes = R.drawable.calendario, error = fechaError, onClick = { mostrarSelectorFecha = true }
                    )
                    Spacer(Modifier.height(24.dp))

                    Box {
                        ReservaReadOnlyField(
                            value = hora, labelRes = R.string.hour_appointment, placeholderRes = R.string.placeholderhour_appointment,
                            iconRes = R.drawable.ic_time_24, error = horaError, onClick = { mostrarSelectorHora = true }
                        )
                        DropdownMenu(
                            expanded = mostrarSelectorHora,
                            onDismissRequest = { mostrarSelectorHora = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            horasDisponibles.forEach { timeSlot ->
                                DropdownMenuItem(
                                    text = { Text(timeSlot) },
                                    onClick = { horaError = null; hora = timeSlot; mostrarSelectorHora = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(48.dp))

                    Button(
                        onClick = { validarYConfirmar() },
                        modifier = Modifier.fillMaxWidth().height(50.dp).shadow(8.dp, RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.MonasteryBlue))
                    ) {
                        Text(stringResource(R.string.confirm_button), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }

    // Diálogo de Fecha
    if (mostrarSelectorFecha) {
        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaError = null
                    mostrarSelectorFecha = false
                    estadoSelectorFecha.selectedDateMillis?.let { fecha = formatoFecha.format(it) }
                }) { Text(stringResource(R.string.datepicker_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelectorFecha = false }) { Text(stringResource(R.string.datepicker_cancel)) }
            }
        ) {
            DatePicker(state = estadoSelectorFecha)
        }
    }
}

// --- COMPONENTES REUTILIZABLES ---

@Composable
fun ReservaTextField(
    value: String,
    labelRes: Int,
    placeholderRes: Int,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White) // Texto blanco para ver sobre el fondo
        Spacer(Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(stringResource(placeholderRes)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                errorContainerColor = Color.White.copy(alpha = 0.9f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = error != null,
            supportingText = { if (error != null) Text(text = error, color = MaterialTheme.colorScheme.error) }
        )
    }
}

@Composable
fun ReservaReadOnlyField(
    value: String,
    labelRes: Int,
    placeholderRes: Int,
    iconRes: Int,
    error: String?,
    onClick: () -> Unit
) {
    Column {
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.clickable { onClick() }) {
            TextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                enabled = false, // Deshabilitado para que el click lo capture el Box, pero visualmente activo
                placeholder = { Text(stringResource(placeholderRes)) },
                trailingIcon = {
                    Icon(painterResource(iconRes), contentDescription = null, tint = Color.Black)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color.White.copy(alpha = 0.8f),
                    disabledTextColor = Color.Black,
                    disabledPlaceholderColor = Color.Gray,
                    disabledIndicatorColor = Color.Transparent
                ),
                isError = error != null,
                supportingText = { if (error != null) Text(text = error, color = MaterialTheme.colorScheme.error) }
            )
        }
    }
}