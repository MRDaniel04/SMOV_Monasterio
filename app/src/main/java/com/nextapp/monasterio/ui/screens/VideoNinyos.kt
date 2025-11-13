package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.MediaController
import android.widget.VideoView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri


@Composable
fun VideoNinyos(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val activity = (context as? Activity)

    val videoUrl = "https://res.cloudinary.com/drx7mujrv/video/upload/q_auto:good/v1763060561/video.mp4"

    var isLoading by remember { mutableStateOf(true) }

    val videoView = remember {
        VideoView(context).apply {
            val mediaController = MediaController(context)
            mediaController.setAnchorView(this)
            setMediaController(mediaController)

            setOnPreparedListener {
                isLoading = false
            }

            setVideoURI(videoUrl.toUri())
            requestFocus()
            start()
        }
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        onDispose {
            videoView.stopPlayback()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    ConstraintLayout(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val(videoPlayer,loadingSpinner) = createRefs()

        AndroidView(
            factory = {videoView},
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
