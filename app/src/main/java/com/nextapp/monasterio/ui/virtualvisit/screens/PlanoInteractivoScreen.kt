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
    val data = PlanoData.monasterio

    var highlightColor by remember { mutableStateOf(Color.Transparent) }
    var isPinPressed by remember { mutableStateOf(false) }
    val planoBackgroundColor = Color(0xFFF5F5F5)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(planoBackgroundColor) // Fondo detrás del plano
    ) {
        var photoViewRef by remember { mutableStateOf<DebugPhotoView?>(null) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                DebugPhotoView(ctx).apply {
                    setImageResource(R.drawable.plano_monasterio)
                    interactivePath = data.path
                    pins = listOf(
                        DebugPhotoView.PinData(
                            x = data.pinX,
                            y = data.pinY,
                            iconId = R.drawable.pin3,
                            isPressed = isPinPressed
                        )
                    )
                    setOnPhotoTapListener { _, x, y ->
                        when {
                            isPointInPath(x, y, data.path) -> {
                                highlightColor = Color.Yellow
                                Handler(Looper.getMainLooper()).postDelayed({
                                    highlightColor = Color.Transparent
                                    navController.navigate("detalle_figura")
                                }, 100)
                            }

                            isPointInPinArea(x, y, data.pinX, data.pinY, data.pinTapRadiusNormalized) -> {
                                isPinPressed = true
                                Toast.makeText(context, "Pin pulsado", Toast.LENGTH_SHORT).show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    isPinPressed = false
                                    navController.navigate("detalle_pin")
                                }, 100)
                            }

                            else -> Toast.makeText(context, "Fuera del área interactiva", Toast.LENGTH_SHORT).show()
                        }
                    }

                    photoViewRef = this
                }
            },
            update = {
                it.highlightColor = highlightColor.toArgb()
                it.invalidate()
            }
        )

        // 🎛️ Botones de control con fondo circular semitransparente
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón Aumentar
            FloatingActionButton(onClick = {
                photoViewRef?.let {
                    val currentScale = it.scale
                    val newScale = (currentScale + 0.2f).coerceAtMost(it.maximumScale)
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

            // Botón Disminuir
            FloatingActionButton(onClick = {
                photoViewRef?.let {
                    val currentScale = it.scale
                    val newScale = (currentScale - 0.2f).coerceAtLeast(it.minimumScale)
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

            // Botón Reajustar
            FloatingActionButton(onClick = {
                photoViewRef?.let {
                    it.setScale(1f, true)
                    it.setTranslationX(0f)
                    it.setTranslationY(0f)
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