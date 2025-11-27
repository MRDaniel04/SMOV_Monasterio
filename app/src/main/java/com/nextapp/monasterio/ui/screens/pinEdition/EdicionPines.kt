package com.nextapp.monasterio.ui.screens.pinEdition

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.repository.PinRepository
import com.nextapp.monasterio.repository.PlanoRepository
import com.nextapp.monasterio.ui.virtualvisit.components.DebugPhotoView
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned // Necesario para obtener el tamaño de la caja
import androidx.compose.ui.unit.IntSize // Necesario para obtener el tamaño de la caja
import com.nextapp.monasterio.AppRoutes
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.zIndex
import com.nextapp.monasterio.ui.screens.pinCreation.CreacionPinSharedViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextapp.monasterio.ui.screens.pinEdition.components.MovingPinOverlay
import com.nextapp.monasterio.ui.screens.pinEdition.components.PinDetailsPanel
import kotlinx.coroutines.delay
import com.nextapp.monasterio.ui.screens.pinEdition.components.PinEditionToolbar

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


    // ⭐ ESTADOS PARA EL MODO MOVIMIENTO ⭐
    var isPinMoving by remember { mutableStateOf(false) }
    var ignoreNextMatrixChange by remember { mutableStateOf(false) }
    var ignoreMatrixChangesCount by remember { mutableStateOf(0) }
    var pinBeingMoved by remember { mutableStateOf<PinData?>(null) }
    var pinDragOffset by remember { mutableStateOf(Offset.Zero) } // Posición en píxeles de pantalla durante el arrastre
    var pinTapScreenPosition by remember { mutableStateOf<Offset?>(null) } // Posición inicial en píxeles de pantalla al hacer tap/abrir panel
    var photoViewSize by remember { mutableStateOf(IntSize.Zero) } // Tamaño del Box principal

    val parentEntry = remember(navController) {
        try {
            navController.getBackStackEntry("pins_graph")
        } catch (e: Exception) {
            null
        }
    }

    val vm = if (parentEntry != null)
        viewModel<CreacionPinSharedViewModel>(parentEntry)
    else
        viewModel<CreacionPinSharedViewModel>()


    var isNewPinMode by remember { mutableStateOf(false) }  // <- identifica modo mover NUEVO pin


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

    /**
     * 🚀 ESTE ES EL EFECTO CORRECTO:
     * Se activa SOLO cuando photoViewSize cambia.
     * Y SOLO cuando ya hay un tamaño válido.
     */
    LaunchedEffect(vm.formSubmitted) {
        if (!vm.formSubmitted) return@LaunchedEffect

        // Esperar 1 frame para que photoViewSize tenga valor real
        delay(50)

        if (photoViewSize.width == 0 || photoViewSize.height == 0) {
            Log.e("EdicionPines", "Tamaño aún no disponible, reintentando…")
            return@LaunchedEffect
        }

        Log.w("EdicionPines", "CHECKPOINT → vm.formSubmitted = TRUE → Activando modo mover")

        vm.formSubmitted = false

        selectedPin = null
        photoViewRef?.translationX = 0f
        photoViewRef?.translationY = 0f

        isNewPinMode = true

        // Crear pin en movimiento
        pinBeingMoved = PinData(
            id = "temp",
            titulo = vm.titulo.es,
            tituloIngles = vm.titulo.en,
            tituloAleman = vm.titulo.de,
            x = 0.5f,
            y = 0.5f,
            iconRes = R.drawable.pin3,
            imagenes = vm.imagenes.uris.map { it.toString() },
            descripcion = vm.descripcion.es
        )

        pinDragOffset = Offset(
            x = photoViewSize.width / 2f,
            y = photoViewSize.height / 2f
        )

        isPinMoving = true
        ignoreNextMatrixChange = true

// Ignorar matrix-changes durante 500ms
        scope.launch {
            ignoreNextMatrixChange = true
            delay(500)
            ignoreNextMatrixChange = false
        }

        Log.e("MOVER_PIN", "🚀 ACTIVADO modo mover pin. ignoreNextMatrixChange=TRUE")
        Log.d("EdicionPines", "¡MODO MOVER PIN ACTIVADO!")
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            // ⭐ MODIFICACIÓN: Capturar el tamaño del Box ⭐
            .onGloballyPositioned { coordinates ->
                photoViewSize = coordinates.size
            }
    ) {

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando plano…")
            }
            return@Box
        }

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
                        Log.d("EdicionPines", "Plano alineado al final (abajo) usando FIT_END.")
                    }
                }.also { photoViewRef = it }
            },
            update = { photoView ->

                photoView.pins =
                    if (isPinMoving) emptyList() else pines.map { pin -> // Cambio 'it' a 'pin' para claridad
                        // 1. Cálculo del color base
                        // Usa pin.color del modelo (Compose Color) y lo convierte a Android Int.
                        // Si pin.color es nulo, usa android.graphics.Color.RED como valor por defecto.
                        val baseColorInt = pin.color?.toArgb() ?: android.graphics.Color.RED

                        DebugPhotoView.PinData(
                            x = pin.x,
                            y = pin.y,
                            // Utilizamos pin.iconRes si lo tiene, si no, mantenemos el pin3 por defecto
                            iconId = pin.iconRes ?: R.drawable.pin3,
                            isPressed = pin.id == selectedPin?.id,
                            // ⭐ 2. Estado de movimiento (Gestionado localmente en Compose)
                            isMoving = pin.id == pinBeingMoved?.id,
                            // ⭐ 3. Color Base
                            pinColor = baseColorInt
                        )
                    }

                // ⭐ LÓGICA: OCULTAR PANEL AL DESPLAZAR/HACER ZOOM (PAN/MATRIX CHANGE) ⭐
                photoView.attacher.setOnMatrixChangeListener {
                    if (ignoreNextMatrixChange) {
                        Log.e("MATRIX", "🟩 MatrixChange ignorado por TIEMPO (protección al activar modo mover)")
                        return@setOnMatrixChangeListener
                    }

                    if (isPinMoving || selectedPin != null) {
                        Log.e("MATRIX", "❌ MatrixChange REAL → cancelando modo mover/panel")
                        isPinMoving = false
                        selectedPin = null
                        return@setOnMatrixChangeListener
                    }
                }



                photoView.setOnPhotoTapListener { _, tapX, tapY ->

                    // ⭐ ADICIÓN: Ignorar el tap si está en modo movimiento ⭐
                    if (isPinMoving) {
                        Log.d("EdicionPines", "Tap ignorado: Pin en movimiento.")
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
                        if (selectedPin != null || photoView.translationY != 0f || photoView.translationX != 0f) {
                            photoViewRef?.translationY = 0f
                            photoViewRef?.translationX = 0f // ⭐ Restaurar desplazamiento HORIZONTAL
                            Log.d(
                                "EdicionPines",
                                "Restaurando translationY/X a 0f antes de aplicar nuevo shift."
                            )
                        }

                        pinTapScreenPosition = Offset(pinScreenXCoord, pinScreenYCoord)
                        selectedPin = touchedPin

                        scope.launch {
                            val fullPin = PinRepository.getPinById(touchedPin!!.id)
                            if (fullPin != null) {
                                // Reemplazamos el pin básico por el pin completo (con imágenes)
                                selectedPin = fullPin
                                Log.d(
                                    "EdicionPines",
                                    "✅ Pin completo cargado. Imágenes detalladas: ${fullPin.imagenesDetalladas.size}"
                                )
                            } else {
                                Log.e(
                                    "EdicionPines",
                                    "❌ No se pudo cargar el pin detallado: ${touchedPin!!.id}"
                                )
                                selectedPin = null // Ocultar el panel si falla
                            }
                        }


                        // --- GESTIÓN DEL DESPLAZAMIENTO HORIZONTAL (Centralización)

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

                            val totalShift = if (neededShiftX != 0f) "X:${
                                String.format(
                                    "%.0f",
                                    neededShiftX
                                )
                            } / Y:${
                                String.format(
                                    "%.0f",
                                    neededShiftY
                                )
                            }" else "Y:${String.format("%.0f", neededShiftY)}"

                            Log.w(
                                "EdicionPines",
                                "Pin oculto. Desplazando Y: -${
                                    String.format(
                                        "%.0f",
                                        neededShiftY
                                    )
                                }px."
                            )
                        } else if (neededShiftX != 0f) {
                            // Mostrar Toast solo si se movió en X
                            Toast.makeText(
                                context,
                                "Desplazando plano: X:${String.format("%.0f", neededShiftX)} px",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        // TOQUE FUERA DEL PIN: Oculta el panel y RESTAURA AMBOS DESPLAZAMIENTOS
                        if (selectedPin != null) {
                            Log.d(
                                "EdicionPines",
                                "❌ Toque estático fuera de Pin. Ocultando panel y RESTAURANDO POSICIONES."
                            )

                            // RESTAURAR AMBOS DESPLAZAMIENTOS (translationY = 0, translationX = 0)
                            if (photoView.translationY != 0f || photoView.translationX != 0f) {
                                photoViewRef?.translationY = 0f
                                photoViewRef?.translationX =
                                    0f // ⭐ Restaurar desplazamiento HORIZONTAL
                                Log.d("EdicionPines", "Restaurando translationY/X a 0f.")
                            }

                            selectedPin = null
                        } else {
                            Log.d(
                                "EdicionPines",
                                "Toque fuera y no había Pin seleccionado. Ignorando."
                            )
                        }
                    }

                    photoView.invalidate() // Forzar redibujo
                }
                photoView.invalidate()
            }
        )

        // -------------------------
        // ⭐ PANEL INFORMATIVO (Estructura fija/scroll de descripción) ⭐
        // -------------------------
        selectedPin?.let { pin ->
            PinDetailsPanel(
                modifier = Modifier
                    .zIndex(50f)
                    .align(Alignment.BottomCenter),
                selectedPin = pin,
                imagenesDetalladas = pin.imagenesDetalladas,
                pinTapScreenPosition = pinTapScreenPosition,
                panelHeightFraction = PANEL_HEIGHT_FRACTION,

                onClosePanel = {
                    // Lógica para cerrar el panel (Hoisting)
                    photoViewRef?.translationY = 0f
                    photoViewRef?.translationX = 0f
                    selectedPin = null
                },

                onStartMove = { movedPin, initialPos ->
                    // Lógica para iniciar el modo mover (Hoisting)
                    Log.d("EdicionPines", "Iniciando modo Mover Pin para ID: ${movedPin.id}")
                    pinBeingMoved = movedPin
                    pinDragOffset = initialPos
                    selectedPin = null
                    isPinMoving = true
                    photoViewRef?.translationY = 0f
                    photoViewRef?.translationX = 0f
                    Toast.makeText(context, "Modo Mover Pin activado. Arrastre.", Toast.LENGTH_LONG)
                        .show()
                },

                onEdit = {
                    // Lógica de edición
                    Toast.makeText(context, "Editar Pin", Toast.LENGTH_SHORT).show()
                }

            )
        }

        // -------------------------
        // ⭐ ADICIÓN: OVERLAY DE PIN EN MOVIMIENTO ⭐
        // -------------------------
        if (isPinMoving && pinBeingMoved != null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(999f)   // 👈 SUPER IMPORTANTE
            ) {
                MovingPinOverlay(
                    pinData = pinBeingMoved!!,
                    initialOffset = pinDragOffset,

                    isPressed = true, // ⭐ ADICIÓN CLAVE: Fuerza el color verde
                    onPinDrag = { newOffset ->
                        // Guardamos la nueva posición de pantalla
                        pinDragOffset = newOffset
                    },
                    onCancel = {

                        if (isNewPinMode) {
                            vm.formSubmitted = false
                            // Volver al formulario con estados preservados
                            navController.navigate(AppRoutes.CREACION_PINES)
                            isNewPinMode = false
                        }
                        // Acción de Cancelar: Restaurar estado anterior
                        isPinMoving = false
                        selectedPin = pinBeingMoved // Vuelve a abrir el panel con el pin original
                        pinBeingMoved = null
                        pinDragOffset = Offset.Zero
                        Toast.makeText(
                            context,
                            "Movimiento cancelado. Pin restaurado.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onConfirm = {
                        val currentScreenPos = pinDragOffset
                        var normalizedCoords: PointF? = null

                        photoViewRef?.let { photoView ->
                            // La punta del pin es la posición de arrastre
                            normalizedCoords = photoView.getNormalizedImageCoords(
                                screenX = currentScreenPos.x,
                                screenY = currentScreenPos.y
                            )
                        }

                        if (normalizedCoords != null) {
                            val newX = normalizedCoords!!.x
                            val newY = normalizedCoords!!.y
                            val pinToUpdate = pinBeingMoved!! // Guardamos una referencia

                            // ⭐ LÓGICA DE ACTUALIZACIÓN EN FIREBASE ⭐
                            scope.launch {
                                try {
                                    PinRepository.updatePinPosition(
                                        pinId = pinToUpdate.id,
                                        newX = newX,
                                        newY = newY
                                    )

                                    // Actualizar el estado local (para forzar el redibujo del mapa)
                                    pines = pines.map { pin ->
                                        if (pin.id == pinToUpdate.id) {
                                            pin.copy(x = newX, y = newY)
                                        } else {
                                            pin
                                        }
                                    }


                                } catch (e: Exception) {
                                    Log.e(
                                        "EdicionPines",
                                        "Error al guardar posición en Firebase",
                                        e
                                    )
                                }
                            }

                        } else {
                            Log.e("EdicionPines", "Error al convertir coordenadas a normalizadas.")
                        }

                        if (isNewPinMode) {
                            val newX = normalizedCoords!!.x
                            val newY = normalizedCoords!!.y

                            scope.launch {
                                val newPinId = PinRepository.createPinFromForm(
                                    titulo = vm.titulo.es,
                                    descripcion = vm.descripcion.es,
                                    imagenes = vm.imagenes.uris.map { it.toString() },
                                    imagenes360 = vm.imagenes360.uris.map { it.toString() },
                                    ubicacion = vm.ubicacion.displayName,
                                    x = newX,
                                    y = newY
                                )


                                // ⭐ Añadir la referencia del pin al plano del monasterio
                                PlanoRepository.addPinToPlano(
                                    planoId = "monasterio_interior",
                                    pinId = newPinId
                                )


                                // Recargar pantalla
                                val plano = PlanoRepository.getPlanoById("monasterio_interior")
                                val allPins = PinRepository.getAllPins()
                                val pinRefs =
                                    plano?.pines?.map { it.substringAfterLast("/") } ?: emptyList()
                                pines = allPins.filter { pinRefs.contains(it.id) }
                            }

                            vm.formSubmitted = false

                            // Salir del modo
                            isNewPinMode = false
                            isPinMoving = false
                            pinBeingMoved = null

                            return@MovingPinOverlay
                        }


                        // 2. Salir del modo movimiento
                        isPinMoving = false
                        // No reabrimos el panel para forzar la actualización del mapa con las nuevas coordenadas.
                        selectedPin = null
                        pinBeingMoved = null
                        pinDragOffset = Offset.Zero
                    },
                    boxSize = photoViewSize
                )
            }
        }

        // -------------------------
        // ⭐ 1. BOTÓN DE ATRÁS (Alineado a TopStart) ⭐
        // -------------------------
        Box(
            modifier = Modifier
                .align(Alignment.TopStart) // Alineado a la esquina superior izquierda
                .zIndex(100f) // Aseguramos que esté por encima del mapa
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xBB000000)) // Fondo semi-transparente
        ) {
            IconButton(
                onClick = {
                    if (rootNavController != null) rootNavController.popBackStack()
                    else navController.popBackStack()
                },
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    // Asumo que tienes R.drawable.arrow_back o similar
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        }

        // -------------------------
        // ⭐ 2. TOOLBAR DE EDICIÓN (Alineado a TopEnd - Componente Extraído) ⭐
        // -------------------------
        PinEditionToolbar(
            onPinAddClick = {
                // Lógica de Añadir Pin (Preservada)
                vm.reset()
                navController.navigate(AppRoutes.CREACION_PINES)
            },
            onCrosshairClick = {
                // LÓGICA DE REAJUSTE (Preservada, utiliza photoViewRef)
                Log.d(
                    "EdicionPines",
                    "Botón Reajustar Plano pulsado. Restaurando posición inicial."
                )
                selectedPin = null
                isPinMoving = false

                photoViewRef?.let { photoView ->
                    photoView.attacher.setScale(1f, true)
                    photoView.attacher.setRotationTo(0f)
                    photoView.attacher.setScaleType(ImageView.ScaleType.FIT_END)
                    photoView.translationY = 0f
                    photoView.translationX = 0f

                    Toast.makeText(
                        context,
                        "Plano reajustado a la posición inicial.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onCancelEditClick = {
                Toast.makeText(context, "Bloquear Edición", Toast.LENGTH_SHORT).show()
            },
            onHelpClick = {
                Toast.makeText(context, "Mostrar Ayuda", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.align(Alignment.TopEnd).zIndex(100f) // Alineado a la derecha
        )
    }
}





