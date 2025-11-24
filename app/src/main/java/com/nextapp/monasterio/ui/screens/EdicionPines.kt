package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
// Nota: Se asume la existencia de PinRepository y PlanoRepository
import com.nextapp.monasterio.repository.PinRepository
import com.nextapp.monasterio.repository.PlanoRepository
// Nota: Se asume la existencia de DebugPhotoView
import com.nextapp.monasterio.ui.virtualvisit.components.DebugPhotoView
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned // Necesario para obtener el tamaño de la caja
import androidx.compose.ui.unit.IntSize // Necesario para obtener el tamaño de la caja
import androidx.compose.ui.platform.LocalDensity
import com.nextapp.monasterio.AppRoutes

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
    var pinBeingMoved by remember { mutableStateOf<PinData?>(null) }
    var pinDragOffset by remember { mutableStateOf(Offset.Zero) } // Posición en píxeles de pantalla durante el arrastre
    var pinTapScreenPosition by remember { mutableStateOf<Offset?>(null) } // Posición inicial en píxeles de pantalla al hacer tap/abrir panel
    var photoViewSize by remember { mutableStateOf(IntSize.Zero) } // Tamaño del Box principal
    var isDeleteDialogOpen by remember { mutableStateOf(false) }

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


                Log.d("DIAG_PINES", "Total de pines cargados: ${pines.size}")
                val pinConImagenes = pines.firstOrNull { it.imagenesDetalladas.isNotEmpty() }
                if (pinConImagenes != null) {
                    Log.d("DIAG_PINES", "✅ Pin con ID '${pinConImagenes.id}' tiene ${pinConImagenes.imagenesDetalladas.size} imágenes detalladas.")
                } else {
                    Log.e("DIAG_PINES", "❌ Ningún pin cargado contiene imágenes detalladas. Revisar el repositorio o el filtro.")
                }

            } catch (e: Exception) {
                Log.e("EdicionPines", "❌ Error al cargar plano/pines", e)
            } finally {
                isLoading = false
            }
        }
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
                        attacher.scaleType = android.widget.ImageView.ScaleType.FIT_END
                        Log.d("EdicionPines", "Plano alineado al final (abajo) usando FIT_END.")
                    }
                }.also { photoViewRef = it }
            },
            update = { photoView ->

                photoView.pins = if (isPinMoving) emptyList() else pines.map {
                    DebugPhotoView.PinData(
                        x = it.x,
                        y = it.y,
                        iconId = R.drawable.pin3,
                        isPressed = it.id == selectedPin?.id
                    )
                }

                // ⭐ LÓGICA: OCULTAR PANEL AL DESPLAZAR/HACER ZOOM (PAN/MATRIX CHANGE) ⭐
                photoView.attacher.setOnMatrixChangeListener {
                    if (selectedPin != null || isPinMoving) { // ⭐ MODIFICACIÓN: Añadir || isPinMoving
                        Log.d(
                            "EdicionPines",
                            "Matrix cambió (Pan/Zoom detectado). Cancelando modo/Ocultando panel."
                        )
                        selectedPin = null
                        isPinMoving = false // ⭐ ADICIÓN: Cancelar el modo mover si hay zoom/pan

                        if (photoView.translationY != 0f) {
                            photoView.translationY = 0f
                            Log.d(
                                "EdicionPines",
                                "Restaurando translationY a 0f tras desplazamiento manual."
                            )
                        }
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

                            // Mostrar Toast combinado
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
        if (selectedPin != null) {

            val imagenesDetalladas =
                selectedPin!!.imagenesDetalladas // Asumimos que esta lista contiene objetos ImagenData
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(PANEL_HEIGHT_FRACTION)
                    .background(
                        Color.White,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)

                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            Log.d("EdicionPines", "Toque en el panel consumido.")
                        }
                    )
            ) {

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween, // Alineación izquierda y derecha
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Grupo Izquierdo: Acciones de Edición (Mover, Editar, BORRAR)
                        Row(
                            // 💡 CAMBIO 2: Ampliar el espaciado
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 💡 CAMBIO 1: Botón 1: Mover Pin (Ahora es el primero)
                            IconButton(onClick = {
                                selectedPin?.let { pin ->
                                    val initialScreenPos = pinTapScreenPosition
                                    if (initialScreenPos == null) {
                                        Toast.makeText(context, "Error: No se encontró la posición inicial del pin.", Toast.LENGTH_SHORT).show()
                                        return@IconButton
                                    }

                                    Log.d("EdicionPines", "Iniciando modo Mover Pin para ID: ${pin.id}")
                                    pinBeingMoved = pin
                                    pinDragOffset = initialScreenPos
                                    selectedPin = null
                                    isPinMoving = true
                                    photoViewRef?.translationY = 0f
                                    photoViewRef?.translationX = 0f

                                    Toast.makeText(context, "Modo Mover Pin activado. Arrastre.", Toast.LENGTH_LONG).show()
                                }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_move), // Asume el icono de mover
                                    contentDescription = "Mover Pin"
                                )
                            }

                            // 💡 CAMBIO 1: Botón 2: Editar Pin (Ahora es el segundo)
                            IconButton(onClick = {
                                Toast.makeText(context, "Editar Pin", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.lapiz), // Asume el icono de lápiz/editar
                                    contentDescription = "Editar Pin"
                                )
                            }

                            // 💡 CAMBIO 1: Botón 3: Borrar Pin (Ahora es el tercero)
                            IconButton(onClick = {
                                // 💡 CAMBIO 3: Mostrar el diálogo de confirmación
                                selectedPin?.let { pin ->
                                    isDeleteDialogOpen = true
                                }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_trash), // Asume el icono de borrar/papelera
                                    contentDescription = "Borrar Pin",
                                    tint = Color(0xFFFF5722) // Rojo para advertencia
                                )
                            }
                        }

                        // Grupo Derecho: Cerrar Panel (Acción de UI)
                        IconButton(onClick = {
                            photoViewRef?.translationY = 0f
                            photoViewRef?.translationX = 0f
                            selectedPin = null // Cierra el panel
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close_24), // Asume el icono de cerrar
                                contentDescription = "Cerrar Panel"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // --- 2. Título del Pin ---
                    Text(
                        text = selectedPin?.titulo ?: "Detalle del Pin",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    val numImages = imagenesDetalladas.size
                    Log.i(
                        "DIAG_PANEL",
                        "Pin seleccionado: ${selectedPin!!.titulo}. Imágenes detalladas: $numImages"
                    )

                    // --- 3. Carrusel de Imágenes (Scroll Horizontal) ---
                    if (imagenesDetalladas.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(end = 16.dp)
                        ) {
                            items(imagenesDetalladas) { imagen -> // Iteramos sobre objetos ImagenData
                                Box(
                                    modifier = Modifier
                                        .size(150.dp) // Tamaño de la celda de la imagen
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = imagen.url, // ⭐ Usamos el campo URL del objeto ImagenData
                                        contentDescription = imagen.etiqueta, // ⭐ Usamos la etiqueta
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Texto de Etiqueta (similar al de tu carrusel original)
                                    Text(
                                        text = imagen.etiqueta, // ⭐ Muestra la etiqueta
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        // Si no hay imágenes detalladas, mostramos un placeholder o espaciador.
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay imágenes detalladas disponibles.",
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = selectedPin?.descripcion ?: "Descripción no disponible.",
                            style = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            color = Color.DarkGray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // -------------------------
        // ⭐ ADICIÓN: OVERLAY DE PIN EN MOVIMIENTO ⭐
        // -------------------------
        if (isPinMoving && pinBeingMoved != null) {
            MovingPinOverlay(
                pinData = pinBeingMoved!!,
                initialOffset = pinDragOffset,
                onPinDrag = { newOffset ->
                    // Guardamos la nueva posición de pantalla
                    pinDragOffset = newOffset
                },
                onCancel = {
                    // Acción de Cancelar: Restaurar estado anterior
                    isPinMoving = false
                    selectedPin = pinBeingMoved // Vuelve a abrir el panel con el pin original
                    pinBeingMoved = null
                    pinDragOffset = Offset.Zero
                    Toast.makeText(context, "Movimiento cancelado. Pin restaurado.", Toast.LENGTH_SHORT).show()
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
                                Log.e("EdicionPines", "Error al guardar posición en Firebase", e)
                            }
                        }

                    } else {
                        Log.e("EdicionPines", "Error al convertir coordenadas a normalizadas.")
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

        // -------------------------
        // ⭐ TOOLBAR SUPERIOR FIJA ⭐
        // -------------------------
        ToolbarEdicionPines(
            onBackClick = {
                if (rootNavController != null) rootNavController.popBackStack()
                else navController.popBackStack()
            },

            onPinAddClick = {
                navController.navigate(AppRoutes.CREACION_PINES)
                Toast.makeText(context, "Navegando a la creación de Pin", Toast.LENGTH_SHORT).show()
            },
            onCrosshairClick = {
                // LÓGICA DE REAJUSTE
                Log.d("EdicionPines", "Botón Reajustar Plano pulsado. Restaurando posición inicial.")

                // 1. Ocultar el panel informativo
                selectedPin = null
                isPinMoving = false // ⭐ ADICIÓN: Cancelar modo mover al reajustar

                // 2. Restaurar la posición y zoom del PhotoView
                photoViewRef?.let { photoView ->

                    photoView.attacher.setScale(1f, true) // Zoom a 1.0 (tamaño original) con animación.

                    photoView.attacher.setRotationTo(0f) // Resetea la rotación
                    photoView.attacher.setScaleType(android.widget.ImageView.ScaleType.FIT_END)

                    // Eliminar traslación manual residual (aplicada para centrar pines)
                    photoView.translationY = 0f
                    photoView.translationX = 0f

                    Toast.makeText(context, "Plano reajustado a la posición inicial.", Toast.LENGTH_SHORT).show()
                }
            },
            onCancelEditClick = {
                Toast.makeText(context, "Bloquear Edición", Toast.LENGTH_SHORT).show()
            },
            onHelpClick = {
                Toast.makeText(context, "Mostrar Ayuda", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Archivo: EdicionPines.kt (al final del composable, pero dentro de su función)

    // ⭐ DIÁLOGO DE CONFIRMACIÓN DE BORRADO PERMANENTE
    if (isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                isDeleteDialogOpen = false // Cerrar el diálogo al pulsar fuera
            },
            title = {
                Text(text = "Confirmar Eliminación")
            },
            text = {
                Text("Estás a punto de eliminar permanentemente el pin '${selectedPin?.titulo ?: "seleccionado"}'. Esta acción no se puede deshacer. ¿Deseas continuar?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleteDialogOpen = false
                        selectedPin?.let { pin ->
                            // Lógica de borrado (la que ya teníamos)
                            scope.launch {
                                try {
                                    if (PinRepository.deletePin(pin.id)) {
                                        Toast.makeText(context, "✅ Pin '${pin.titulo}' borrado.", Toast.LENGTH_LONG).show()
                                        pines = pines.filter { it.id != pin.id }
                                    } else {
                                        Toast.makeText(context, "❌ Error al borrar el pin.", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "❌ Error de conexión al borrar pin.", Toast.LENGTH_LONG).show()
                                }
                                // Cerrar el panel después de intentar borrar
                                selectedPin = null
                                photoViewRef?.translationY = 0f
                                photoViewRef?.translationX = 0f
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)) // Botón de confirmación en color de advertencia
                ) {
                    Text("Eliminar Pin")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        isDeleteDialogOpen = false // Simplemente cerrar
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

}

@Composable
fun MovingPinOverlay(
    pinData: PinData,
    initialOffset: Offset,
    onPinDrag: (Offset) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    boxSize: IntSize
) {
    val context = LocalContext.current
    // 1. OBTENER LA DENSIDAD LOCAL para habilitar .toDp()
    val density = LocalDensity.current

    // La posición actual se mantendrá en pinDragOffset del padre (EdicionPines)
    var currentOffset by remember { mutableStateOf(initialOffset) }

    // Sincronizar el offset con el valor del estado padre
    LaunchedEffect(initialOffset) {
        currentOffset = initialOffset
    }

    val pinIconSize = 48.dp
    val buttonSize = 40.dp
    // Corrección para que la punta del pin (no el centro) esté en currentOffset
    val pinOffsetCorrection = pinIconSize / 2

    // Usamos Box para dibujar el icono y los botones, y detectamos el arrastre sobre el área completa
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newOffset = currentOffset.plus(dragAmount)

                    // Lógica para mantener el pin dentro de los límites visibles de la pantalla (usando px)
                    val density = context.resources.displayMetrics.density
                    val safeAreaDp = 60.dp.value
                    val safeAreaPx = safeAreaDp * density

                    val boundedX = newOffset.x.coerceIn(0f + safeAreaPx, boxSize.width.toFloat() - safeAreaPx)
                    val boundedY = newOffset.y.coerceIn(0f + safeAreaPx, boxSize.height.toFloat() - safeAreaPx)

                    currentOffset = Offset(boundedX, boundedY)
                    onPinDrag(currentOffset)
                }
            }
    ) {
        // 2. USAR with(density) para habilitar las extensiones .toDp()
        with(density) {
            // --- Icono del Pin Flotante ---
            Icon(
                painter = painterResource(id = R.drawable.pin3), // Usamos el mismo icono de pin
                contentDescription = "Pin en movimiento",
                tint = Color.Red, // Destacamos el pin que se está moviendo
                modifier = Modifier
                    .offset(
                        x = currentOffset.x.toDp() - pinOffsetCorrection,
                        y = currentOffset.y.toDp() - pinIconSize
                    )
                    .size(pinIconSize)
            )

            // --- Botones de Control Flotantes (arriba del pin) ---
            Row(
                modifier = Modifier
                    .offset(
                        x = currentOffset.x.toDp() - pinIconSize,
                        y = currentOffset.y.toDp() - pinIconSize - buttonSize - 4.dp
                    )
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de Cancelación (❌)
                IconButton(onClick = onCancel, modifier = Modifier.size(buttonSize)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_24),
                        contentDescription = "Cancelar Movimiento",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Botón de Confirmación (✔️)
                IconButton(onClick = onConfirm, modifier = Modifier.size(buttonSize)) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_done_24),
                        contentDescription = "Confirmar Posición",
                        tint = Color(0xFF4CAF50), // Verde
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } // Cierre del bloque with(density)
    }
}

@Composable
fun ToolbarEdicionPines(
    onBackClick: () -> Unit,
    onPinAddClick: () -> Unit,
    onCrosshairClick: () -> Unit,
    onCancelEditClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    // Fila para contener todos los elementos de la Toolbar (Botón Atrás + Botones de Acción)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Aplicamos el padding aquí
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Para separar el botón de atrás de la toolbar de edición
    ) {

        // --- 1. Botón Atrás (Izquierda) ---
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back), // Asume que R.drawable.arrow_back existe
                contentDescription = stringResource(R.string.go_back),
                tint = Color.White
            )
        }

        // --- 2. Barra de Botones de Edición (Derecha) ---
        Row(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 4.dp), // Padding interno para los botones
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            // Botón 2: Añadir Pin
            IconButton(onClick = onPinAddClick) {
                Icon(
                    painter = painterResource(id = R.drawable.pin3),
                    contentDescription = "Añadir Pin",
                    tint = Color.White
                )
            }
            // Botón 3: Modo Cruz (Crosshair)
            IconButton(onClick = onCrosshairClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reajuste),
                    contentDescription = "Centrar/Posicionar",
                    tint = Color.White
                )
            }
            // Botón 4: Cancelar Edición / Prohibido
            IconButton(onClick = onCancelEditClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_eye_on),
                    contentDescription = "Bloquear Edición",
                    tint = Color.White
                )
            }
            // Botón 5: Ayuda/Información (El botón extra a la derecha)
            IconButton(onClick = onHelpClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_help),
                    contentDescription = "Ayuda",
                    tint = Color.White
                )
            }
        }
    }
}


