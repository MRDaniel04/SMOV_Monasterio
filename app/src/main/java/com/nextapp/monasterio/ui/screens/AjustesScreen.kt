package com.nextapp.monasterio.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.LanguageHelper // Importa el Helper que creamos

@Composable
fun AjustesScreen() {
    // Obtenemos el contexto como ComponentActivity para poder llamar a .recreate()
    val context = LocalContext.current as ComponentActivity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Seleccionar Idioma", // Puedes cambiar esto por un string resource
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Botón para Español
        Button(onClick = {
            // 1. Guarda la preferencia de idioma
            LanguageHelper.saveLocale(context, "es")
            // 2. Reinicia la actividad para aplicar el cambio
            context.recreate()
        }) {
            Text("Español")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para Inglés
        Button(onClick = {
            LanguageHelper.saveLocale(context, "en")
            context.recreate()
        }) {
            Text("English")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para Alemán
        Button(onClick = {
            LanguageHelper.saveLocale(context, "de")
            context.recreate()
        }) {
            Text("Deutsch")
        }
    }
}