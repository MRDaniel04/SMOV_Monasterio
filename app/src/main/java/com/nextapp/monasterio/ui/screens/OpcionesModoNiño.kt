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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange

@Composable
fun OpcionesModoNiño(
    navController: NavController,
    // 👇 Recibimos el padding del sistema (igual que en HomeScreen)
    topPadding: PaddingValues = PaddingValues(0.dp)
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
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. FONDO (Ocupa TODO, sin padding)
        Image(
            painter = painterResource(R.drawable.fondo),
            contentDescription = "Fondo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. CONTENIDO (Aplica el padding AQUÍ para respetar la barra roja)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding) // 👈 CLAVE: El contenido baja, el fondo se queda
        ) {
            // Contenedor interno con padding visual para separar de los bordes de la pantalla
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLandscape) {
                    // --- DISEÑO HORIZONTAL (Lado a lado) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Video (50% ancho)
                        OpcionNinyoButton(
                            text = stringResource(id = R.string.video_child),
                            iconRes = R.drawable.youtube,
                            backgroundColor = MonasteryOrange,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(0.7f), // Altura controlada en horizontal
                            onClick = { navController.navigate(AppRoutes.VIDEO_NINYOS) }
                        )

                        // Botón Juego (50% ancho)
                        OpcionNinyoButton(
                            text = stringResource(id = R.string.game_child),
                            iconRes = R.drawable.console,
                            backgroundColor = MonasteryBlue,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(0.7f),
                            onClick = { navController.navigate(AppRoutes.JUEGO_NINYOS) }
                        )
                    }
                } else {
                    // --- DISEÑO VERTICAL (Uno encima de otro) ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Botón Video
                        OpcionNinyoButton(
                            text = stringResource(id = R.string.video_child),
                            iconRes = R.drawable.youtube,
                            backgroundColor = MonasteryOrange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp), // Altura fija grande
                            onClick = { navController.navigate(AppRoutes.VIDEO_NINYOS) }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón Juego
                        OpcionNinyoButton(
                            text = stringResource(id = R.string.game_child),
                            iconRes = R.drawable.console,
                            backgroundColor = MonasteryBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            onClick = { navController.navigate(AppRoutes.JUEGO_NINYOS) }
                        )
                    }
                }
            }
        }
    }
}
// --- COMPONENTE REUTILIZABLE PARA LOS BOTONES ---
@Composable
fun OpcionNinyoButton(
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
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(20.dp)), // Sombra bonita
        contentPadding = PaddingValues(16.dp) // Padding interno
    ) {
        // Contenido del botón (Icono + Texto)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(56.dp), // Icono grande
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(24.dp))

            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}