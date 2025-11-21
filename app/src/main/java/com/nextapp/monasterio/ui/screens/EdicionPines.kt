package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Matrix
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
    val CENTRALIZATION_THRESHOLD = 0.15f // 15% de los bordes de la pantalla

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
                    var pinScreenXCoord = 0f // ⭐ Guardamos la coordenada X del pin

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
                            pinScreenXCoord = pinScreenX // ⭐ Asignación de la coordenada X
                            return@forEach
                        }
                    }

                    if (touchedPin != null) {
                        // PIN ENCONTRADO: Muestra el panel y chequea si lo ocultará

                        // 1. RESTAURAR POSICIONES ANTERIORES (Horizontal y Vertical)
                        // Limpiamos los desplazamientos aplicados anteriormente.
                        if (selectedPin != null || photoView.translationY != 0f || photoView.translationX != 0f) {
                            photoViewRef?.translationY = 0f
                            photoViewRef?.translationX = 0f // ⭐ Restaurar desplazamiento HORIZONTAL
                            Log.d("EdicionPines", "Restaurando translationY/X a 0f antes de aplicar nuevo shift.")
                        }

                        selectedPin = touchedPin

                        // --- ⭐ GESTIÓN DEL DESPLAZAMIENTO HORIZONTAL (Centralización) ⭐

                        val screenWidth = photoView.width.toFloat()
                        val targetCenter = screenWidth / 2f
                        val thresholdPx = screenWidth * CENTRALIZATION_THRESHOLD

                        var neededShiftX = 0f

                        // Borde Izquierdo (0% al 15%)
                        if (pinScreenXCoord < thresholdPx) {
                            // Mover la vista hacia la DERECHA (shift POSITIVO) para que el pin se mueva a la IZQUIERDA.
                            neededShiftX = targetCenter - pinScreenXCoord

                            // Borde Derecho (85% al 100%)
                        } else if (pinScreenXCoord > (screenWidth - thresholdPx)) {
                            // Mover la vista hacia la IZQUIERDA (shift NEGATIVO) para que el pin se mueva a la DERECHA.
                            neededShiftX = targetCenter - pinScreenXCoord
                        }

                        if (neededShiftX != 0f) {
                            // Aplicar el desplazamiento horizontal (moveHorizontalFree(deltaX))
                            photoViewRef?.moveHorizontalFree(neededShiftX)
                            Log.w("EdicionPines", "Pin Lateral. Desplazando X: ${String.format("%.0f", neededShiftX)}px para centrar.")
                        }

                        // --- GESTIÓN DEL DESPLAZAMIENTO VERTICAL (Se mantiene) ---

                        val pinMarginDp = 80.dp
                        val density = context.resources.displayMetrics.density
                        val pinMarginPx = pinMarginDp.value * density

                        val panelHeightPx = photoView.height * PANEL_HEIGHT_FRACTION
                        val pinTargetY = photoView.height - panelHeightPx - pinMarginPx

                        if (pinScreenYCoord > pinTargetY) {
                            val neededShiftY = pinScreenYCoord - pinTargetY
                            photoViewRef?.moveVerticalFree(-neededShiftY)

                            // Mostrar Toast combinado
                            val totalShift = if (neededShiftX != 0f) "X:${String.format("%.0f", neededShiftX)} / Y:${String.format("%.0f", neededShiftY)}" else "Y:${String.format("%.0f", neededShiftY)}"
                            Toast.makeText(context, "Desplazando plano: $totalShift px", Toast.LENGTH_SHORT).show()
                            Log.w("EdicionPines", "Pin oculto. Desplazando Y: -${String.format("%.0f", neededShiftY)}px.")
                        } else if (neededShiftX != 0f) {
                            // Mostrar Toast solo si se movió en X
                            Toast.makeText(context, "Desplazando plano: X:${String.format("%.0f", neededShiftX)} px", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        // TOQUE FUERA DEL PIN: Oculta el panel y RESTAURA AMBOS DESPLAZAMIENTOS
                        if (selectedPin != null) {
                            Log.d("EdicionPines", "❌ Toque estático fuera de Pin. Ocultando panel y RESTAURANDO POSICIONES.")

                            // RESTAURAR AMBOS DESPLAZAMIENTOS (translationY = 0, translationX = 0)
                            if (photoView.translationY != 0f || photoView.translationX != 0f) {
                                photoViewRef?.translationY = 0f
                                photoViewRef?.translationX = 0f // ⭐ Restaurar desplazamiento HORIZONTAL
                                Log.d("EdicionPines", "Restaurando translationY/X a 0f.")
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
        // ⭐ PANEL INFORMATIVO (35% de altura y estilo Google Maps) ⭐
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