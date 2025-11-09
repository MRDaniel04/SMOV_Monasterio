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
import androidx.compose.foundation.shape.RoundedCornerShape // <-- Import
import androidx.compose.material3.Icon // <-- Import
import androidx.compose.material3.IconButton // <-- Import
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource // <-- Import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp // <-- Import
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController // <-- Import
import com.nextapp.monasterio.R // <-- Import

@Composable
fun GenericDetailScreen(
    title: String,
    navController: NavHostController // <-- 1. Añadimos NavController
) {

    // --- ¡¡CORRECCIÓN AQUÍ!! ---
    // 2. Envolvemos todo en un Box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)), // El fondo se mueve al Box
    ) {

        // 3. Tu Column con el texto va "debajo"
        Column(
            modifier = Modifier
                .fillMaxSize(),
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

        // 4. Tu IconButton va "encima"
        IconButton(
            onClick = { navController.popBackStack() }, // Vuelve atrás
            modifier = Modifier
                .align(Alignment.TopStart) // <-- ¡Ahora SÍ funciona!
                .statusBarsPadding() // Para que no se ponga debajo de la barra de estado
                .padding(16.dp) // Margen
                .background(
                    color = Color.Black.copy(alpha = 0.5f), // Fondo negro semitransparente
                    shape = RoundedCornerShape(12.dp) // Esquinas redondeadas
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Volver",
                tint = Color.White // Flecha blanca
            )
        }
    }
}