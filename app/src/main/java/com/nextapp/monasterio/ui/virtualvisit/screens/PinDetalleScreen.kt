package com.nextapp.monasterio.ui.virtualvisit.screens

import android.app.Activity
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

    val imagenes = if (pin.imagenesDetalladas.isNotEmpty()) pin.imagenesDetalladas else emptyList()

    val pagerState = rememberPagerState(pageCount = { imagenes.size })
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageTitle by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val locale: Locale = configuration.locales[0]
    val language = locale.language

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
                .padding(bottom = 90.dp),
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

            Spacer(modifier = Modifier.height(16.dp))

            // 🏷️ Título principal
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

            Spacer(modifier = Modifier.height(20.dp))

            // 🖼️ Carrusel con etiqueta (solo aquí)
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

                            // 🔹 Etiqueta abajo a la izquierda (solo carrusel)
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

                    // 🔸 Indicadores inferiores
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
                Spacer(modifier = Modifier.height(24.dp))
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
        }

        // 🔘 Botón 360
        onVer360?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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

        // 🔍 Zoom con título traducido
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

// 🔍 Diálogo de zoom
@Composable
private fun ZoomableImageDialog(imageUrl: String, label: String, onDismiss: () -> Unit) {
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
