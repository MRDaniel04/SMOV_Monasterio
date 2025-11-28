package com.nextapp.monasterio.ui.screens

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.LanguageHelper
import com.nextapp.monasterio.R
import com.nextapp.monasterio.utils.FontSize
import com.nextapp.monasterio.viewModels.AjustesViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(viewModel: AjustesViewModel) {
    val activity = LocalActivity.current as? AppCompatActivity ?: return
    val botonesVisibles by viewModel.botonesVisibles.collectAsState()

    // Configuración actual para detectar idioma y fuente inicial
    val configuration = LocalConfiguration.current
    val currentLocale = configuration.locales[0]
    val currentFontScale = configuration.fontScale

    DisposableEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ==========================================
        // 1. SELECTOR DE IDIOMA
        // ==========================================
        SettingsSectionCard(title = stringResource(R.string.language_select)) {
            LanguageSelector(activity, currentLocale)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 2. TAMAÑO DE FUENTE (RADIO BUTTONS)
        // ==========================================
        SettingsSectionCard(title = stringResource(R.string.font_size_select)) {
            // 👇 Usamos el nuevo componente de Radio Buttons
            FontSizeSelector(activity, currentFontScale)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 3. BOTONES DE NAVEGACIÓN (SWITCH)
        // ==========================================
        SettingsSectionCard(title = stringResource(R.string.navegation_button)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (botonesVisibles) stringResource(R.string.visible) else stringResource(R.string.hidden),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = botonesVisibles,
                    onCheckedChange = { viewModel.setBotonesVisibles(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

// --- COMPONENTE TARJETA ---
@Composable
fun SettingsSectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

// --- COMPONENTE SELECTOR DE IDIOMA ---
@Composable
fun LanguageSelector(activity: AppCompatActivity, currentLocale: Locale) {
    var expanded by remember { mutableStateOf(false) }
    val languageCode = currentLocale.language

    val currentFlag = when (languageCode) {
        "de" -> R.drawable.alemania
        "en" -> R.drawable.reinounido
        "fr" -> R.drawable.francia
        else -> R.drawable.espanya
    }

    val currentName = when (languageCode) {
        "de" -> stringResource(R.string.lang_de)
        "en" -> stringResource(R.string.lang_en)
        "fr" -> stringResource(R.string.lang_fr)
        else -> stringResource(R.string.lang_es)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = currentFlag),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = currentName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                Icon(
                    painter = painterResource(id = R.drawable.arrow_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.rotate(if (expanded) 180f else 0f)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            LanguageItem(activity, "es", stringResource(R.string.lang_es), R.drawable.espanya) { expanded = false }
            LanguageItem(activity, "en", stringResource(R.string.lang_en), R.drawable.reinounido) { expanded = false }
            LanguageItem(activity, "de", stringResource(R.string.lang_de), R.drawable.alemania) { expanded = false }
            LanguageItem(activity, "fr", stringResource(R.string.lang_fr), R.drawable.francia) { expanded = false }
        }
    }
}

@Composable
fun LanguageItem(
    activity: AppCompatActivity,
    code: String,
    name: String,
    flagRes: Int,
    onDismiss: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = flagRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = name)
            }
        },
        onClick = {
            onDismiss()
            val currentLanguage = activity.resources.configuration.locales[0].language
            if (currentLanguage != code) {
                LanguageHelper.saveLocale(activity, code)
                activity.recreate()
            }
        }
    )
}

// --- NUEVO SELECTOR VERTICAL (ROBUSTO Y CON DRAWABLES) ---
@Composable
fun FontSizeSelector(activity: AppCompatActivity, currentScale: Float) {

    // 1. Definimos los tamaños exactos
    val sizeSmall = 1.2f
    val sizeMedium = 1.5f
    val sizeLarge = 1.7f

    // 2. Estado de la opción seleccionada.
    // Usamos 'rememberSaveable' para intentar mantener el estado si la recreación es parcial,
    // pero inicializamos basándonos en 'currentScale'.
    var selectedOption by remember(currentScale) {
        mutableIntStateOf(
            when {
                currentScale >= 1.65f -> 2 // Grande
                currentScale >= 1.45f -> 1 // Mediana
                else -> 0                  // Pequeña
            }
        )
    }

    fun updateFontSize(newScale: Float, newOptionIndex: Int) {
        // 1. Actualizamos la UI INMEDIATAMENTE para que el check cambie
        selectedOption = newOptionIndex

        // 2. Guardamos y recreamos
        // (Añadimos un pequeño delay o comprobación para no bloquear la UI visual)
        if (kotlin.math.abs(newScale - currentScale) > 0.05f) {
            FontSize.guardarFontScale(activity, newScale)

            // Usamos un Handler para dar tiempo a que se pinte el check antes de reiniciar
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                activity.recreate()
            }, 150) // 150ms de espera visual
        }
    }

    // 3. Diseño Vertical
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Opción 1: PEQUEÑA
        FontSizeOptionCard(
            text = stringResource(R.string.font_small),
            isSelected = (selectedOption == 0),
            onClick = { updateFontSize(sizeSmall, 0) }
        )

        // Opción 2: MEDIANA
        FontSizeOptionCard(
            text = stringResource(R.string.font_medium),
            isSelected = (selectedOption == 1),
            onClick = { updateFontSize(sizeMedium, 1) }
        )

        // Opción 3: GRANDE
        FontSizeOptionCard(
            text = stringResource(R.string.font_large),
            isSelected = (selectedOption == 2),
            onClick = { updateFontSize(sizeLarge, 2) }
        )
    }
}

@Composable
fun FontSizeOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Colores dinámicos: Si está seleccionado, fondo coloreado y borde primario
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    // Grosor del borde: Más grueso si está seleccionado
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Texto de la opción
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )

            // Icono o Indicador
            if (isSelected) {
                // 👇 AQUÍ DEBES ASEGURARTE DE TENER EL DRAWABLE 'check'
                Icon(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Círculo vacío para simular radio button desmarcado
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Transparent, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
        }
    }
}