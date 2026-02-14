package com.nextapp.monasterio.ui.virtualvisit.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import android.graphics.Matrix
import android.graphics.Path
import android.util.Log
import com.nextapp.monasterio.R
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.models.*
import com.nextapp.monasterio.repository.*
import com.nextapp.monasterio.ui.virtualvisit.components.DebugPhotoView
import com.nextapp.monasterio.ui.virtualvisit.utils.isPointInPath
import com.nextapp.monasterio.ui.virtualvisit.utils.isPointInPinArea
import com.nextapp.monasterio.ui.screens.VirtualVisitRoutes
import com.nextapp.monasterio.viewModels.AjustesViewModel
import com.nextapp.monasterio.ui.virtualvisit.components.GenericTutorialOverlay
import kotlinx.coroutines.delay
import android.media.MediaPlayer


// Clase auxiliar para definir los pasos del tutorial
private data class TutorialStep(
    val description: String,
    val focusCenter: Offset,
    val focusRadius: Float = 0f,
    val rectSize: Size? = null,
    val alignment: Alignment = Alignment.BottomCenter
)

@Composable
fun PlanoScreen(
    viewModel: AjustesViewModel,
    planoId: String,
    navController: NavController,
    rootNavController: NavHostController? = null
) {
    val context = LocalContext.current
    val soundPines = remember {
        MediaPlayer.create(context, R.raw.piness)
    }

    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {}
    }
    val zoomPlayer = remember {
        // Si no tienes 'zoom_sound', usa R.raw.click_button
        MediaPlayer.create(context, R.raw.zoom)
    }
    val deszoomPlayer = remember {
        // Si no tienes 'navigation_back', usa R.raw.click_button
        MediaPlayer.create(context, R.raw.deszoom)
    }

    // 1. CÁLCULOS DE PANTALLA (LAYOUT)
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenCenter = remember(configuration) {
        with(density) {
            Offset(
                x = configuration.screenWidthDp.dp.toPx() / 2,
                y = configuration.screenHeightDp.dp.toPx() / 2
            )
        }
    }

    // Variables de estado para layout real
    var backButtonLayout by remember { mutableStateOf<Pair<Offset, Size>?>(null) }
    var zoomControlsLayout by remember { mutableStateOf<Pair<Offset, Size>?>(null) }

    val scope = rememberCoroutineScope()

    // --- Estados de Datos ---
    var plano by remember { mutableStateOf<PlanoData?>(null) }
    var figuras by remember { mutableStateOf<List<FiguraData>>(emptyList()) }
    var pines by remember { mutableStateOf<List<PinData>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    // --- Estados Visuales ---
    var activePath by remember { mutableStateOf<Path?>(null) }
    var activeHighlight by remember { mutableStateOf<Color?>(null) }
    var isPinPressed by remember { mutableStateOf(false) }
    var selectedPinId by remember { mutableStateOf<String?>(null) }

    // 👇 ESTADO PARA ANIMACIÓN DE PINES (GIF)
    var animatingPinId by remember { mutableStateOf<String?>(null) }

    // --- LOGICA DE TUTORIAL ---
    val isMainDismissed by viewModel.isMainMapDismissed.collectAsState()
    val isSubDismissed by viewModel.isSubMapDismissed.collectAsState()
    var showTutorialSession by remember { mutableStateOf(true) }

    var currentStepIndex by remember { mutableIntStateOf(0) }

    val initialZoom = 1f
    val planoBackgroundColor = Color(0xFFF5F5F5)
    val botonesVisibles by viewModel.botonesVisibles.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "BlinkTransition")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse)
    )
    fun playSound(player: MediaPlayer?) {
        if (player != null) {
            if (player.isPlaying) player.seekTo(0)
            player.start()
        }
    }

    // --- Carga inicial ---
    LaunchedEffect(planoId, retryTrigger) {
        isLoading = true
        isError = false
        showTutorialSession = true
        currentStepIndex = 0
        try {
            plano = PlanoRepository.getPlanoById(planoId)
            if (plano != null) {
                val figuraRefs = plano!!.figuras.map { it.substringAfterLast("/") }
                figuras = FiguraRepository.getAllFiguras().filter { figuraRefs.contains(it.id) }
                val pinRefs = plano!!.pines.map { it.substringAfterLast("/") }
                pines = PinRepository.getAllPins().filter { pinRefs.contains(it.id) }
            } else {
                isError = true
            }
        } catch (e: Exception) {
            isError = true
        } finally {
            isLoading = false
        }
    }
    // 👇 TEMPORIZADOR DE ANIMACIÓN MEJORADO 👇
    // Usamos 'Unit' para que SOLO se lance una vez y el bucle while controle todo
    LaunchedEffect(Unit) {
        while (true) {
            // 1. Fase estática (4 segundos)
            animatingPinId = null
            delay(4000)

            // 2. Fase Animada (Elegir y activar)
            if (pines.isNotEmpty()) {
                val randomPin = pines.random()
                animatingPinId = randomPin.id

                // 3. Duración del GIF (2.5 segundos)
                delay(2500)

                // 4. Apagar
                animatingPinId = null
            }
        }
    }

    // --- UI Principal ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(planoBackgroundColor)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Box
        }

        if (isError) {
            ConnectionErrorView(
                onRetry = { retryTrigger++ },
                onBack = { if (!navController.popBackStack()) rootNavController?.popBackStack() }
            )
            return@Box
        }

        var photoViewRef by remember { mutableStateOf<DebugPhotoView?>(null) }

        // 1. Capa del Mapa Interactivo
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                DebugPhotoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    plano?.let { p -> setImageFromUrl(p.plano) }
                    post { setScale(initialZoom, true) }
                    this.pins = emptyList()
                }
            },
            // El bloque update se llama cuando cambia animatingPinId
            update = { photoView ->
                photoView.staticZones = figuras.map {
                    DebugPhotoView.StaticZoneData(
                        createPathFromFirebase(it),
                        it.colorResaltado.toUInt().toInt()
                    )
                }

                photoView.pins = pines.map { pin ->
                    val baseColorInt = android.graphics.Color.WHITE

                    // 👇 SELECCIÓN DE ICONO (GIF vs ESTÁTICO)
                    val isAnimating = pin.id == animatingPinId
                    val iconRes = R.drawable.pin_animado

                    DebugPhotoView.PinData(
                        x = pin.x,
                        y = pin.y,
                        iconId = iconRes, // Usamos el recurso dinámico
                        isPressed = pin.id == selectedPinId,
                        isMoving = false
                    )
                }

                photoView.blinkingAlpha = blinkAlpha
                photoView.interactivePath = activePath
                photoView.highlightColor = activeHighlight?.toArgb() ?: Color.Transparent.toArgb()
                photoView.invalidate()

                photoView.setOnPhotoTapListener { _, x, y ->
                    val figura = figuras.find { isPointInPath(x, y, createPathFromFirebase(it)) }
                    val pin = pines.find { isPointInPinArea(x, y, it.x, it.y, it.tapRadius) }
                    when {
                        figura != null -> {
                            activePath = createPathFromFirebase(figura)
                            activeHighlight = Color(figura.colorResaltado.toUInt().toInt())
                            Handler(Looper.getMainLooper()).postDelayed({
                                activeHighlight = null
                                when (figura.tipoDestino) {
                                    "detalle" -> navController.navigate("${VirtualVisitRoutes.DETALLE_GENERICO}/${figura.valorDestino}")
                                    "plano" -> {
                                        val destinoId = figura.valorDestino
                                        if (destinoId.isNotBlank()) {
                                            // 👇👇 AQUÍ SUENA AL ENTRAR AL SUBMAPA 👇👇
                                            playSound(zoomPlayer)
                                            navController.navigate("${VirtualVisitRoutes.PLANO}/$destinoId")
                                        } else {
                                            Toast.makeText(context, context.getString(R.string.notdefined_destinyplane), Toast.LENGTH_SHORT).show()
                                        }}
                                    else -> Toast.makeText(context, context.getString(R.string.notdefined_destiny), Toast.LENGTH_SHORT).show()
                                }
                            }, 200)
                        }

                        pin != null -> {
                            if (soundPines.isPlaying) {
                                soundPines.seekTo(0)
                            }
                            soundPines.start()
                            selectedPinId = pin.id
                            Handler(Looper.getMainLooper()).postDelayed({
                                selectedPinId = null
                                when (pin.tipoDestino?.lowercase()) {
                                    "ruta" -> if (pin.valorDestino != null) rootNavController?.navigate(
                                        "${AppRoutes.PIN_ENTRADA_MONASTERIO}/${pin.id}"
                                    )
                                    "detalle" -> navController.navigate(AppRoutes.PIN_DETALLE + "/${pin.id}")
                                }
                            }, 200)
                        }

                        /*else -> {
                            selectedPinId = null
                            Toast.makeText(context, context.getString(R.string.out_interactive_area), Toast.LENGTH_SHORT).show()
                        }*/
                    }
                }
                photoViewRef = photoView
            }
        )

        // 2. Controles de Zoom
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    val size = coordinates.size
                    zoomControlsLayout = position to Size(size.width.toFloat(), size.height.toFloat())
                },
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (botonesVisibles) {
                // --- AUMENTAR ---
                FloatingActionButton(
                    onClick = { photoViewRef?.let { it.setScale((it.scale + 0.2f).coerceAtMost(it.maximumScale), true) } },
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Box(modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.7f), CircleShape).border(1.dp, Color.Black, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.aumentar_zoom),
                            contentDescription = stringResource(R.string.increase),
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // --- DISMINUIR ---
                FloatingActionButton(
                    onClick = { photoViewRef?.let { it.setScale((it.scale - 0.2f).coerceAtLeast(it.minimumScale), true) } },
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Box(modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.7f), CircleShape).border(1.dp, Color.Black, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.disminuir_zoom),
                            contentDescription = stringResource(R.string.decrease),
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // --- REAJUSTAR ---
                FloatingActionButton(
                    onClick = { photoViewRef?.apply { setScale(initialZoom, true); setTranslationX(0f); setTranslationY(0f) } },
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                            .border(1.dp, Color.Black, CircleShape), // BORDE NEGRO
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.reajustar),
                            contentDescription = stringResource(R.string.readjust),
                            tint = Color.Unspecified,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // 3. Botón Atrás
        IconButton(
            onClick = {
                if (!navController.popBackStack()) {
                    if (planoId == "monasterio_exterior") {
                        rootNavController?.popBackStack()
                    } else {
                        playSound(deszoomPlayer)
                        navController.navigate("${VirtualVisitRoutes.PLANO}/monasterio_exterior") {
                            popUpTo("${VirtualVisitRoutes.PLANO}/monasterio_exterior") { inclusive = false }
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    val size = coordinates.size
                    backButtonLayout = position to Size(size.width.toFloat(), size.height.toFloat())
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = stringResource(R.string.go_back),
                tint = Color.White
            )
        }

        // 4. TUTORIALES
        val isMainMap = (planoId == "monasterio_exterior")
        val isLayoutReady = backButtonLayout != null && !isLoading && !isError

        if (showTutorialSession && isLayoutReady) {
            if (isMainMap && !isMainDismissed) {
                // ... (Lógica tutorial mapa principal) ...
                val steps = remember(backButtonLayout, zoomControlsLayout) {
                    val list = mutableListOf<TutorialStep>()

                    if (backButtonLayout != null) {
                        val (pos, size) = backButtonLayout!!
                        list.add(
                            TutorialStep(
                                description = context.getString(R.string.map_tutorial_body1),
                                focusCenter = Offset(pos.x + size.width / 2, pos.y + size.height / 2),
                                focusRadius = 100f,
                                alignment = Alignment.BottomCenter
                            )
                        )
                    }

                    list.add(
                        TutorialStep(
                            description = context.getString(R.string.map_tutorial_body1_2),
                            focusCenter = screenCenter,
                            focusRadius = 250f,
                            alignment = Alignment.BottomCenter
                        )
                    )

                    if (zoomControlsLayout != null) {
                        val (pos, size) = zoomControlsLayout!!
                        list.add(
                            TutorialStep(
                                description = context.getString(R.string.map_tutorial_body1_3),
                                focusCenter = Offset(pos.x + size.width / 2, pos.y + size.height / 2),
                                rectSize = size,
                                alignment = Alignment.TopCenter
                            )
                        )
                    }
                    list
                }

                if (steps.isNotEmpty() && currentStepIndex < steps.size) {
                    val step = steps[currentStepIndex]
                    val isLastStep = currentStepIndex == steps.size - 1
                    val btnText = if (isLastStep) stringResource(R.string.understand) else stringResource(R.string.next)

                    GenericTutorialOverlay(
                        description = step.description,
                        highlightCenter = step.focusCenter,
                        highlightRadius = step.focusRadius,
                        highlightRectSize = step.rectSize,
                        dialogAlignment = step.alignment,
                        buttonText = btnText,
                        onCloseClicked = { permanent ->
                            if (isLastStep) {
                                showTutorialSession = false
                                if (permanent) viewModel.dismissMainMap()
                            } else {
                                currentStepIndex++
                                if (permanent) viewModel.dismissMainMap()
                            }
                        }
                    )
                }

            } else if (!isMainMap && !isSubDismissed) {
                // ... (Lógica tutorial submapa) ...
                val subSteps = remember(backButtonLayout) {
                    val list = mutableListOf<TutorialStep>()

                    list.add(
                        TutorialStep(
                            description = context.getString(R.string.submapa_1),
                            focusCenter = screenCenter,
                            focusRadius = 300f,
                            alignment = Alignment.BottomCenter
                        )
                    )

                    if (backButtonLayout != null) {
                        val (pos, size) = backButtonLayout!!
                        list.add(
                            TutorialStep(
                                description = context.getString(R.string.submapa_2),
                                focusCenter = Offset(pos.x + size.width / 2, pos.y + size.height / 2),
                                focusRadius = 100f,
                                alignment = Alignment.BottomCenter
                            )
                        )
                    }
                    list
                }

                if (subSteps.isNotEmpty() && currentStepIndex < subSteps.size) {
                    val step = subSteps[currentStepIndex]
                    val isLastStep = currentStepIndex == subSteps.size - 1
                    val btnText = if (isLastStep) stringResource(R.string.understand) else stringResource(R.string.next)

                    GenericTutorialOverlay(
                        description = step.description,
                        highlightCenter = step.focusCenter,
                        highlightRadius = step.focusRadius,
                        highlightRectSize = step.rectSize,
                        buttonText = btnText,
                        dialogAlignment = step.alignment,
                        onCloseClicked = { permanent ->
                            if (isLastStep) {
                                showTutorialSession = false
                                if (permanent) viewModel.dismissSubMap()
                            } else {
                                currentStepIndex++
                                if (permanent) viewModel.dismissSubMap()
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- COMPONENTE DE ERROR DE CONEXIÓN ---
@Composable
fun ConnectionErrorView(onRetry: () -> Unit, onBack: () -> Unit) {
    val wifiIcons = listOf(
        R.drawable.wifi_1_bar,
        R.drawable.wifi_2_bar,
        R.drawable.wifi
    )
    var currentIconIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            currentIconIndex = (currentIconIndex + 1) % wifiIcons.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                painter = painterResource(id = wifiIcons[currentIconIndex]),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.error_connection_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.error_connection_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.retry))
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = stringResource(R.string.go_back),
                tint = Color.Black
            )
        }
    }
}

// --- Helper para crear Paths ---
private fun createPathFromFirebase(figura: FiguraData): Path {
    val path = Path()
    if (figura.path.isEmpty()) return path
    path.moveTo(figura.path[0].x, figura.path[0].y)
    for (i in 1 until figura.path.size) {
        path.lineTo(figura.path[i].x, figura.path[i].y)
    }
    path.close()
    val matrix = Matrix().apply {
        setScale(figura.scale, figura.scale)
        postTranslate(figura.offsetX, figura.offsetY)
    }
    path.transform(matrix)
    return path
}