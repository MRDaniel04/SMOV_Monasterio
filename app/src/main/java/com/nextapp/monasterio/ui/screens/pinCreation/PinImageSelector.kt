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
fun ImageDetailsDialog( // 🆕 Renombrado
    pinImage: PinImage, // 🆕 Ahora recibe el objeto PinImage completo
    index: Int,
    totalImages: Int,
    isInitialTagging: Boolean, // 🆕 Nuevo: Indica si es la primera vez que se etiquetan las URIs.
    onSave: (ImageTag?, String, String, String, String) -> Unit, // 🆕 Guarda tag y 4 títulos
    onCancel: () -> Unit
) {
    // 1. Estados locales para los campos del diálogo
    var selectedTag by remember { mutableStateOf(pinImage.tag) }
    var tituloEs by remember { mutableStateOf(pinImage.titulo_es) }
    var tituloEn by remember { mutableStateOf(pinImage.titulo_en) }
    var tituloDe by remember { mutableStateOf(pinImage.titulo_de) }
    var tituloFr by remember { mutableStateOf(pinImage.titulo_fr) }

    val isFormValid = tituloEs.isNotBlank() && selectedTag != null
    val dialogTitle = if (isInitialTagging) {
        "Etiqueta y Título (Imagen ${index + 1} de $totalImages)"
    } else {
        "Editar Detalles de Imagen"
    }

    Dialog(onDismissRequest = onCancel) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .heightIn(max = 600.dp), // Limitar altura para hacer espacio para el scroll
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                // --- VISUALIZACIÓN DE IMAGEN ---
                Card(modifier = Modifier.size(150.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(pinImage.uri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // --- SELECTOR DE ETIQUETAS ---
                ImageTagSelector(
                    selectedTag = selectedTag,
                    onTagSelected = { selectedTag = it }
                )

                Spacer(Modifier.height(16.dp))

                // --- CAMPOS DE TÍTULO MULTILINGÜE ---
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                ) {
                    Text(
                        text = "Título del Contenido de la Imagen (ES - Obligatorio)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = tituloEs,
                        onValueChange = { tituloEs = it },
                        label = { Text("Título (ES)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tituloEn,
                        onValueChange = { tituloEn = it },
                        label = { Text("Título (EN - Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tituloDe,
                        onValueChange = { tituloDe = it },
                        label = { Text("Título (DE - Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tituloFr,
                        onValueChange = { tituloFr = it },
                        label = { Text("Título (FR - Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // --- BOTONES ---
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onCancel) {
                        Text(if (isInitialTagging) "DESCARTAR" else "CANCELAR")
                    }

                    Button(
                        onClick = {
                            onSave(selectedTag, tituloEs, tituloEn, tituloDe, tituloFr)
                            // Limpiamos los estados locales, aunque el diálogo se cierra.
                            selectedTag = null
                            tituloEs = ""
                        },
                        enabled = isFormValid,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(if (isInitialTagging && index < totalImages - 1) "SIGUIENTE" else "GUARDAR")
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
    mandatory: Boolean,
    onChanged: () -> Unit
) {
    var urisToTag by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentTaggingIndex by remember { mutableStateOf(0) }
    var isSelecting by remember { mutableStateOf(false) }

    // 🆕 Estado para la edición individual
    var imageToEdit by remember { mutableStateOf<PinImage?>(null) }

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
                        onChanged()
                    },
                    // 🆕 Nuevo: Dispara el diálogo de edición
                    onEditDetails = { imageToEdit = it }
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

            modifier = Modifier.padding(start = 136.dp)
        )
    }

    Spacer(Modifier.height(16.dp))

    val hasUntaggedOrUntitledImages = !state.allImagesTagged

    if (mandatory && state.images.isEmpty()) {
        Text(
            text = "Debe añadir al menos una imagen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp)
        )
    } else if (hasUntaggedOrUntitledImages) {
        Text(
            text = "Una o más imágenes no tienen etiqueta o les falta el Título (ES). Por favor, corríjalo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    // --- LÓGICA DEL DIÁLOGO DE EDICIÓN INDIVIDUAL ---
    imageToEdit?.let { image ->
        ImageDetailsDialog(
            pinImage = image,
            index = -1, // Irrelevante para edición
            totalImages = 1, // Irrelevante para edición
            isInitialTagging = false,
            onSave = { tag, es, en, de, fr ->
                state.updateImageDetails(image.uri, tag, es, en, de, fr)
                imageToEdit = null
                onChanged()
            },
            onCancel = {
                imageToEdit = null
            }
        )
    }


    // --- LÓGICA DEL DIÁLOGO SECUENCIAL (Creación) ---
    if (urisToTag.isNotEmpty() && imageToEdit == null) { // Aseguramos que no se superpongan diálogos
        if (currentTaggingIndex >= urisToTag.size) {

            urisToTag = emptyList()
            currentTaggingIndex = 0
            onChanged() // Llamamos onChanged al finalizar el flujo
            return
        }

        val currentUri = urisToTag[currentTaggingIndex]

        // Creamos un objeto PinImage temporal para el diálogo.
        val tempPinImage = remember { PinImage(uri = currentUri) }


        ImageDetailsDialog(
            pinImage = tempPinImage,
            index = currentTaggingIndex,
            totalImages = urisToTag.size,
            isInitialTagging = true, // Indica que es el flujo secuencial
            onSave = { tag, es, en, de, fr ->
                // Creamos el PinImage final con todos los detalles
                val taggedPinImage = PinImage(
                    uri = currentUri,
                    tag = tag,
                    titulo_es = es,
                    titulo_en = en,
                    titulo_de = de,
                    titulo_fr = fr
                )
                state.addTaggedImage(taggedPinImage) // Añadimos la imagen completa

                val nextIndex = currentTaggingIndex + 1
                if (nextIndex < urisToTag.size) {
                    currentTaggingIndex = nextIndex
                } else {
                    // Proceso finalizado
                    urisToTag = emptyList()
                    currentTaggingIndex = 0
                    onChanged()
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
                    onChanged() // Llamamos onChanged al descartar todas las pendientes
                }
            }
        )
    }
}