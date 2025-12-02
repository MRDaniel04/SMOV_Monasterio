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

@Composable
fun HistoriaScreen(
    isEditing: Boolean = false,
    viewModel: HistoriaViewModel = viewModel(),
    topPadding: PaddingValues = PaddingValues(0.dp) // Recibimos el padding
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    // Datos de Firebase
    val historyPeriods by viewModel.historyPeriods.collectAsState()
    val uploadingPeriodId by viewModel.uploadingPeriodId.collectAsState()

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

        // 1. IMAGEN DE FONDO (Ocupa todo, sin padding)
        Image(
            painter = painterResource(id = R.drawable.fondo_desplegable3),
            contentDescription = "Fondo de pantalla",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. CONTENIDO (Aplica el padding AQUÍ para respetar la barra roja)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding) // 👈 El contenido baja
        ) {
            // 3. Columna con Scroll y márgenes internos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp) // Margen interno visual
            ) {
                // Iteramos sobre los períodos de Firebase ordenados por 'order'
                historyPeriods.forEach { period ->
                    ExpandableHistoryCard(
                        title = period.title,
                        contentMap = period.content,
                        imageUrls = period.imageUrls,
                        isEditing = isEditing,
                        isUploading = uploadingPeriodId == period.id,
                        onAddImage = {
                            selectedPeriodId = period.id
                            imagePickerLauncher.launch("image/*")
                        },
                        onDeleteImage = { imageUrl ->
                            viewModel.deleteImage(period.id, imageUrl)
                        },
                        onUpdateContent = { newContentMap ->
                            viewModel.updatePeriodContent(period.id, newContentMap)
                        },
                        onUpdateTitle = { newTitleMap ->
                            viewModel.updatePeriodTitle(period.id, newTitleMap)
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
            com.nextapp.monasterio.ui.components.EditableText(
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
                com.nextapp.monasterio.ui.components.EditableText(
                    textMap = contentMap,
                    isEditing = isEditing,
                    onTextMapChange = onUpdateContent,
                    modifier = Modifier.padding(bottom = 16.dp),
                    readOnlyStyle = MaterialTheme.typography.bodyMedium
                )

                // Galería de imágenes (solo muestra si hay imágenes o está editando)
                if (imageUrls.isNotEmpty() || isEditing) {
                    com.nextapp.monasterio.ui.components.EditableImageGallery(
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