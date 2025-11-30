package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.ParejasPieza
import com.nextapp.monasterio.models.ParejasSize
import com.nextapp.monasterio.repository.UserPreferencesRepository
import com.nextapp.monasterio.viewModels.ParejasViewModel
import com.nextapp.monasterio.viewModels.ParejasViewModelFactory


@Composable
fun ParejasScreen(
    navController: NavController,
    size : ParejasSize,
    imagenes : List<Int>
){
    val prefsRepository = remember { UserPreferencesRepository.instance }

    val factory = remember { ParejasViewModelFactory(size, imagenes,prefsRepository) }
    val viewModel: ParejasViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    val showInstructionsPreviewDialog by viewModel.showInstructionsDialog.collectAsState()

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {

        }
    }

    if(showInstructionsPreviewDialog){
        ParejaDialog(
            onDismiss = {},
            onConfirm = { viewModel.markInstructionsAsShown()},
            titulo = stringResource(R.string.title_instructions),
            texto = stringResource(R.string.text_instructions_puzzle)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.left_pairs,state.parejas),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(size.columns),
            modifier = Modifier
                .wrapContentHeight(Alignment.CenterVertically)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp)
                .then(
                    if(size.rows == 4 ) {
                        Modifier.width(300.dp)
                    } else if (size.rows == 5){
                        Modifier.width(250.dp)
                    } else{
                        Modifier.fillMaxWidth()
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(
                state.piezas,
            ){pieza ->
                GridCell(pieza = pieza, mostradoInicial = state.mostradoInicial, verificandoPareja = state.verificandoPareja, onClick = { id -> viewModel.onClickPieza(id) })
            }
        }
    }

    if (state.solucionado) {
        ParejaDialog(onDismiss = {}, onConfirm = { navController.popBackStack() },stringResource(R.string.congratulations),stringResource(R.string.message_game_completed))
    }
}



@Composable
fun ParejaDialog(
    onDismiss: () -> Unit,
    onConfirm : () -> Unit,
    titulo:String,
    texto:String
){
    AlertDialog(
        onDismissRequest=onDismiss,
        title = {
            Text(titulo)
        },
        text = {
            Text(texto)
        },confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.ready))
            }
        },
    )
}

@Composable
fun GridCell(
    pieza: ParejasPieza,
    mostradoInicial : Boolean,
    verificandoPareja : Boolean,
    onClick : (Int) -> Unit,
) {
    val conPareja = pieza.conPareja
    val isClickable = !conPareja && !verificandoPareja
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = if (conPareja || pieza.estaVolteada) 4.dp else 2.dp),
        modifier = Modifier
            .aspectRatio(1f)
            .then(
                if (isClickable && !mostradoInicial){
                    Modifier.clickable{ onClick(pieza.id) }
                } else{
                    Modifier
                }
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = if(conPareja || pieza.estaVolteada || mostradoInicial) 1 else 0,
                animationSpec = tween(durationMillis = 300),
                label="CardFlipAnimation"
            ) { state ->
                if (state == 1) {
                    Image(
                        painter = painterResource(id = pieza.imagen),
                        contentDescription = "Pieza ${pieza.id}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painterResource(R.drawable.escudo),
                        contentDescription = stringResource(R.string.back_piece),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}