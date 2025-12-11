package com.nextapp.monasterio.ui.screens.pinEdition

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nextapp.monasterio.R
import com.nextapp.monasterio.repository.ImagenRepository
import com.nextapp.monasterio.services.CloudinaryService
import com.nextapp.monasterio.ui.screens.pinEdition.components.AppStatus
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange
import kotlinx.coroutines.launch
import com.nextapp.monasterio.ui.components.MonasteryButton
import androidx.compose.ui.res.stringResource

// COLORES
val EditModePurple = Color(0xFF9C27B0)

@Composable
fun EdicionFondoInicio(
    navController: NavController,
    topPadding: PaddingValues = PaddingValues(0.dp) // Recibimos el padding
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val repo = ImagenRepository()
    val coroutineScope = rememberCoroutineScope()

    // Estados
    var imagenFondoInicio by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentSavedImageUrl by remember { mutableStateOf<String?>(null) }
    val isUploading by AppStatus.isUploading.collectAsState()
    var showConfirmationToast by remember { mutableStateOf(false) }

    // Detectar orientación
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        val data = repo.getImagenFondoInicio()
        imagenFondoInicio = data?.url
        currentSavedImageUrl = data?.url
    }

    LaunchedEffect(showConfirmationToast) {
        if (showConfirmationToast) {
            Toast.makeText(context, "Imagen de fondo actualizada", Toast.LENGTH_LONG).show()
            showConfirmationToast = false
        }
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Determinar qué fondo mostrar (El nuevo seleccionado o el actual)
    val backgroundUriToDisplay = selectedImageUri
        ?: currentSavedImageUrl?.let { Uri.parse(it) }
        ?: imagenFondoInicio?.let { Uri.parse(it) }

    // --- ESTRUCTURA PRINCIPAL ---
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. FONDO (Ocupa todo)
        if (backgroundUriToDisplay != null) {
            AsyncImage(
                model = backgroundUriToDisplay,
                contentDescription = "Fondo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.monastery_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. CONTENIDO (Respeta barra superior)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding)
        ) {
            // Contenedor interno con margen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isLandscape) {
                    // --- DISEÑO HORIZONTAL ---
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // IZQUIERDA: Logo
                        Box(
                            modifier = Modifier.weight(0.4f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.huelgas_inicio),
                                contentDescription = "Escudo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // DERECHA: Botones (Morado o Previsualización)
                        Box(
                            modifier = Modifier.weight(0.6f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri == null) {
                                // MODO SELECCIÓN: Solo botón morado
                                EditButton(
                                    text = stringResource(id = R.string.preview_change_background),
                                    iconRes = R.drawable.lapiz,
                                    onClick = { imagePickerLauncher.launch("image/*") }
                                )
                            } else {
                                // MODO PREVISUALIZACIÓN: Botones Fake estilo Home
                                PreviewButtonsGridLandscape()
                            }
                        }
                    }
                } else {
                    // --- DISEÑO VERTICAL ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.huelgas_inicio),
                            contentDescription = "Escudo",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 48.dp)
                        )

                        if (selectedImageUri == null) {
                            // MODO SELECCIÓN: Botón morado grande
                            Spacer(Modifier.height(40.dp))
                            EditButton(
                                text = stringResource(id = R.string.preview_change_background),
                                iconRes = R.drawable.lapiz,
                                onClick = { imagePickerLauncher.launch("image/*") }
                            )
                        } else {
                            // MODO PREVISUALIZACIÓN: Botones Fake estilo Home
                            PreviewButtonsListPortrait()
                        }

                        // Espacio extra al final para la barra de confirmación
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }

        // 3. BARRA DE CONFIRMACIÓN (Solo si hay imagen seleccionada)
        AnimatedVisibility(
            visible = selectedImageUri != null && !isUploading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ActionControlBar(
                onConfirm = {
                    coroutineScope.launch {
                        AppStatus.startUpload()
                        val result = CloudinaryService.uploadImage(selectedImageUri!!, context)
                        result.onSuccess { url ->
                            repo.updateImagenFondoInicio(url)
                            currentSavedImageUrl = url
                            selectedImageUri = null
                            showConfirmationToast = true
                        }
                        result.onFailure {
                            Toast.makeText(context, context.getString(R.string.preview_error_uploading), Toast.LENGTH_LONG).show()
                        }
                        AppStatus.finishUpload()
                    }
                },
                onCancel = {
                    selectedImageUri = null
                }
            )
        }
    }
}

// -----------------------------------------------------------
// COMPONENTES AUXILIARES
// -----------------------------------------------------------

@Composable
fun EditButton(text: String, iconRes: Int, onClick: () -> Unit) {
    MonasteryButton(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = EditModePurple),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().height(80.dp),
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = text.uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(32.dp))
        }
    }
}

// Botones "Fake" para previsualizar cómo queda el fondo (Portrait)
@Composable
fun PreviewButtonsListPortrait() {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        MockHomeButton(stringResource(id = R.string.preview_virtual_visit), R.drawable.ic_map_24, MonasteryOrange)
        MockHomeButton(stringResource(id = R.string.preview_kids_mode), R.drawable.outline_account_child_invert_24, Color(0xFF6EB017))
        MockHomeButton(stringResource(id = R.string.preview_book_appointment), R.drawable.ic_time_24, MonasteryBlue)
    }
}

// Botones "Fake" para previsualizar (Landscape)
@Composable
fun PreviewButtonsGridLandscape() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) { MockHomeGridButton(stringResource(id = R.string.preview_virtual_visit), R.drawable.ic_map_24, MonasteryOrange) }
            Box(Modifier.weight(1f)) { MockHomeGridButton(stringResource(id = R.string.preview_kids_mode), R.drawable.outline_account_child_invert_24, Color(0xFF6EB017)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) { MockHomeGridButton(stringResource(id = R.string.preview_book_appointment), R.drawable.ic_time_24, MonasteryBlue) }
            Spacer(Modifier.weight(1f)) // Hueco vacío
        }
    }
}

@Composable
fun MockHomeButton(text: String, iconRes: Int, color: Color) {
    Button(
        onClick = {}, // No hace nada, solo visual
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().height(80.dp),
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.weight(1f))
            Text(text = text.uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(32.dp))
        }
    }
}

@Composable
fun MockHomeGridButton(text: String, iconRes: Int, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.White)
            Text(text = text.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ActionControlBar(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        ) { Text(stringResource(id = R.string.preview_cancel).uppercase(), color = Color.White) }

        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.8f)),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        ) { Text(stringResource(id = R.string.preview_confirm).uppercase(), color = Color.White) }
    }
}