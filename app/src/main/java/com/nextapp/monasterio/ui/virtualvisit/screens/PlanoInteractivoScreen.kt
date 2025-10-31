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
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.virtualvisit.components.DebugPhotoView
import com.nextapp.monasterio.ui.virtualvisit.data.PlanoData
import com.nextapp.monasterio.ui.virtualvisit.utils.isPointInPath
import com.nextapp.monasterio.ui.virtualvisit.utils.isPointInPinArea

@Composable
fun PlanoInteractivoScreen(navController: NavController) {
    val context = LocalContext.current

    var highlightMonasterio by remember { mutableStateOf(Color.Transparent) }
    var highlightIglesia by remember { mutableStateOf(Color.Transparent) }
    var activePath by remember { mutableStateOf(PlanoData.monasterio.path) }
    var isPinPressed by remember { mutableStateOf(false) }

    val planoBackgroundColor = Color(0xFFF5F5F5)
    val initialZoom = 1.5f // 🔹 Zoom inicial deseado para el plano.

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
                    setImageResource(R.drawable.plano_monasterio)

                    // 🔹 Aplica el zoom inicial cuando la imagen ya está cargada
                    post {
                        setScale(initialZoom, true)
                    }

                    interactivePath = activePath

                    pins = listOf(
                        DebugPhotoView.PinData(
                            x = PlanoData.monasterio.pinX,
                            y = PlanoData.monasterio.pinY,
                            iconId = R.drawable.pin3,
                            isPressed = isPinPressed
                        ),
                        DebugPhotoView.PinData(
                            x = PlanoData.pin2.pinX,
                            y = PlanoData.pin2.pinY,
                            iconId = R.drawable.pin3,
                            isPressed = isPinPressed
                        )
                    )

                    setOnPhotoTapListener { _, x, y ->
                        when {
                            // --- Tocar MONASTERIO ---
                            isPointInPath(x, y, PlanoData.monasterio.path) -> {
                                activePath = PlanoData.monasterio.path
                                highlightMonasterio = Color.Yellow.copy(alpha = 0.5f)
                                highlightIglesia = Color.Transparent

                                Handler(Looper.getMainLooper()).postDelayed({
                                    highlightMonasterio = Color.Transparent
                                    navController.navigate("detalle_monasterio")
                                }, 200)
                            }

                            // --- Tocar IGLESIA ---
                            isPointInPath(x, y, PlanoData.iglesia.path) -> {
                                activePath = PlanoData.iglesia.path
                                highlightIglesia = Color.Yellow.copy(alpha = 0.5f)
                                highlightMonasterio = Color.Transparent

                                Handler(Looper.getMainLooper()).postDelayed({
                                    highlightIglesia = Color.Transparent
                                    navController.navigate("detalle_iglesia")
                                }, 200)
                            }

                            // --- Tocar PIN 1 ---
                            isPointInPinArea(
                                x, y,
                                PlanoData.monasterio.pinX,
                                PlanoData.monasterio.pinY,
                                PlanoData.monasterio.pinTapRadiusNormalized
                            ) -> {
                                isPinPressed = true
                                Toast.makeText(context, "Pin 1 pulsado", Toast.LENGTH_SHORT).show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    isPinPressed = false
                                    navController.navigate("detalle_pin")
                                }, 200)
                            }

                            // --- Tocar PIN 2 ---
                            isPointInPinArea(
                                x, y,
                                PlanoData.pin2.pinX,
                                PlanoData.pin2.pinY,
                                PlanoData.pin2.pinTapRadiusNormalized
                            ) -> {
                                isPinPressed = true
                                Toast.makeText(context, "Pin 2 pulsado", Toast.LENGTH_SHORT).show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    isPinPressed = false
                                    navController.navigate("detalle_pin2")
                                }, 200)
                            }

                            else -> Toast.makeText(context, "Fuera del área interactiva", Toast.LENGTH_SHORT).show()
                        }
                    }

                    photoViewRef = this
                }
            },
            update = {
                it.interactivePath = activePath
                it.highlightColor = (
                        if (highlightMonasterio != Color.Transparent)
                            highlightMonasterio
                        else highlightIglesia
                        ).toArgb()
                it.invalidate()
            }
        )

        // 🎛️ Controles de zoom y reajuste
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 🔍 Aumentar
            FloatingActionButton(onClick = {
                photoViewRef?.let {
                    val newScale = (it.scale + 0.2f).coerceAtMost(it.maximumScale)
                    it.setScale(newScale, true)
                }
            }) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.7f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.aumentar_zoom),
                        contentDescription = "Aumentar",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                }
            }

            // 🔎 Disminuir
            FloatingActionButton(onClick = {
                photoViewRef?.let {
                    val newScale = (it.scale - 0.2f).coerceAtLeast(it.minimumScale)
                    it.setScale(newScale, true)
                }
            }) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.7f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.disminuir_zoom),
                        contentDescription = "Disminuir",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                }
            }

            // ♻️ Reajustar (vuelve al zoom inicial configurado)
            FloatingActionButton(onClick = {
                photoViewRef?.apply {
                    setScale(initialZoom, true)
                    setTranslationX(0f)
                    setTranslationY(0f)
                }
            }) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.7f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
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
}
