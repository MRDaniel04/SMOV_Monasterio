package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoNinyosScreen(
    modifier: Modifier = Modifier,
    // Recibimos el padding por si en el futuro quieres que el video no se meta debajo de la barra
    // aunque generalmente en video se prefiere fullscreen.
    topPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    // URLs de los vídeos
    val videoEspanyol = "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1763317610/videoespa%C3%B1ol_agayqb.mp4"
    val videoIngles = "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1763317613/videoingles_h8ejvc.mp4"
    val videoAleman = "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1763317621/videoaleman_x5he9w.mp4"
    val videoFrances = "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1764283280/videofrances_rszxyk.mp4"

    var isLoading by remember { mutableStateOf(true) }

    // Detectar idioma
    val configuration = LocalConfiguration.current
    val language = configuration.locales[0].language

    // Configurar ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    // Solo mostramos carga si está bufferizando.
                    // Si está listo o reproduciendo, quitamos el spinner.
                    isLoading = playbackState == Player.STATE_BUFFERING
                }
            })

            val videoUrl = when(language){
                "es" -> videoEspanyol
                "de" -> videoAleman
                "fr" -> videoFrances
                else -> videoIngles
            }

            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    // Gestión del ciclo de vida y orientación
    DisposableEffect(Unit) {
        // Permitimos que el usuario gire el móvil para ver el vídeo en horizontal
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        onDispose {
            exoPlayer.release()
            // Al salir, podríamos forzar vertical de nuevo si tu app es mayormente vertical
            // activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // ESTRUCTURA PRINCIPAL (Usando Box en vez de ConstraintLayout)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            // Aplicamos el padding para que la barra roja no tape el video (opcional)
            .padding(topPadding),
        contentAlignment = Alignment.Center
    ) {
        // 1. REPRODUCTOR DE VÍDEO
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true // Muestra los controles de play/pause
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. INDICADOR DE CARGA (Centrado encima)
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White
            )
        }
    }
}