package com.nextapp.monasterio.ui.virtualvisit.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.load
import com.github.chrisbanes.photoview.PhotoView
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.models.Ubicacion
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
// Imports de Media3 (ExoPlayer)
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import com.nextapp.monasterio.viewModels.AjustesViewModel
import com.nextapp.monasterio.ui.virtualvisit.components.GenericTutorialOverlay
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size // Importante para el foco cuadrado

// Clase privada para definir los pasos del tutorial del Pin
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
    // Índice para recorrer los pasos (Atrás -> Imagen -> Audio -> 360)
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val imagenes = if (pin.imagenesDetalladas.isNotEmpty()) pin.imagenesDetalladas else emptyList()

    val pagerState = rememberPagerState(pageCount = { imagenes.size })
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageTitle by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val locale: Locale = configuration.locales[0]
    val language = locale.language

    // --- Textos Multilingües ---
    val titulo_pin: String
    val descripcion_pin: String?
    val ubicacion_pin: Ubicacion?

    when (language) {
        "es" -> {
            titulo_pin = pin.titulo
            descripcion_pin = pin.descripcion
            ubicacion_pin = pin.ubicacion
        }
        "de" -> {
            titulo_pin = pin.tituloAleman
            descripcion_pin = pin.descripcionAleman
            ubicacion_pin = pin.ubicacionAleman
        }
        else -> {
            titulo_pin = pin.tituloIngles
            descripcion_pin = pin.descripcionIngles
            ubicacion_pin = pin.ubicacionIngles
        }
    }

    // --- AUDIO ---
    val context = LocalContext.current
    val audioUrl = when (language) {
        "es" -> pin.audioUrl_es
        "en" -> pin.audioUrl_en
        "de" -> pin.audioUrl_ge
        else -> pin.audioUrl_es
    }

    Log.d("AudioDebug", "Pin: '${pin.titulo}', URL Audio: [$audioUrl]")

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

    // =====================================================================
    // CÁLCULO DE COORDENADAS PARA EL TUTORIAL
    // =====================================================================
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // 1. Botón Atrás (Aprox Top-Left)
    val backButtonPos = remember(density) {
        with(density) { Offset(30.dp.toPx(), 85.dp.toPx()) }
    }

    // 2. Imagen Principal (Carrusel) - Aprox Centro-Superior
    // Título(~100dp) + Imagen(250dp). Centro aprox en Y=240dp
    val imageCenterPos = remember(density) {
        with(density) { Offset(screenWidthPx / 2, 280.dp.toPx()) }
    }
    val imageRectSize = remember(density, screenWidthPx) {
        with(density) { Size(screenWidthPx - 32.dp.toPx(), 250.dp.toPx()) }
    }

    // 3. Botón Audio - Debajo de la imagen, izquierda.
    // Aprox Y = 100(titulo) + 250(img) + 20(pads) + 24(mitad btn) ≈ 400dp
    val audioBtnPos = remember(density) {
        with(density) { Offset(40.dp.toPx(), 450.dp.toPx()) }
    }

    // 4. Botón 360 - Abajo del todo
    val btn360Pos = remember(density, screenHeightPx) {
        with(density) { Offset(screenWidthPx / 2, screenHeightPx - 100.dp.toPx()) }
    }
    val btn360Size = remember(density, screenWidthPx) {
        with(density) { Size(screenWidthPx - 32.dp.toPx(), 60.dp.toPx()) }
    }


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
            // 🔙 Botón atrás
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.go_back),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBack() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildString {
                    append(titulo_pin)
                    ubicacion_pin?.let { append(" (${it.displayName})") }
                },
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🖼️ Carrusel
            if (imagenes.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
                ) {
                    HorizontalPager(state = pagerState) { page ->
                        val imagen = imagenes[page]
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = imagen.url,
                                contentDescription = imagen.etiqueta,
                                contentScale = ContentScale.Crop,
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
                        Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                    ) {
                        repeat(imagenes.size) { index ->
                            val selected = pagerState.currentPage == index
                            Box(
                                Modifier
                                    .padding(3.dp)
                                    .size(if (selected) 8.dp else 6.dp)
                                    .background(Color.White.copy(alpha = if (selected) 1f else 0.6f), CircleShape)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 🔊 Botón Audio
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!audioUrl.isNullOrBlank()) {
                    val iconRes = when {
                        isPlaying -> R.drawable.pause
                        !hasUserInteracted -> R.drawable.microphone
                        else -> R.drawable.play_arrow
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = {
                            hasUserInteracted = true
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        }) {
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

            // 📝 Descripción
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

            Spacer(modifier=Modifier.height(24.dp))

            // 🔘 Botón 360
            onVer360?.let {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    Button(
                        onClick = it,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
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
        // LOGICA VISUAL DEL TUTORIAL
        // ====================================================================

        if (!isPinDismissed && showTutorialSession) {
            // Construimos la lista de pasos dinámicamente
            // (Solo mostramos el paso de audio si hay audio, y el de 360 si hay 360)
            val steps = remember(audioUrl, onVer360) {
                val list = mutableListOf<PinTutorialStep>()

                // PASO 1: Volver (Círculo, Texto Abajo)
                list.add(
                    PinTutorialStep(
                        description = activity?.getString(R.string.pin_1) ?: "Volver",
                        focusCenter = backButtonPos,
                        focusRadius = 70f,
                        alignment = Alignment.BottomCenter
                    )
                )

                // PASO 2: Imágenes (Cuadrado, Texto Abajo)
                list.add(
                    PinTutorialStep(
                        description = activity?.getString(R.string.pin_2) ?: "Volver",
                        focusCenter = imageCenterPos,
                        rectSize = imageRectSize,
                        alignment = Alignment.BottomCenter
                    )
                )

                // PASO 3: Audio (Solo si existe) (Círculo, Texto Abajo)
                if (!audioUrl.isNullOrBlank()) {
                    list.add(
                        PinTutorialStep(
                            description = activity?.getString(R.string.pin_3) ?: "Volver",
                            focusCenter = audioBtnPos,
                            focusRadius = 80f,
                            alignment = Alignment.BottomCenter
                        )
                    )
                }

                // PASO 4: Botón 360 (Solo si existe) (Rectángulo, Texto ARRIBA)
                if (onVer360 != null) {
                    list.add(
                        PinTutorialStep(
                            description = activity?.getString(R.string.pin_4) ?: "Volver",
                            focusCenter = btn360Pos,
                            rectSize = btn360Size,
                            alignment = Alignment.TopCenter // 👈 EL CAMBIO CLAVE: Texto arriba
                        )
                    )
                }
                list
            }

            if (currentStepIndex < steps.size) {
                val step = steps[currentStepIndex]
                val isLastStep = currentStepIndex == steps.size - 1
                val btnText = if (isLastStep) stringResource(R.string.understand) else stringResource(R.string.next)

                GenericTutorialOverlay(
                    description = step.description,
                    highlightCenter = step.focusCenter,
                    highlightRadius = step.focusRadius,
                    highlightRectSize = step.rectSize,
                    buttonText = btnText,
                    dialogAlignment = step.alignment, // 👈 Pasamos la alineación

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
                factory = { context -> PhotoView(context).apply { load(imageUrl) { crossfade(true) } } },
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
                    .align(Alignment.TopEnd)
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