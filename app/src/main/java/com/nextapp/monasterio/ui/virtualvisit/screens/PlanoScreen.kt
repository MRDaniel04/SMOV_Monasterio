package com.nextapp.monasterio.ui.virtualvisit.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

// Clase auxiliar para definir los pasos del tutorial
private data class TutorialStep(
    val title: String? = null,
    val description: String,
    val focusCenter: Offset,
    val focusRadius: Float
)

@Composable
fun PlanoScreen(
    viewModel: AjustesViewModel,
    planoId: String,
    navController: NavController,
    rootNavController: NavHostController? = null
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {}
    }

    // -----------------------------------------------------------
    // 1. CÁLCULOS DE COORDENADAS PARA LOS FOCOS DEL TUTORIAL
    // -----------------------------------------------------------
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // A. Centro de la pantalla (Para el Mapa / Bienvenida)
    val centerOffset = remember(configuration) {
        Offset(screenWidthPx / 3.5f, screenHeightPx / 2.1f)
    }

    val backButtonOffset = remember(density) {
        Offset(screenWidthPx / 9.5f, screenHeightPx / 8.6f)
    }

    val rightbuttons = remember(density) {
        Offset(screenWidthPx / 1f, screenHeightPx / 2.2f)
    }

    val pins = remember(density) {
        Offset(screenWidthPx / 2.5f, screenHeightPx / 6f)
    }

    // C. Controles de Zoom (Abajo Derecha)
    val zoomControlsOffset = remember(density, configuration) {
        with(density) {
            Offset(
                x = screenWidthPx - 40.dp.toPx(),
                y = screenHeightPx - 160.dp.toPx()
            )
        }
    }

    val scope = rememberCoroutineScope()

    // --- Estados de Datos ---
    var plano by remember { mutableStateOf<PlanoData?>(null) }
    var figuras by remember { mutableStateOf<List<FiguraData>>(emptyList()) }
    var pines by remember { mutableStateOf<List<PinData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // --- Estados Visuales ---
    var activePath by remember { mutableStateOf<Path?>(null) }
    var activeHighlight by remember { mutableStateOf<Color?>(null) }
    var isPinPressed by remember { mutableStateOf(false) }
    // --- LOGICA DE TUTORIAL ---
    val isMainDismissed by viewModel.isMainMapDismissed.collectAsState()
    val isSubDismissed by viewModel.isSubMapDismissed.collectAsState()
    var showTutorialSession by remember { mutableStateOf(true) }

    // Estado para saber en qué paso del tutorial estamos (0, 1, 2...)
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val initialZoom = 1f
    val planoBackgroundColor = Color(0xFFF5F5F5)
    val botonesVisibles by viewModel.botonesVisibles.collectAsState()

    // --- Efecto de parpadeo ---
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkTransition")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse)
    )

    // --- Carga inicial ---
    LaunchedEffect(planoId) {
        isLoading = true
        showTutorialSession = true
        currentStepIndex = 0 // Reiniciamos los pasos al cambiar de plano
        try {
            plano = PlanoRepository.getPlanoById(planoId)
            if (plano != null) {
                Log.d("PlanoUniversal", "✅ Plano cargado: ${plano!!.nombre}")
                val figuraRefs = plano!!.figuras.map { it.substringAfterLast("/") }
                figuras = FiguraRepository.getAllFiguras().filter { figuraRefs.contains(it.id) }
                val pinRefs = plano!!.pines.map { it.substringAfterLast("/") }
                pines = PinRepository.getAllPins().filter { pinRefs.contains(it.id) }
            } else {
                Log.e("PlanoUniversal", "⚠️ Plano no encontrado con id=$planoId")
            }
        } catch (e: Exception) {
            Log.e("PlanoUniversal", "❌ Error cargando plano", e)
            Toast.makeText(
                context,
                context.getString(R.string.error_loading_plane),
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            isLoading = false
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
                Text(stringResource(R.string.plane_loading))
            }
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
            update = { photoView ->
                photoView.staticZones = figuras.map {
                    DebugPhotoView.StaticZoneData(
                        createPathFromFirebase(it), // Ahora sí encontrará la función
                        it.colorResaltado.toUInt().toInt()
                    )
                }
                photoView.pins =
                    pines.map { DebugPhotoView.PinData(it.x, it.y, R.drawable.pin3, isPinPressed) }
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
                                        if (destinoId.isNotBlank()) navController.navigate("${VirtualVisitRoutes.PLANO}/$destinoId")
                                        else Toast.makeText(
                                            context,
                                            context.getString(R.string.notdefined_destinyplane),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    else -> Toast.makeText(
                                        context,
                                        context.getString(R.string.notdefined_destiny),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }, 200)
                        }

                        pin != null -> {
                            isPinPressed = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                isPinPressed = false
                                when (pin.tipoDestino?.lowercase()) {
                                    "ruta" -> if (pin.valorDestino != null) rootNavController?.navigate(
                                        "${AppRoutes.PIN_ENTRADA_MONASTERIO}/${pin.id}"
                                    )
                                    "detalle" -> navController.navigate(AppRoutes.PIN_DETALLE + "/${pin.id}")
                                }
                            }, 200)
                        }

                        else -> Toast.makeText(
                            context,
                            context.getString(R.string.out_interactive_area),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                photoViewRef = photoView
            }
        )

        // 2. Controles de Zoom
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (botonesVisibles) {
                FloatingActionButton(onClick = {
                    photoViewRef?.let {
                        it.setScale(
                            (it.scale + 0.2f).coerceAtMost(
                                it.maximumScale
                            ), true
                        )
                    }
                }) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.aumentar_zoom),
                            contentDescription = stringResource(R.string.increase),
                            tint = Color.Unspecified
                        )
                    }
                }
                FloatingActionButton(onClick = {
                    photoViewRef?.let {
                        it.setScale(
                            (it.scale - 0.2f).coerceAtLeast(
                                it.minimumScale
                            ), true
                        )
                    }
                }) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.disminuir_zoom),
                            contentDescription = stringResource(R.string.decrease),
                            tint = Color.Unspecified
                        )
                    }
                }
                FloatingActionButton(onClick = {
                    photoViewRef?.apply {
                        setScale(
                            initialZoom,
                            true
                        ); setTranslationX(0f); setTranslationY(0f)
                    }
                }) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.reajustar),
                            contentDescription = stringResource(R.string.readjust),
                            tint = Color.Unspecified
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
                        navController.navigate("${VirtualVisitRoutes.PLANO}/monasterio_exterior") {
                            popUpTo("${VirtualVisitRoutes.PLANO}/monasterio_exterior") {
                                inclusive = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = stringResource(R.string.go_back),
                tint = Color.White
            )
        }

        // 4. TUTORIALES (Lógica Step-by-Step)
        val isMainMap = (planoId == "monasterio_exterior")

        if (showTutorialSession) {
            if (isMainMap && !isMainDismissed) {
                // --- TUTORIAL MAPA PRINCIPAL (3 Pasos) ---
                val steps = listOf(
                    // Paso 0: Bienvenida -> Foco Centro
                    TutorialStep(
                        description = stringResource(R.string.map_tutorial_body1),
                        focusCenter = backButtonOffset,
                        focusRadius = 120f
                    ),
                    // Paso 1: Zoom -> Foco Abajo Derecha
                    TutorialStep(
                        description = stringResource(R.string.map_tutorial_body1_2),
                        focusCenter = centerOffset,
                        focusRadius = 220f
                    ),
                    // Paso 2: Atrás -> Foco Arriba Izquierda
                    TutorialStep(
                        description = stringResource(R.string.map_tutorial_body1_3),
                        focusCenter = rightbuttons,
                        focusRadius = 400f
                    )
                )

                if (currentStepIndex < steps.size) {
                    val step = steps[currentStepIndex]
                    val isLastStep = currentStepIndex == steps.size - 1
                    val btnText =
                        if (isLastStep) stringResource(R.string.understand) else stringResource(R.string.next)

                    GenericTutorialOverlay(
                        title = step.title,
                        description = step.description,
                        highlightCenter = step.focusCenter,
                        highlightRadius = step.focusRadius,
                        buttonText = btnText, // Pasamos el texto dinámico
                        onCloseClicked = { permanent ->
                            if (isLastStep) {
                                // Fin del tutorial
                                showTutorialSession = false
                                if (permanent) viewModel.dismissMainMap()
                            } else {
                                // Siguiente paso
                                currentStepIndex++
                                if (permanent) viewModel.dismissMainMap() // Guardar preferencia si marca
                            }
                        }
                    )
                }

            } else if (!isMainMap && !isSubDismissed) {
                // --- TUTORIAL SUB-MAPA (2 Pasos) ---

                val subSteps = listOf(
                    // PASO 1: Bienvenida a la sala (Foco Circular en el centro)
                    TutorialStep(
                        description = stringResource(R.string.submapa_1),
                        focusCenter = backButtonOffset,
                        focusRadius = 120f
                    ),

                    // PASO 2: Cómo salir (Foco Cuadrado en el botón atrás)
                    TutorialStep(
                        description = stringResource(R.string.submapa_2),
                        focusCenter = pins,
                        focusRadius = 300f,
                    )
                )

                // Lógica de control de pasos
                if (currentStepIndex < subSteps.size) {
                    val step = subSteps[currentStepIndex]
                    val isLastStep = currentStepIndex == subSteps.size - 1
                    val btnText =
                        if (isLastStep) stringResource(R.string.understand) else stringResource(R.string.next)

                    GenericTutorialOverlay(
                        description = step.description,
                        highlightCenter = step.focusCenter,
                        highlightRadius = step.focusRadius,
                        buttonText = btnText,
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
} // <--- AQUÍ TERMINA LA FUNCIÓN PlanoScreen

// --- Helper para crear Paths (AHORA ESTÁ FUERA, COMO DEBE SER) ---
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