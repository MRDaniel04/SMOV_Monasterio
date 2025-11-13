package com.nextapp.monasterio.ui.screens

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.nextapp.monasterio.models.PinData
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntradaMonasterioScreen(
    pin: PinData, // 🔹 ahora recibe el pin completo
    onBack: () -> Unit
) {
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
    }

    val imagenes = pin.imagenesDetalladas.ifEmpty { emptyList() }

    val pagerState = rememberPagerState(pageCount = { imagenes.size })
    val configuration = LocalConfiguration.current
    val locale: Locale = configuration.locales[0]
    val language = locale.language

    // 🔹 Seleccionar idioma del título
    val titulo_pin = when (language) {
        "de" -> pin.tituloAleman.ifBlank { pin.tituloAleman }
        "en" -> pin.tituloIngles.ifBlank { pin.tituloIngles }
        else -> pin.titulo
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔙 Encabezado superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD50000))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = titulo_pin, // 👈 usa el título del pin
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🖼️ Carrusel con imágenes reales del pin
        if (imagenes.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF2196F3), RoundedCornerShape(12.dp))
            ) {
                HorizontalPager(state = pagerState) { page ->
                    val imagen = imagenes[page]
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = imagen.url,
                            contentDescription = imagen.etiqueta,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // 🔹 Etiqueta abajo a la izquierda
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

                // 🔘 Indicadores del carrusel
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
                                    Color.White.copy(alpha = if (selected) 1f else 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 🧾 Contenido principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 Título centrado
            Text(
                text = "Datos de contacto",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            InfoRow(label = "Ubicación:", value = "Av. Ramón y Cajal, 4A, 47005 Valladolid")
            InfoRow(label = "Correo electrónico:", value = "smrhv@huelgasreales.es")
            InfoRow(label = "Teléfono:", value = "+34 983 29 13 95")

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Horarios de Visita",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lunes a Viernes: ", fontWeight = FontWeight.SemiBold)
                Text("17:00 - 19:30", fontWeight = FontWeight.Normal)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}



@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text = "$label ", fontWeight = FontWeight.SemiBold)
        Text(text = value, color = Color(0xFF1976D2))
    }
}
