package com.nextapp.monasterio.ui.screens

import android.app.Activity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EntradaMonasterioScreen(
    pin: PinData,
    onBack: () -> Unit
) {
    val view = LocalView.current
    LaunchedEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
    }

    val imagenes = pin.imagenesDetalladas.ifEmpty { emptyList() }
    var currentIndex by remember { mutableStateOf(0) }
    val configuration = LocalConfiguration.current
    val locale: Locale = configuration.locales[0]
    val language = locale.language

    val titulo_pin = when (language) {
        "de" -> pin.tituloAleman.ifBlank { pin.titulo }
        "en" -> pin.tituloIngles.ifBlank { pin.titulo }
        else -> pin.titulo
    }

    // 🔄 Cambio automático de imagen (cada 4 s)
    if (imagenes.size > 1) {
        LaunchedEffect(currentIndex, imagenes.size) {
            delay(4000L)
            currentIndex = (currentIndex + 1) % imagenes.size
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        item {
            // 🔙 Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD50000))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = titulo_pin,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 🖼️ Carrusel tipo fade
        if (imagenes.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF2196F3), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // 👇 Transición suave entre imágenes
                    androidx.compose.animation.Crossfade(
                        targetState = currentIndex,
                        label = "imageFade"
                    ) { page ->
                        val imagen = imagenes[page]
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = imagen.url,
                                contentDescription = imagen.etiqueta,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (imagen.etiqueta.isNotBlank()) {
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
                    }

                    // 🔘 Indicadores
                    Row(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    ) {
                        repeat(imagenes.size) { index ->
                            val selected = currentIndex == index
                            Box(
                                Modifier
                                    .padding(3.dp)
                                    .size(if (selected) 9.dp else 7.dp)
                                    .background(
                                        if (selected)
                                            Color.White
                                        else
                                            Color.White.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // 🧾 Contenido inferior
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
            }
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
