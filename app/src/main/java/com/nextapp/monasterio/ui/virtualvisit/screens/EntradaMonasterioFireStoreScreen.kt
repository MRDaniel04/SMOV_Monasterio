package com.nextapp.monasterio.ui.virtualvisit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController // <-- ¡IMPORT AÑADIDO!
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.ui.screens.EntradaMonasterioScreen

@Composable
fun EntradaMonasterioFirestoreScreen(
    pinId: String,
    navController: NavHostController,
    rootNavController: NavHostController?
) {
    val (pin, setPin) = remember { mutableStateOf<PinData?>(null) }

    LaunchedEffect(pinId) {
        val loadedPin = com.nextapp.monasterio.repository.PinRepository.getPinById(pinId)
        setPin(loadedPin)
    }

    if (pin != null) {
        EntradaMonasterioScreen(
            pin = pin!!,
            onBack = { navController.popBackStack() }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
