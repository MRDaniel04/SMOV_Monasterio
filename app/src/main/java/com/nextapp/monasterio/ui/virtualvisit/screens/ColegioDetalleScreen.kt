package com.nextapp.monasterio.ui.virtualvisit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape // <-- Import
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource // <-- Import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp // <-- Import
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nextapp.monasterio.R // <-- Import

/**
 * 🔹 Pantalla de detalle específica para la figura del Colegio.
 * Muestra información general o específica del área tocada en el plano.
 */
@Composable
fun ColegioDetalleScreen(navController: NavHostController) {

    // --- ¡¡CORRECCIÓN AQUÍ!! ---
    // 1. Envolvemos todo en un Box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0)), // Fondo cálido diferente
    ) {

        // 2. Tu Column con el texto va "debajo"
        Column(
            modifier = Modifier
                .fillMaxSize(),
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

        // 3. Tu IconButton va "encima"
        IconButton(
            onClick = { navController.popBackStack() }, // Vuelve atrás en el navegador local
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