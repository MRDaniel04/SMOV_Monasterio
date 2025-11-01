package com.nextapp.monasterio.ui.virtualvisit.screens

import android.graphics.Path
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

    // 🔹 Mostrar el colegio al inicio para verificar su posición
    var activeHighlight by remember { mutableStateOf<Color?>(Color(0x804CAF50)) } // verde semitransparente
    var activePath by remember { mutableStateOf<Path?>(PlanoData.figuras.find { it.id == "colegio" }?.path) }

    var isPinPressed by remember { mutableStateOf(false) }

    val planoBackgroundColor = Color(0xFFF5F5F5)
    val initialZoom = 1.5f // Zoom inicial del plano

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(planoBackgroundColor)
    ) {
        var photoViewRef by remember { mutableStateOf<DebugPhotoView?>(null) }

        // 🖼️ Componente principal del plano interactivo
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                DebugPhotoView(ctx).apply {
                    // Imagen base del plano
                    setImageResource(R.drawable.plano_monasterio)

                    // Aplica zoom inicial tras la carga
                    post {
                        setScale(initialZoom, true)
                    }

                    // Asigna los pines desde la lista dinámica
                    pins = PlanoData.pines.map {
                        DebugPhotoView.PinData(
                            x = it.x,
                            y = it.y,
                            iconId = it.iconRes,
                            isPressed = isPinPressed
                        )
                    }

                    // 🔹 Detección de toques en figuras o pines
                    setOnPhotoTapListener { _, x, y ->
                        val figura = PlanoData.figuras.find { isPointInPath(x, y, it.path) }
                        val pin = PlanoData.pines.find {
                            isPointInPinArea(x, y, it.x, it.y, it.tapRadius)
                        }

                        when {
                            // --- FIGURA TOCADA ---
                            figura != null -> {
                                activePath = figura.path
                                activeHighlight = Color(figura.colorResaltado)

                                Handler(Looper.getMainLooper()).postDelayed({
                                    activeHighlight = null
                                    navController.navigate(figura.destino)
                                }, 200)
                            }

                            // --- PIN TOCADO ---
                            pin != null -> {
                                isPinPressed = true
                                Toast.makeText(context, "${pin.id} pulsado", Toast.LENGTH_SHORT).show()

                                Handler(Looper.getMainLooper()).postDelayed({
                                    isPinPressed = false
                                    navController.navigate(pin.destino)
                                }, 200)
                            }

                            else -> Toast.makeText(
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

        // 🎛️ Controles flotantes (zoom y reajuste)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 🔍 Aumentar zoom
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

            // 🔎 Disminuir zoom
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

            // ♻️ Reajustar (volver al zoom inicial)
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
