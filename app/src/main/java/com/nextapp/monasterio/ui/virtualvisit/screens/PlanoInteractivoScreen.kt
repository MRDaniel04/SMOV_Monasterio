package com.nextapp.monasterio.ui.virtualvisit.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape // <-- Import necesario
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // <-- ¡IMPORT AÑADIDO!
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
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.models.PinData // Importamos el modelo
import androidx.navigation.NavHostController
// (Se ha eliminado el import erróneo de rootNavController)

@Composable
fun PlanoInteractivoScreen(
    navController: NavController,
    rootNavController: NavHostController? = null
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {

        }
    }

    var activePath by remember { mutableStateOf<android.graphics.Path?>(null) }
    var activeHighlight by remember { mutableStateOf<Color?>(null) }
    var isPinPressed by remember { mutableStateOf(false) }

    val planoBackgroundColor = Color(0xFFF5F5F5)
    val initialZoom = 1.5f

    val infiniteTransition = rememberInfiniteTransition(label="BlinkTransition")
    val blinkalpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation=tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

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

                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )

                    setImageResource(R.drawable.plano_monasterio)

                    post { setScale(initialZoom, true) }

                    pins = PlanoData.pines.map {
                        DebugPhotoView.PinData(
                            x = it.x,
                            y = it.y,
                            iconId = it.iconRes ?: 0,
                            isPressed = isPinPressed
                        )
                    }

                    staticZones = PlanoData.figuras.map{figura->
                        DebugPhotoView.StaticZoneData(
                            path = figura.path,
                            color=figura.colorResaltado
                        )
                    }

                    setOnPhotoTapListener { _, x, y ->
                        val figura = PlanoData.figuras.find { isPointInPath(x, y, it.path) }

                        // Buscamos el objeto PinData completo
                        val pin: PinData? = PlanoData.pines.find {
                            isPointInPinArea(x, y, it.x, it.y, it.tapRadius)
                        }

                        when {
                            figura != null -> {
                                activePath = figura.path
                                activeHighlight = Color(figura.colorResaltado)

                                Handler(Looper.getMainLooper()).postDelayed({
                                    activeHighlight = null
                                    figura.destino.let { destino ->
                                        try {
                                            navController.navigate(destino)
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Pantalla no encontrada: $destino $e",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }, 200)
                            }

                            pin != null -> {
                                isPinPressed = true
                                // Toast.makeText(context, "${pin.id} pulsado", Toast.LENGTH_SHORT).show()

                                Handler(Looper.getMainLooper()).postDelayed({
                                    isPinPressed = false

                                    // Navegamos con el navController local
                                    navController.navigate(AppRoutes.PIN_DETALLE + "/${pin.id}")

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
                it.blinkingAlpha = blinkalpha
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
        } // --- FIN DE LA COLUMN ---

        // --- ¡¡BOTÓN DE "ATRÁS" CORREGIDO!! ---
        IconButton(
            onClick = { navController.popBackStack() }, // Vuelve atrás en el navegador local
            modifier = Modifier
                .align(Alignment.TopStart) // Arriba a la izquierda
                .statusBarsPadding() // Para que no se ponga debajo de la barra de estado
                .padding(16.dp) // Margen
                .background(
                    color = Color.Black.copy(alpha = 0.5f), // Fondo negro semitransparente
                    shape = RoundedCornerShape(12.dp) // Esquinas redondeadas
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Volver",
                tint = Color.White // Flecha blanca
            )
        }
    } // --- FIN DEL BOX ---
}