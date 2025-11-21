package com.nextapp.monasterio.ui.screens

import android.app.Activity
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

    // ⭐ VALOR CONSTANTE PARA LA ALTURA DEL PANEL
    val PANEL_HEIGHT_FRACTION = 0.50f // 35% de la altura total de la pantalla

    Log.d("EdicionPines", "Composición iniciada - Modo Interacción Pin (Panel 35%)")

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

                // ⭐ LÓGICA: OCULTAR PANEL AL DESPLAZAR/HACER ZOOM (PAN/MATRIX CHANGE) ⭐
                photoView.attacher.setOnMatrixChangeListener {
                    if (selectedPin != null) {
                        Log.d("EdicionPines", "Matrix cambió (Pan/Zoom detectado). Ocultando panel.")
                        selectedPin = null

                        if (photoView.translationY != 0f) {
                            photoView.translationY = 0f
                            Log.d("EdicionPines", "Restaurando translationY a 0f tras desplazamiento manual.")
                        }
                    }
                }

                // 2. DETECCIÓN DE PIN PULSADO y GESTIÓN DEL PANEL
                photoView.setOnPhotoTapListener { _, tapX, tapY ->

                    val drawable = photoView.drawable ?: return@setOnPhotoTapListener

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

                        // --- ⭐ CÁLCULO DE DESPLAZAMIENTO ADAPTATIVO CON PORCENTAJE ⭐

                        val pinMarginDp = 80.dp
                        val density = context.resources.displayMetrics.density
                        val pinMarginPx = pinMarginDp.value * density

                        // 1. Calcular la altura del panel en píxeles (35% de la altura de la PhotoView)
                        val panelHeightPx = photoView.height * PANEL_HEIGHT_FRACTION

                        // 2. Límite superior (Target Y): Posición donde queremos que quede el pin.
                        val pinTargetY = photoView.height - panelHeightPx - pinMarginPx

                        // 3. Comprobar si el pin queda por debajo del límite (será ocultado)
                        if (pinScreenYCoord > pinTargetY) {

                            // 4. CALCULAR DESPLAZAMIENTO NECESARIO
                            val neededShift = pinScreenYCoord - pinTargetY

                            // 5. APLICAR DESPLAZAMIENTO ADAPTATIVO (hacia ARRIBA: negativo)
                            photoViewRef?.moveVerticalFree(-neededShift)

                            Toast.makeText(context, "Desplazando plano: ${String.format("%.0f", neededShift)}px", Toast.LENGTH_SHORT).show()
                            Log.w("EdicionPines", "Pin oculto. Desplazando: -${String.format("%.0f", neededShift)}px.")
                        } else {
                            Log.d("EdicionPines", "El pin es visible. No se requiere desplazamiento.")
                        }

                    } else {
                        // TOQUE FUERA DEL PIN: Oculta el panel y RESTAURA EL DESPLAZAMIENTO
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
        )

        // -------------------------
        // ⭐ PANEL INFORMATIVO (35% de altura) ⭐
        // -------------------------
        if (selectedPin != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(PANEL_HEIGHT_FRACTION) // ⭐ APLICACIÓN DEL 35%
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