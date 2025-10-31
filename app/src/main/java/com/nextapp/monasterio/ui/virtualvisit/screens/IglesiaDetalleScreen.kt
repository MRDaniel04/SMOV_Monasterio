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

/**
 * 🔹 Pantalla de detalle específica para la figura de la Iglesia.
 * Muestra información general o específica del área tocada en el plano.
 */
@Composable
fun IglesiaDetalleScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0)), // Fondo cálido diferente
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DETALLE DE LA IGLESIA",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF8F00)
        )
        Text(
            text = "Vista interactiva del área de la iglesia.",
            fontSize = 16.sp,
            color = Color(0xFFFF8F00)
        )
    }
}
