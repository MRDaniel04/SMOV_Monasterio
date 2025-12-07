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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange
import com.nextapp.monasterio.ui.theme.MonasteryRed

@Composable
fun OpcionesJuegoNinyos(
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

        // 2. CONTENIDO (Aplica el padding AQUÍ)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding) // 👈 El contenido baja para no chocarse con la barra
        ) {
            // Contenedor interno con margen visual
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLandscape) {
                    // --- DISEÑO HORIZONTAL (3 Botones en línea) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Puzzle
                        GameOptionButton(
                            text = stringResource(id = R.string.puzzle_option),
                            iconRes = R.drawable.puzzle,
                            backgroundColor = MonasteryBlue,
                            modifier = Modifier.weight(1f).fillMaxHeight(0.6f),
                            onClick = { navController.navigate(AppRoutes.JUEGO_PUZZLE) }
                        )
                        // Parejas (Memory)
                        GameOptionButton(
                            text = stringResource(id = R.string.memory_option),
                            iconRes = R.drawable.memory,
                            backgroundColor = MonasteryRed,
                            modifier = Modifier.weight(1f).fillMaxHeight(0.6f),
                            onClick = { navController.navigate(AppRoutes.JUEGO_PAREJAS) }
                        )
                        // Diferencias
                        GameOptionButton(
                            text = stringResource(id = R.string.differences_game),
                            iconRes = R.drawable.lupa,
                            backgroundColor = MonasteryOrange,
                            modifier = Modifier.weight(1f).fillMaxHeight(0.6f),
                            onClick = { navController.navigate(AppRoutes.JUEGO_DIFERENCIAS) }
                        )
                    }
                } else {
                    // --- DISEÑO VERTICAL (3 Botones en columna) ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(32.dp), // Espacio entre botones
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Puzzle
                        GameOptionButton(
                            text = stringResource(id = R.string.puzzle_option),
                            iconRes = R.drawable.puzzle,
                            backgroundColor = MonasteryBlue,
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            onClick = { navController.navigate(AppRoutes.JUEGO_PUZZLE) }
                        )

                        // Parejas (Memory)
                        GameOptionButton(
                            text = stringResource(id = R.string.memory_option),
                            iconRes = R.drawable.memory,
                            backgroundColor = MonasteryRed,
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            onClick = { navController.navigate(AppRoutes.JUEGO_PAREJAS) }
                        )

                        // Diferencias
                        GameOptionButton(
                            text = stringResource(id = R.string.differences_game),
                            iconRes = R.drawable.lupa,
                            backgroundColor = MonasteryOrange,
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            onClick = { navController.navigate(AppRoutes.JUEGO_DIFERENCIAS) }
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTE REUTILIZABLE PARA BOTONES DE JUEGO ---
@Composable
fun GameOptionButton(
    text: String,
    iconRes: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier,
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}