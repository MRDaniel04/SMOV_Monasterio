package com.nextapp.monasterio.ui.screens


import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

    val context = LocalContext.current
    val winSoundPlayer = remember {
        android.media.MediaPlayer.create(context, R.raw.juego) // Asegúrate de que el archivo existe
    }

    val viewModel: DiferenciasViewModel = viewModel(factory = DiferenciasViewModel.Companion.Factory(prefsRepository,context))

    val juegoActual by viewModel.juegoActual.collectAsState()
    val contador by viewModel.diferenciasEncontradas.collectAsState()

    val diferenciasList = juegoActual.diferencias
    val juegoTerminado = contador == diferenciasList.size

    var showInstructionsPreviewDialogBoton by remember { mutableStateOf(false) }
    val showInstructionsPreviewDialog by viewModel.showInstructionsDialog.collectAsState()

    var mostrarPista by remember { mutableStateOf(false) }
    var textoPista by remember { mutableStateOf("") }
    
    val activity = context as? Activity

    if(showInstructionsPreviewDialogBoton || showInstructionsPreviewDialog){
        DiferenciasDialog(
            onDismiss = {
                viewModel.markInstructionsAsShown()
                showInstructionsPreviewDialogBoton = false},
            onConfirm = {
                viewModel.markInstructionsAsShown()
                showInstructionsPreviewDialogBoton = false},
            titulo = stringResource(R.string.title_instructions),
            texto = stringResource(R.string.text_instructions_spot_the_difference)
        )
    }

    if(mostrarPista){
        DiferenciasDialog(
            onDismiss = {mostrarPista = false},
            onConfirm = {mostrarPista = false},
            titulo = stringResource(R.string.clue),
            texto = textoPista
        )
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {winSoundPlayer.release()}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp,top=8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.differences_counter, contador, diferenciasList.size),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(end = 32.dp)
            )
            IconButton(
                onClick = { showInstructionsPreviewDialogBoton = true },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.question),
                    contentDescription = stringResource(R.string.title_instructions),
                    tint = Color.Black.copy(alpha = 0.7f),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    textoPista = viewModel.mostrarPistasAleatoria()
                    mostrarPista = true},
            ) {
                Text(stringResource(R.string.clue_button))
            }
        }
    }
    LaunchedEffect(juegoTerminado) {
        if (juegoTerminado) {
            if (winSoundPlayer != null) {
                if (winSoundPlayer.isPlaying) winSoundPlayer.seekTo(0)
                winSoundPlayer.start()
            }
        }
    }
    if (juegoTerminado) {
        AlertDialog(
            title = { Text(stringResource(R.string.congratulations)) },
            text = { Text(stringResource(R.string.message_game_completed)) },
            confirmButton = {
                Button(onClick = {
                    navController.navigate(AppRoutes.JUEGO_DIFERENCIAS) {
                        popUpTo(AppRoutes.JUEGO_DIFERENCIAS) {
                            inclusive = true
                        }
                    }
                }) {
                    Text(stringResource(R.string.ready))
                }
            },
            onDismissRequest = { /* No se puede cerrar sin reiniciar */ }
        )
    }
}

@Composable
fun DiferenciasDialog(
    onDismiss: () -> Unit,
    onConfirm : () -> Unit,
    titulo: String,
    texto:String
){
    AlertDialog(
        onDismissRequest=onDismiss,
        title = {
            Text(titulo)
        },
        text = {
            Text(texto)
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.ready))
            }
        },
    )
}