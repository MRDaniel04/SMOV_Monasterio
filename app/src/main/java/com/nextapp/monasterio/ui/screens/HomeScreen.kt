package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.repository.ImagenRepository
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange

@Composable
fun HomeScreenContent(
    isEditing: Boolean,
    navController: NavController,
    currentUser: FirebaseUser? = null,
    topPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val repo = ImagenRepository()
    var imagenFondoInicio by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        val data = repo.getImagenFondoInicio()
        imagenFondoInicio = data?.url
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {}
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. FONDO
        if (imagenFondoInicio != null) {
            AsyncImage(
                model = imagenFondoInicio,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.monastery_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. CONTENIDO
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding)
        ) {
            if (isLandscape) {
                LandscapeLayout(isEditing, navController, currentUser)
            } else {
                PortraitLayout(isEditing, navController, currentUser)
            }
        }
    }
}

// =============================================================================
// DISEÑO VERTICAL (PORTRAIT)
// =============================================================================
@Composable
fun PortraitLayout(
    isEditing: Boolean,
    navController: NavController,
    currentUser: FirebaseUser?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // Centrado general
        verticalArrangement = Arrangement.Center
    ) {
        // Logo Superior
        Image(
            painter = painterResource(id = R.drawable.huelgas_inicio),
            contentDescription = "Logo",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .scale(1.1f) // Un poco más grande visualmente
                .padding(bottom = 48.dp)
        )

        if (currentUser != null) {
            Text(
                text = "Hola, ${currentUser.displayName ?: "Usuario"}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        val buttonModifier = Modifier
            .fillMaxWidth()
            .height(80.dp)

        // 1. Visita
        HomeListButton(
            text = stringResource(R.string.virtual_visit),
            iconRes = R.drawable.ic_map_24,
            backgroundColor = MonasteryOrange,
            modifier = buttonModifier,
            onClick = { navController.navigate(AppRoutes.VIRTUAL_VISIT) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Niños
        HomeListButton(
            text = stringResource(R.string.child_mode),
            iconRes = R.drawable.outline_account_child_invert_24,
            backgroundColor = Color(0xFF6EB017),
            modifier = buttonModifier,
            onClick = { navController.navigate(AppRoutes.MODO_NINYOS) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Reserva
        HomeListButton(
            text = stringResource(R.string.book_appointment),
            iconRes = R.drawable.ic_time_24,
            backgroundColor = MonasteryBlue,
            modifier = buttonModifier,
            onClick = { navController.navigate(AppRoutes.OPCIONES_RESERVA) }
        )

        // 4. Edición
        if (!isEditing) {
            Spacer(modifier = Modifier.height(24.dp))
            HomeListButton(
                text = stringResource(R.string.edit_mode),
                iconRes = R.drawable.lapiz,
                backgroundColor = Color(0xFF9C27B0),
                modifier = buttonModifier,
                onClick = { navController.navigate(AppRoutes.MODO_EDICION) }
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// =============================================================================
// DISEÑO HORIZONTAL (LANDSCAPE)
// =============================================================================
@Composable
fun LandscapeLayout(
    isEditing: Boolean,
    navController: NavController,
    currentUser: FirebaseUser?
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // IZQUIERDA: Logo (40% ancho)
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.huelgas_inicio),
                contentDescription = "Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.1f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // DERECHA: Cuadrícula (60% ancho)
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Alineados a la derecha si sobra espacio
        ) {
            // FILA 1
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeGridButton(
                    text = stringResource(R.string.virtual_visit),
                    iconRes = R.drawable.ic_map_24,
                    backgroundColor = MonasteryOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(AppRoutes.VIRTUAL_VISIT) }
                )
                HomeGridButton(
                    text = stringResource(R.string.child_mode),
                    iconRes = R.drawable.outline_account_child_invert_24,
                    backgroundColor = Color(0xFF6EB017),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(AppRoutes.MODO_NINYOS) }
                )
            }

            // FILA 2
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeGridButton(
                    text = stringResource(R.string.book_appointment),
                    iconRes = R.drawable.ic_time_24,
                    backgroundColor = MonasteryBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(AppRoutes.OPCIONES_RESERVA) }
                )

                if (!isEditing) {
                    HomeGridButton(
                        text = stringResource(R.string.edit_mode),
                        iconRes = R.drawable.lapiz,
                        backgroundColor = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(AppRoutes.MODO_EDICION) }
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// =============================================================================
// COMPONENTES
// =============================================================================

@Composable
fun HomeListButton(
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
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 1, // Evita saltos de línea feos en lista
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

@Composable
fun HomeGridButton(
    text: String,
    iconRes: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Autoajuste de tamaño según espacio disponible
            val availableHeight = maxHeight
            val iconSize = if (availableHeight < 100.dp) 24.dp else 40.dp
            val textSize = if (availableHeight < 100.dp) 14.sp else 18.sp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text,
                    fontSize = textSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = textSize * 1.1,
                    maxLines = 2, // Permite 2 líneas
                    overflow = TextOverflow.Ellipsis // Puntos suspensivos si no cabe
                )
            }
        }
    }
}