package com.nextapp.monasterio.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.viewModels.ImagenConToque
import com.nextapp.monasterio.viewModels.DiferenciasViewModel
import com.nextapp.monasterio.R
import com.nextapp.monasterio.repository.UserPreferencesRepository

@Composable
fun DiferenciasScreen(navController : NavController
) {

    val prefsRepository = remember { UserPreferencesRepository.instance }

    val viewModel: DiferenciasViewModel = viewModel(factory = DiferenciasViewModel.Companion.Factory(prefsRepository))

    val juegoActual by viewModel.juegoActual.collectAsState()
    val contador by viewModel.diferenciasEncontradas.collectAsState()

    val diferenciasList = juegoActual.diferencias
    val juegoTerminado = contador == diferenciasList.size

    var showInstructionsPreviewDialog by remember { mutableStateOf(true) }

    if(showInstructionsPreviewDialog){
        PuzzleDialog(
            onDismiss = {showInstructionsPreviewDialog =false},
            onConfirm = {showInstructionsPreviewDialog =false},
            titulo = stringResource(R.string.title_instructions),
            texto = stringResource(R.string.text_instructions_spot_the_difference)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.differences_counter, contador, diferenciasList.size),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )
            IconButton(
                onClick = { showInstructionsPreviewDialog = true },
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.help),
                    contentDescription = stringResource(R.string.title_instructions),
                    tint = Color.Black.copy(alpha = 0.7f),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(juegoActual.imagenOriginal),
                contentDescription="",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            ImagenConToque(
                resId = juegoActual.imagenModificada,
                diferencias = diferenciasList,
                onHit = viewModel::onTouch,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (juegoTerminado) {
        AlertDialog(
            title = { Text(stringResource(R.string.congratulations)) },
            text = { Text(stringResource(R.string.message_game_completed)) },
            confirmButton = {
                Button(onClick = {
                    navController.navigate(AppRoutes.JUEGO_DIFERENCIAS)
                }) {
                    Text(stringResource(R.string.ready))
                }
            },
            onDismissRequest = { /* No se puede cerrar sin reiniciar */ }
        )
    }
}