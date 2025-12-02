package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.utils.llamarTelefono

@Composable
fun OpcionesReservaScreen(
    navController: NavController,
    topPadding: PaddingValues = PaddingValues(0.dp) // Recibimos el padding
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val numerodetelefono = "+34983291395"

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

        // 1. IMAGEN DE FONDO (Ocupa todo)
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
            // Contenedor con margen visual
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
                        // Botón Teléfono
                        ReservaOptionButton(
                            text = stringResource(R.string.option_phone_appointment),
                            iconRes = R.drawable.telefono,
                            backgroundColor = Color(0xFF1E8E3E), // Verde
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(0.7f), // Altura controlada en horizontal
                            onClick = { context.llamarTelefono(numerodetelefono) }
                        )

                        // Botón Online
                        ReservaOptionButton(
                            text = stringResource(R.string.option_online_appointment),
                            iconRes = R.drawable.calendario,
                            backgroundColor = Color(0xFF303F9F), // Azul
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(0.7f),
                            onClick = { navController.navigate(AppRoutes.RESERVA) }
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
                        // Botón Teléfono
                        ReservaOptionButton(
                            text = stringResource(R.string.option_phone_appointment),
                            iconRes = R.drawable.telefono,
                            backgroundColor = Color(0xFF1E8E3E),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp), // Un poco más altos para que quepa el icono grande
                            onClick = { context.llamarTelefono(numerodetelefono) }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón Online
                        ReservaOptionButton(
                            text = stringResource(R.string.option_online_appointment),
                            iconRes = R.drawable.calendario,
                            backgroundColor = Color(0xFF303F9F),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            onClick = { navController.navigate(AppRoutes.RESERVA) }
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTE REUTILIZABLE ---
@Composable
fun ReservaOptionButton(
    text: String,
    iconRes: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = true,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.Black // Iconos y texto negros según tu diseño original
        ),
        modifier = modifier
    ) {
        // Usamos BoxWithConstraints para ajustar el tamaño del icono si el botón se hace pequeño
        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val availableHeight = maxHeight
            // Si hay poco espacio vertical, reducimos el icono
            val iconSize = if (availableHeight < 100.dp) 48.dp else 80.dp

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = Color.Black // Aseguramos color negro
                )

                Spacer(Modifier.width(24.dp))

                Text(
                    text = text,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
            }
        }
    }
}