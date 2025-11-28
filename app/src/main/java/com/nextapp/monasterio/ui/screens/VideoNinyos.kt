package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView


@Composable
fun VideoNinyosScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val activity = (context as? Activity)

    val videoEspanyol = "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1763317610/videoespa%C3%B1ol_agayqb.mp4"
    val videoIngles = "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1763317613/videoingles_h8ejvc.mp4"
    val videoAleman = "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1763317621/videoaleman_x5he9w.mp4"
    val videoFrances = "https://res.cloudinary.com/drx7mujrv/video/upload/f_auto,q_auto:low,w_auto:1000/v1764283280/videofrances_rszxyk.mp4"

    var isLoading by remember { mutableStateOf(true) }

    val configuration = LocalConfiguration.current
    val language = configuration.locales[0].language

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
           addListener(object : Player.Listener{
               override fun onPlaybackStateChanged(playbackState: Int) {
                   isLoading = when(playbackState){
                        Player.STATE_BUFFERING -> true
                        Player.STATE_READY -> false
                        else -> false
                   }
               }
           })
            when(language){
                "es" -> {
                    setMediaItem(MediaItem.fromUri(videoEspanyol))
                    prepare()
                    playWhenReady = true
                }
                "de" -> {
                    setMediaItem(MediaItem.fromUri(videoAleman))
                    prepare()
                    playWhenReady = true
                }
                "fr" -> {
                    setMediaItem(MediaItem.fromUri(videoFrances))
                    prepare()
                    playWhenReady = true
                }
                else -> {
                    setMediaItem(MediaItem.fromUri(videoIngles))
                    prepare()
                    playWhenReady = true
                }
            }
        }

    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        onDispose {
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    ConstraintLayout(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val(videoPlayer,loadingSpinner) = createRefs()

        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier
                .constrainAs(videoPlayer){
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.constrainAs(loadingSpinner) {
                    // Lo centramos encima del reproductor
                    top.linkTo(videoPlayer.top)
                    bottom.linkTo(videoPlayer.bottom)
                    start.linkTo(videoPlayer.start)
                    end.linkTo(videoPlayer.end)
                }
            )
        }
    }
}
