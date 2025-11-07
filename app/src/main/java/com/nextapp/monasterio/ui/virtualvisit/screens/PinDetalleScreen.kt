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
    var expanded by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { pin.imagenes.size })
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔙 Botón atrás
            Box(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
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

            // 🖼️ Carrusel de imágenes desde Cloudinary
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

            Text(
                text = pin.tema.displayName,
                color = categoriaColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val descripcion = pin.descripcion ?: ""
            if (descripcion.isNotBlank()) {
                val textoMostrado = if (expanded) descripcion else descripcion.take(250) + "..."
                Text(
                    text = textoMostrado,
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )

                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        if (expanded) "Leer menos" else "Leer más",
                        color = categoriaColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        onVer360?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
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
