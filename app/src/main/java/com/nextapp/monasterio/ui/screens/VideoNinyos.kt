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
import androidx.media3.common.MediaMetadata
import io.sanghun.compose.video.VideoPlayer
import io.sanghun.compose.video.controller.VideoPlayerControllerConfig
import io.sanghun.compose.video.uri.VideoPlayerMediaItem

@Composable
fun VideoNinyosScreen(
    modifier: Modifier = Modifier,
    // Recibimos el padding por si en el futuro quieres que el video no se meta debajo de la barra
    // aunque generalmente en video se prefiere fullscreen.
    topPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    // Detectar idioma
    val configuration = LocalConfiguration.current
    val language = configuration.locales[0].language

    // URLs de los vídeos
    val videoEspanyol =
        "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1763317610/videoespa%C3%B1ol_agayqb.mp4"
    val videoIngles =
        "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1763317613/videoingles_h8ejvc.mp4"
    val videoAleman =
        "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1763317621/videoaleman_x5he9w.mp4"
    val videoFrances =
        "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1764283280/videofrances_rszxyk.mp4"

    val videoUrlActual = when (language) {
        "es" -> videoEspanyol
        "fr" -> videoFrances
        "de" -> videoAleman
        else -> videoIngles
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Fondo blanco detrás
            .padding(topPadding)
    ) {
        VideoPlayer(
            mediaItems = listOf(
                VideoPlayerMediaItem.NetworkMediaItem(
                    url = videoUrlActual,
                    mediaMetadata = MediaMetadata.EMPTY,
                    mimeType = "null"
                )
            ),
            handleLifecycle = true,
            autoPlay = true,
            usePlayerController = true,
            enablePip = false,
            handleAudioFocus = true,

            controllerConfig = VideoPlayerControllerConfig(
                showSpeedAndPitchOverlay = false,
                showSubtitleButton = false,
                showCurrentTimeAndTotalTime = true,
                showBufferingProgress = true,
                showForwardIncrementButton = true,
                showBackwardIncrementButton = true,
                showBackTrackButton = false,
                showNextTrackButton = false,
                showRepeatModeButton = false,
                showFullScreenButton = true,
                controllerShowTimeMilliSeconds = 3_000,
                controllerAutoShow = true,
            ),
            modifier = Modifier.fillMaxSize().padding(topPadding)
        )
    }
}