package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextapp.monasterio.R
import com.nextapp.monasterio.viewModels.HistoriaViewModel

// Clase de datos auxiliar para mapear los recursos de strings con un ID
private data class StaticHistoryPeriod(
    val id: String,
    val titleRes: Int,
    val contentRes: Int
)

@Composable
fun HistoriaScreen(
    isEditing: Boolean = false,
    viewModel: HistoriaViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    // Datos dinámicos (Imágenes) del ViewModel
    val historyPeriodsFromVm by viewModel.historyPeriods.collectAsState()
    val uploadingPeriodId by viewModel.uploadingPeriodId.collectAsState()

    // Datos estáticos (Textos traducibles) definidos en strings.xml
    // Usamos los IDs como clave para vincular con las imágenes del ViewModel
    val definedPeriods = remember {
        listOf(
            StaticHistoryPeriod("1328", R.string.history_1328, R.string.history_1328_body),
            StaticHistoryPeriod("1579", R.string.history_1579, R.string.history_579_body),
            StaticHistoryPeriod("1869", R.string.history_1869, R.string.history_1869_body),
            StaticHistoryPeriod("XX", R.string.history_XX, R.string.history_XX_body),
            StaticHistoryPeriod("2007", R.string.history_2007, R.string.history_2007_body)
        )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Iteramos sobre la lista estática para garantizar el orden y los textos correctos
        definedPeriods.forEach { staticPeriod ->

            // Buscamos si hay imágenes en el ViewModel asociadas a este ID
            val dynamicData = historyPeriodsFromVm.find { it.id == staticPeriod.id }
            val currentImages = dynamicData?.imageUrls ?: emptyList()

            ExpandableHistoryCard(
                // Usamos stringResource para que soporte el cambio de idioma
                title = stringResource(staticPeriod.titleRes),
                content = stringResource(staticPeriod.contentRes),
                imageUrls = currentImages,
                isEditing = isEditing,
                isUploading = uploadingPeriodId == staticPeriod.id,
                onAddImage = {
                    selectedPeriodId = staticPeriod.id
                    imagePickerLauncher.launch("image/*")
                },
                onDeleteImage = { imageUrl ->
                    viewModel.deleteImage(staticPeriod.id, imageUrl)
                }
            )
        }
    }
}

@Composable
fun ExpandableHistoryCard(
    title: String,
    content: String,
    imageUrls: List<String> = emptyList(),
    isEditing: Boolean = false,
    isUploading: Boolean = false,
    onAddImage: () -> Unit = {},
    onDeleteImage: (String) -> Unit = {}
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
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
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Galería de imágenes (solo muestra si hay imágenes o está editando)
                if (imageUrls.isNotEmpty() || isEditing) {
                    // Asegúrate de que este componente exista en tu proyecto o sustitúyelo por tu lógica de galería
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