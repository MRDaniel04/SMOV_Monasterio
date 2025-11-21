package com.nextapp.monasterio.ui.virtualvisit.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.navigation.NavHostController
import com.nextapp.monasterio.R
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.models.*
import com.nextapp.monasterio.repository.*
import com.nextapp.monasterio.ui.virtualvisit.components.DebugPhotoView
import com.nextapp.monasterio.ui.virtualvisit.utils.isPointInPath
import com.nextapp.monasterio.ui.virtualvisit.utils.isPointInPinArea
import com.nextapp.monasterio.ui.screens.VirtualVisitRoutes
import kotlinx.coroutines.launch
import android.graphics.Matrix
import android.graphics.Path
import android.util.Log
import androidx.compose.runtime.snapshots.toInt
import androidx.compose.ui.res.stringResource
import com.nextapp.monasterio.viewModels.AjustesViewModel
import com.nextapp.monasterio.viewModels.AuthViewModel

@Composable
fun PlanoScreen(
    viewModel: AjustesViewModel,
    planoId: String, // ← id del documento de Firestore (p.ej. "monasterio_exterior")
    navController: NavController,
    rootNavController: NavHostController? = null
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {}
    }

    val scope = rememberCoroutineScope()

    // --- Estados ---
    var plano by remember { mutableStateOf<PlanoData?>(null) }
    var figuras by remember { mutableStateOf<List<FiguraData>>(emptyList()) }
    var pines by remember { mutableStateOf<List<PinData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // --- Estados visuales ---
    var activePath by remember { mutableStateOf<Path?>(null) }
    var activeHighlight by remember { mutableStateOf<Color?>(null) }
    var isPinPressed by remember { mutableStateOf(false) }

    val initialZoom = 1f
    val planoBackgroundColor = Color(0xFFF5F5F5)

    val botonesVisibles by viewModel.botonesVisibles.collectAsState()

    // --- Efecto de parpadeo (blink) ---
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkTransition")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // --- Carga inicial del plano + figuras + pines ---
    LaunchedEffect(planoId) {
        isLoading = true
        try {
            plano = PlanoRepository.getPlanoById(planoId)

            if (plano != null) {
                Log.d("PlanoUniversal", "✅ Plano cargado: ${plano!!.nombre}")
                // Cargar figuras
                val figuraRefs = plano!!.figuras.map { it.substringAfterLast("/") }
                figuras = FiguraRepository.getAllFiguras().filter { figuraRefs.contains(it.id) }

                // Cargar pines
                val pinRefs = plano!!.pines.map { it.substringAfterLast("/") }
                pines = PinRepository.getAllPins().filter { pinRefs.contains(it.id) }

                Log.d("PlanoUniversal", "🟢 Figuras: ${figuras.size}, Pines: ${pines.size}")
            } else {
                Log.e("PlanoUniversal", "⚠️ Plano no encontrado con id=$planoId")
            }
        } catch (e: Exception) {
            Log.e("PlanoUniversal", "❌ Error cargando plano", e)
            Toast.makeText(context, context.getString(R.string.error_loading_plane), Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // --- UI principal ---
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

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                DebugPhotoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )

                    // 🔹 Cargar imagen del plano (URL)
                    plano?.let { p ->
                        setImageFromUrl(p.plano)
                    }

                    post { setScale(initialZoom, true) }
                    this.pins = emptyList()
                }
            },
            update = { photoView ->
                // 🔹 Actualizar zonas interactivas de figuras
                photoView.staticZones = figuras.map { figura ->
                    DebugPhotoView.StaticZoneData(
                        path = createPathFromFirebase(figura),
                        color = figura.colorResaltado.toUInt().toInt()
                    )
                }

                // 🔹 Actualizar pines
                photoView.pins = pines.map {
                    DebugPhotoView.PinData(
                        x = it.x,
                        y = it.y,
                        iconId = R.drawable.pin3,
                        isPressed = isPinPressed
                    )
                }

                // 🔹 Actualizar estado dinámico
                photoView.blinkingAlpha = blinkAlpha
                photoView.interactivePath = activePath
                photoView.highlightColor = activeHighlight?.toArgb() ?: Color.Transparent.toArgb()

                photoView.invalidate()

                // 👆 Listener de tap
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
                                    "detalle" -> {
                                        navController.navigate("${VirtualVisitRoutes.DETALLE_GENERICO}/${figura.valorDestino}")
                                    }

                                    "plano" -> {
                                        val destinoId = figura.valorDestino
                                        if (destinoId.isNotBlank()) {
                                            // 🚀 Usar el navController local (no el root)
                                            navController.navigate("${VirtualVisitRoutes.PLANO}/$destinoId")
                                        } else {
                                            Toast.makeText(context, context.getString(R.string.notdefined_destinyplane), Toast.LENGTH_SHORT).show()
                                        }
                                    }


                                    else -> {
                                        Toast.makeText(context, context.getString(R.string.notdefined_destiny), Toast.LENGTH_SHORT).show()
                                    }
                                }

                            }, 200)
                        }

                        pin != null -> {
                            isPinPressed = true
                            Log.d("PlanoScreen", "🟡 Pin pulsado → ${pin.id}, tipoDestino=${pin.tipoDestino}, valorDestino=${pin.valorDestino}")
                            try {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    try {
                                        isPinPressed = false
                                        val tipo = pin.tipoDestino?.lowercase()
                                        val destino = pin.valorDestino
                                        Log.d("PlanoScreen", "🔹 Dentro del handler → tipo=$tipo, destino=$destino")

                                        when (tipo) {
                                            "ruta" -> {
                                                val destino = pin.valorDestino ?: return@postDelayed
                                                Log.d("PlanoScreen", "➡ Navegando (root) a: $destino con pinId=${pin.id}")
                                                rootNavController?.navigate("${AppRoutes.PIN_ENTRADA_MONASTERIO}/${pin.id}")
                                            }

                                            "detalle" -> {
                                                Log.d("PlanoScreen", "➡ Navegando a detalle: ${pin.id}")
                                                navController.navigate(AppRoutes.PIN_DETALLE + "/${pin.id}")
                                            }
                                            else -> {
                                                Log.w("PlanoScreen", "⚠️ tipoDestino desconocido: $tipo")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("PlanoScreen", "❌ Error dentro del Handler al navegar (interno)", e)
                                    }
                                }, 200)
                            } catch (e: Exception) {
                                Log.e("PlanoScreen", "❌ Error al iniciar el Handler (externo)", e)
                            }
                        }



                        else -> Toast.makeText(context, context.getString(R.string.out_interactive_area), Toast.LENGTH_SHORT).show()
                    }
                }

                photoViewRef = photoView
            }
        )

        // --- Controles de zoom ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if(botonesVisibles) {
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
                            contentDescription = stringResource(R.string.increase),
                            tint = Color.Unspecified
                        )
                    }
                }

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
                            contentDescription = stringResource(R.string.decrease),
                            tint = Color.Unspecified
                        )
                    }
                }

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
                            contentDescription = stringResource(R.string.readjust),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }

        // --- Botón "Atrás" ---
        IconButton(
            onClick = {
                if (!navController.popBackStack()) {
                    if (planoId == "monasterio_exterior") {
                        // Si estamos en el plano raíz, volver al inicio de la app
                        rootNavController?.popBackStack()
                        // o si prefieres ir explícitamente:
                        // rootNavController?.navigate(AppRoutes.INICIO)
                    } else {
                        // Si no es el exterior, volver al exterior como fallback
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
                .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = stringResource(R.string.go_back),
                tint = Color.White
            )
        }

    }
}

// --- Helper para crear Paths de Firebase ---
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