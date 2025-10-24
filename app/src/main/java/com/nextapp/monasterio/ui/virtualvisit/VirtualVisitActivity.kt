package com.nextapp.monasterio.ui.virtualvisit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nextapp.monasterio.ui.theme.Smov_monasterioTheme

class VirtualVisitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Smov_monasterioTheme {
                // Contenido simple de marcador de posición
                Box(
                    modifier = Modifier.Companion.fillMaxSize(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text(
                        text = "Pantalla de Visita Virtual",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}