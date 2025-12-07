package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.repository.PinRepository
import com.nextapp.monasterio.ui.components.EditableText
import com.nextapp.monasterio.ui.theme.MonasteryRed // Usado por si acaso
import com.nextapp.monasterio.ui.theme.White
import com.nextapp.monasterio.utils.abrirUbicacion
import com.nextapp.monasterio.utils.crearCorreo
import com.nextapp.monasterio.utils.llamarTelefono
import com.nextapp.monasterio.viewModels.InfoViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun InfoScreen(
    isEditing: Boolean = false,
    viewModel: InfoViewModel = viewModel(),
    topPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val view = LocalView.current

    // 1. Fetch data from ViewModel
    val infoData by viewModel.infoState.collectAsState()

    // 2. Fetch Pin Data for Carousel (Pin ID: "pin_entrada")
    // We maintain simple internal state for this, consistent with EntradaMonasterioFirestoreScreen pattern
    val (pin, setPin) = remember { mutableStateOf<PinData?>(null) }
    
    LaunchedEffect(Unit) {
        // Ensure status bar transparency logic from EntradaMonasterioScreen
        val window = (view.context as? Activity)?.window
        window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }

        // Load the pin
        try {
            val loadedPin = PinRepository.getPinById("pin_entrada")
            setPin(loadedPin)
        } catch (e: Exception) {
            // Handle error silently or show toast? For now, we just rely on null check
        }
    }

    // Permitir rotación
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { }
    }

    // --- CAROUSEL LOGIC ---
    val imagenes = pin?.imagenesDetalladas?.ifEmpty { emptyList() } ?: emptyList()
    var currentIndex by remember { mutableIntStateOf(0) }

    if (imagenes.size > 1) {
        LaunchedEffect(currentIndex, imagenes.size) {
            delay(4000L)
            currentIndex = (currentIndex + 1) % imagenes.size
        }
    }

    // --- TEXTOS LOCALIZADOS ---
    val tituloPagina = "Monasterio de las Huelgas Reales"
    val tDatosContacto = stringResource(R.string.contact_data)
    val tUbicacion = stringResource(R.string.location_data)
    val tCorreo = stringResource(R.string.email_data)
    val tTelefono = stringResource(R.string.phone_data)
    val tHorarios = stringResource(R.string.visit_schedule)
    val tLunesViernes = stringResource(R.string.monday_to_friday)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo (Historia Style)
        Image(
            painter = painterResource(id = R.drawable.fondo_desplegable3),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(topPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), // Sin background blanco
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 0.dp, bottom = 40.dp)
            ) {
                // TÍTULO SUPERIOR
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp), // Sin background blanco
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tituloPagina,
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // CARRUSEL DE IMAGENES
                item {
                    if (imagenes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, MonasteryRed, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Crossfade(
                                targetState = currentIndex,
                                label = "imageFade"
                            ) { page ->
                                val imagen = imagenes[page]
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = imagen.url,
                                        contentDescription = imagen.titulo,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            if (imagenes.size > 1) {
                                Row(
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 6.dp)
                                ) {
                                    repeat(imagenes.size) { index ->
                                        val selected = currentIndex == index
                                        Box(
                                            Modifier
                                                .padding(3.dp)
                                                .size(if (selected) 9.dp else 7.dp)
                                                .background(
                                                    if (selected) Color.White
                                                    else Color.White.copy(alpha = 0.4f),
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Fallback visual
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, MonasteryRed, RoundedCornerShape(12.dp))
                                .background(Color.Gray.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                           if (pin == null) {
                               CircularProgressIndicator()
                           } else {
                               Image(
                                   painter = painterResource(id = R.drawable.fondo_desplegable),
                                   contentDescription = "Monasterio",
                                   contentScale = ContentScale.Crop,
                                   modifier = Modifier.fillMaxSize()
                               )
                           }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
    
                // DATOS EDITABLES
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        // Descripcion
                        EditableText(
                            textMap = infoData.mainContent,
                            isEditing = isEditing,
                            onTextMapChange = { viewModel.updateMainContent(it) },
                            readOnlyStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Black.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.padding(bottom = 30.dp)
                        )
                        Text(
                            text = tDatosContacto,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                        // Ubicacion
                        EditableStringInfoItem(
                            label = tUbicacion,
                            text = infoData.location,
                            isEditing = isEditing,
                            onUpdate = { viewModel.updateLocation(it) },
                            onClick = {
                                if (infoData.location.isNotEmpty()) context.abrirUbicacion(infoData.location)
                            }
                        )
                        // Email
                        EditableStringInfoItem(
                            label = tCorreo,
                            text = infoData.email,
                            isEditing = isEditing,
                            onUpdate = { viewModel.updateEmail(it) },
                            onClick = {
                                 if (infoData.email.isNotEmpty()) context.crearCorreo( infoData.email,"", "", "", false)
                            }
                        )
                        // Telefono
                        EditableStringInfoItem(
                            label = tTelefono,
                            text = infoData.phone,
                            isEditing = isEditing,
                            onUpdate = { viewModel.updatePhone(it) },
                            onClick = {
                                if (infoData.phone.isNotEmpty()) context.llamarTelefono(infoData.phone)
                            }
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        // Horarios
                        Text(
                            text = tHorarios,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                        Text(text = tLunesViernes, fontWeight = FontWeight.SemiBold)
                        EditableText(
                            textMap = infoData.hours,
                            isEditing = isEditing,
                            onTextMapChange = { viewModel.updateHours(it) },
                            readOnlyStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente auxiliar para Strings simples (Ubicación, Email, Teléfono)
 */
@Composable
private fun EditableStringInfoItem(
    label: String,
    text: String,
    isEditing: Boolean,
    onUpdate: (String) -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)

        if (isEditing) {
            // Modo Edición: TextField simple
            OutlinedTextField(
                value = text,
                onValueChange = onUpdate,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = MonasteryRed),
                singleLine = true,
                 colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonasteryRed,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = MonasteryRed,
                    unfocusedTextColor = MonasteryRed
                )
            )
        } else {
            // Modo Lectura: Text Clickable
            Text(
                text = text,
                color = MonasteryRed,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(vertical = 4.dp)
            )
        }
    }
}
