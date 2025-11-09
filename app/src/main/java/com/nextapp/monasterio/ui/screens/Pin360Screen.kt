package com.nextapp.monasterio.ui.screens

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.repository.PinRepository // Importa tu repositorio
import com.panoramagl.PLImage
import com.panoramagl.PLManager
import com.panoramagl.PLSphericalPanorama
import coil.imageLoader
import coil.request.ImageRequest

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Pin360Screen(
    pinId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estados para manejar la carga
    var pin by remember { mutableStateOf<PinData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) } // El bitmap descargado

    // Visor 360
    val plManager = remember { PLManager(context) }
    val frameLayout = remember { FrameLayout(context) }

    // 1. Cargar datos del Pin desde Firestore
    LaunchedEffect(pinId) {
        isLoading = true
        try {
            val loadedPin = PinRepository.getPinById(pinId)
            if (loadedPin?.vista360Url.isNullOrBlank()) {
                errorMessage = "Pin no encontrado o no tiene URL 360"
            } else {
                pin = loadedPin
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar pin: ${e.message}"
        }
    }

    // 2. Cuando el Pin esté cargado, descargar la imagen 360
    LaunchedEffect(pin) {
        if (pin != null) {
            val imageLoader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(pin?.vista360Url) // <-- ¡Carga la URL desde Firebase!
                .allowHardware(false) // Requerido para Bitmap
                .target { drawable ->
                    // La imagen se ha descargado
                    bitmap = (drawable as BitmapDrawable).bitmap
                    isLoading = false // Dejamos de cargar
                }
                .build()
            imageLoader.execute(request)
        }
    }

    // 3. Efecto para el ciclo de vida de PLManager
    DisposableEffect(lifecycleOwner, plManager, bitmap) {

        // 1. Creamos una copia local inmutable del bitmap
        val currentBitmap = bitmap

        // 2. Comprobamos la copia local
        if (currentBitmap != null) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        plManager.setContentView(frameLayout)
                        plManager.onCreate()
                        try {
                            val panorama = PLSphericalPanorama()
                            panorama.camera.lookAt(0.0f, 0.0f)

                            // 3. Usamos la copia local (currentBitmap)
                            // El compilador ahora sabe que esto no puede ser nulo
                            panorama.setImage(PLImage(currentBitmap, false))

                            plManager.panorama = panorama
                        } catch (e: Throwable) {
                            Log.e("Pin360Screen", "Error al setear bitmap: ${e.message}", e)
                        }
                    }
                    Lifecycle.Event.ON_RESUME -> plManager.onResume()
                    Lifecycle.Event.ON_PAUSE -> plManager.onPause()
                    Lifecycle.Event.ON_DESTROY -> plManager.onDestroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                plManager.onDestroy()
            }
        } else {
            onDispose { /* No hay nada que limpiar si el bitmap es nulo */ }
        }
    }

    // --- Interfaz de Usuario ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        when {
            // Mientras carga el pin o la imagen
            isLoading || (pin != null && bitmap == null) -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // Si hay un error
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
            // Si todo está listo
            else -> {
                AndroidView(
                    factory = { frameLayout },
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInteropFilter { motionEvent ->
                            plManager.onTouchEvent(motionEvent)
                            true
                        }
                )
            }
        }

        // Botón de "Atrás" (siempre visible)
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Volver",
                tint = Color.White
            )
        }
    }
}