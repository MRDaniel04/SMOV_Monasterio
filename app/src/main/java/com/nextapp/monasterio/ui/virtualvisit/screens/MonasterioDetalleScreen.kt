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


@Composable
fun MonasterioDetalleScreen(navController: NavHostController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "VISTA DE LA FIGURA SELECCIONADA",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00796B)
        )
        Text(
            text = "Detalle del monasterio.",
            fontSize = 16.sp,
            color = Color(0xFF00796B)
        )
    }
}
