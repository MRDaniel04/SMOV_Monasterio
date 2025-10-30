package com.nextapp.monasterio.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

//Pantallas aun vacias, borrar archivo cuando las pantallas tengan contenido

@Composable
fun GaleriaScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Contenido de Galería", style = MaterialTheme.typography.headlineMedium)
    }
}

/*@Composable
fun PerfilScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Contenido de Perfil", style = MaterialTheme.typography.headlineMedium)
    }
}*/