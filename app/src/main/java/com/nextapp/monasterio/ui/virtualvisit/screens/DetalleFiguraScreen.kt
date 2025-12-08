package com.nextapp.monasterio.ui.virtualvisit.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.FiguraData
import com.nextapp.monasterio.models.ImagenData
import com.nextapp.monasterio.repository.FiguraRepository
import com.nextapp.monasterio.repository.ImagenRepository
import kotlinx.coroutines.delay
import android.util.Log

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DetalleFiguraScreen(
    navController: NavHostController,
    rootNavController: NavHostController? = null, // 👈 AÑADIDO: Ahora acepta el navegador raíz
    nombre: String
) {
    // 1. Estados de Datos
    var figura by remember { mutableStateOf<FiguraData?>(null) }
    var imagenes by remember { mutableStateOf<List<ImagenData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 2. Determinación del Idioma
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val currentLanguageCode = locale.language

    // 3. ESTADO DEL CARRUSEL
    var currentIndex by remember { mutableStateOf(0) }

    // Repositorios
    val figuraRepository = remember { FiguraRepository }
    val imagenRepository = remember { ImagenRepository() }

    // 4. LÓGICA DE CARGA ROBUSTA (DOBLE INTENTO)
    LaunchedEffect(nombre, currentLanguageCode) {
        isLoading = true
        Log.d("DetalleFigura", "Buscando figura con clave: $nombre")

        // INTENTO 1: Buscar por campo 'nombre'
        var loadedFigura = figuraRepository.getFiguraByNombre(nombre)

        // INTENTO 2: Si falla, buscar por ID del documento
        if (loadedFigura == null) {
            Log.d("DetalleFigura", "No encontrada por nombre. Probando por ID...")
            loadedFigura = figuraRepository.getFiguraById(nombre)
        }

        figura = loadedFigura

        // Cargar imágenes referenciadas
        if (loadedFigura != null) {
            Log.d("DetalleFigura", "Figura encontrada: ${loadedFigura.nombre}")
            val loadedImages = loadedFigura.imagenes.mapNotNull { imageId ->
                // Asumimos que la lista 'imagenes' contiene IDs de string
                imagenRepository.getImageById(imageId)
            }
            imagenes = loadedImages
        } else {
            Log.e("DetalleFigura", "❌ No se encontró ninguna figura con clave: $nombre")
        }
        isLoading = false
    }

    // 5. LÓGICA DE TRANSICIÓN AUTOMÁTICA DEL CARRUSEL
    if (imagenes.size > 1) {
        LaunchedEffect(currentIndex, imagenes.size) {
            delay(4000L)
            currentIndex = (currentIndex + 1) % imagenes.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF8F00))
            }
        } else if (figura == null) {
            // Mensaje de error si fallan ambos intentos
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error cargando datos: $nombre", color = Color.Red)
            }
        } else {
            // CONTENIDO PRINCIPAL
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 40.dp) // Espacio inferior
            ) {

                // --- CABECERA (Atrás + Título) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // 1. Botón Atrás (Izq)
                    Box(modifier = Modifier.align(Alignment.CenterStart)) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_back),
                                contentDescription = stringResource(R.string.go_back),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    val nombreFigura = when (currentLanguageCode) {
                        "de" -> figura!!.nombre_de
                        "en" -> figura!!.nombre_en
                        "fr" -> figura!!.nombre_fr
                        else -> figura!!.nombre
                    }

                    // 2. Título (Centro)
                    Text(
                        text = nombreFigura,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(figura!!.colorResaltado.toUInt().toInt()),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 7. IMÁGENES (CARRUSEL) ---
                if (imagenes.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, Color(figura!!.colorResaltado.toUInt().toInt()), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Crossfade(targetState = currentIndex, label = "img") { page ->
                            AsyncImage(
                                model = imagenes[page].url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Puntos indicadores
                        Row(
                            Modifier.align(Alignment.BottomCenter).padding(6.dp)
                        ) {
                            repeat(imagenes.size) { index ->
                                val selected = currentIndex == index
                                Box(
                                    Modifier.padding(3.dp).size(if (selected) 8.dp else 6.dp)
                                        .background(if (selected) Color.White else Color.White.copy(0.5f), CircleShape)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- 8. INFORMACIÓN ---
                val infoText = when (currentLanguageCode) {
                    "de" -> figura!!.info_de
                    "en" -> figura!!.info_en
                    "fr" -> figura!!.info_fr
                    else -> figura!!.info_es
                }

                Text(
                    text = stringResource(R.string.general_information_figure),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(figura!!.colorResaltado.toUInt().toInt()),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = infoText ?: "",
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- 9. BOTÓN VER 360 (PIE DE PÁGINA) ---
                // Solo se muestra si la figura tiene URL 360 en Firebase
                if (!figura!!.vista360Url.isNullOrBlank()) {
                    Button(
                        onClick = {
                            // Navegamos usando el rootNavController para salir del contexto del mapa
                            rootNavController?.navigate(AppRoutes.PIN_360 + "/${figura!!.id}")
                        },
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
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Espacio extra al final
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}