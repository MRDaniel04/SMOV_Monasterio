package com.nextapp.monasterio.ui.screens.pinEdition.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nextapp.monasterio.MainLanguageItem
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.ImagenData
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.repository.PinRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinDetailsPanel(
    modifier: Modifier = Modifier,
    selectedPin: PinData,
    imagenesDetalladas: List<ImagenData>,
    pinTapScreenPosition: Offset?,
    onClosePanel: () -> Unit,
    onStartMove: (PinData, Offset) -> Unit,
    onEdit: (PinData) -> Unit,
    panelHeightFraction: Float,
    onPinDeleted: (String) -> Unit,
    panelAlignment: String
) {
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context.getActivity() as AppCompatActivity
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember(selectedPin.id) { mutableStateOf(true) }
    val selectedLang = LocalConfiguration.current.locales[0].language

    val titulo = when {
        selectedLang.startsWith("es") -> selectedPin.ubicacion_es
        selectedLang.startsWith("en") -> selectedPin.ubicacion_en
        selectedLang.startsWith("fr") -> selectedPin.ubicacion_fr
        selectedLang.startsWith("de") -> selectedPin.ubicacion_de
        else -> selectedPin.ubicacion_es
    }.orEmpty().ifBlank { stringResource(R.string.pin_detail_default_title) }

    val area = when {
        selectedLang.startsWith("es") -> selectedPin.area_es
        selectedLang.startsWith("en") -> selectedPin.area_en
        selectedLang.startsWith("fr") -> selectedPin.area_fr
        selectedLang.startsWith("de") -> selectedPin.area_de
        else -> selectedPin.area_es
    }.orEmpty()

    val descripcion = when {
        selectedLang.startsWith("es") -> selectedPin.descripcion_es
        selectedLang.startsWith("en") -> selectedPin.descripcion_en
        selectedLang.startsWith("fr") -> selectedPin.descripcion_fr
        selectedLang.startsWith("de") -> selectedPin.descripcion_de
        else -> selectedPin.descripcion_es
    }.orEmpty().ifBlank { stringResource(R.string.pin_description_not_available) }


    LaunchedEffect(selectedPin.id) {
        delay(500)
        isLoading = false
    }

    Log.d("PinDetailsPanel", "Pin ID: ${selectedPin.id}, Area_es recibida: '${selectedPin.area_es}'")

    val hasImages = imagenesDetalladas.isNotEmpty()

    val panelShape = if (panelAlignment == "RIGHT") {
        RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp) // Esquina redondeada a la izquierda
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp) // Esquina redondeada arriba (Original)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(panelHeightFraction) // Puedes ignorar esta línea si usas el modifier en EdicionPines
            .background(
                Color.White,
                shape = panelShape // ⬅️ USAR FORMA DINÁMICA
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { Log.d("EdicionPines", "Toque en el panel consumido.") }
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón 1: Mover Pin
                    IconButton(onClick = {
                        val initialScreenPos = pinTapScreenPosition
                        if (initialScreenPos == null) {
                            Toast.makeText(context, context.getString(R.string.error_no_pin_position), Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        onStartMove(selectedPin, initialScreenPos)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_move),
                            contentDescription = stringResource(R.string.move_pin),
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Botón 2: Editar Pin
                    IconButton(onClick = {
                        onEdit(selectedPin)
                        Toast.makeText(context, context.getString(R.string.edit_pin_toast), Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.lapiz),
                            contentDescription = stringResource(R.string.edit_pin),
                            modifier = Modifier.size(38.dp)   // ← AQUÍ defines 48x48
                        )
                    }

                    // Botón 3: Borrar Pin
                    IconButton(onClick = {
                        isDeleteDialogOpen = true
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.borrar),
                            contentDescription = stringResource(R.string.delete_pin_cd),
                            modifier = Modifier.size(38.dp)

                        )
                    }
                    MainLanguageSelector(activity)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {



                    // --- BOTÓN CERRAR PANEL ---
                    IconButton(onClick = onClosePanel) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_24),
                            contentDescription = stringResource(R.string.close_panel),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(24.dp))

            // ⭐ CORRECCIÓN 1: Manejo seguro de 'area_es' (null safety)
            val areaText = if (area.isNotBlank()) {
                " ($area)"
            } else {
                ""
            }

            Text(
                text = titulo + areaText,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )


            Spacer(modifier = Modifier.height(24.dp))

            when {

                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(stringResource(R.string.loading_images), color = Color.Gray)
                    }
                }
                // 2. NO HAY IMÁGENES (Solo si isLoading es false Y la lista está vacía)
                !hasImages -> {
                    Text(stringResource(R.string.no_images_available),
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                // 3. IMÁGENES DISPONIBLES (isLoading es false y la lista NO está vacía)
                else -> {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {

                        items(imagenesDetalladas) { imagen ->

                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {

                                AsyncImage(
                                    model = imagen.url,
                                    contentDescription = imagen.titulo,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = descripcion,
                    style = TextStyle(fontSize = 12.sp),
                    color = Color.DarkGray
                )
            }


            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // --- DIÁLOGO DE CONFIRMACIÓN DE BORRADO PERMANENTE ---
    if (isDeleteDialogOpen) {
        // ⭐ CORRECCIÓN 3: Manejo seguro de 'ubicacion_es' en el diálogo
        val pinTitleForDialog =
            selectedPin.ubicacion_es.orEmpty().ifBlank { stringResource(R.string.untitled_pin) }


        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            title = { Text(text = stringResource(R.string.confirm_delete_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.confirm_delete_message,
                        pinTitleForDialog
                    )
                )
            },


            confirmButton = {
                Button(
                    onClick = {
                        isDeleteDialogOpen = false
                        val pinId = selectedPin.id
                        val ctx = context

                        coroutineScope.launch {
                            val ok = PinRepository.deletePinAndImages(pinId)

                            if (ok) {
                                Toast.makeText(ctx, ctx.getString(R.string.pin_deleted_success), Toast.LENGTH_SHORT).show()
                                onPinDeleted(pinId)
                                onClosePanel()
                            } else {
                                Toast.makeText(ctx, ctx.getString(R.string.pin_deleted_error), Toast.LENGTH_SHORT).show()

                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text(stringResource(R.string.delete_pin))
                }
            },
            dismissButton = {
                Button(onClick = { isDeleteDialogOpen = false }) { Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun MainLanguageSelector(activity: AppCompatActivity) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    // Obtenemos idioma actual
    val currentLanguageCode = configuration.locales[0].language
    // Determinamos bandera actual
    val currentFlag = when (currentLanguageCode) {
        "de" -> R.drawable.alemania
        "en" -> R.drawable.reinounido
        "fr" -> R.drawable.francia
        else -> R.drawable.espanya

    }

    Box {
        // Botón de la bandera
        IconButton(onClick = { expanded = true }) {
            Image(
                painter = painterResource(id = currentFlag),
                contentDescription = "Cambiar idioma",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            )
        }

        // Menú desplegable
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(150.dp)
        ) {
            MainLanguageItem(activity, "es", stringResource(R.string.lang_es), R.drawable.espanya) { expanded = false }
            MainLanguageItem(activity, "en", stringResource(R.string.lang_en), R.drawable.reinounido) { expanded = false }
            MainLanguageItem(activity, "de", stringResource(R.string.lang_de), R.drawable.alemania) { expanded = false }
            MainLanguageItem(activity, "fr", stringResource(R.string.lang_fr), R.drawable.francia) { expanded = false }
        }
    }
}

fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

