package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange
import com.nextapp.monasterio.ui.components.MonasteryButton


@Composable
fun OpcionesModoEdicion(
    navController: NavController,
    modifier: Modifier = Modifier,
    topPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // 1. CALCULAMOS LA ALTURA DE LA PANTALLA
    val screenHeight = configuration.screenHeightDp.dp

    // 2. DEFINIMOS EL PADDING SUPERIOR EXTRA
    val extraTopSpace = if (isLandscape) {
        screenHeight * 0.2f
    } else {
        // En Portrait la pantalla es alta. Un 18% es un buen margen superior "aireado".
        screenHeight * 0.25f
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { }
    }

    Box(modifier = modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.fondo),
            contentDescription = "Fondo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding)
                .padding(top = extraTopSpace)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // En Vertical alineamos arriba (Top) para que sobre espacio abajo
            verticalArrangement = if (isLandscape) Arrangement.Center else Arrangement.Top
        ) {

            if (isLandscape) {
                // --- LANDSCAPE (Botones Grandes) ---
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OpcionEdicionButton(
                        text = stringResource(id = R.string.edit_home_background),
                        iconRes = R.drawable.ic_photo,
                        backgroundColor = MonasteryOrange,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(0.8f), // 80% del alto disponible
                        isVerticalContent = true,
                        onClick = { navController.navigate(AppRoutes.EDICION_FONDO_INICIO) }
                    )

                    OpcionEdicionButton(
                        text = stringResource(id = R.string.edit_pins),
                        iconRes = R.drawable.pin,
                        backgroundColor = MonasteryBlue,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(0.8f),
                        isVerticalContent = true,
                        onClick = { navController.navigate(AppRoutes.EDICION_PINES) }
                    )
                }

            } else {
                // --- PORTRAIT (Botones "Pequeños" - 22% de pantalla cada uno) ---
                Column(
                    modifier = Modifier.fillMaxWidth(), // Ancho total, pero alto automático
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    OpcionEdicionButton(
                        text = stringResource(id = R.string.edit_home_background),
                        iconRes = R.drawable.ic_photo,
                        backgroundColor = MonasteryOrange,
                        modifier = Modifier
                            .fillMaxWidth()
                            // AQUÍ ESTÁ LA MAGIA:
                            // En vez de weight(1f) o fillMaxHeight(), le damos una altura
                            // calculada basándonos en el tamaño real de la pantalla.
                            // 0.22f significa el 22% de la altura total del dispositivo.
                            .height(screenHeight * 0.22f),
                        isVerticalContent = false, // Diseño horizontal interno
                        onClick = { navController.navigate(AppRoutes.EDICION_FONDO_INICIO) }
                    )

                    OpcionEdicionButton(
                        text = stringResource(id = R.string.edit_pins),
                        iconRes = R.drawable.pin,
                        backgroundColor = MonasteryBlue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight * 0.22f), // Mismo tamaño exacto
                        isVerticalContent = false,
                        onClick = { navController.navigate(AppRoutes.EDICION_PINES) }
                    )

                    // Al usar .height() fijo (calculado), el resto del espacio abajo queda vacío,
                    // que es lo que querías para que no se vean gigantes.
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
    isVerticalContent: Boolean = false,
    onClick: () -> Unit
) {
    MonasteryButton(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier,
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        if (isVerticalContent) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = text.uppercase(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else {
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
}