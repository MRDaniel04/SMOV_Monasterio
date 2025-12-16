package com.nextapp.monasterio.ui.virtualvisit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.nextapp.monasterio.repository.PinRepository
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.AppRoutes
import android.util.Log
// 1. IMPORTAMOS EL VIEWMODEL
import com.nextapp.monasterio.viewModels.AjustesViewModel

@Composable
fun PinDetalleFirestoreScreen(
    viewModel: AjustesViewModel, // <--- 2. AÑADIMOS ESTE PARÁMETRO
    pinId: String,
    navController: NavController,
    rootNavController: NavHostController? = null
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

        pin != null -> {
            val currentPin = pin

            val onVer360: (() -> Unit)? =
                if (!pin!!.vista360Url.isNullOrBlank()) {
                    {
                        rootNavController?.navigate(AppRoutes.PIN_360 + "/${pin!!.id}")
                    }
                } else {
                    null
                }

            // AQUI ESTABA EL ERROR:
            PinDetalleScreen(
                viewModel = viewModel,
                pin = pin!!,
                onBack = { navController.popBackStack() },
                onVer360 = onVer360
            )
        }
    }
}