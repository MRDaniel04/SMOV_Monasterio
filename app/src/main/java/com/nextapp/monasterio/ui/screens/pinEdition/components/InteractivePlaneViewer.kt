package com.nextapp.monasterio.ui.screens.pinEdition.components

import android.graphics.Matrix
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.ui.virtualvisit.components.DebugPhotoView

@Composable
fun InteractivePlanoViewer(
    planoUrl: String,
    pines: List<PinData>,
    isPinMoving: Boolean,
    selectedPin: PinData?,
    pinBeingMoved: PinData?,
    ignoreNextMatrixChange: Boolean,

    // Callbacks (Eventos que se envían de vuelta al padre)
    onRefReady: (DebugPhotoView) -> Unit, // Para pasar la referencia del PhotoView
    onSizeChange: (IntSize) -> Unit, // Para notificar el tamaño del PhotoView al padre
    onMatrixChange: () -> Unit, // Para notificar que se ha hecho pan/zoom (ocultar panel/modo mover)
    onPinTap: (PinData, Float, Float) -> Unit, // Pin tocado + coordenadas de pantalla (X, Y)
    onBackgroundTap: () -> Unit // Toque fuera de un pin
) {
    val context = LocalContext.current
    val photoViewRef = remember { mutableStateOf<DebugPhotoView?>(null) }
    val CENTRALIZATION_THRESHOLD = 0.15f
    val PANEL_HEIGHT_FRACTION = 0.50f
    val pinMarginDp = 80.dp
    val density = context.resources.displayMetrics.density
    val pinMarginPx = pinMarginDp.value * density

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
                    attacher.scaleType = ImageView.ScaleType.FIT_END
                    Log.d("InteractivePlanoViewer", "Plano alineado al final (abajo) usando FIT_END.")
                }
            }.also {
                photoViewRef.value = it
                onRefReady(it) // Devolvemos la referencia al padre
            }
        },
        update = { photoView ->

            // 1. Notificar el tamaño del PhotoView al padre
            val currentSize = IntSize(photoView.width, photoView.height)
            if (currentSize.width > 0 && currentSize.height > 0) {
                onSizeChange(currentSize)
            }

            // 2. Lógica de Pins (incluyendo estados)
            photoView.pins = pines.map { pin ->

                // --- LÓGICA DE COLOR SIMPLIFICADA PARA MODO MOVIMIENTO ---
                val color: Color = when {

                    // 1. Si este es el pin que está siendo movido (creado o reubicado):
                    // Lo hacemos transparente para que solo se vea el pin amarillo flotante.
                    pin.id == pinBeingMoved?.id -> Color.Transparent

                    // 2. Todos los demás pines estáticos: ROJO
                    else -> Color.Red
                }

                val baseColorInt = color.toArgb()

                DebugPhotoView.PinData(
                    x = pin.x,
                    y = pin.y,
                    iconId = pin.iconRes ?: R.drawable.pin3,

                    // Mantenemos 'isPressed' en false si el pin está siendo movido,
                    // y usamos la propiedad pinColor para el color.
                    isPressed = false,
                    isMoving = false,

                    pinColor = baseColorInt // Aplicamos el color (Rojo o Transparente)
                )
            }
            // 3. Lógica: OCULTAR PANEL AL DESPLAZAR/HACER ZOOM (MATRIX CHANGE)
            photoView.attacher.setOnMatrixChangeListener {
                if (ignoreNextMatrixChange) {
                    Log.e("MATRIX", "🟩 MatrixChange ignorado por TIEMPO (protección al activar modo mover)")
                    return@setOnMatrixChangeListener
                }

                if (isPinMoving || selectedPin != null) {
                    onMatrixChange() // Notifica al padre que cancele el modo mover/panel
                    return@setOnMatrixChangeListener
                }
            }


            // 4. Lógica: DETECCIÓN DE TAP SOBRE PINES
            photoView.setOnPhotoTapListener { _, tapX, tapY ->

                if (isPinMoving) {
                    Log.d("InteractivePlanoViewer", "Tap ignorado: Pin en movimiento.")
                    return@setOnPhotoTapListener
                }
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
                var pinScreenXCoord = 0f

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
                        pinScreenXCoord = pinScreenX
                        return@forEach
                    }
                }

                if (touchedPin != null) {
                    // Si ya había un pin seleccionado o el plano está desplazado, lo reseteamos antes
                    if (selectedPin != null || photoView.translationY != 0f || photoView.translationX != 0f) {
                        photoViewRef.value?.translationY = 0f
                        photoViewRef.value?.translationX = 0f
                        Log.d(
                            "InteractivePlanoViewer",
                            "Restaurando translationY/X a 0f antes de aplicar nuevo shift."
                        )
                    }

                    // ⭐ AVISO AL PADRE: Se ha tocado un pin
                    // El padre (EdicionPines) se encargará de cargar los datos completos y actualizar selectedPin
                    onPinTap(touchedPin!!, pinScreenXCoord, pinScreenYCoord)


                    // --- GESTIÓN DEL DESPLAZAMIENTO HORIZONTAL (Centralización)
                    val screenWidth = photoView.width.toFloat()
                    val targetCenter = screenWidth / 2f
                    val thresholdPx = screenWidth * CENTRALIZATION_THRESHOLD

                    var neededShiftX = 0f

                    // Borde Izquierdo (0% al 15%)
                    if (pinScreenXCoord < thresholdPx) {
                        neededShiftX = targetCenter - pinScreenXCoord
                        // Borde Derecho (85% al 100%)
                    } else if (pinScreenXCoord > (screenWidth - thresholdPx)) {
                        neededShiftX = targetCenter - pinScreenXCoord
                    }

                    if (neededShiftX != 0f) {
                        photoViewRef.value?.moveHorizontalFree(neededShiftX)
                    }

                    // --- GESTIÓN DEL DESPLAZAMIENTO VERTICAL ---
                    val panelHeightPx = photoView.height * PANEL_HEIGHT_FRACTION
                    val pinTargetY = photoView.height - panelHeightPx - pinMarginPx

                    if (pinScreenYCoord > pinTargetY) {
                        val neededShiftY = pinScreenYCoord - pinTargetY
                        photoViewRef.value?.moveVerticalFree(-neededShiftY)

                        Log.w(
                            "InteractivePlanoViewer",
                            "Pin oculto. Desplazando Y: -${String.format("%.0f", neededShiftY)}px."
                        )
                    } else if (neededShiftX != 0f) {
                        Toast.makeText(
                            context,
                            "Desplazando plano: X:${String.format("%.0f", neededShiftX)} px",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    // ⭐ AVISO AL PADRE: Toque fuera de un pin
                    onBackgroundTap()
                }
                photoView.invalidate()
            }
            photoView.invalidate()
        }
    )

}