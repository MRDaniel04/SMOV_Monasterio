package com.nextapp.monasterio.ui.screens
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextapp.monasterio.R
import com.nextapp.monasterio.viewModels.HistoriaViewModel
import com.nextapp.monasterio.models.HistoriaPeriod
import com.nextapp.monasterio.ui.components.EditableText
import com.nextapp.monasterio.ui.components.EditableImageGallery
import com.nextapp.monasterio.ui.components.EditModeHandler

@Composable
fun HistoriaScreen(
    isEditing: Boolean = false,
    viewModel: HistoriaViewModel = viewModel(),
    topPadding: PaddingValues = PaddingValues(0.dp) // Recibimos el padding
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    val historyPeriods by viewModel.historyPeriods.collectAsState()
    val uploadingPeriodId by viewModel.uploadingPeriodId.collectAsState()

    // Estado Local
    var draftPeriods by remember { mutableStateOf<List<HistoriaPeriod>>(emptyList()) }

    // Detectamos si hay cambios pendientes de guardar (solo texto)
    // Comparamos título y contenido del draft vs original
    val hasChanges = remember(draftPeriods, historyPeriods) {
        if (draftPeriods.size != historyPeriods.size) return@remember false 
        draftPeriods.zip(historyPeriods).any { (draft, original) ->
            draft.title != original.title || draft.content != original.content
        }
    }

    // Handlers de Guardado / Descarte
    val saveChanges = {
        var changesFound = 0
        // Enviar cada periodo modificado al ViewModel
        draftPeriods.forEach { draft ->
            val original = historyPeriods.find { it.id == draft.id }
            if (original != null) {
                if (draft.title != original.title || draft.content != original.content) {
                    viewModel.updatePeriod(draft)
                    changesFound++
                }
            }
        }
    }
    
    val discardChanges = {
        // Revertir draft a lo que hay en viewModel
        draftPeriods = historyPeriods
    }

    // Manejo de "Save on Exit"
    // Devuelve true si el diálogo está abierto, para mantener la UI en modo edición
    val isSaving = EditModeHandler(
        isEditing = isEditing,
        hasChanges = hasChanges,
        onSave = saveChanges,
        onDiscard = discardChanges
    )

    // Sincronización inteligente:
    // Movida debajo de isSaving para poder usar esa variable en la condición.
    LaunchedEffect(historyPeriods, isEditing, isSaving) {
        if (!isEditing && !isSaving) {
            // Si no estamos editando NI guardando, el draft DEBE ser idéntico al original.
            // Esto evita que cambios "atascados" persistan en modo lectura.
            if (draftPeriods != historyPeriods) {
                 draftPeriods = historyPeriods
            }
        } else {
            // Si estamos editando (o guardando), hacemos merge cuidadoso si llegan datos nuevos (ej: imágenes)
            if (draftPeriods.isEmpty()) {
                draftPeriods = historyPeriods
            } else {
                // Merge: Tomamos la estructura nueva (orden, imagenes nuevas)
                // pero mantenemos título y contenido del draft si coinciden los IDs
                draftPeriods = historyPeriods.map { freshPeriod ->
                    val existingDraft = draftPeriods.find { it.id == freshPeriod.id }
                    if (existingDraft != null) {
                        freshPeriod.copy(
                            title = existingDraft.title,
                            content = existingDraft.content
                        )
                    } else {
                        freshPeriod
                    }
                }
            }
        }
    }

    // Estado para controlar qué período está seleccionando imagen
    var selectedPeriodId by remember { mutableStateOf<String?>(null) }

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            selectedPeriodId?.let { periodId ->
                viewModel.uploadImage(imageUri, context, periodId)
            }
        }
        selectedPeriodId = null
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { }
    }

    // --- ESTRUCTURA PRINCIPAL ---
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_desplegable3),
            contentDescription = "Fondo de pantalla",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Iteramos sobre DRAFT periods para ver los cambios en tiempo real
                draftPeriods.forEach { period ->
                    ExpandableHistoryCard(
                        title = period.title,
                        contentMap = period.content,
                        imageUrls = period.imageUrls,
                        // Mantenemos la UI en modo edición mientras se muestra el diálogo
                        isEditing = isEditing || isSaving,  
                        isUploading = uploadingPeriodId == period.id,
                        onAddImage = {
                            selectedPeriodId = period.id
                            imagePickerLauncher.launch("image/*")
                        },
                        onDeleteImage = { imageUrl ->
                            viewModel.deleteImage(period.id, imageUrl)
                        },
                        onUpdateContent = { newContentMap ->
                            // Actualizamos solo el draft local
                            draftPeriods = draftPeriods.map { 
                                if (it.id == period.id) it.copy(content = newContentMap) else it
                            }
                        },
                        onUpdateTitle = { newTitleMap ->
                             // Actualizamos solo el draft local
                            draftPeriods = draftPeriods.map { 
                                if (it.id == period.id) it.copy(title = newTitleMap) else it
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ExpandableHistoryCard(
    title: Map<String, String>,
    contentMap: Map<String, String>,
    imageUrls: List<String> = emptyList(),
    isEditing: Boolean = false,
    isUploading: Boolean = false,
    onAddImage: () -> Unit = {},
    onDeleteImage: (String) -> Unit = {},
    onUpdateContent: (Map<String, String>) -> Unit = {},
    onUpdateTitle: (Map<String, String>) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Título editable
            EditableText(
                textMap = title,
                isEditing = isEditing,
                onTextMapChange = onUpdateTitle,
                modifier = Modifier.weight(1f),
                readOnlyStyle = MaterialTheme.typography.titleLarge
            )

            Icon(
                painter = painterResource(id = R.drawable.arrow_down),
                contentDescription = "Expandir/Colapsar",
                modifier = Modifier.rotate(rotationAngle)
            )
        }

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {

                // Usamos el nuevo EditableText con soporte para mapa de idiomas
                EditableText(
                    textMap = contentMap,
                    isEditing = isEditing,
                    onTextMapChange = onUpdateContent,
                    modifier = Modifier.padding(bottom = 16.dp),
                    readOnlyStyle = MaterialTheme.typography.bodyMedium
                )

                // Galería de imágenes (solo muestra si hay imágenes o está editando)
                if (imageUrls.isNotEmpty() || isEditing) {
                    EditableImageGallery(
                        imageUrls = imageUrls,
                        isEditing = isEditing,
                        isUploading = isUploading,
                        onAddImage = onAddImage,
                        onDeleteImage = onDeleteImage,
                        title = stringResource(id = R.string.image_gallery_title)
                    )
                }
            }
        }
    }
}