package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import com.nextapp.monasterio.ui.components.ZoomableImageDialog
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
    isDiscarding: Boolean = false,
    onKeepEditing: () -> Unit = {},
    viewModel: InfoViewModel = viewModel(),
    topPadding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val view = LocalView.current

    val infoData by viewModel.infoState.collectAsState()

    // --- ESTADO LOCAL (DRAFT) ---
    var draftInfo by remember { mutableStateOf<com.nextapp.monasterio.models.InfoModel?>(null) }
    
    if (draftInfo == null && infoData != null) {
        draftInfo = infoData
    }

    val hasChanges = remember(draftInfo, infoData) {
        draftInfo != null && draftInfo != infoData
    }

    val saveChanges = {
        draftInfo?.let {
             viewModel.saveFullInfo(it) 
        }
        Unit
    }

    val discardChanges = {
         draftInfo = infoData
    }

    val isSaving = com.nextapp.monasterio.ui.components.EditModeHandler(
        isEditing = isEditing,
        hasChanges = hasChanges,
        isDiscarding = isDiscarding,
        onSave = saveChanges,
        onDiscard = discardChanges,
        onKeepEditing = onKeepEditing
    )

    // Sincronización al salir de edición
    LaunchedEffect(infoData, isEditing, isSaving) {
        if (!isEditing && !isSaving) {
             if (draftInfo != infoData) {
                 draftInfo = infoData
             }
        } else {
            // Si estamos editando y llega una actualización externa actualizamos el draft solo si no hemos tocado nada o si es la inicialización
             if (draftInfo == null) {
                 draftInfo = infoData
             }
        }
    }

    val (pin, setPin) = remember { mutableStateOf<PinData?>(null) }
    
    LaunchedEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
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
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }


    val pagerState = rememberPagerState(pageCount = { imagenes.size })

    if (imagenes.size > 1 && selectedImageIndex == null) {
        // Usamos 'Unit' para que el LaunchedEffect no se reinicie en cada cambio de página
        LaunchedEffect(Unit) {
            while (true) {
                kotlinx.coroutines.delay(5000L) // Espera 5 segundos

                // Verificamos que el usuario no esté tocando el carrusel en este momento
                if (!pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.currentPage + 1) % imagenes.size
                    pagerState.animateScrollToPage(
                        page = nextPage,
                        // Animación suave para evitar saltos bruscos
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                    )
                }
            }
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

    // Objeto a mostrar: draft si existe, sino el original (fallback seguro)
    val currentInfo = draftInfo ?: infoData

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
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                // CLAVE 1: Precarga la siguiente imagen para suavidad total
                                beyondViewportPageCount = 1,
                                // CLAVE 2: Asegura que el espacio entre páginas sea cero
                                pageSpacing = 0.dp
                            ) { page ->
                                val imagen = imagenes[page]
                                // CLAVE 3: El contenedor de la imagen DEBE llenar todo el espacio del Pager
                                Box(modifier = Modifier.fillMaxSize()) {
                                    val alignment = remember {
                                        BiasAlignment(horizontalBias = 0f, verticalBias = imagen.foco)
                                    }

                                    AsyncImage(
                                        model = imagen.url,
                                        contentDescription = imagen.titulo,
                                        contentScale = ContentScale.Crop,
                                        alignment = alignment,
                                        modifier = Modifier
                                            .fillMaxSize() // Ocupa todo el ancho de la página del pager
                                            .clickable {
                                                selectedImageIndex = page
                                            }
                                    )
                                }
                            }

                            // Indicadores (Dots)
                            Row(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 6.dp)
                            ) {
                                repeat(imagenes.size) { index ->
                                    val selected = pagerState.currentPage == index
                                    Box(
                                        Modifier
                                            .padding(3.dp)
                                            .size(if (selected) 8.dp else 6.dp)
                                            .background(
                                                Color.White.copy(alpha = if (selected) 1f else 0.6f),
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }
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
                            textMap = currentInfo.mainContent,
                            isEditing = isEditing || isSaving, // Mantenemos UI editable durante diálogo
                            onTextMapChange = { 
                                draftInfo = draftInfo?.copy(mainContent = it) 
                            },
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
                            text = currentInfo.location,
                            isEditing = isEditing || isSaving,
                            onUpdate = { 
                                draftInfo = draftInfo?.copy(location = it)
                            },
                            onClick = {
                                if (currentInfo.location.isNotEmpty()) context.abrirUbicacion(currentInfo.location)
                            }
                        )
                        // Email
                        EditableStringInfoItem(
                            label = tCorreo,
                            text = currentInfo.email,
                            isEditing = isEditing || isSaving,
                            onUpdate = { 
                                draftInfo = draftInfo?.copy(email = it)
                            },
                            onClick = {
                                 if (currentInfo.email.isNotEmpty()) context.crearCorreo( currentInfo.email,"", "", "", false)
                            }
                        )
                        // Telefono
                        EditableStringInfoItem(
                            label = tTelefono,
                            text = currentInfo.phone,
                            isEditing = isEditing || isSaving,
                            onUpdate = { 
                                draftInfo = draftInfo?.copy(phone = it)
                            },
                            onClick = {
                                if (currentInfo.phone.isNotEmpty()) context.llamarTelefono(currentInfo.phone)
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
                            textMap = currentInfo.hours,
                            isEditing = isEditing || isSaving,
                            onTextMapChange = { 
                                draftInfo = draftInfo?.copy(hours = it)
                            },
                            readOnlyStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                        )
                    }
                }
            }
        }

        // ZoomableImage
        if (selectedImageIndex != null) {
            val config = LocalConfiguration.current
            val locale = config.locales[0].language
            
            ZoomableImageDialog(
                imagenes = imagenes,
                initialIndex = selectedImageIndex!!,
                languageCode = locale,
                onDismiss = {
                    selectedImageIndex = null
                }
            )
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
