package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log // Importante para los logs
import android.view.HapticFeedbackConstants // Importante para la vibración
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.GridPosicion
import com.nextapp.monasterio.models.PiezaPuzzle
import com.nextapp.monasterio.models.PuzzleData
import com.nextapp.monasterio.models.PuzzleRotador
import com.nextapp.monasterio.models.PuzzleSize
import com.nextapp.monasterio.repository.UserPreferencesRepository
import com.nextapp.monasterio.viewModels.PuzzleViewModel
import com.nextapp.monasterio.viewModels.PuzzleViewModelFactory

@Composable
fun PuzzleScreen(
    navController: NavController,
    tamaño: PuzzleSize,
) {

    val conjuntosDelNivel = remember(tamaño){
        when(tamaño.rows * tamaño.columns){
            4 -> PuzzleData.PUZZLES_NIVEL1
            9 -> PuzzleData.PUZZLES_NIVEL2
            16 -> PuzzleData.PUZZLES_NIVEL3
            else -> PuzzleData.PUZZLES_NIVEL4
        }
    }

    val ruta = remember(tamaño){
        when(tamaño.rows * tamaño.columns){
            4 -> AppRoutes.PUZZLENIVEL1
            9 -> AppRoutes.PUZZLENIVEL2
            16 -> AppRoutes.PUZZLENIVEL3
            else -> AppRoutes.PUZZLENIVEL4
        }
    }

    val puzzleSetSeleccionado = remember {
        val siguienteIndice = PuzzleRotador.getSiguienteIndice(conjuntosDelNivel.size)
        conjuntosDelNivel.get(siguienteIndice)
    }

    val listaPiezas = puzzleSetSeleccionado.piezas
    val imagenCompleta = puzzleSetSeleccionado.imagenCompleta

    val prefsRepository = remember { UserPreferencesRepository.instance }

    val factory = remember { PuzzleViewModelFactory(tamaño, listaPiezas,prefsRepository,imagenCompleta) }
    val viewModel: PuzzleViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()
    val density = LocalDensity.current

    val context = LocalContext.current
    val winSoundPlayer = remember {
        android.media.MediaPlayer.create(context, R.raw.juego)
    }
    val activity = context as? Activity


    var tamañoCelda by remember { mutableStateOf(0.dp) }
    var gridOriginOffset by remember { mutableStateOf(Offset.Zero) }

    var piezaArrastradaId by remember { mutableStateOf<Int?>(null) }
    var desplazamientoPiezaArrastrada by remember { mutableStateOf<Offset>(Offset.Zero) }


    val trayPositionsMap = remember { mutableStateMapOf<Int, Offset>() }
    
    var overlayRootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    var showImagePreviewDialog by remember { mutableStateOf(false) }
    val showInstructionsPreviewDialog by viewModel.showInstructionsDialog.collectAsState()
    var showInstructionsPreviewDialogBoton by remember { mutableStateOf(false) }

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidth > 600
    val piezasSueltasRestantes = state.piezas.count { !it.encajada }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            winSoundPlayer.release()
        }
    }

    if(showInstructionsPreviewDialogBoton || showInstructionsPreviewDialog){
        PuzzleDialog(
            onDismiss = {
                viewModel.markInstructionsAsShown()
                showInstructionsPreviewDialogBoton = false},
            onConfirm = {
                viewModel.markInstructionsAsShown()
                showInstructionsPreviewDialogBoton = false},
            titulo = stringResource(R.string.title_instructions),
            texto = stringResource(R.string.text_instructions_puzzle)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)

                .onGloballyPositioned { coords ->
                    overlayRootCoordinates = coords
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = stringResource(R.string.left_pieces,piezasSueltasRestantes),
                    style = typography.titleMedium,
                    modifier = Modifier.padding(end = 32.dp)
                )
                Spacer(modifier= Modifier.width(16.dp))
                IconButton(
                    onClick = { showInstructionsPreviewDialogBoton = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.question),
                        contentDescription = stringResource(R.string.title_instructions),
                        tint = Color.Black.copy(alpha = 0.7f),
                    )
                }
            }


            LazyVerticalGrid(
                columns = GridCells.Fixed(tamaño.columns),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .background(Color.LightGray.copy(0.5f))
                    .border(1.dp,Color.Black)
                    .onGloballyPositioned { coordinates ->
                        val gridWith = coordinates.size.width
                        if (gridWith > 0) {
                            tamañoCelda = with(density) { (gridWith / state.size.columns).toDp() }
                            gridOriginOffset = coordinates.positionInWindow()
                        }
                    },
                userScrollEnabled = false
            ) {
                items(tamaño.rows * tamaño.columns) { index ->
                    val row = index / tamaño.columns
                    val col = index % tamaño.columns
                    val gridPosicion = GridPosicion(row, col)
                    val piezaEnPosicion = state.piezas.find { it.posicionCorrecta == gridPosicion }
                    GridCell(pieza = piezaEnPosicion)
                }
            }

            Button(
                onClick = { showImagePreviewDialog = true },
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text(text = stringResource(R.string.original_image))
            }
            Text(
                text = stringResource(R.string.puzzle_slide),
                style = typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))


            if (tamañoCelda > 0.dp) {
                val shuffledLoosePieces by remember(state.piezas) {
                    mutableStateOf(state.piezas.filter { !it.encajada }.shuffled())
                }

                PuzzleTray(
                    piezas = shuffledLoosePieces,
                    tamañoPieza = tamañoCelda,
                    idPiezaArrastrada = piezaArrastradaId,
                    overlayRootCoordinates = overlayRootCoordinates,


                    onDragStart = { pieza, posicionAbsoluta ->
                        piezaArrastradaId = pieza.id
                        desplazamientoPiezaArrastrada = Offset.Zero

                        trayPositionsMap[pieza.id] = posicionAbsoluta
                    },


                    onDrag = { dragAmount ->
                        desplazamientoPiezaArrastrada += dragAmount
                    },


                    onDragEnd = {
                        if (piezaArrastradaId != null) {
                            val startOffset = trayPositionsMap[piezaArrastradaId] ?: Offset.Zero
                            val piezaSizePx = with(density) { tamañoCelda.toPx() }

                            val finalTLX = startOffset.x + desplazamientoPiezaArrastrada.x
                            val finalTLY = startOffset.y + desplazamientoPiezaArrastrada.y


                            val relativeX = finalTLX - gridOriginOffset.x
                            val relativeY = finalTLY - gridOriginOffset.y


                            val centroRelativeX = relativeX + (piezaSizePx / 2f)
                            val centroRelativeY = relativeY + (piezaSizePx / 2f)

                            val newColumn = (centroRelativeX / piezaSizePx).toInt().coerceIn(0, tamaño.columns - 1)
                            val newRow = if ((tamaño.columns == 2 || tamaño.columns == 3) && isTablet) {
                                (centroRelativeY / piezaSizePx).toInt().coerceIn(0, tamaño.rows - 1)
                            } else {
                                val Y_Compensado = centroRelativeY + piezaSizePx * 1.0f
                                (Y_Compensado / piezaSizePx).toInt().coerceIn(0, tamaño.rows - 1)
                            }


                            viewModel.soltarPieza(piezaArrastradaId!!, GridPosicion(newRow, newColumn))
                        }

                        piezaArrastradaId = null
                        desplazamientoPiezaArrastrada = Offset.Zero
                    }
                )
            }

            LaunchedEffect(state.solucionado) {
                if (state.solucionado) {
                    if (winSoundPlayer != null) {
                        if (winSoundPlayer.isPlaying) winSoundPlayer.seekTo(0)
                        winSoundPlayer.start()
                    }
                }
            }

            if (state.solucionado) {
                PuzzleDialog(
                    onDismiss = {},
                    onConfirm = {
                        navController.navigate(ruta){
                            popUpTo(ruta){ inclusive = true }
                        }
                    },
                    stringResource(R.string.congratulations),
                    stringResource(R.string.message_game_completed)
                )
            }
            if (showImagePreviewDialog) {
                ImagePreviewDialog(
                    imageResId = imagenCompleta,
                    onDismiss = { showImagePreviewDialog = false }
                )
            }
        }

        piezaArrastradaId?.let { id ->
            val piezaFlotante = state.piezas.find { it.id == id }!!
            val startOffset = trayPositionsMap[id] ?: Offset.Zero

            DraggableFloatingPiece(
                pieza = piezaFlotante,
                tamañoCelda = tamañoCelda,
                startOffset = startOffset,
                desplazamientoActual = desplazamientoPiezaArrastrada
            )
        }
    }
}




