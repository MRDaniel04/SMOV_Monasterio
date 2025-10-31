package com.nextapp.monasterio.ui.virtualvisit.screens

/**
 * 🔹 Pantalla genérica de detalle.
 *
 * Se usa como plantilla para mostrar información al seleccionar
 * una figura o un pin. Muestra un título central configurable.
 *
 * Ideal para pruebas, demostraciones o como base para vistas de detalle reales.
 *
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun GenericDetailScreen(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title, // Ejemplo: "Figura: Sala Irregular" o "Pin: Punto A"
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D47A1)
        )
    }
}
