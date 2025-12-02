package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nextapp.monasterio.models.GridPosicion
import com.nextapp.monasterio.models.PiezaPuzzle
import com.nextapp.monasterio.models.PuzzleSize
import com.nextapp.monasterio.viewModels.PuzzleViewModel
import com.nextapp.monasterio.viewModels.PuzzleViewModelFactory
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PuzzleData
import com.nextapp.monasterio.models.PuzzleRotador
import com.nextapp.monasterio.repository.UserPreferencesRepository

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

    val tamañoTotal = tamaño.rows * tamaño.columns

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
    val activity = context as? Activity

    // Estado del Grid y la celda
    var tamañoCelda by remember { mutableStateOf(0.dp) }
    var gridOriginOffset by remember { mutableStateOf(Offset.Zero) }

    // Estado de Drag and Drop (flotante)
    var piezaArrastradaId by remember { mutableStateOf<Int?>(null) }
    var desplazamientoPiezaArrastrada by remember { mutableStateOf<Offset>(Offset.Zero) }

    val normalizedTrayY = remember { mutableStateOf(0f) }

    // Mapa de posiciones iniciales de las piezas SUELTAS en la bandeja (llenado por LazyRow)
    val trayPositionsMap = remember { mutableStateMapOf<Int, Offset>() }
    var showImagePreviewDialog by remember { mutableStateOf(false) }
    val showInstructionsPreviewDialog by viewModel.showInstructionsDialog.collectAsState()

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidth > 600

    var dragStartRelativeOffset by remember { mutableStateOf<Offset>(Offset.Zero) }

    var trayOriginY by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {

        }
    }

    if(showInstructionsPreviewDialog){
        PuzzleDialog(
            onDismiss = {viewModel.markInstructionsAsShown()},
            onConfirm = {
                viewModel.markInstructionsAsShown()},
            titulo = stringResource(R.string.title_instructions),
            texto = stringResource(R.string.text_instructions_puzzle)
        )
    }
    // El Box principal actúa como la capa de superposición (Overlay) para la pieza arrastrada.
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. PUZZLE GRID (Drop Targets y Piezas Encajadas) ---
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
                onClick = { showImagePreviewDialog = true }, // Cambia el estado a true
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text(text = stringResource(R.string.original_image)) // "Mostrar Imagen Original"
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tamañoCelda > 0.dp) {
                val shuffledLoosePieces by remember(state.piezas) { // Clave: Número de piezas sueltas
                    mutableStateOf(state.piezas.filter { !it.encajada }.shuffled())
                }
                PuzzleTray(
                    piezas = shuffledLoosePieces,
                    tamañoPieza = tamañoCelda,
                    idPiezaArrastrada =  piezaArrastradaId,
                    isDragInProgress = piezaArrastradaId != null,
                    onPiecePositioned = { id, offset ->
                        if (normalizedTrayY.value == 0f) {
                            // 📌 Capturar el Y de la primera pieza que se posiciona
                            normalizedTrayY.value = offset.y
                            Log.d("PUZZLE_DRAG", "Normalizando Y: ${normalizedTrayY.value}")
                        }
                        // Llenar el mapa de posiciones iniciales para el Drag & Drop
                        trayPositionsMap[id] = offset
                    },
                    onTrayPositioned = { y->
                        trayOriginY = y
                    },
                    onDragStart = { p, absolutePressOffset ->
                        // INICIO DEL DRAG: Activar la pieza flotante
                        piezaArrastradaId = p.id
                        desplazamientoPiezaArrastrada = Offset.Zero
                        dragStartRelativeOffset = absolutePressOffset
                    }
                )
            }

            // --- 3. DIÁLOGO DE SOLUCIONADO ---
            if (state.solucionado) {
                PuzzleDialog(
                    onDismiss = {},
                    onConfirm = { navController.popBackStack() },
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

            Log.d("PUZZLE_DRAG", "TrayPosition (Esquina): ${startOffset.x}, ${startOffset.y}")

            DraggableFloatingPiece(
                pieza = piezaFlotante,
                tamañoCelda = tamañoCelda,
                startOffset = startOffset,
                desplazamientoActual = desplazamientoPiezaArrastrada,
                dragStartRelativeOffset = dragStartRelativeOffset,
                onDrag = { dragAmount ->
                    // MOVIMIENTO: Actualizar el desplazamiento
                    desplazamientoPiezaArrastrada += dragAmount
                },
                onDragEnd = {
                    // FIN DEL DRAG: Calcular la nueva posición y llamar al ViewModel
                    if (piezaArrastradaId != null) {
                        val piezaSize = with(density) { tamañoCelda.toPx() }
                        val piezaActual = state.piezas.find { it.id == piezaArrastradaId }!!

                        // Calcular la posición final de la pieza respecto al origen del Grid
                        val finalTLX = startOffset.x + desplazamientoPiezaArrastrada.x - dragStartRelativeOffset.x
                        val finalTLY = startOffset.y + desplazamientoPiezaArrastrada.y - dragStartRelativeOffset.y

                        val relativeX = finalTLX - gridOriginOffset.x
                        val relativeY = finalTLY - gridOriginOffset.y

                        val centroRelativeX = relativeX + (piezaSize / 2f)
                        val centroRelativeY = relativeY + (piezaSize / 2f)

                        if((tamaño.columns==2 || tamaño.columns == 3) && isTablet) {
                            val newColumn = (centroRelativeX / piezaSize).toInt().coerceIn(0, tamaño.columns - 1)
                            val newRow = (centroRelativeY / piezaSize).toInt().coerceIn(0, tamaño.rows - 1)
                            viewModel.soltarPieza(piezaArrastradaId!!, GridPosicion(newRow, newColumn))
                        }
                        else{

                            val newColumn = (centroRelativeX / piezaSize).toInt().coerceIn(0, tamaño.columns - 1)
                            val newRow = (centroRelativeY / piezaSize).toInt().coerceIn(0, tamaño.rows - 1)

                            Log.d("PUZZLE_DRAG", "--- DRAG END ---")
                            Log.d("PUZZLE_DRAG", "Tamaño Pieza (Px): $piezaSize")
                            Log.d("PUZZLE_DRAG", "Origin Grid Y: ${gridOriginOffset.y}")
                            Log.d("PUZZLE_DRAG", "Final Center Y Relative: $centroRelativeY")
                            Log.d("PUZZLE_DRAG", "Calculated Row: $newRow")
                            Log.d("PUZZLE_DRAG", "--- /DRAG END ---")

                            viewModel.soltarPieza(piezaArrastradaId!!, GridPosicion(newRow, newColumn))
                        }
                    }
                    piezaArrastradaId = null
                    desplazamientoPiezaArrastrada = Offset.Zero
                }
            )
        }
    } // Fin Box
}

