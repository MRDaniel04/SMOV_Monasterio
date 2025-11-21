package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Matrix
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
// Nota: Se asume la existencia de PinRepository y PlanoRepository
import com.nextapp.monasterio.repository.PinRepository
import com.nextapp.monasterio.repository.PlanoRepository
// Nota: Se asume la existencia de DebugPhotoView
import com.nextapp.monasterio.ui.virtualvisit.components.DebugPhotoView
import kotlinx.coroutines.launch

@Composable
fun EdicionPines(
    navController: NavController,
    rootNavController: NavController? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Log.d("EdicionPines", "Composición iniciada - Modo Interacción Pin")

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { /* Cleanup */ }
    }

    // --- Estados de Datos y UI ---
    var pines by remember { mutableStateOf<List<PinData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var photoViewRef by remember { mutableStateOf<DebugPhotoView?>(null) }
    var selectedPin by remember { mutableStateOf<PinData?>(null) }
    var planoUrl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // --- Carga inicial del plano y pines ---
    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            try {
                // Se asume la existencia de PlanoRepository (para fines de compilación)
                val plano = PlanoRepository.getPlanoById("monasterio_interior")
                planoUrl = plano?.plano ?: ""

                val allPins = PinRepository.getAllPins()
                val pinRefs = plano?.pines?.map { it.substringAfterLast("/") } ?: emptyList()
                pines = allPins.filter { pinRefs.contains(it.id) }
            } catch (e: Exception) {
                Log.e("EdicionPines", "❌ Error al cargar plano/pines", e)
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando plano…")
            }
            return@Box
        }

        // --- PhotoView (DebugPhotoView) integrado en Compose ---
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                DebugPhotoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setImageFromUrl(planoUrl)

                    post {
                        // Alineación al fondo (parte inferior de la pantalla)
                        attacher.scaleType = android.widget.ImageView.ScaleType.FIT_END
                        Log.d("EdicionPines", "Plano alineado al final (abajo) usando FIT_END.")
                    }
                }.also { photoViewRef = it }
            },
            update = { photoView ->

                // 1. Dibuja los pines
                photoView.pins = pines.map {
                    DebugPhotoView.PinData(
                        x = it.x,
                        y = it.y,
                        iconId = R.drawable.pin3,
                        isPressed = it.id == selectedPin?.id
                    )
                }

                // ⭐ NUEVA LÓGICA: OCULTAR PANEL AL DESPLAZAR/HACER ZOOM (PAN/MATRIX CHANGE) ⭐
                photoView.attacher.setOnMatrixChangeListener {
                    // Se dispara cada vez que la matriz de la imagen cambia (por pan o zoom)
                    if (selectedPin != null) {
                        Log.d("EdicionPines", "Matrix cambió (Pan/Zoom detectado). Ocultando panel.")

                        // Oculta el panel
                        selectedPin = null

                        // Restaura el desplazamiento si existía
                        if (photoView.translationY != 0f) {
                            photoView.translationY = 0f
                            Log.d("EdicionPines", "Restaurando translationY a 0f tras desplazamiento manual.")
                        }
                    }
                }

                // 2. DETECCIÓN DE PIN PULSADO y GESTIÓN DEL PANEL
                photoView.setOnPhotoTapListener { _, tapX, tapY ->

                    val drawable = photoView.drawable ?: return@setOnPhotoTapListener

                    // ... (Cálculos de coordenadas y búsqueda del pin, se mantienen iguales) ...
                    val m = FloatArray(9)
                    photoView.imageMatrix.getValues(m)
                    val scaleX = m[Matrix.MSCALE_X]
                    val transX = m[Matrix.MTRANS_X]
                    val transY = m[Matrix.MTRANS_Y]

                    val tapImageX = tapX * drawable.intrinsicWidth
                    val tapImageY = tapY * drawable.intrinsicHeight
                    val tapScreenX = tapImageX * scaleX + transX
                    val tapScreenY = tapImageY * scaleX + transY

                    var touchedPin: PinData? = null
                    var pinScreenYCoord = 0f

                    pines.forEach { pin ->
                        val pinImageX = pin.x * drawable.intrinsicWidth
                        val pinImageY = pin.y * drawable.intrinsicHeight

                        val pinScreenX = pinImageX * scaleX + transX
                        val pinScreenY = pinImageY * scaleX + transY

                        val dx = tapScreenX - pinScreenX
                        val dy = tapScreenY - pinScreenY

                        val tapRadiusPx = pin.tapRadius * drawable.intrinsicWidth * scaleX

                        if ((dx * dx + dy * dy) <= tapRadiusPx * tapRadiusPx) {
                            touchedPin = pin
                            pinScreenYCoord = pinScreenY
                            return@forEach
                        }
                    }

                    if (touchedPin != null) {
                        // PIN ENCONTRADO: Muestra el panel y chequea si lo ocultará

                        // 1. RESTAURAR POSICIÓN ANTERIOR (evitar desplazamiento acumulado)
                        if (selectedPin != null && photoView.translationY != 0f) {
                            photoViewRef?.translationY = 0f
                            Log.d("EdicionPines", "Restaurando translationY a 0f antes de aplicar nuevo shift.")
                        }

                        selectedPin = touchedPin

                        // --- CÁLCULO DE DESPLAZAMIENTO ADAPTATIVO (Se mantiene) ---
                        val panelHeightDp = 300.dp
                        val pinMarginDp = 80.dp
                        val density = context.resources.displayMetrics.density

                        val panelHeightPx = panelHeightDp.value * density
                        val pinMarginPx = pinMarginDp.value * density

                        val pinTargetY = photoView.height - panelHeightPx - pinMarginPx

                        if (pinScreenYCoord > pinTargetY) {
                            val neededShift = pinScreenYCoord - pinTargetY

                            photoViewRef?.moveVerticalFree(-neededShift)

                            Toast.makeText(context, "Desplazando plano: ${String.format("%.0f", neededShift)}px", Toast.LENGTH_SHORT).show()
                            Log.w("EdicionPines", "Pin oculto. Desplazando: -${String.format("%.0f", neededShift)}px.")
                        }
                    } else {
                        // TOQUE FUERA DEL PIN (Toque estático)
                        if (selectedPin != null) {
                            Log.d("EdicionPines", "❌ Toque estático fuera de Pin. Ocultando panel y RESTAURANDO POSICIÓN.")

                            // RESTAURAR DESPLAZAMIENTO (translationY = 0)
                            if (photoView.translationY != 0f) {
                                photoViewRef?.translationY = 0f
                                Log.d("EdicionPines", "Restaurando translationY a 0f.")
                            }

                            selectedPin = null
                        } else {
                            Log.d("EdicionPines", "Toque fuera y no había Pin seleccionado. Ignorando.")
                        }
                    }

                    photoView.invalidate() // Forzar redibujo
                }

                photoView.invalidate()
            }


// ... (resto del código sin cambios)
        )

        // -------------------------
        // PANEL INFORMATIVO
        // -------------------------
        if (selectedPin != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = selectedPin?.titulo ?: "Detalle", color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Coordenadas: X=${selectedPin?.x}, Y=${selectedPin?.y}", color = Color.Gray)
                }
            }
        }

        // --- Botón Atrás ---
        IconButton(
            onClick = {
                if (rootNavController != null) rootNavController.popBackStack()
                else navController.popBackStack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = stringResource(R.string.go_back),
                tint = Color.White
            )
        }
    }
}