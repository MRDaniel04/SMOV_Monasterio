package com.nextapp.monasterio.ui.screens.pinEdition

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.PointF
import android.net.Uri
import android.util.Log
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

import androidx.compose.ui.zIndex
import com.nextapp.monasterio.ui.screens.pinCreation.CreacionPinSharedViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextapp.monasterio.services.CloudinaryService
import com.nextapp.monasterio.ui.screens.pinEdition.components.InteractivePlanoViewer
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
    val PANEL_HEIGHT_FRACTION = 0.50f

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

    val parentEntry = remember(navController.currentBackStackEntry) {
        try {
            navController.getBackStackEntry("pins_graph")
        } catch (e: Exception) {
            // La excepción es esperada y logeada.
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

        InteractivePlanoViewer(
            planoUrl = planoUrl,
            pines = pines,
            isPinMoving = isPinMoving,
            selectedPin = selectedPin,
            pinBeingMoved = pinBeingMoved,
            ignoreNextMatrixChange = ignoreNextMatrixChange,

            // Callbacks (Eventos que se envían de vuelta al padre)
            onRefReady = { photoView ->
                photoViewRef = photoView // 1. Para guardar la referencia del PhotoView
            },
            onSizeChange = { size ->
                photoViewSize = size // 2. Para guardar el tamaño (necesario para MovingPinOverlay)
            },
            onMatrixChange = {
                // 3. LÓGICA: OCULTAR PANEL AL DESPLAZAR/HACER ZOOM (PAN/MATRIX CHANGE)
                // (Mismo código que estaba en setOnMatrixChangeListener)
                Log.e("MATRIX", "❌ MatrixChange REAL → cancelando modo mover/panel")
                isPinMoving = false
                selectedPin = null
            },
            onPinTap = { pin, screenX, screenY ->
                // 4. LÓGICA DE TAP SOBRE PIN (Parte 1: Guardar posición y estado)
                pinTapScreenPosition = Offset(screenX, screenY)
                selectedPin = pin

                // Parte 2: Cargar datos completos del Pin
                scope.launch {
                    val fullPin = PinRepository.getPinById(pin.id)
                    selectedPin = fullPin ?: selectedPin
                    if (fullPin == null) {
                        Log.e("EdicionPines", "❌ No se pudo cargar el pin detallado: ${pin.id}")
                        selectedPin = null // Ocultar el panel si falla
                    }
                }
                // NOTA: La lógica de desplazamiento (translationY/X) se ha movido al Viewer.
            },
            onBackgroundTap = {
                // 5. LÓGICA DE TOQUE FUERA DEL PIN: Oculta el panel y RESTAURA AMBOS DESPLAZAMIENTOS
                if (selectedPin != null) {
                    Log.d("EdicionPines", "❌ Toque estático fuera de Pin. Ocultando panel y RESTAURANDO POSICIONES.")
                    // ¡IMPORTANTE! Restaurar desplazamiento, si lo hay.
                    photoViewRef?.translationY = 0f
                    photoViewRef?.translationX = 0f
                    selectedPin = null
                } else {
                    Log.d("EdicionPines", "Toque fuera y no había Pin seleccionado. Ignorando.")
                }
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
                            normalizedCoords = photoView.getNormalizedImageCoords(
                                screenX = currentScreenPos.x,
                                screenY = currentScreenPos.y
                            )
                        }


                        if (normalizedCoords == null) {
                            Log.e("EdicionPines", "Error al convertir coordenadas a normalizadas. Confirmación cancelada.")
                            Toast.makeText(context, "Error al obtener la posición del pin.", Toast.LENGTH_SHORT).show()
                            // Si falla, el valor de retorno ya fue manejado.
                            return@MovingPinOverlay
                        }

                        val finalX = normalizedCoords.x // Ya no es necesario '!!' si se ha comprobado antes
                        val finalY = normalizedCoords.y // Ya no es necesario '!!'
                        val pinToUpdate = pinBeingMoved!!

                        if (isNewPinMode) {

                            // A. VALIDACIÓN DE CAMPOS OBLIGATORIOS (Aunque se valida en CreacionPines.kt, se re-valida por seguridad)
                            val isPinValid = vm.titulo.es.isNotBlank() &&
                                    vm.descripcion.es.isNotBlank() &&
                                    vm.imagenes.uris.isNotEmpty()

                            if (!isPinValid) {
                                Toast.makeText(context, "Error: Faltan datos obligatorios (Título/Descripción/Imágenes).", Toast.LENGTH_LONG).show()
                                return@MovingPinOverlay
                            }

                            scope.launch {

                                Log.d("EdicionPines", "Confirmando posición final. Iniciando subida de imágenes a Cloudinary...")

                                // 1. SUBIR IMÁGENES NORMALES
                                val uploadedImageUrls = vm.imagenes.uris.mapNotNull { uri ->
                                    val result = CloudinaryService.uploadImage(uri, context)
                                    result.getOrNull()
                                }

                                val uploaded360Url: String? = vm.imagen360?.let { uri ->
                                    val result = CloudinaryService.uploadImage(uri, context)
                                    result.getOrNull()
                                }

                                // C. VALIDACIÓN FINAL DE SUBIDA
                                if (uploadedImageUrls.isEmpty()) {
                                    Toast.makeText(context, "Error: No se pudo subir ninguna imagen. Pin NO creado.", Toast.LENGTH_LONG).show()
                                    Log.e("EdicionPines", "ERROR: Subida de imágenes falló o se descartaron las URIs.")
                                    return@launch
                                }

                                // D. CREAR PIN CON LAS URLs Y COORDENADAS FINALES
                                try {
                                    val newPinId = PinRepository.createPinFromForm(
                                        titulo = vm.titulo.es,
                                        descripcion = vm.descripcion.es,

                                        // Campos de Traducción Opcionales
                                        tituloIngles = vm.titulo.en.ifBlank { null },
                                        tituloAleman = vm.titulo.de.ifBlank { null },
                                        descripcionIngles = vm.descripcion.en.ifBlank { null },
                                        descripcionAleman = vm.descripcion.de.ifBlank { null },

                                        ubicacion = vm.ubicacion.displayName,
                                        imagenes = uploadedImageUrls,
                                        imagen360 = uploaded360Url,
                                        x = finalX, // ✅ Coordenadas Normalizadas
                                        y = finalY // ✅ Coordenadas Normalizadas
                                    )

                                    // E. AÑADIR A PLANO
                                    PlanoRepository.addPinToPlano(
                                        planoId = "monasterio_interior",
                                        pinId = newPinId
                                    )

                                    // F. ÉXITO
                                    Toast.makeText(context, "Pin Creado. ID: $newPinId", Toast.LENGTH_LONG).show()
                                    Log.d("EdicionPines", "Pin con ID:$newPinId creado correctamente en (x=$finalX, y=$finalY)")

                                    // Recargar la lista de pines para mostrar el nuevo pin
                                    val plano = PlanoRepository.getPlanoById("monasterio_interior")
                                    val allPins = PinRepository.getAllPins()
                                    val pinRefs = plano?.pines?.map { it.substringAfterLast("/") } ?: emptyList()
                                    pines = allPins.filter { pinRefs.contains(it.id) }

                                } catch (e: Exception) {
                                    Log.e("EdicionPines", "Error en PinRepository.createPinFromForm", e)
                                    Toast.makeText(context, "Error al guardar el Pin en Firebase.", Toast.LENGTH_LONG).show()
                                }
                            }

                            vm.formSubmitted = false
                            isNewPinMode = false

                        } else {
                            // Este es el bloque de código que YA tenías para actualizar la posición
                            // de un pin que ya existía.
                            scope.launch {
                                try {
                                    PinRepository.updatePinPosition(
                                        pinId = pinToUpdate.id,
                                        newX = finalX,
                                        newY = finalY
                                    )

                                    // Actualizar el estado local (para forzar el redibujo del mapa)
                                    pines = pines.map { pin ->
                                        if (pin.id == pinToUpdate.id) {
                                            pin.copy(x = finalX, y = finalY)
                                        } else {
                                            pin
                                        }
                                    }
                                    Toast.makeText(context, "Pin ${pinToUpdate.id} movido a (x=$finalX, y=$finalY)", Toast.LENGTH_SHORT).show()

                                } catch (e: Exception) {
                                    Log.e("EdicionPines", "Error al guardar posición en Firebase", e)
                                    Toast.makeText(context, "Error al mover el pin existente.", Toast.LENGTH_SHORT).show()
                                }
                            }
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





