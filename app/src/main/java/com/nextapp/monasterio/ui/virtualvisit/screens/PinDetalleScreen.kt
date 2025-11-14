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
import androidx.compose.ui.platform.LocalContext // <-- ¡NUEVO IMPORT!
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinDetalleScreen(
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

    val imagenes = if (pin.imagenesDetalladas.isNotEmpty()) pin.imagenesDetalladas else emptyList()

    val pagerState = rememberPagerState(pageCount = { imagenes.size })
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageTitle by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val locale: Locale = configuration.locales[0]
    val language = locale.language

    // --- Lógica de Texto Multilingüe (Tu código) ---
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

    // --- ¡¡NUEVA LÓGICA DE AUDIO!! ---
    val context = LocalContext.current

    // 1. Selecciona la URL de audio correcta
    val audioUrl = when (language) {
        "es" -> pin.audioUrl_es
        "en" -> pin.audioUrl_en
        "de" -> pin.audioUrl_ge
        else -> pin.audioUrl_es
    }

    Log.d(
        "AudioDebug",
        "Pin: '${pin.titulo}', Idioma: $language, URL de Audio Seleccionada: [$audioUrl]"
    )

    // 2. Configura el ExoPlayer
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackState by remember { mutableStateOf(Player.STATE_IDLE) }
    var hasUserInteracted by remember { mutableStateOf(false) }

    // 3. Prepara el player si la URL cambia (y no es nula)
    DisposableEffect(audioUrl) {
        if (!audioUrl.isNullOrBlank()) {
            val mediaItem = MediaItem.fromUri(audioUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
        // (No necesitamos onDispose aquí, se libera abajo)
        onDispose { }
    }

    // 4. Listener para actualizar el botón Play/Pause y rebobinar
    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }
            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state

                if (state == Player.STATE_ENDED) {
                    isPlaying = false
                    exoPlayer.seekTo(0)
                }
            }
        })
    }

    // 5. Libera el player al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    // --- FIN LÓGICA DE AUDIO ---

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

            // 🖼️ Carrusel con etiqueta (se queda igual)
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

// 👇 título según idioma actual

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

                                        shape = CircleShape

                                    )

                            )

                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de Audio (solo si hay URL)
                if (!audioUrl.isNullOrBlank()) {
                    // 1. Determina el icono correcto
                    val iconRes = when {
                        isPlaying -> R.drawable.pause
                        !hasUserInteracted -> R.drawable.microphone
                        else -> R.drawable.play_arrow
                    }

                    // 2. Botón de audio con redondel, alineado a la derecha del título
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp), // Espacio entre el título y el botón
                        horizontalArrangement = Arrangement.Start // Alinea a la derecha
                    ) {
                        IconButton(onClick = {
                            hasUserInteracted = true
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        }) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp) // Tamaño del redondel
                                    .clip(CircleShape) // Forma redonda
                                    .background(Color.Gray.copy(alpha = 0.3f)), // Color de fondo del redondel (ej. gris suave)
                                contentAlignment = Alignment.Center // Centra el icono dentro del redondel
                            ) {
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = stringResource(R.string.play_audio),
                                    tint = Color.Black, // Color del icono
                                    modifier = Modifier.size(28.dp) // Tamaño del icono dentro del redondel
                                )
                            }
                        }
                    }
                }
            }

            // 📝 Descripción (se queda igual)
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

            // 🔘 Botón 360 (se queda igual)
            onVer360?.let {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
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

        // 🔍 Zoom con título traducido (se queda igual)
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

// 🔍 Diálogo de zoom (se queda igual)
@Composable
private fun ZoomableImageDialog(imageUrl: String, label: String, onDismiss: () -> Unit) {

    val view =LocalView.current
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
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            AndroidView(
                factory = { context ->
                    PhotoView(context).apply {
                        load(imageUrl) { crossfade(true) }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 🔹 Título (ya traducido según idioma)
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

            // ❌ Botón cerrar
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