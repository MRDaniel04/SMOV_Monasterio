package com.nextapp.monasterio.ui.virtualvisit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.nextapp.monasterio.repository.PinRepository
import com.nextapp.monasterio.models.PinData

@Composable
fun PinDetalleFirestoreScreen(
    pinId: String,
    navController: NavController
) {
    var pin by remember { mutableStateOf<PinData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pinId) {
        try {
            val loadedPin = PinRepository.getPinById(pinId)
            if (loadedPin != null) pin = loadedPin
            else errorMessage = "Pin no encontrado (ID: $pinId)"
        } catch (e: Exception) {
            errorMessage = "Error al cargar datos: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(errorMessage ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
        }

        pin != null -> PinDetalleScreen(
            pin = pin!!,
            onBack = { navController.popBackStack() }
        )
    }
}