@Composable
fun PuzzleTray(
    piezas: List<PiezaPuzzle>,
    tamañoPieza: Dp,
    idPiezaArrastrada : Int?,
    overlayRootCoordinates: LayoutCoordinates?,
    onDragStart: (PiezaPuzzle, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {

    val view = LocalView.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(tamañoPieza + 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(piezas, key = { it.id }) { pieza ->
                val isBeingDragged = pieza.id == idPiezaArrastrada

                var myItemCoordinates: LayoutCoordinates? = null

                Box(
                    modifier = Modifier
                        .size(tamañoPieza)
                        .onGloballyPositioned { coords ->
                            myItemCoordinates = coords
                        }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { _ ->

                                    Log.d("PuzzleDebug", "LONG PRESS detectado en pieza ${pieza.id}")


                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

                                    val currentCoords = myItemCoordinates
                                    val rootCoords = overlayRootCoordinates

                                    if (currentCoords != null && rootCoords != null && currentCoords.isAttached) {
                                        val posInOverlay = rootCoords.localPositionOf(currentCoords, Offset.Zero)
                                        onDragStart(pieza, posInOverlay)
                                    } else {
                                        Log.e("PuzzleDebug", "ERROR: Coordenadas nulas o no adjuntas")
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDrag(dragAmount)
                                },
                                onDragEnd = {
                                    Log.d("PuzzleDebug", "Drag End")
                                    onDragEnd()
                                },
                                onDragCancel = {
                                    Log.d("PuzzleDebug", "Drag Cancelled")
                                    onDragEnd()
                                }
                            )
                        }
                ) {
                    if (isBeingDragged) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent))
                    } else {
                        Image(
                            painter = painterResource(id = pieza.imagen),
                            contentDescription = "Pieza ${pieza.id}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DraggableFloatingPiece(
    pieza: PiezaPuzzle,
    tamañoCelda: Dp,
    startOffset: Offset,
    desplazamientoActual: Offset
) {
    val totalOffsetX = startOffset.x + desplazamientoActual.x
    val totalOffsetY = startOffset.y + desplazamientoActual.y

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = totalOffsetX.toInt(),
                    y = totalOffsetY.toInt()
                )
            }
            .zIndex(10f)
            .size(tamañoCelda)
    ) {
        Image(
            painter = painterResource(id = pieza.imagen),
            contentDescription = "Pieza flotante ${pieza.id}",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.85f)
        )
    }
}

@Composable
fun GridCell(pieza: PiezaPuzzle?) {
    val isEncajada = pieza?.encajada ?: false
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .border(1.dp, Color.Black)
            .background(Color.LightGray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (isEncajada) {
            Image(
                painter = painterResource(id = pieza!!.imagen),
                contentDescription = "Pieza encajada ${pieza.id}",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun PuzzleDialog(
    onDismiss: () -> Unit,
    onConfirm : () -> Unit,
    titulo: String,
    texto:String
){
    AlertDialog(
        onDismissRequest=onDismiss,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.ready))
            }
        },
    )
}

@Composable
fun ImagePreviewDialog(
    imageResId: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = stringResource(R.string.original_image),
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}