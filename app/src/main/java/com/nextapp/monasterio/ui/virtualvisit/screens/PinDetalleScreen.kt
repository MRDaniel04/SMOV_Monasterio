package com.nextapp.monasterio.ui.virtualvisit.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import coil.load
import com.github.chrisbanes.photoview.PhotoView
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.models.Ubicacion
import com.nextapp.monasterio.ui.virtualvisit.components.GenericTutorialOverlay
import com.nextapp.monasterio.viewModels.AjustesViewModel
import java.util.Locale


private data class PinTutorialStep(
    val description: String,
    val focusCenter: Offset,
    val focusRadius: Float = 0f,      // Para círculos
    val rectSize: Size? = null,       // Para cuadrados/rectángulos
    val alignment: Alignment = Alignment.BottomCenter // Dónde poner el cuadro de texto
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinDetalleScreen(
    viewModel: AjustesViewModel,
    pin: PinData,
    onBack: () -> Unit,
    onVer360: (() -> Unit)? = null
) {
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
    }

    val activity = view.context as? Activity

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // --- LÓGICA DE ESTADO DEL TUTORIAL ---
    val isPinDismissed by viewModel.isPinDismissed.collectAsState()
    var showTutorialSession by remember { mutableStateOf(true) }
    var currentStepIndex by remember { mutableIntStateOf(0) }

    // --- VARIABLES PARA CAPTURAR POSICIONES REALES (LAYOUT) ---
    // Estas variables se llenarán cuando Android dibuje la pantalla
    var backButtonLayout by remember { mutableStateOf<Pair<Offset, Size>?>(null) }
    var imageLayout by remember { mutableStateOf<Pair<Offset, Size>?>(null) }
    var audioButtonLayout by remember { mutableStateOf<Pair<Offset, Size>?>(null) }
    var button360Layout by remember { mutableStateOf<Pair<Offset, Size>?>(null) }

    // --- Datos del Pin ---
    val imagenes = if (pin.imagenesDetalladas.isNotEmpty()) pin.imagenesDetalladas else emptyList()
    val pagerState = rememberPagerState(pageCount = { imagenes.size })
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageTitle by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isTablet = configuration.screenWidthDp > 600
    var carouselHeight = 0.dp
    if(isTablet){carouselHeight = screenHeight*0.5f} else {carouselHeight = screenHeight*0.35f}
    val locale: Locale = configuration.locales[0]
    val language = locale.language

    // --- Textos Multilingües ---
    val titulo_pin: String // Ubicación compleja
    val descripcion_pin: String // Descripción detallada
    val area_pin: String? // Nombre del Área (anteriormente Ubicacion)

    when (language) {
        "de" -> {
            titulo_pin = pin.ubicacion_de.orEmpty().ifBlank { pin.ubicacion_es.orEmpty() }
            descripcion_pin = pin.descripcion_de.orEmpty().ifBlank { pin.descripcion_es.orEmpty() }
            area_pin = pin.area_de
        }
        "fr" -> {
            titulo_pin = pin.ubicacion_fr.orEmpty().ifBlank { pin.ubicacion_es.orEmpty() }
            descripcion_pin = pin.descripcion_fr.orEmpty().ifBlank { pin.descripcion_es.orEmpty() }
            area_pin = pin.area_fr
        }
        "en" -> {
            titulo_pin = pin.ubicacion_en.orEmpty().ifBlank { pin.ubicacion_es.orEmpty() }
            descripcion_pin = pin.descripcion_en.orEmpty().ifBlank { pin.descripcion_es.orEmpty() }
            area_pin = pin.area_en
        }
        else -> { // Español por defecto
            titulo_pin = pin.ubicacion_es.orEmpty()
            descripcion_pin = pin.descripcion_es.orEmpty()
            area_pin = pin.area_es
        }
    }

    // --- LÓGICA DE AUDIO ---
    val context = LocalContext.current
    val audioUrl = when (language) {
        "en" -> pin.audioUrl_en
        "de" -> pin.audioUrl_de
        "fr" -> pin.audioUrl_fr
        else -> pin.audioUrl_es
    }

    Log.d("AudioDebug", "Pin: '${pin.ubicacion_es}', Idioma: $language, URL Audio: [$audioUrl]")

    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPlaying by remember { mutableStateOf(false) }
    var hasUserInteracted by remember { mutableStateOf(false) }

    DisposableEffect(audioUrl) {
        if (!audioUrl.isNullOrBlank()) {
            val mediaItem = MediaItem.fromUri(audioUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
        onDispose { }
    }

    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    isPlaying = false
                    exoPlayer.seekTo(0)
                }
            }
        })
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // --- UI PRINCIPAL ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- CABECERA UNIFICADA (Flecha + Título) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp) // Margen superior general
            ) {
                // 1. BOTÓN ATRÁS (Alineado a la Izquierda)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart) // 👈 Lo pegamos a la izquierda
                        // 👇 MANTENEMOS TU LÓGICA DE POSICIÓN PARA EL TUTORIAL
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInRoot()
                            val size = coordinates.size
                            backButtonLayout = position to Size(size.width.toFloat(), size.height.toFloat())
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.go_back),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onBack() }
                    )
                }


                Text(
                    text = buildString {
                        append(titulo_pin)
                        area_pin?.let { if (it.isNotBlank()) append(" ($it)") }
                    },
                    // Bajamos un poco la fuente (de 26 a 22) para que quepa mejor junto a la flecha
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                    modifier = Modifier
                        .align(Alignment.Center) // 👈 Lo centramos en la pantalla
                        .padding(horizontal = 48.dp) // 👈 Importante: Margen para no escribir encima de la flecha si el texto es largo
                )
            }

            // Espacio antes de la imagen
            Spacer(modifier = Modifier.height(16.dp))

            // 2. CARRUSEL DE IMÁGENES
            if (imagenes.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(carouselHeight)
                        .clip(RoundedCornerShape(16.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
                        // 👇 CAPTURAMOS POSICIÓN REAL
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInRoot()
                            val size = coordinates.size
                            imageLayout = position to Size(size.width.toFloat(), size.height.toFloat())
                        }
                ) {
                    HorizontalPager(state = pagerState) { page ->
                        val imagen = imagenes[page]
                        Box(modifier = Modifier.fillMaxSize()) {

                            val alignment = remember{
                                BiasAlignment(horizontalBias = 0f, verticalBias = imagen.foco)
                            }


                            AsyncImage(
                                model = imagen.url,
                                contentDescription = imagen.etiqueta,
                                contentScale = ContentScale.Crop,
                                alignment = alignment,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        selectedImageUrl = imagen.url
                                        selectedImageTitle = when (language) {
                                            "es" -> imagen.titulo
                                            "de" -> imagen.tituloAleman
                                            else -> imagen.tituloIngles
                                        }
                                    }
                            )
                            Text(
                                text = imagen.etiqueta,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Row(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    ) {
                        repeat(imagenes.size) { index ->
                            val selected = pagerState.currentPage == index
                            Box(
                                Modifier
                                    .padding(3.dp)
                                    .size(if (selected) 8.dp else 6.dp)
                                    .background(
                                        Color.White.copy(alpha = if (selected) 1f else 0.6f),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3. BOTÓN DE AUDIO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!audioUrl.isNullOrBlank()) {
                    val iconRes = when {
                        isPlaying -> R.drawable.pause
                        !hasUserInteracted -> R.drawable.sound
                        else -> R.drawable.play_arrow
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = {
                                hasUserInteracted = true
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            },
                            modifier = Modifier
                                // 👇 CAPTURAMOS POSICIÓN REAL
                                .onGloballyPositioned { coordinates ->
                                    val position = coordinates.positionInRoot()
                                    val size = coordinates.size
                                    audioButtonLayout = position to Size(size.width.toFloat(), size.height.toFloat())
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = stringResource(R.string.play_audio),
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Descripción Deslizable
            val descripcion = descripcion_pin ?: ""
            if (descripcion.isNotBlank()) {
                val textScrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = descripcion,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .verticalScroll(textScrollState)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. BOTÓN 360
            onVer360?.let {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInRoot()
                            val size = coordinates.size
                            button360Layout = position to Size(size.width.toFloat(), size.height.toFloat())
                        }
                ) {
                    Button(
                        onClick = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.see_360),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // ====================================================================
        // LOGICA VISUAL DEL TUTORIAL (CON COORDENADAS REALES)
        // ====================================================================

        // Verificar que tenemos al menos las coordenadas básicas (Botón atrás es lo mínimo)
        val isLayoutReady = backButtonLayout != null

        if (!isPinDismissed && showTutorialSession && isLayoutReady) {

            // Construimos la lista de pasos usando las coordenadas CAPTURADAS
            val steps = remember(backButtonLayout, imageLayout, audioButtonLayout, button360Layout, audioUrl, onVer360) {
                val list = mutableListOf<PinTutorialStep>()

                // PASO 1: Volver (Usamos las coordenadas reales del botón atrás)
                backButtonLayout?.let { (pos, size) ->
                    list.add(
                        PinTutorialStep(
                            description = activity?.getString(R.string.pin_1) ?: "Volver",
                            // Calculamos el centro geométrico
                            focusCenter = Offset(pos.x + size.width / 2, pos.y + size.height / 2),
                            // Radio un poco más grande que el botón
                            focusRadius = (size.width),
                            alignment = Alignment.BottomCenter
                        )
                    )
                }

                // PASO 2: Imágenes (Usamos coordenadas reales del carrusel)
                imageLayout?.let { (pos, size) ->
                    list.add(
                        PinTutorialStep(
                            description = activity?.getString(R.string.pin_2) ?: "Imágenes",
                            focusCenter = Offset(pos.x + size.width / 2, pos.y + size.height / 2),
                            rectSize = size, // Usamos el tamaño exacto
                            alignment = Alignment.BottomCenter
                        )
                    )
                }

                // PASO 3: Audio (Solo si existe y se ha medido)
                if (!audioUrl.isNullOrBlank() && audioButtonLayout != null) {
                    audioButtonLayout?.let { (pos, size) ->
                        list.add(
                            PinTutorialStep(
                                description = activity?.getString(R.string.pin_3) ?: "Audio",
                                focusCenter = Offset(pos.x + size.width / 2, pos.y + size.height / 2),
                                focusRadius = (size.width / 2) + 20f,
                                alignment = Alignment.TopCenter
                            )
                        )
                    }
                }

                // PASO 4: Botón 360 (Solo si existe y se ha medido)
                if (onVer360 != null && button360Layout != null) {
                    button360Layout?.let { (pos, size) ->
                        list.add(
                            PinTutorialStep(
                                description = activity?.getString(R.string.pin_4) ?: "360",
                                focusCenter = Offset(pos.x + size.width / 2, pos.y + size.height / 2),
                                rectSize = size,
                                alignment = Alignment.TopCenter // Dialogo ARRIBA
                            )
                        )
                    }
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
                    buttonText = btnText,
                    dialogAlignment = step.alignment,

                    onCloseClicked = { permanent ->
                        if (isLastStep) {
                            showTutorialSession = false
                            if (permanent) viewModel.dismissPin()
                        } else {
                            currentStepIndex++
                            if (permanent) viewModel.dismissPin()
                        }
                    }
                )
            }
        }

        // 🔍 Zoom Dialog
        if (selectedImageUrl != null) {
            ZoomableImageDialog(
                imageUrl = selectedImageUrl!!,
                label = selectedImageTitle ?: "",
                onDismiss = {
                    selectedImageUrl = null
                    selectedImageTitle = null
                }
            )
        }
    }
}

@Composable
private fun ZoomableImageDialog(imageUrl: String, label: String, onDismiss: () -> Unit) {
    val view = LocalView.current
    val activity = view.context as? Activity

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f))
        ) {
            AndroidView(
                factory = { context ->
                    PhotoView(context).apply {
                        load(imageUrl) { crossfade(true) }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.close),
                    tint = Color.White
                )
            }
        }
    }
}