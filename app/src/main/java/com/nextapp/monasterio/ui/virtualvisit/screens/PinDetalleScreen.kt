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
import androidx.compose.ui.viewinterop.AndroidView // <-- Import para PhotoView
import androidx.compose.ui.window.Dialog // <-- Import para el Diálogo
import androidx.compose.ui.window.DialogProperties // <-- Import para el Diálogo
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.load // <-- Import para cargar la URL en PhotoView
import com.github.chrisbanes.photoview.PhotoView // <-- Import de la librería de Zoom
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
// Imports para el scroll de texto
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.nextapp.monasterio.models.Ubicacion
import java.util.Locale


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

    val pagerState = rememberPagerState(pageCount = { pin.imagenes.size })

    // --- 1. ESTADO PARA EL ZOOM ---
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val locale: Locale = configuration.locales[0]
    val language = locale.language

    var titulo_pin: String
    var descripcion_pin : String? = null
    var ubicacion_pin: Ubicacion? = null

    if(language == "es"){
        titulo_pin = pin.titulo
        descripcion_pin = pin.descripcion
        ubicacion_pin = pin.ubicacion
    } else if(language == "de"){
        titulo_pin = pin.tituloAleman
        descripcion_pin = pin.descripcionAleman
        ubicacion_pin = pin.ubicacionAleman
    }else{
        titulo_pin = pin.tituloIngles
        descripcion_pin = pin.descripcionIngles
        ubicacion_pin = pin.ubicacionIngles
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding() // Padding para la barra de estado (arriba)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 90.dp), // Padding para el botón 360
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔙 Botón atrás
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), // Padding para bajarlo
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

            // Título (igual que antes)
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

            // 🖼️ Carrusel de imágenes (AHORA CLICABLE)
            if (pin.imagenes.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
                ) {
                    HorizontalPager(state = pagerState) { page ->
                        AsyncImage(
                            model = pin.imagenes[page],
                            contentDescription = stringResource(R.string.contentdescription_image_pin),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                // --- 2. ACCIÓN DE CLICK ---
                                .clickable { selectedImageUrl = pin.imagenes[page] }
                        )
                    }
                    Row(
                        Modifier.align(Alignment.BottomCenter).padding(8.dp)
                    ) {
                        repeat(pin.imagenes.size) { index ->
                            val selected = pagerState.currentPage == index
                            Box(
                                Modifier
                                    .padding(3.dp)
                                    .size(if (selected) 8.dp else 6.dp)
                                    .background(
                                        Color.Black,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Cuadro de texto deslizable (igual que antes) ---
            val descripcion = descripcion_pin?: ""
            if (descripcion.isNotBlank()) {
                val textScrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Ocupa el espacio restante
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = descripcion, // Texto completo
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

            // (El Spacer(80.dp) que tenías fue eliminado y reemplazado por el padding(bottom=90.dp) en la Column)

        } // --- FIN DE LA COLUMNA PRINCIPAL ---

        // --- Botón 360 (igual que antes) ---
        onVer360?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Fijo abajo
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding() // Sube el botón por encima de la barra de Samsung
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

        // --- 3. DIÁLOGO DE ZOOM (se muestra si selectedImageUrl no es null) ---
        if (selectedImageUrl != null) {
            ZoomableImageDialog(
                imageUrl = selectedImageUrl!!,
                onDismiss = { selectedImageUrl = null }
            )
        }
    }
}

/**
 * Un Diálogo Composable que muestra una imagen con zoom (usando PhotoView).
 */
@Composable
private fun ZoomableImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Ocupa todo el ancho
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(onClick = onDismiss), // Permite cerrar pulsando el fondo
            contentAlignment = Alignment.Center
        ) {
            // Usamos AndroidView para hostear la librería 'PhotoView' que permite zoom
            AndroidView(
                factory = { context ->
                    PhotoView(context).apply {
                        // Usamos Coil para cargar la URL en el PhotoView
                        load(imageUrl) {
                            crossfade(true)
                            // (Opcional) puedes poner un placeholder
                            // placeholder(R.drawable.ic_placeholder)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp) // Un pequeño margen para que no toque los bordes
            )

            // Botón de Cerrar (X)
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