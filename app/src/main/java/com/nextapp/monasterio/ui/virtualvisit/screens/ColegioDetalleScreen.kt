package com.nextapp.monasterio.ui.virtualvisit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

/**
 * 🔹 Pantalla de detalle específica para la figura del Colegio.
 * Muestra información general o específica del área tocada en el plano.
 */
@Composable
fun ColegioDetalleScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0)), // Fondo cálido diferente
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DETALLE DEL COLEGIO",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF8F00)
        )
        Text(
            text = "Vista interactiva del área del colegio",
            fontSize = 16.sp,
            color = Color(0xFFFF8F00)
        )
    }
}
