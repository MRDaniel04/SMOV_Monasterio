package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange
import com.nextapp.monasterio.ui.theme.MonasteryRed

@Composable
fun OpcionesModoEdicion(
    navController: NavController,
    modifier: Modifier = Modifier,
    topPadding: PaddingValues = PaddingValues(0.dp) // Recibimos el padding
    ) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Bloque para establecer la orientación vertical y limpiarla (copiado del original)
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        onDispose {

        }
    }

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
                    // --- DISEÑO VERTICAL (Lado a lado) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Video (50% ancho)
                        OpcionEdicionButton(
                            text = stringResource(id = R.string.edit_home_background),
                            iconRes = R.drawable.ic_photo,
                            backgroundColor = MonasteryOrange,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(0.7f), // Altura controlada en horizontal
                            onClick = { navController.navigate(AppRoutes.EDICION_FONDO_INICIO) }
                        )

                        // Botón Juego (50% ancho)
                        OpcionEdicionButton(
                            text = stringResource(id = R.string.edit_pins),
                            iconRes = R.drawable.pin,
                            backgroundColor = MonasteryBlue,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(0.7f),
                            onClick = { navController.navigate(AppRoutes.EDICION_PINES) }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(32.dp), // Espacio entre botones
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Puzzle
                        OpcionEdicionButton(
                            text = stringResource(id = R.string.edit_home_background),
                            iconRes = R.drawable.ic_photo,
                            backgroundColor = MonasteryOrange,
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            onClick = { navController.navigate(AppRoutes.EDICION_FONDO_INICIO) }
                        )

                        // Parejas (Memory)
                        OpcionEdicionButton(
                            text = stringResource(id = R.string.edit_pins),
                            iconRes = R.drawable.pin,
                            backgroundColor = MonasteryBlue,
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            onClick = { navController.navigate(AppRoutes.EDICION_PINES) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OpcionEdicionButton(
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
                text = text.uppercase(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}