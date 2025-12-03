package com.nextapp.monasterio.ui.screens.pinEdition.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.ImagenData
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.repository.PinRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinDetailsPanel(
    modifier: Modifier = Modifier,
    selectedPin: PinData,
    imagenesDetalladas: List<ImagenData>,
    pinTapScreenPosition: Offset?,
    onClosePanel: () -> Unit,
    onStartMove: (PinData, Offset) -> Unit,
    onEdit: (PinData) -> Unit,
    panelHeightFraction: Float,
    onPinDeleted: (String) -> Unit
) {
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember(selectedPin.id) { mutableStateOf(true) }


    LaunchedEffect(selectedPin.id) {
        delay(500)
        isLoading = false
    }

    val hasImages = imagenesDetalladas.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(panelHeightFraction)
            .background(
                Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { Log.d("EdicionPines", "Toque en el panel consumido.") }
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón 1: Mover Pin
                    IconButton(onClick = {
                        val initialScreenPos = pinTapScreenPosition
                        if (initialScreenPos == null) {
                            Toast.makeText(
                                context,
                                "Error: No se encontró la posición inicial del pin.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@IconButton
                        }
                        onStartMove(selectedPin, initialScreenPos)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_move),
                            contentDescription = "Mover Pin"
                        )
                    }

                    // Botón 2: Editar Pin
                    IconButton(onClick = {
                        onEdit(selectedPin)
                        Toast.makeText(context, "Editar Pin", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.lapiz),
                            contentDescription = "Editar Pin"
                        )
                    }

                    // Botón 3: Borrar Pin
                    IconButton(onClick = {
                        isDeleteDialogOpen = true
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_trash),
                            contentDescription = "Borrar Pin",
                            tint = Color(0xFFFF5722)
                        )
                    }
                }

                // Grupo Derecho: Cerrar Panel
                IconButton(onClick = onClosePanel) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_24),
                        contentDescription = "Cerrar Panel"
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val ubicacionText = selectedPin.ubicacion?.name?.let { " ($it)" } ?: ""
            Text(
                text = (selectedPin.titulo ?: "Detalle del Pin") + ubicacionText,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {

                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Cargando imágenes...", color = Color.Gray)
                    }
                }
                // 2. NO HAY IMÁGENES (Solo si isLoading es false Y la lista está vacía)
                !hasImages -> {
                    Text(
                        text = "No hay imágenes detalladas disponibles.",
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                // 3. IMÁGENES DISPONIBLES (isLoading es false y la lista NO está vacía)
                else -> {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {

                        items(imagenesDetalladas) { imagen ->

                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {

                                AsyncImage(
                                    model = imagen.url,
                                    contentDescription = imagen.etiqueta,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )

                                Text(
                                    text = imagen.etiqueta,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = selectedPin.descripcion ?: "Descripción no disponible.",
                    style = TextStyle(fontSize = 12.sp),
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // --- DIÁLOGO DE CONFIRMACIÓN DE BORRADO PERMANENTE --- (Sin cambios)
    if (isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            title = { Text(text = "Confirmar Eliminación") },
            text = { Text("Estás a punto de eliminar permanentemente el pin '${selectedPin.titulo}'. Esta acción no se puede deshacer. ¿Deseas continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleteDialogOpen = false
                        val pinId = selectedPin.id
                        val ctx = context

                        coroutineScope.launch {
                            val ok = PinRepository.deletePinAndImages(pinId)

                            if (ok) {
                                Toast.makeText(ctx, "Pin eliminado correctamente", Toast.LENGTH_SHORT).show()
                                onPinDeleted(pinId)
                                onClosePanel()
                            } else {
                                Toast.makeText(ctx, "Error eliminando el pin", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("Eliminar Pin")
                }
            },
            dismissButton = {
                Button(onClick = { isDeleteDialogOpen = false }) { Text("Cancelar") }
            }
        )
    }
}