package com.nextapp.monasterio.ui.virtualvisit.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nextapp.monasterio.R
import androidx.compose.foundation.Image

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinDetalleScreen(navController: NavController) {
    val categoriaColor = Color(0xFF4CAF50) // Verde para ejemplo (puedes cambiar por cada punto)
    val titulo = "Retablo del nacimiento"
    val categoria = "Pintura y arte visual"
    val descripcion = """
        Existe un segundo retablo del mismo autor conocido como Retablo del Nacimiento (1614), en la capilla que fuera de San Juan, junto al coro, que da a la Sacristía. 
        En el centro del relieve está el Niño en cuna, la Virgen lo adora con las manos plegadas y hay un pastor ofreciendo un cordero. 
        Junto al Niño hay un ángel de rodillas y más figuras. La escena está tallada con gran detalle y expresividad, destacando por su realismo y dinamismo.
    """.trimIndent()
    val imagenes = listOf(
        R.drawable.escudo, // asegúrate de tener estas imágenes en drawable
        R.drawable.huelgas_inicio,
        R.drawable.monasterio_interior
    )

    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val pagerState = rememberPagerState(pageCount = { imagenes.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {

        // 🔹 Carrusel de imágenes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            HorizontalPager(state = pagerState) { page ->
                Image(
                    painter = painterResource(id = imagenes[page]),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Indicadores del carrusel
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            ) {
                repeat(imagenes.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        Modifier
                            .padding(3.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .background(
                                if (selected) Color.White else Color.White.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Título
        Text(
            text = titulo,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Categoría
        Text(
            text = categoria,
            color = categoriaColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Descripción con “Leer más”
        val textoMostrado = if (expanded) descripcion else descripcion.take(250) + "..."
        Text(
            text = textoMostrado,
            fontSize = 16.sp,
            color = Color.DarkGray
        )

        TextButton(onClick = { expanded = !expanded }) {
            Text(
                if (expanded) "Leer menos" else "Leer más",
                color = categoriaColor,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Botón 360°
        Button(
            onClick = { navController.navigate("retablo_360") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = categoriaColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Ver 360°", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
