package com.nextapp.monasterio.ui.screens.pinEdition

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.PointF
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize // Necesario para obtener el tamaño de la caja
import com.nextapp.monasterio.AppRoutes
import androidx.compose.ui.zIndex
import com.nextapp.monasterio.ui.screens.pinCreation.CreacionPinSharedViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextapp.monasterio.ui.screens.pinCreation.components.LoadingOverlay
import com.nextapp.monasterio.ui.screens.pinEdition.components.InteractivePlanoViewer
import com.nextapp.monasterio.ui.screens.pinEdition.components.MovingPinOverlay
import com.nextapp.monasterio.ui.screens.pinEdition.components.PinDetailsPanel
import kotlinx.coroutines.delay
import com.nextapp.monasterio.ui.screens.pinEdition.components.PinEditionToolbar
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun EdicionPines(
    navController: NavController,
    rootNavController: NavController? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current // ⬅️ NEW
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT // ⬅️ NEW

    val PANEL_HEIGHT_FRACTION = 0.50f
    val PANEL_WIDTH_FRACTION = 0.40f

    Log.d("EdicionPines", "Composición iniciada - Modo Interacción Pin (Panel 35%)")

    // --- Estados de Datos y UI ---
    var pines by remember { mutableStateOf<List<PinData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var photoViewRef by remember { mutableStateOf<DebugPhotoView?>(null) }
    var selectedPin by remember { mutableStateOf<PinData?>(null) }
    var planoUrl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isPinMoving by remember { mutableStateOf(false) }
    var ignoreNextMatrixChange by remember { mutableStateOf(false) }
    var pinBeingMoved by remember { mutableStateOf<PinData?>(null) }
    var pinDragOffset by remember { mutableStateOf(Offset.Zero) }
    var pinTapScreenPosition by remember { mutableStateOf<Offset?>(null) }
    var photoViewSize by remember { mutableStateOf(IntSize.Zero) }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {
            // 🆕 CORRECCIÓN DEL LAG: Asegurar que la referencia a la vista nativa se limpia
            // cuando el Composable se destruye para prevenir posibles fugas o conflictos de ciclo de vida.
            photoViewRef = null
        }
    }

    val parentEntry = remember(navController.currentBackStackEntry) {
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

    var isNewPinMode by remember { mutableStateOf(false) }

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


    LaunchedEffect(vm.formSubmitted, vm.isEditing) { // ⬅️ CAMBIO 1: Añadir vm.isEditing a la lista de dependencias
        if (!vm.formSubmitted) {
            Log.d("FLUJO_PIN", "EdicionPines: Observando formSubmitted. Estado actual: false. Esperando...")
            return@LaunchedEffect
        }

        if (vm.isEditing) {
            Log.d("FLUJO_PIN", "EdicionPines: ⚠️ formSubmitted detectado, pero vm.isEditing es TRUE. Ignorando colocación de Pin (flujo de Edición/Actualización).")
            vm.formSubmitted = false // Resetear la bandera para que no se reejecute.
            return@LaunchedEffect
        }

        Log.d("FLUJO_PIN", "EdicionPines: 🚀 formSubmitted DETECTADO (Creación). Iniciando modo de colocación de Pin.")
        // 1. BUCLER DE ESPERA Y VALIDACIÓN CON TIMEOUT
        val maxWaitTime = 2000L // 2 segundos máximo
        val isReady = withTimeoutOrNull(maxWaitTime) {
            while (photoViewSize.width == 0 || photoViewRef == null) {
                Log.d("FLUJO_PIN", "EdicionPines: ⏳ Esperando PhotoView. Size=${photoViewSize}, Ref=${photoViewRef != null}")
                delay(100) // Esperar 100ms antes de reintentar
            }
            true // Si salimos del while, está listo
        }

        if (isReady == null || isReady == false) {
            Log.e("FLUJO_PIN", "EdicionPines: ❌ ERROR. PhotoView no estuvo listo en ${maxWaitTime}ms. Abortando inicio de colocación.")
            // Resetear el flag para permitir un futuro intento
            vm.formSubmitted = false
            Toast.makeText(context, "Error: El mapa no cargó a tiempo para colocar el pin. Inténtelo de nuevo.", Toast.LENGTH_LONG).show()
            return@LaunchedEffect
        }

        Log.d("FLUJO_PIN", "EdicionPines: ✅ PhotoView listo. Iniciando colocación del Pin.")

        // --- 2. Lógica de Colocación (El resto es igual) ---
        selectedPin = null
        photoViewRef?.translationX = 0f
        photoViewRef?.translationY = 0f
        isNewPinMode = true

        Log.d("FLUJO_PIN", "EdicionPines: Pin temporal creado en el centro. PinMoving=true, NewPinMode=true.")

        // 🆕 Pin Being Moved: Usando los nuevos campos del ViewModel
        pinBeingMoved = PinData(
            id = "temp",
            ubicacion_es = vm.ubicacion_es,
            ubicacion_en = vm.pinTitleManualTrads.en.ifBlank { null },
            ubicacion_de = vm.pinTitleManualTrads.de.ifBlank { null },
            ubicacion_fr = vm.pinTitleManualTrads.fr.ifBlank { null },
            area_es = vm.area_es,
            area_en = vm.area_en, // Usando nuevo getter
            area_de = vm.area_de, // Usando nuevo getter
            area_fr = vm.area_fr, // Usando nuevo getter
            x = 0.5f,
            y = 0.5f,
            iconRes = R.drawable.pin3,
            imagenes = vm.imagenes.uris.map { it.toString() },
            descripcion_es = vm.descripcion.es // Acceso directo (sin .value)
        )

        pinDragOffset = Offset(
            x = photoViewSize.width / 2f,
            y = photoViewSize.height / 2f
        )

        isPinMoving = true
        ignoreNextMatrixChange = true

        scope.launch {
            ignoreNextMatrixChange = true
            delay(500)
            ignoreNextMatrixChange = false
        }

    }

    LaunchedEffect(vm.updateRequested) {
        if (!vm.updateRequested) return@LaunchedEffect

        vm.updateRequested = false

        val pinId = vm.editingPinId
        if (pinId == null) {
            Log.e("EdicionPines", "❌ updateRequested pero editingPinId es null")
            return@LaunchedEffect
        }

        scope.launch {
            try {
                // Recargar pines
                val plano = PlanoRepository.getPlanoById("monasterio_interior")
                val allPins = PinRepository.getAllPins()
                val pinRefs = plano?.pines?.map { it.substringAfterLast("/") } ?: emptyList()
                pines = allPins.filter { pinRefs.contains(it.id) }

                Toast.makeText(context, "Pin actualizado correctamente", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Log.e("EdicionPines", "❌ Error al recargar el plano/pines tras la actualización", e)
                Toast.makeText(context, "Error al recargar el mapa tras la actualización.", Toast.LENGTH_LONG).show()
            }
        }

        // Reset final del modo edición
        vm.isEditing = false
        vm.editingPinId = null

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
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

            // Callbacks
            onRefReady = { photoView -> photoViewRef = photoView },
            onSizeChange = { size -> photoViewSize = size },
            onMatrixChange = {
                Log.e("MATRIX", "❌ MatrixChange REAL → cancelando modo mover/panel")
                isPinMoving = false
                selectedPin = null
            },
            onPinTap = { pin, screenX, screenY ->
                pinTapScreenPosition = Offset(screenX, screenY)
                selectedPin = pin

                scope.launch {
                    val fullPin = PinRepository.getPinById(pin.id)
                    selectedPin = fullPin ?: selectedPin
                    if (fullPin == null) {
                        Log.e("EdicionPines", "❌ No se pudo cargar el pin detallado: ${pin.id}")
                        selectedPin = null
                    }
                }
            },
            onBackgroundTap = {
                if (selectedPin != null) {
                    Log.d("EdicionPines", "❌ Toque estático fuera de Pin. Ocultando panel y RESTAURANDO POSICIONES.")
                    photoViewRef?.translationY = 0f
                    photoViewRef?.translationX = 0f
                    selectedPin = null
                } else {
                    Log.d("EdicionPines", "Toque fuera y no había Pin seleccionado. Ignorando.")
                }
            },

            isPortrait = isPortrait // ⬅️ ¡AÑADIR ESTA LÍNEA!
        )

        // -------------------------
        // ⭐ PANEL INFORMATIVO (Estructura fija/scroll de descripción) ⭐
        // -------------------------
        selectedPin?.let { pin ->

            val panelModifier = if (isPortrait) {
                // MODO VERTICAL (Portrait): Panel ABAJO (comportamiento que quieres para vertical)
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(PANEL_HEIGHT_FRACTION)
            } else {
                // MODO HORIZONTAL (Landscape): Panel a la DERECHA (comportamiento que quieres para horizontal)
                Modifier
                    .align(Alignment.CenterEnd) // Usar CenterEnd en lugar de TopEnd
                    .fillMaxHeight(1f) // ⬅️ Ocupa 100% de la altura
                    .fillMaxWidth(PANEL_WIDTH_FRACTION)
                    .padding(end = 12.dp, top = 12.dp, bottom = 12.dp) // ⬅️ Padding uniforme, sin los 70.dp
            }

            PinDetailsPanel(
                modifier = panelModifier
                    .zIndex(50f),
                selectedPin = pin,
                imagenesDetalladas = pin.imagenesDetalladas,
                pinTapScreenPosition = pinTapScreenPosition,
                panelHeightFraction = PANEL_HEIGHT_FRACTION, // Se mantiene por si se usa internamente
                panelAlignment = if (isPortrait) "BOTTOM" else "RIGHT", // ⬅️ CAMBIO: La lógica de orientación se invierte
                onClosePanel = {
                    photoViewRef?.translationY = 0f
                    photoViewRef?.translationX = 0f
                    selectedPin = null
                },

                onStartMove = { movedPin, initialPos ->
                    pinBeingMoved = movedPin
                    pinDragOffset = initialPos
                    selectedPin = null
                    isPinMoving = true
                    photoViewRef?.translationY = 0f
                    photoViewRef?.translationX = 0f
                    Toast.makeText(context, "Modo Mover Pin activado. Arrastre.", Toast.LENGTH_LONG).show()
                },

                onEdit = {
                    vm.loadPinForEditing(pin)
                    navController.navigate(AppRoutes.CREACION_PINES)
                },

                onPinDeleted = { pinId ->
                    pines = pines.filter { it.id != pinId }
                    selectedPin = null
                }
            )
        }

        if (isPinMoving && pinBeingMoved != null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(999f)
            ) {
                MovingPinOverlay(
                    pinData = pinBeingMoved!!,
                    initialOffset = pinDragOffset,

                    isPressed = true,
                    onPinDrag = { newOffset ->
                        pinDragOffset = newOffset
                    },
                    onCancel = {

                        if (isNewPinMode) {
                            vm.formSubmitted = false
                            navController.navigate(AppRoutes.CREACION_PINES)
                            isNewPinMode = false
                        }
                        isPinMoving = false
                        selectedPin = pinBeingMoved
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
                            Toast.makeText(context, "Error al obtener la posición del pin.", Toast.LENGTH_SHORT).show()
                            vm.isUploading = false // Asegurar que se apaga en caso de fallo crítico de coordenadas
                            return@MovingPinOverlay
                        }

                        val finalX = normalizedCoords.x
                        val finalY = normalizedCoords.y
                        val pinToUpdate = pinBeingMoved!!

                        if (isNewPinMode) {

                            val isPinValid =
                                vm.descripcion.es.isNotBlank() &&
                                        vm.imagenes.allImagesTagged &&
                                        vm.ubicacion_es.isNotBlank() &&
                                        vm.area_es.isNotBlank()

                            if (!isPinValid) {
                                vm.isUploading = false
                                vm.uploadMessage = ""
                                return@MovingPinOverlay // <-- Sale si la validación falla (motivo más probable del fallo reportado)
                            }



                            // 3. Llamar al VM para iniciar el proceso de guardado ASÍNCRONO
                            vm.onCreateConfirmed(context, finalX, finalY) {
                                // Callback de ÉXITO del ViewModel (se ejecuta cuando el Pin ya está en Firebase/Cloudinary)

                                vm.isUploading = false
                                vm.uploadMessage = ""

                                // 4. Resetear el estado de la UI (MOVIDO AQUÍ DENTRO)
                                isPinMoving = false
                                isNewPinMode = false
                                pinBeingMoved = null
                                pinDragOffset = Offset.Zero
                                selectedPin = null // Asegurar que no hay pin seleccionado

                                // Recargar la lista de pines y limpiar el formulario
                                scope.launch {
                                    try {
                                        val plano = PlanoRepository.getPlanoById("monasterio_interior")
                                        val allPins = PinRepository.getAllPins()
                                        val pinRefs = plano?.pines?.map { it.substringAfterLast("/") } ?: emptyList()
                                        pines = allPins.filter { pinRefs.contains(it.id) }
                                        vm.reset() // Limpiamos el formulario tras el éxito.
                                        Toast.makeText(context, "Pin Creado y guardado con éxito.", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        Log.e("EdicionPines", "Error recargando pines: ${e.message}")
                                    }
                                }
                            }

                            return@MovingPinOverlay

                        } else {
                            // Este bloque para mover un pin existente sigue usando la función de posición
                            scope.launch {
                                try {
                                    PinRepository.updatePinPosition(
                                        pinId = pinToUpdate.id,
                                        newX = finalX,
                                        newY = finalY
                                    )

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
                                } finally {
                                    vm.isUploading = false
                                    vm.uploadMessage = ""
                                }
                            }
                        }


                        // 2. Salir del modo movimiento
                        isPinMoving = false
                        selectedPin = null
                        pinBeingMoved = null
                        pinDragOffset = Offset.Zero
                    },
                    boxSize = photoViewSize
                )
            }
        }

        // -------------------------
        // ⭐ TOOLBARS (No afectadas por los cambios de nombre) ⭐
        // -------------------------
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(100f)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xBB000000))
        ) {
            IconButton(
                onClick = {
                    if (rootNavController != null) rootNavController.popBackStack()
                    else navController.popBackStack()
                },
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        }

        val toolbarModifier = if (isPortrait) {
            // MODO VERTICAL (Portrait): Arriba a la derecha (Horizontal)
            Modifier.align(Alignment.TopEnd)
        } else {
            // MODO HORIZONTAL (Landscape): Centrada a la Izquierda (Vertical)
            Modifier.align(Alignment.CenterStart) // ⬅️ CAMBIO: Alineación a la izquierda, centrada
        }

        PinEditionToolbar(
            onPinAddClick = {
                vm.reset()
                navController.navigate(AppRoutes.CREACION_PINES)
            },
            onCrosshairClick = {
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
                navController.navigate(AppRoutes.MANUAL_EDICION)
            },
            modifier = toolbarModifier.zIndex(100f),
            isPortrait = isPortrait // ⬅️ Pasar el nuevo parámetro al componente
        )

        if (vm.isUploading) {
            LoadingOverlay(vm.uploadMessage)
        }

    }

}





