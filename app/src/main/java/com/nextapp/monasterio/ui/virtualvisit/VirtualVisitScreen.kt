package com.nextapp.monasterio.ui.virtualvisit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualVisitScreen() {
    Scaffold(
        topBar = { MyToolbar() }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clipToBounds()
        ) {
            val mapWidth = 2000.dp
            val mapHeight = 1600.dp // 👈 mapa más alto para que veas el efecto

            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            val density = LocalDensity.current

            // Calcula scroll inicial centrado
            val initialHorizontalPx = with(density) {
                ((mapWidth - screenWidth) / 2).coerceAtLeast(0.dp).toPx().toInt()
            }
            val initialVerticalPx = with(density) {
                ((mapHeight - screenHeight) / 2).coerceAtLeast(0.dp).toPx().toInt()
            }

            val horizontalScroll = rememberScrollState(initial = initialHorizontalPx)
            val verticalScroll = rememberScrollState(initial = initialVerticalPx)

            // 🟨 Contenedor scrollable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
                    .horizontalScroll(horizontalScroll),
                contentAlignment = Alignment.Center  // 👈 Esto es CLAVE
            ) {
                // Este Box ahora se expande desde el centro en ambas direcciones
                Box(
                    modifier = Modifier
                        .width(mapWidth)
                        .height(mapHeight)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mapa_monasterio),
                        contentDescription = "Mapa del Monasterio",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 🎛️ Botones fijos sobre el mapa
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(onClick = { /* Zoom in */ }) { Text("+") }
                FloatingActionButton(onClick = { /* Zoom out */ }) { Text("-") }
                FloatingActionButton(onClick = { /* Centrar mapa */ }) { Text("🎯") }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyToolbar() {
    TopAppBar(
        title = { Text("Monasterio") },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVirtualVisitScreen() {
    VirtualVisitScreen()
}


