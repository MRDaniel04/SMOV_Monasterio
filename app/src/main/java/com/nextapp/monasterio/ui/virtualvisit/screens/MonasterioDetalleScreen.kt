package com.nextapp.monasterio.ui.virtualvisit.screens

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape // <-- Import
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // <-- Import
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.ui.virtualvisit.components.DebugPhotoView
import com.nextapp.monasterio.ui.virtualvisit.utils.isPointInPinArea
import com.nextapp.monasterio.repository.PinRepository
/*
@Composable
fun MonasterioDetalleScreen(
    navController: NavController,
    rootNavController: NavHostController? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isPinPressed by remember { mutableStateOf(false) }
    val initialZoom = 1.5f
    val planoBackgroundColor = Color(0xFFF5F5F5)

    // 🔥 Estado que guarda los pines cargados de Firestore
    var pines by remember { mutableStateOf<List<PinData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🚀 Cargar pines desde Firestore al entrar
    LaunchedEffect(Unit) {
        try {
            pines = PinRepository.getAllPins()
            Log.d("FirestoreDebug", "✅ Pines cargados: ${pines.size}")
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "❌ Error cargando pines", e)
            Toast.makeText(context, "Error cargando pines", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // --- ¡¡CORRECCIÓN AQUÍ!! ---
    // 1. Envolvemos todo en un Box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(planoBackgroundColor)
    ) {

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando pines...")
            }
            // Salimos temprano si está cargando, pero el Box ya está dibujado
            return@Box
        }

        // 🖼️ Vista principal con el plano (va "debajo")
        var photoViewRef by remember { mutableStateOf<DebugPhotoView?>(null) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                DebugPhotoView(ctx).apply {
                    setImageResource(R.drawable.monasterio_interior)
                    post { setScale(initialZoom, true) }

                    // 🔴 Pintamos los pines cargados desde Firestore
                    this.pins = pines.map { // Usamos 'this.pins' para evitar confusión de nombres
                        DebugPhotoView.PinData(
                            x = it.x,
                            y = it.y,
                            iconId = R.drawable.pin3,
                            isPressed = isPinPressed
                        )
                    }

                    // 👆 Listener de toque
                    setOnPhotoTapListener { _, x, y ->
                        // Usamos 'pines' (la variable de @Composable) para buscar
                        val pin = pines.find {
                            isPointInPinArea(x, y, it.x, it.y, it.tapRadius)
                        }

                        if (pin != null) {
                            Log.d("MonasterioDebug", "🟢 Pulsado pin con id=${pin.id}")
                            isPinPressed = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                isPinPressed = false
                                // Usamos el 'navController' (local) para ir al detalle
                                navController.navigate("pin_detalle/${pin.id}") {
                                    launchSingleTop = true
                                }
                            }, 200)
                        } else {
                            Toast.makeText(context, "Fuera del área interactiva", Toast.LENGTH_SHORT).show()
                        }
                    }

                    photoViewRef = this
                }
            }
        )

        // 🎛️ Controles flotantes (zoom) (van "encima")
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(onClick = {
                photoViewRef?.let {
                    val newScale = (it.scale + 0.2f).coerceAtMost(it.maximumScale)
                    it.setScale(newScale, true)
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.aumentar_zoom),
                    contentDescription = "Aumentar zoom",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }

            FloatingActionButton(onClick = {
                photoViewRef?.let {
                    val newScale = (it.scale - 0.2f).coerceAtLeast(it.minimumScale)
                    it.setScale(newScale, true)
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.disminuir_zoom),
                    contentDescription = "Disminuir zoom",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }

            FloatingActionButton(onClick = {
                photoViewRef?.apply {
                    setScale(initialZoom, true)
                    setTranslationX(0f)
                    setTranslationY(0f)
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.reajustar),
                    contentDescription = "Reajustar vista",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
        }

        // 3. Tu IconButton (flecha) va "encima"
        IconButton(
            onClick = { navController.popBackStack() }, // Vuelve atrás en el navegador local
            modifier = Modifier
                .align(Alignment.TopStart) // <-- ¡Ahora SÍ funciona!
                .statusBarsPadding() // Para que no se ponga debajo de la barra de estado
                .padding(16.dp) // Margen
                .background(
                    color = Color.Black.copy(alpha = 0.5f), // Fondo negro semitransparente
                    shape = RoundedCornerShape(12.dp) // Esquinas redondeadas
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back), // Usa tu icono
                contentDescription = "Volver",
                tint = Color.White // Flecha blanca
            )
        }
    }
}
*/