package com.nextapp.monasterio.ui.virtualvisit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.nextapp.monasterio.R
import androidx.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualVisitScreen(
    viewModel: VirtualVisitViewModel,
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa del Monasterio") },
                navigationIcon = {
                    // 🔄 Placeholder temporal en lugar del icono de retroceso
                    Text(
                        text = "<",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onBackClick() }
                    )
                },
                actions = {
                    // 🔄 Placeholder temporal en lugar del icono de ajustes
                    Text(
                        text = "⚙",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onSettingsClick() }
                    )
                }
            )
        }
    ) { innerPadding ->

        // 🗺️ Cuerpo principal (mapa y controles)
        MapContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun MapContainer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
    ) {
        // Imagen del mapa base
        Image(
            painter = painterResource(id = R.drawable.mapa_monasterio),
            contentDescription = "Mapa del Monasterio",
            modifier = Modifier.fillMaxSize()
        )

        // 🔄 Placeholder para el botón flotante
        FloatingActionButton(
            onClick = { /* TODO: centrar mapa */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("🎯") // Icono temporal con emoji
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewVirtualVisitScreen() {
    val dummyViewModel = VirtualVisitViewModel()
    VirtualVisitScreen(viewModel = dummyViewModel)
}
