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
    val lang = locale.language

    // ⭐ TITULO DEL PIN SEGÚN IDIOMA
    val titulo_pin = when (lang) {
        "de" -> pin.tituloAleman.ifBlank { pin.titulo }
        "en" -> pin.tituloIngles.ifBlank { pin.titulo }
        else -> pin.titulo
    }

    // ⭐ TEXTOS MULTIDIOMA
    val tDatosContacto = when (lang) {
        "de" -> "Kontaktinformationen"
        "en" -> "Contact Information"
        else -> "Datos de contacto"
    }

    val tUbicacion = when (lang) {
        "de" -> "Standort"
        "en" -> "Location"
        else -> "Ubicación"
    }

    val tCorreo = when (lang) {
        "de" -> "E-Mail"
        "en" -> "Email"
        else -> "Correo electrónico"
    }

    val tTelefono = when (lang) {
        "de" -> "Telefon"
        "en" -> "Phone"
        else -> "Teléfono"
    }

    val tHorarios = when (lang) {
        "de" -> "Öffnungszeiten"
        "en" -> "Opening Hours"
        else -> "Horarios de Visita"
    }

    val tLunesViernes = when (lang) {
        "de" -> "Montag bis Freitag:"
        "en" -> "Monday to Friday:"
        else -> "Lunes a Viernes:"
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = titulo_pin,
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
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
                        }
                    }

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
                                        if (selected) Color.White
                                        else Color.White.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        // 🧾 Contenido inferior
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {

                // ⭐ TÍTULO CENTRADO (Multidioma)
                Text(
                    text = tDatosContacto,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )

                InfoItem(label = tUbicacion, value = "Av. Ramón y Cajal, 4A, 47005 Valladolid")
                InfoItem(label = tCorreo, value = "smrhv@huelgasreales.es")
                InfoItem(label = tTelefono, value = "+34 983 29 13 95")

                Spacer(modifier = Modifier.height(30.dp))

                // ⭐ TÍTULO CENTRADO (Multidioma)
                Text(
                    text = tHorarios,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Text(tLunesViernes, fontWeight = FontWeight.SemiBold)
                Text("17:00 - 19:30")
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = Color(0xFF1976D2))
    }
}