// ... (El resto de composables en el siguiente bloque)

// Este Composable dibuja una celda en el Grid (vacía o con pieza encajada)
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

// --- BANDEJA (LAZY ROW) ---
@Composable
fun PuzzleTray(
    piezas: List<PiezaPuzzle>,
    tamañoPieza: Dp,
    idPiezaArrastrada : Int?,
    isDragInProgress: Boolean,
    onTrayPositioned : (Float) -> Unit,
    onPiecePositioned: (Int, Offset) -> Unit,
    onDragStart: (PiezaPuzzle, Offset) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.left_pieces,piezas.size),
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(tamañoPieza + 16.dp) // Altura fija para el LazyRow
                .onGloballyPositioned{ coordinates ->
                    onTrayPositioned(coordinates.positionInWindow().y)
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(piezas, key = { it.id }) { pieza ->
                // Este Box representa la pieza en la bandeja, que inicia el arrastre.
                val isBeingDragged = pieza.id == idPiezaArrastrada
                Box(
                    modifier = Modifier
                        .size(tamañoPieza)
                        .onGloballyPositioned { coordinates ->
                            onPiecePositioned(pieza.id, coordinates.positionInWindow())
                        }
                        .pointerInput(isDragInProgress) {
                            detectTapGestures(
                                onLongPress = { offset ->
                                    onDragStart(pieza, offset)
                                }
                            )
                        }
                ) {
                    if(!isBeingDragged) {
                        Image(
                            painter = painterResource(id = pieza.imagen),
                            contentDescription = "Pieza ${pieza.id}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else{
                        Spacer(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

// --- PIEZA FLOTANTE (OVERLAY) ---
// Este componente se dibuja en el Box principal y sigue el dedo.
@Composable
fun DraggableFloatingPiece(
    pieza: PiezaPuzzle,
    tamañoCelda: Dp,
    startOffset: Offset,
    desplazamientoActual: Offset,
    dragStartRelativeOffset : Offset,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    // Calcular la posición absoluta en la pantalla (Base de la Bandeja + Movimiento del dedo)
    val totalOffsetX = startOffset.x + desplazamientoActual.x - dragStartRelativeOffset.x
    val totalOffsetY = startOffset.y + desplazamientoActual.y - dragStartRelativeOffset.y

    Box(
        modifier = Modifier
            .offset {
                // Aplicar el desplazamiento total
                IntOffset(
                    x = totalOffsetX.toInt(),
                    y = totalOffsetY.toInt()
                )
            }
            .zIndex(10f) // Asegura que esté por encima de todo
            .size(tamañoCelda)
            .pointerInput(Unit) {
                // DETECCIÓN DE MOVIMIENTO Y FIN DE ARRASTRE
                detectDragGestures(
                    onDragStart = {  },
                    onDrag = { change, dragAmount ->
                        onDrag(dragAmount)
                        change.consume()
                    },
                    onDragEnd = onDragEnd // Llamar al fin para la lógica de soltar
                )
            }
    ) {
        Image(
            painter = painterResource(id = pieza.imagen),
            contentDescription = "Pieza flotante ${pieza.id}",
            modifier = Modifier.fillMaxSize()
        )
    }
}

// (Asumimos que PuzzleDialog está definido en tu archivo)
// ...
@Composable
fun PuzzleDialog(
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

@Composable
fun ImagePreviewDialog(
    imageResId: Int, // Recibe el ID del recurso de la imagen completa
    onDismiss: () -> Unit // Función para cerrar el diálogo
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            // Muestra la imagen completa
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = stringResource(R.string.original_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Mantener la proporción de la imagen
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close)) // "Cerrar"
            }
        }
    )
}