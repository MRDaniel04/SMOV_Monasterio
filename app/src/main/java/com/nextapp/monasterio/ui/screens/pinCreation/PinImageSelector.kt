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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import com.nextapp.monasterio.R

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

    val initialShow = pinImage.titulo_en.isNotBlank() || pinImage.titulo_de.isNotBlank() || pinImage.titulo_fr.isNotBlank()
    var showOptionalTitles by remember { mutableStateOf(initialShow) }

    LaunchedEffect(pinImage.uri) {
        selectedTag = pinImage.tag
        tituloEs = pinImage.titulo_es
        tituloEn = pinImage.titulo_en
        tituloDe = pinImage.titulo_de
        tituloFr = pinImage.titulo_fr
        // Reiniciamos el estado de expansión de traducciones para la nueva imagen
        showOptionalTitles = pinImage.titulo_en.isNotBlank() || pinImage.titulo_de.isNotBlank() || pinImage.titulo_fr.isNotBlank()
    }

    val isFormValid = tituloEs.isNotBlank() && selectedTag != null
    val dialogTitle = if (isInitialTagging) {
        stringResource(R.string.img_dialog_title_initial, index + 1, totalImages)
    } else {
        stringResource(R.string.img_dialog_title_edit)
    }


    val scrollState = rememberScrollState() // ⬅️ NUEVO


    Dialog(
        onDismissRequest = { /* Bloquear clics fuera */ } // ⬅️ MODIFICACIÓN CLAVE
    ) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .heightIn(max = 600.dp) // Limitar altura para hacer espacio para el scroll
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.img_field_title_es_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = tituloEs,
                        onValueChange = { tituloEs = it },
                        label = { Text(stringResource(R.string.img_field_title_es)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    if (showOptionalTitles) {

                        TextButton(
                            onClick = {
                                showOptionalTitles = false
                            },
                            modifier = Modifier.fillMaxWidth().align(Alignment.Start)
                        ) {
                            Text(stringResource(R.string.img_optional_titles_hide))
                        }
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tituloEn,
                            onValueChange = { tituloEn = it },
                            label = { Text(stringResource(R.string.img_field_title_en)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tituloDe,
                            onValueChange = { tituloDe = it },
                            label = { Text(stringResource(R.string.img_field_title_de)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tituloFr,
                            onValueChange = { tituloFr = it },
                            label = { Text(stringResource(R.string.img_field_title_fr)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))

                    } else {
                        // Botón para desplegar las traducciones
                        Spacer(Modifier.height(8.dp)) // Espaciado para separarlo del título ES
                        TextButton(
                            onClick = { showOptionalTitles = true },
                            modifier = Modifier.fillMaxWidth().align(Alignment.Start)
                        ) {
                            Text(stringResource(R.string.img_optional_titles_show))

                        }
                        Spacer(Modifier.height(8.dp))
                    }


                }

                Spacer(Modifier.height(16.dp))

                // --- BOTONES ---
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onCancel) {
                        Text(
                            if (isInitialTagging)
                                stringResource(R.string.img_btn_discard)
                            else
                                stringResource(R.string.img_btn_cancel)
                        )

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
                        Text(
                            if (isInitialTagging && index < totalImages - 1)
                                stringResource(R.string.img_btn_next)
                            else
                                stringResource(R.string.img_btn_save)
                        )

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
        text = "$label (${stringResource(R.string.pinimg_added_count, state.images.size)})",
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
            text = stringResource(R.string.img360_add)
        ) {
            if (!isSelecting) {
                isSelecting = true
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
            text = stringResource(R.string.pinimg_swipe_hint),
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
            text = stringResource(R.string.pinimg_error_mandatory),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp)
        )
    } else if (hasUntaggedOrUntitledImages) {
        Text(
            text = stringResource(R.string.pinimg_error_missing),
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
        val tempPinImage = PinImage(uri = currentUri) // ⬅️ ¡CAMBIO CLAVE!


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