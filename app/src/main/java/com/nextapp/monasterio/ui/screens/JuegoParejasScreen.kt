package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import androidx.navigation.NavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.components.MonasteryButton
@Composable
fun JuegoParejasScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    topPadding: PaddingValues = PaddingValues(0.dp) // Recibimos el padding
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    // Detectar orientación
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Permitir rotación
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { }
    }

    // --- ESTRUCTURA PRINCIPAL ---
    Box(modifier = modifier.fillMaxSize()) {

        // 1. FONDO (Ocupa TODO, sin padding)
        Image(
            painter = painterResource(R.drawable.fondo),
            contentDescription = "Fondo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. CONTENIDO (Respeta la barra superior)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding)
        ) {
            // Contenedor interno con margen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLandscape) {
                    // --- DISEÑO HORIZONTAL (Cuadrícula 2x2) ---
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Fila 1 (Nivel 1 y 2)
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GameButton(
                                text = stringResource(id = R.string.three_pairs),
                                modifier = Modifier.weight(1f).fillMaxHeight(0.8f),
                                onClick = { navController.navigate(AppRoutes.PAREJASNIVEL1) }
                            )
                            GameButton(
                                text = stringResource(id = R.string.four_pairs),
                                modifier = Modifier.weight(1f).fillMaxHeight(0.8f),
                                onClick = { navController.navigate(AppRoutes.PAREJASNIVEL2) }
                            )
                        }

                        // Fila 2 (Nivel 3 y 4)
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GameButton(
                                text = stringResource(id = R.string.five_pairs),
                                modifier = Modifier.weight(1f).fillMaxHeight(0.8f),
                                onClick = { navController.navigate(AppRoutes.PAREJASNIVEL3) }
                            )
                            GameButton(
                                text = stringResource(id = R.string.six_pairs),
                                modifier = Modifier.weight(1f).fillMaxHeight(0.8f),
                                onClick = { navController.navigate(AppRoutes.PAREJASNIVEL4) }
                            )
                        }
                    }
                } else {
                    // --- DISEÑO VERTICAL (Lista) ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp), // Espacio entre botones
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameButton(
                            text = stringResource(id = R.string.three_pairs),
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            onClick = { navController.navigate(AppRoutes.PAREJASNIVEL1) }
                        )

                        GameButton(
                            text = stringResource(id = R.string.four_pairs),
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            onClick = { navController.navigate(AppRoutes.PAREJASNIVEL2) }
                        )

                        GameButton(
                            text = stringResource(id = R.string.five_pairs),
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            onClick = { navController.navigate(AppRoutes.PAREJASNIVEL3) }
                        )

                        GameButton(
                            text = stringResource(id = R.string.six_pairs),
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            onClick = { navController.navigate(AppRoutes.PAREJASNIVEL4) }
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTE REUTILIZABLE PARA BOTONES ---
@Composable
fun GameButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    MonasteryButton(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.shadow(8.dp, RoundedCornerShape(20.dp)),
        contentPadding = PaddingValues(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}