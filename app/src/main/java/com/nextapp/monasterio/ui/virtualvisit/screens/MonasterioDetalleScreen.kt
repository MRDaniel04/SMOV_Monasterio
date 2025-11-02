package com.nextapp.monasterio.ui.virtualvisit.screens

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.virtualvisit.components.DebugPhotoView
import com.nextapp.monasterio.ui.virtualvisit.data.MonasterioData
import com.nextapp.monasterio.ui.virtualvisit.utils.isPointInPinArea

@Composable
fun MonasterioDetalleScreen(
    navController: NavController,
    rootNavController: NavHostController? = null // 👈 nuevo parámetro opcional
) {
    val context = LocalContext.current

    var activePath by remember { mutableStateOf<android.graphics.Path?>(null) }
    var activeHighlight by remember { mutableStateOf<Color?>(null) }
    var isPinPressed by remember { mutableStateOf(false) }

    val planoBackgroundColor = Color(0xFFF5F5F5)
    val initialZoom = 1.5f

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

                    pins = MonasterioData.pines.map {
                        DebugPhotoView.PinData(
                            x = it.x,
                            y = it.y,
                            iconId = it.iconRes,
                            isPressed = isPinPressed
                        )
                    }

                    setOnPhotoTapListener { _, x, y ->
                        val pin = MonasterioData.pines.find {
                            isPointInPinArea(x, y, it.x, it.y, it.tapRadius)
                        }

                        if (pin != null) {
                            isPinPressed = true
                            Toast.makeText(context, "${pin.id} pulsado", Toast.LENGTH_SHORT).show()

                            Handler(Looper.getMainLooper()).postDelayed({
                                isPinPressed = false
                                // 👇 Navegación al NavController principal
                                if (rootNavController != null) {
                                    rootNavController.navigate(AppRoutes.PIN_DETALLE)
                                } else {
                                    Toast.makeText(context, "No se pudo navegar al detalle del pin", Toast.LENGTH_SHORT).show()
                                }
                            }, 200)
                        } else {
                            Toast.makeText(
                                context,
                                "Fuera del área interactiva",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    photoViewRef = this
                }
            },
            update = {
                it.interactivePath = activePath
                it.highlightColor = activeHighlight?.toArgb() ?: Color.Transparent.toArgb()
                it.invalidate()
            }
        )

        // 🎛️ Controles flotantes
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
                    contentDescription = "Aumentar",
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
                    contentDescription = "Disminuir",
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
                    contentDescription = "Reajustar",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}