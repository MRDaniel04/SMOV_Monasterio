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
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
// Imports para el scroll de texto
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

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

    val categoriaColor = pin.tema.color
    val pagerState = rememberPagerState(pageCount = { pin.imagenes.size })

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
                .padding(bottom = 90.dp),
            // La columna principal ya no es deslizable
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
                    contentDescription = "Volver",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBack() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título (igual que antes)
            Text(
                text = buildString {
                    append(pin.titulo)
                    pin.ubicacion?.let { append(" (${it.displayName})") }
                },
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🖼️ Carrusel de imágenes (igual que antes)
            if (pin.imagenes.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(3.dp, categoriaColor, RoundedCornerShape(16.dp))
                ) {
                    HorizontalPager(state = pagerState) { page ->
                        AsyncImage(
                            model = pin.imagenes[page],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
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
                                        if (selected) categoriaColor else categoriaColor.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Texto del Tema (igual que antes)
            Text(
                text = pin.tema.displayName,
                color = categoriaColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- ¡¡CUADRO DE TEXTO DESLIZABLE CORREGIDO!! ---
            val descripcion = pin.descripcion ?: ""
            if (descripcion.isNotBlank()) {

                // 1. Creamos el estado de scroll (función que SÍ te reconoce)
                val textScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Ocupa el espacio restante
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp)) // Recorta el contenido al Box
                ) {
                    Text(
                        text = descripcion, // Texto completo
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            // 2. Hacemos que el texto sea deslizable (función que SÍ te reconoce)
                            .verticalScroll(textScrollState)
                    )

                    // (No añadimos la barra de scroll visual)
                }
            }

            // Espacio para que el botón "Ver 360" no tape el texto
            Spacer(modifier = Modifier.height(80.dp))
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
                    colors = ButtonDefaults.buttonColors(containerColor = categoriaColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Ver 360°",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}