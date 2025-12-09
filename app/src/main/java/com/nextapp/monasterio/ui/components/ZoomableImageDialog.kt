package com.nextapp.monasterio.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.load
import com.github.chrisbanes.photoview.PhotoView
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.ImagenData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableImageDialog(
    imagenes: List<ImagenData>,
    initialIndex: Int,
    languageCode: String,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val activity = view.context as? Activity

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    val zoomPagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { imagenes.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {

            HorizontalPager(
                state = zoomPagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val imagen = imagenes[page]
                // Cada página tiene su propio PhotoView para zoom independiente
                AndroidView(
                    factory = { context ->
                        PhotoView(context).apply {
                            load(imagen.url) {
                                crossfade(true)
                            }
                            maximumScale = 5.0f
                            mediumScale = 2.5f
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Título de la imagen actual
            val currentImage = imagenes[zoomPagerState.currentPage]
            val titulo = when (languageCode) {
                "es" -> currentImage.titulo
                "de" -> currentImage.tituloAleman
                else -> currentImage.tituloIngles
            }

            Text(
                text = titulo,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .padding(bottom = 16.dp)
            )

            Text(
                text = "${zoomPagerState.currentPage + 1} / ${imagenes.size}",
                color = Color.White,
                modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp) // Added padding to avoid clipping
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                .align(Alignment.TopStart)
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
