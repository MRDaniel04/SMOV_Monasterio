package com.nextapp.monasterio.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextapp.monasterio.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun ReservaScreen(){

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    var formatoFecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    var mostrarSelectorHora by remember { mutableStateOf(false) }
    var horasDisponibles = listOf("17:00","18:00","19:00")

    val misFechasSeleccionables = object : SelectableDates{
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val hoy = Calendar.getInstance()
            hoy.set(Calendar.HOUR_OF_DAY,0)
            hoy.set(Calendar.MINUTE,0)
            hoy.set(Calendar.SECOND,0)
            hoy.set(Calendar.MILLISECOND,0)

            val calendario = Calendar.getInstance()
            calendario.timeInMillis = utcTimeMillis

            val esDespues = utcTimeMillis >= hoy.timeInMillis

            val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
            val esMiercolesOJueves = diaSemana == Calendar.WEDNESDAY || diaSemana == Calendar.THURSDAY
            
            return esDespues && esMiercolesOJueves
        }

        override fun isSelectableYear(year: Int): Boolean {
            return year >= Calendar.getInstance().get(Calendar.YEAR)
        }
    }
    val estadoSelectorFecha = rememberDatePickerState(
        selectableDates = misFechasSeleccionables
    )

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = AbsoluteAlignment.Left,
        verticalArrangement = Arrangement.Center
    ){
        Text("Nombre", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = nombre,
            onValueChange = {nombre=it},
            placeholder = {Text("Introduce tu nombre completo")},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.LightGray.copy(alpha = 0.6f),
                unfocusedContainerColor = Color.LightGray.copy(alpha=0.6f),
                disabledContainerColor = Color.LightGray.copy(alpha=0.6f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Correo Electrónico",style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = email,
            onValueChange = {email=it},
            placeholder = {Text("ej: tu@email.com")},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.LightGray.copy(alpha = 0.6f),
                unfocusedContainerColor = Color.LightGray.copy(alpha=0.6f),
                disabledContainerColor = Color.LightGray.copy(alpha=0.6f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Fecha",style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = fecha,
            onValueChange = {},
            readOnly = true,
            placeholder = {"Selecciona una fecha"},
            trailingIcon = {
                IconButton(onClick = {mostrarSelectorFecha = true}) {
                    Icon(painterResource(R.drawable.calendario),"Seleccionar Fecha")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.LightGray.copy(alpha = 0.6f),
                unfocusedContainerColor = Color.LightGray.copy(alpha=0.6f),
                disabledContainerColor = Color.LightGray.copy(alpha=0.6f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Hora",style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Box {
            TextField(
                value = hora,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Selecciona una hora") },
                trailingIcon = {
                    Icon(painterResource(R.drawable.ic_time_24), "Selecciona una fecha")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.LightGray.copy(alpha = 0.6f),
                    unfocusedContainerColor = Color.LightGray.copy(alpha = 0.6f),
                    disabledContainerColor = Color.LightGray.copy(alpha = 0.6f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { mostrarSelectorHora = true }
            )
            DropdownMenu(
                expanded = mostrarSelectorHora,
                onDismissRequest = {mostrarSelectorHora = false},
                modifier = Modifier.fillMaxWidth(0.8f),
            ) {
                horasDisponibles.forEach { timeSlot ->
                    DropdownMenuItem(
                        text = {Text(timeSlot)},
                        onClick = {
                            hora=timeSlot
                            mostrarSelectorHora = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.MonasteryBlue)
            )
        ) {
            Text("CONFIRMAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
    if (mostrarSelectorFecha) {
        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarSelectorFecha = false
                        val selectedDate = estadoSelectorFecha.selectedDateMillis
                        if (selectedDate != null) {
                            fecha = formatoFecha.format(selectedDate)
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelectorFecha = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(
                state = estadoSelectorFecha,
            )
        }
    }
}
