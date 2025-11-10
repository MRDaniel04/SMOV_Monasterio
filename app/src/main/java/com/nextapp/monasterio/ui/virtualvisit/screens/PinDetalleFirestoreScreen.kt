package com.nextapp.monasterio.ui.virtualvisit.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController // <-- ¡IMPORT AÑADIDO!
import com.nextapp.monasterio.repository.PinRepository
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.AppRoutes // <-- ¡IMPORT AÑADIDO!
import android.util.Log

@Composable
fun PinDetalleFirestoreScreen(
    pinId: String,
    navController: NavController, // Este es el local (para 'onBack')
    rootNavController: NavHostController? = null // <-- 1. ACEPTA EL NAVEGADOR RAÍZ
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

            // 2. Usamos 'currentPin' para todas las operaciones
            Log.d(
                "Pin360Debug",
                "Pin cargado: '${currentPin?.titulo}'. URL 360: [${currentPin?.vista360Url}]"
            )
            val onVer360: (() -> Unit)? =
                // Usamos 'vista360Url' (el campo que añadimos a PinData.kt)
                if (!pin!!.vista360Url.isNullOrBlank()) {
                    // 3. Si la tiene, creamos la lambda de navegación
                    {
                        // Llama al navegador RAÍZ (rootNavController)
                        // y navega a la NUEVA ruta 'PIN_360'
                        rootNavController?.navigate(AppRoutes.PIN_360 + "/${pin!!.id}")
                    }
                } else {
                    // 4. Si no la tiene, pasa null
                    null
                }

            PinDetalleScreen(
                pin = pin!!,
                onBack = { navController.popBackStack() },
                onVer360 = onVer360 // <-- 5. PASAMOS LA LAMBDA (O NULL)
            )
        }
    }
}