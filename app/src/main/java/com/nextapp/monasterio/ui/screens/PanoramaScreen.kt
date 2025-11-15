package com.nextapp.monasterio.ui.screens

import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.nextapp.monasterio.R
import com.nextapp.monasterio.data.MonasterioMapRepository
import androidx.compose.ui.res.painterResource
import com.panoramagl.PLManager

// Imports de la librería
import com.panoramagl.PLImage
import com.panoramagl.PLSphericalPanorama
import com.panoramagl.utils.PLUtils

// El import para AndroidView
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PanoramaScreen(
    vistaId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val vista = remember(vistaId) {
        MonasterioMapRepository.getVistaPorId(vistaId)
    }

    val plManager = remember { PLManager(context) }
    val frameLayout = remember { FrameLayout(context) }

    // --- (El DisposableEffect se queda igual que tu código) ---
    DisposableEffect(lifecycleOwner, plManager, vista) {
        val observer = LifecycleEventObserver { _, event ->
            if (vista != null) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        plManager.setContentView(frameLayout)
                        plManager.setInertiaEnabled(false)
                        plManager.setAcceleratedTouchScrollingEnabled(true)
                        plManager.accelerometerSensitivity = 0.5f
                        plManager.isAcceleratedTouchScrollingEnabled = false
                        plManager.onCreate()
                        try {
                            val panorama = PLSphericalPanorama()
                            panorama.camera.lookAt(0.0f, 0.0f)

                            val bitmap = PLUtils.getBitmap(context, vista.imagenResId)
                            panorama.setImage(PLImage(bitmap, false))

                            plManager.panorama = panorama
                        } catch (e: Throwable) {
                            Log.e("PanoramaScreen", "Error al cargar la panorámica: ${e.message}", e)
                        }
                    }
                    Lifecycle.Event.ON_RESUME -> plManager.onResume()
                    Lifecycle.Event.ON_PAUSE -> plManager.onPause()
                    Lifecycle.Event.ON_DESTROY -> plManager.onDestroy()
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            plManager.onDestroy()
        }
    }
    // --- FIN DEL DISPOSABLEEFFECT ---

    // --- ¡MODIFICACIÓN AQUÍ! ---
    // 1. Envolvemos todo en un Box
    Box(modifier = Modifier.fillMaxSize()) {

        // 2. Tu AndroidView (el panorama) va al fondo
        AndroidView(
            factory = { frameLayout },
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { motionEvent ->
                    plManager.onTouchEvent(motionEvent)
                    true
                }
        )

        // 3. El botón de "Atrás" personalizado va encima
        IconButton(
            onClick = { navController.popBackStack() }, // Navega hacia atrás
            modifier = Modifier
                .align(Alignment.TopStart) // Arriba a la izquierda
                .padding(16.dp) // Margen
                .background(
                    color = Color.Black.copy(alpha = 0.5f), // Fondo negro semitransparente
                    shape = RoundedCornerShape(12.dp) // Esquinas redondeadas
                )
        ) {
            Icon(
                // ¡CAMBIA ESTA LÍNEA!
                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                contentDescription = "Volver",
                tint = Color.White
            )
        }
    }
}