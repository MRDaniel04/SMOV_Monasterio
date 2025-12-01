
package com.nextapp.monasterio.ui.virtualvisit.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.FiguraData
import com.nextapp.monasterio.models.ImagenData
import com.nextapp.monasterio.repository.FiguraRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.nextapp.monasterio.repository.ImagenRepository
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DetalleFiguraScreen(
    navController: NavHostController,
    figuraId: String // 👈 Recibimos el ID
) {
    // 1. Estados de Datos
    var figura by remember { mutableStateOf<FiguraData?>(null) }
    var imagenes by remember { mutableStateOf<List<ImagenData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 2. Determinación del Idioma (desde la configuración local)
    val currentLanguageCode = LocalConfiguration.current.locales[0].language

    // 3. ESTADO DEL CARRUSEL (NUEVO)
    var currentIndex by remember { mutableStateOf(0) }

    // Repositorios
    val figuraRepository = remember { FiguraRepository }
    val imagenRepository = remember { ImagenRepository() }

    // 4. LÓGICA DE CARGA Y EFECTO DE TRANSICIÓN
    LaunchedEffect(figuraId, currentLanguageCode) {
        isLoading = true
        // Cargar la figura
        val loadedFigura = figuraRepository.getFiguraById(figuraId)
        figura = loadedFigura

        // Cargar las imágenes referenciadas
        if (loadedFigura != null) {
            // Se asume que FiguraData.imagenes ahora es List<DocumentReference>
            val loadedImages = loadedFigura.imagenes.mapNotNull { ref ->
                val imageId = ref.id // Usamos .id de DocumentReference
                imagenRepository.getImageById(imageId)
            }
            imagenes = loadedImages
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
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF8F00))
            }
        } else if (figura == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: No se encontró la figura con ID $figuraId", color = Color.Red)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 48.dp)
            ) {
                // 6. TÍTULO
                Text(
                    text = figura!!.nombre.uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF8F00),
                    textAlign = TextAlign.Center, // 👈 Se centra el texto
                    modifier = Modifier
                        .fillMaxWidth() // 👈 Se asegura que ocupe todo el ancho
                        .padding(bottom = 16.dp)
                )

                // 7. IMÁGENES (CARRUSEL AUTOMÁTICO - REEMPLAZADO)
                if (imagenes.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, Color(0xFFFF8F00), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Crossfade(
                                targetState = currentIndex,
                                label = "imageFade",
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                val imagen = imagenes[page]
                                AsyncImage(
                                    model = imagen.url,
                                    contentDescription = imagen.titulo,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Indicadores (Dots)
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
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }


                // 8. INFORMACIÓN (Multilenguaje)
                val infoText = when (currentLanguageCode) {
                    "de" -> figura!!.info_de
                    "en" -> figura!!.info_en
                    "fr" -> figura!!.info_fr
                    else -> figura!!.info_es
                }

                Text(
                    text = "Información General:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF996600),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = infoText,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }

        // 9. BOTÓN ATRÁS
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Volver",
                tint = Color.White
            )
        }
    }
}