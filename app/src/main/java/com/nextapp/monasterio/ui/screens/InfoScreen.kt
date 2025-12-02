package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.Image // IMPORTANTE: Añadido import de Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale // IMPORTANTE: Añadido
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.InfoModel
import com.nextapp.monasterio.ui.components.EditableText
import com.nextapp.monasterio.ui.theme.MonasteryRed
import com.nextapp.monasterio.ui.theme.White
import com.nextapp.monasterio.viewModels.InfoViewModel

@Composable
fun InfoScreen(
    isEditing: Boolean = false,
    viewModel: InfoViewModel = viewModel(),
    topPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    // Obtenemos el estado (que es de tipo InfoModel según tu ViewModel)
    val infoData by viewModel.infoState.collectAsState()
    val backgroundColor = Color(0xFFC8A284)
    // Detectar orientación
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Permitir rotación
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { }
    }

    // --- CONTENEDOR RAÍZ CON FONDO ---
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. IMAGEN DE FONDO (Ocupa todo, SIN padding)
        Image(
            painter = painterResource(id = R.drawable.fondo_desplegable), // 👈 TU IMAGEN
            contentDescription = "Fondo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. CONTENIDO (Con Padding)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding) // 👈 AJUSTE AL TOP PADDING
                .padding(16.dp)      // Padding interno original
        ) {
            if (isLandscape) {
                // --- DISEÑO HORIZONTAL ---
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // IZQUIERDA: Texto Principal
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                    ) {
                        InfoMainContent(
                            infoData = infoData,
                            isEditing = isEditing,
                            viewModel = viewModel
                        )
                    }

                    // DERECHA: Tarjeta Roja
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Scroll vertical por si la pantalla es bajita
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            InfoDetailsCard(
                                infoData = infoData,
                                isEditing = isEditing,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            } else {
                // --- DISEÑO VERTICAL (Tu original) ---
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ARRIBA: Texto Principal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        InfoMainContent(
                            infoData = infoData,
                            isEditing = isEditing,
                            viewModel = viewModel
                        )
                    }

                    // ABAJO: Tarjeta Roja
                    Box(modifier = Modifier.padding(top = 16.dp)) {
                        InfoDetailsCard(
                            infoData = infoData,
                            isEditing = isEditing,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------
// COMPONENTES REUTILIZABLES (SIN CAMBIOS)
// -----------------------------------------------------------

@Composable
fun InfoMainContent(
    infoData: InfoModel,
    isEditing: Boolean,
    viewModel: InfoViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Información General",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        EditableText(
            textMap = infoData.mainContent,
            isEditing = isEditing,
            onTextMapChange = { viewModel.updateMainContent(it) },
            readOnlyStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun InfoDetailsCard(
    infoData: InfoModel,
    isEditing: Boolean,
    viewModel: InfoViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MonasteryRed
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.general_info),
                style = MaterialTheme.typography.titleLarge,
                color = White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                iconResId = R.drawable.location,
                textMap = infoData.location,
                isEditing = isEditing,
                onUpdate = { viewModel.updateLocation(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(
                iconResId = R.drawable.ic_time_24,
                textMap = infoData.hours,
                isEditing = isEditing,
                onUpdate = { viewModel.updateHours(it) }
            )
        }
    }
}

@Composable
fun InfoRow(
    iconResId: Int,
    textMap: Map<String, String>,
    isEditing: Boolean,
    onUpdate: (Map<String, String>) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))

        // Contenido editable
        Box(modifier = Modifier.weight(1f)) {
            EditableText(
                textMap = textMap,
                isEditing = isEditing,
                onTextMapChange = onUpdate,
                readOnlyStyle = MaterialTheme.typography.bodyLarge.copy(color = White),
                editTextColor = White
            )
        }
    }
}