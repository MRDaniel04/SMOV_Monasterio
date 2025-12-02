package com.nextapp.monasterio.ui.screens.pinCreation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.nextapp.monasterio.models.ImageTag
import com.nextapp.monasterio.ui.screens.pinCreation.components.*
import com.nextapp.monasterio.ui.screens.pinCreation.state.ImagenesState
import com.nextapp.monasterio.ui.screens.pinCreation.state.PinImage

@Composable
fun ImageTaggingDialog(
    uri: Uri,
    index: Int,
    totalImages: Int,
    onTagSelected: (ImageTag) -> Unit,
    onCancel: () -> Unit
) {
    var selectedTag by remember { mutableStateOf<ImageTag?>(null) }

    Dialog(onDismissRequest = onCancel) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(
                // Ajustamos la altura mínima para el diseño 2x2
                modifier = Modifier.padding(20.dp).heightIn(min = 350.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Etiqueta para la Imagen ${index + 1} de $totalImages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                Card(modifier = Modifier.size(150.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Selector de Etiquetas (Con "Otro")
                ImageTagSelector(
                    selectedTag = selectedTag,
                    onTagSelected = { selectedTag = it }
                )

                Spacer(Modifier.height(16.dp))

                // ALINEACIÓN DE BOTONES (DESCARTAR a la izquierda, SIGUIENTE/HECHO a la derecha)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween, // Alinea extremos
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón DESCARTAR (Izquierda)
                    TextButton(onClick = onCancel) {
                        Text("DESCARTAR")
                    }

                    // Botón SIGUIENTE/HECHO (Derecha)
                    Button(
                        onClick = {
                            if (selectedTag != null) {
                                onTagSelected(selectedTag!!)
                                selectedTag = null
                            }
                        },
                        enabled = selectedTag != null,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(if (index < totalImages - 1) "SIGUIENTE" else "HECHO")
                    }
                }
            }
        }
    }
}
@Composable
fun PinImageSelector(
    label: String,
    state: ImagenesState,
    mandatory: Boolean
) {
    var urisToTag by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentTaggingIndex by remember { mutableStateOf(0) }
    var isSelecting by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        isSelecting = false

        if (uris.isNotEmpty()) {
            urisToTag = uris
            currentTaggingIndex = 0
        }
    }

    Text(
        text = "$label (${state.images.size} añadidas)",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        ),
    )

    Spacer(Modifier.height(10.dp))


    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AddImageButton(
        ) {
            if (!isSelecting) {
                isSelecting = true // Activa el semáforo
                launcher.launch("image/*")
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.images, key = { it.id}) { pinImage ->

                PinImageItem(
                    pinImage = pinImage,
                    onRemove = {
                        state.remove(pinImage.uri.toString())
                    },
                    onTagSelected = { newTag ->
                        state.updateTag(pinImage.uri, newTag)
                    }
                )
            }
        }


    }


    Spacer(Modifier.height(4.dp)) // Espacio reducido

    if (state.images.size > 2) { // Solo mostrar si hay suficientes imágenes para desbordar (aprox.)
        Text(
            text = "Desliza para ver más imágenes",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.secondary,
            // Alineación: 120dp (AddImageButton) + 16dp (Spacing) = 136.dp
            modifier = Modifier.padding(start = 136.dp)
        )
    }

    Spacer(Modifier.height(6.dp))

    Spacer(Modifier.height(10.dp))

    val hasUntaggedImages = !state.allImagesTagged

    if (mandatory && state.images.isEmpty()) {
        Text(
            text = "Debe añadir al menos una imagen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp)
        )
    } else if (hasUntaggedImages) {
        Text(
            text = "Una o más imágenes cargadas no tienen etiqueta. Por favor, corríjalo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    // LÓGICA DEL DIÁLOGO SECUENCIAL
    if (urisToTag.isNotEmpty()) {
        if (currentTaggingIndex >= urisToTag.size) {

            urisToTag = emptyList()
            currentTaggingIndex = 0
            return
        }

        val currentUri = urisToTag[currentTaggingIndex]

        ImageTaggingDialog(
            uri = currentUri,
            index = currentTaggingIndex,
            totalImages = urisToTag.size,
            onTagSelected = { tag ->
                val taggedPinImage = PinImage(uri = currentUri, tag = tag)
                state.addTaggedImage(taggedPinImage)

                val nextIndex = currentTaggingIndex + 1
                if (nextIndex < urisToTag.size) {
                    currentTaggingIndex = nextIndex
                } else {
                    // Proceso finalizado
                    urisToTag = emptyList()
                    currentTaggingIndex = 0
                }
            },
            onCancel = {
                // Descarta la URI actual y avanza
                val nextIndex = currentTaggingIndex + 1
                if (nextIndex < urisToTag.size) {
                    currentTaggingIndex = nextIndex
                } else {
                    urisToTag = emptyList()
                    currentTaggingIndex = 0
                }
            }
        )
    }
}