package com.nextapp.monasterio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.ImagenData
import com.nextapp.monasterio.ui.GalleryType
import com.nextapp.monasterio.viewModels.GaleriaViewModel
import java.util.Locale

@Composable
fun GaleriaScreen(
    navController: NavController,
    viewModel: GaleriaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentLanguage = Locale.getDefault().language

    // State for full screen image
    var selectedImage by remember { mutableStateOf<ImagenData?>(null) }

    if (selectedImage != null) {
        Dialog(
            onDismissRequest = { selectedImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { selectedImage = null }
            ) {
                AsyncImage(
                    model = selectedImage?.url,
                    contentDescription = selectedImage?.titulo,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Close button
                IconButton(
                    onClick = { selectedImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_24),
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // Title overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = when(currentLanguage) {
                            "es" -> selectedImage?.titulo ?: ""
                            "en" -> if (selectedImage?.tituloIngles?.isNotEmpty() == true) selectedImage?.tituloIngles!! else selectedImage?.titulo ?: ""
                            "de" -> if (selectedImage?.tituloAleman?.isNotEmpty() == true) selectedImage?.tituloAleman!! else selectedImage?.titulo ?: ""
                            else -> selectedImage?.titulo ?: ""
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Category Selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(GalleryType.entries) { type ->
                val isSelected = type == uiState.selectedType
                val label = stringResource(id = type.resourceId)

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onTypeSelected(type) },
                    label = { Text(label) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_done_24),
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Image Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredImages) { image ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable {
                                selectedImage = image
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = image.url,
                                contentDescription = image.titulo,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = when(currentLanguage) {
                                        "es" -> image.titulo
                                        "en" -> if (image.tituloIngles.isNotEmpty()) image.tituloIngles else image.titulo
                                        "de" -> if (image.tituloAleman.isNotEmpty()) image.tituloAleman else image.titulo
                                        else -> image.titulo
                                    },
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}