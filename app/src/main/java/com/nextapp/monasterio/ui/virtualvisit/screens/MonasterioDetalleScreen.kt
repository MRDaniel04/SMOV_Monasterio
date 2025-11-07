package com.nextapp.monasterio.ui.virtualvisit.screens

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import kotlinx.coroutines.launch
import com.nextapp.monasterio.repository.PinRepository

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

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando pines...")
        }
        return
    }

    // 🖼️ Vista principal con el plano
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(planoBackgroundColor)
    ) {
        var photoViewRef by remember { mutableStateOf<DebugPhotoView?>(null) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                DebugPhotoView(ctx).apply {
                    setImageResource(R.drawable.monasterio_interior)
                    post { setScale(initialZoom, true) }

                    // 🔴 Pintamos los pines cargados desde Firestore
                    pins = pines.map {
                        DebugPhotoView.PinData(
                            x = it.x,
                            y = it.y,
                            iconId = R.drawable.pin3,
                            isPressed = isPinPressed
                        )
                    }

                    // 👆 Listener de toque
                    setOnPhotoTapListener { _, x, y ->
                        val pin = pines.find {
                            isPointInPinArea(x, y, it.x, it.y, it.tapRadius)
                        }

                        if (pin != null) {
                            Log.d("MonasterioDebug", "🟢 Pulsado pin con id=${pin.id}")
                            isPinPressed = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                isPinPressed = false
                                rootNavController?.navigate("pin_detalle/${pin.id}") {
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

        // 🎛️ Controles flotantes (zoom)
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
    }
}
