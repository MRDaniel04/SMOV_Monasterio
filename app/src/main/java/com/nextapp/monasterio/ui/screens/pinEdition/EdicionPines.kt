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
import androidx.compose.ui.res.stringResource
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
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val PANEL_HEIGHT_FRACTION = 0.50f
    val PANEL_WIDTH_FRACTION = 0.40f


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


    LaunchedEffect(vm.formSubmitted, vm.isEditing) {
        if (!vm.formSubmitted) {
            return@LaunchedEffect
        }

        if (vm.isEditing) {
            vm.formSubmitted = false
            return@LaunchedEffect
        }

        val maxWaitTime = 2000L
        val isReady = withTimeoutOrNull(maxWaitTime) {
            while (photoViewSize.width == 0 || photoViewRef == null) {
                delay(100)
            }
            true
        }

        if (isReady == null || isReady == false) {
            vm.formSubmitted = false
            Toast.makeText(context, context.getString(R.string.error_map_timeout), Toast.LENGTH_LONG).show()
            return@LaunchedEffect
        }

        selectedPin = null
        photoViewRef?.translationX = 0f
        photoViewRef?.translationY = 0f
        isNewPinMode = true

        pinBeingMoved = PinData(
            id = "temp",
            ubicacion_es = vm.ubicacion_es,
            ubicacion_en = vm.pinTitleManualTrads.en.ifBlank { null },
            ubicacion_de = vm.pinTitleManualTrads.de.ifBlank { null },
            ubicacion_fr = vm.pinTitleManualTrads.fr.ifBlank { null },
            area_es = vm.area_es,
            area_en = vm.area_en,
            area_de = vm.area_de,
            area_fr = vm.area_fr,
            x = 0.5f,
            y = 0.5f,
            iconRes = R.drawable.pin3,
            imagenes = vm.imagenes.uris.map { it.toString() },
            descripcion_es = vm.descripcion.es
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
            return@LaunchedEffect
        }

        scope.launch {
            try {
                val plano = PlanoRepository.getPlanoById("monasterio_interior")
                val allPins = PinRepository.getAllPins()
                val pinRefs = plano?.pines?.map { it.substringAfterLast("/") } ?: emptyList()
                pines = allPins.filter { pinRefs.contains(it.id) }
                Toast.makeText(context, context.getString(R.string.pin_updated_success), Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_loading_plane), Toast.LENGTH_LONG).show()
            }
        }

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
                Text(stringResource(R.string.plane_loading))
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

            onRefReady = { photoView -> photoViewRef = photoView },
            onSizeChange = { size -> photoViewSize = size },
            onMatrixChange = {
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
                        selectedPin = null
                    }
                }
            },
            onBackgroundTap = {
                if (selectedPin != null) {
                    photoViewRef?.translationY = 0f
                    photoViewRef?.translationX = 0f
                    selectedPin = null
                }
            },

            isPortrait = isPortrait
        )


        selectedPin?.let { pin ->

            val panelModifier = if (isPortrait) {
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(PANEL_HEIGHT_FRACTION)
            } else {

                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(1f)
                    .fillMaxWidth(PANEL_WIDTH_FRACTION)
                    .padding(end = 12.dp, top = 12.dp, bottom = 12.dp)
            }

            PinDetailsPanel(
                modifier = panelModifier
                    .zIndex(50f),
                selectedPin = pin,
                imagenesDetalladas = pin.imagenesDetalladas,
                pinTapScreenPosition = pinTapScreenPosition,
                panelHeightFraction = PANEL_HEIGHT_FRACTION,
                panelAlignment = if (isPortrait) "BOTTOM" else "RIGHT",
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
                    Toast.makeText(context, context.getString(R.string.move_pin), Toast.LENGTH_LONG).show()
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
                        Toast.makeText(context, context.getString(R.string.move_cancelled), Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, context.getString(R.string.error_pin_position), Toast.LENGTH_SHORT).show()
                            vm.isUploading = false
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
                                return@MovingPinOverlay
                            }


                            vm.onCreateConfirmed(context, finalX, finalY) {

                                vm.isUploading = false
                                vm.uploadMessage = ""

                                isPinMoving = false
                                isNewPinMode = false
                                pinBeingMoved = null
                                pinDragOffset = Offset.Zero
                                selectedPin = null

                                scope.launch {
                                    try {
                                        val plano = PlanoRepository.getPlanoById("monasterio_interior")
                                        val allPins = PinRepository.getAllPins()
                                        val pinRefs = plano?.pines?.map { it.substringAfterLast("/") } ?: emptyList()
                                        pines = allPins.filter { pinRefs.contains(it.id) }
                                        vm.reset()
                                        Toast.makeText(context, context.getString(R.string.pin_created_success), Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                    }
                                }
                            }

                            return@MovingPinOverlay

                        } else {

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
                                    Toast.makeText(context, context.getString(R.string.pin_moved_success), Toast.LENGTH_SHORT).show()

                                } catch (e: Exception) {
                                    Toast.makeText(context, context.getString(R.string.error_pin_position), Toast.LENGTH_SHORT).show()
                                } finally {
                                    vm.isUploading = false
                                    vm.uploadMessage = ""
                                }
                            }
                        }

                        isPinMoving = false
                        selectedPin = null
                        pinBeingMoved = null
                        pinDragOffset = Offset.Zero
                    },
                    boxSize = photoViewSize
                )
            }
        }


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
                    contentDescription = stringResource(R.string.go_back),
                    tint = Color.White
                )
            }
        }

        val toolbarModifier = if (isPortrait) {
            Modifier.align(Alignment.TopEnd)
        } else {
            Modifier.align(Alignment.CenterStart)
        }

        PinEditionToolbar(
            onPinAddClick = {
                vm.reset()
                navController.navigate(AppRoutes.CREACION_PINES)
            },
            onCrosshairClick = {

                selectedPin = null
                isPinMoving = false

                photoViewRef?.let { photoView ->
                    photoView.attacher.setScale(1f, true)
                    photoView.attacher.setRotationTo(0f)
                    photoView.attacher.setScaleType(ImageView.ScaleType.FIT_END)
                    photoView.translationY = 0f
                    photoView.translationX = 0f
                    Toast.makeText(context, context.getString(R.string.map_reset_success), Toast.LENGTH_SHORT).show()
                }
            },
            onCancelEditClick = {
                Toast.makeText(context, context.getString(R.string.lock_edition), Toast.LENGTH_SHORT).show()
            },
            onHelpClick = {
                navController.navigate(AppRoutes.MANUAL_EDICION)
            },
            modifier = toolbarModifier.zIndex(100f),
            isPortrait = isPortrait
        )

        if (vm.isUploading) {
            LoadingOverlay(vm.uploadMessage)
        }

    }

}





