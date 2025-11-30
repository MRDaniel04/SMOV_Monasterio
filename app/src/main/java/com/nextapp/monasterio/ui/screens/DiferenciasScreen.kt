package com.nextapp.monasterio.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextapp.monasterio.viewModels.ImagenConToque
import com.nextapp.monasterio.viewModels.DiferenciasViewModel
import com.nextapp.monasterio.R

@Composable
fun DiferenciasScreen(
    viewModel: DiferenciasViewModel = viewModel()
) {
    val juegoActual by viewModel.juegoActual.collectAsState()
    val contador by viewModel.diferenciasEncontradas.collectAsState()

    val diferenciasList = juegoActual.diferencias
    val juegoTerminado = contador == diferenciasList.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
       Text(
            text = stringResource(R.string.differences_counter,contador,diferenciasList.size),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImagenConToque(
                resId = juegoActual.imagenOriginal,
                diferencias = diferenciasList,
                onHit = viewModel::onTouch,
                modifier = Modifier.weight(1f)
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
                Button(onClick = viewModel::reiniciarJuego) {
                    Text("Jugar de Nuevo")
                }
            },
            onDismissRequest = { /* No se puede cerrar sin reiniciar */ }
        )
    }
}