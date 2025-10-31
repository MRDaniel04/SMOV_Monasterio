package com.nextapp.monasterio.ui.virtualvisit.screens

/**
 * 🔹 Pantalla interactiva del plano.
 *
 * Muestra una imagen del plano del monasterio con zonas táctiles y pines interactivos.
 * Al pulsar sobre una figura (Path) o un pin, se realiza una acción (navegación o feedback visual).
 *
 * Se apoya en:
 *   - `DebugPhotoView`: para manejar el lienzo interactivo y los toques.
 *   - `PlanoData`: donde se definen las coordenadas y el Path del plano.
 *   - `PathUtils`: utilidades matemáticas para detección de toques.
 *
 * Navegación:
 *   - Figura → DetalleFiguraScreen
 *   - Pin → DetallePinScreen
 *
 */


import com.nextapp.monasterio.ui.virtualvisit.data.PlanoData
import com.nextapp.monasterio.ui.virtualvisit.utils.isPointInPath
import com.nextapp.monasterio.ui.virtualvisit.utils.isPointInPinArea
import com.nextapp.monasterio.R
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.*
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import androidx.navigation.NavController
import com.nextapp.monasterio.ui.virtualvisit.components.DebugPhotoView


@Composable
fun PlanoInteractivoScreen(navController: NavController) {
    val context = LocalContext.current
    val data = PlanoData.monasterio

    var highlightColor by remember { mutableStateOf(Color.TRANSPARENT) }
    var isPinPressed by remember { mutableStateOf(false) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            DebugPhotoView(it).apply {
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
                            highlightColor = Color.YELLOW
                            Handler(Looper.getMainLooper()).postDelayed({
                                highlightColor = Color.TRANSPARENT
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
            }
        },
        update = {
            it.highlightColor = highlightColor
            it.invalidate()
        }
    )
}



