package com.nextapp.monasterio

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.ui.navigation.AppDrawerContent
import com.nextapp.monasterio.ui.navigation.AppNavigationHost
import com.nextapp.monasterio.ui.theme.*
import com.nextapp.monasterio.utils.FontSize
import com.nextapp.monasterio.viewModels.AuthViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        LanguageHelper.loadLocale(this)

        setContent {
            // 1. Leemos el tamaño guardado
            val appFontScale = FontSize.devolverFontScale(this)
            val currentDensity = LocalDensity.current
            val currentConfig = LocalConfiguration.current

            // 2. Calculamos la nueva Densidad
            val newDensity = Density(
                density = currentDensity.density,
                fontScale = appFontScale
            )

            // 3. Actualizamos la Configuración
            val newConfiguration = android.content.res.Configuration(currentConfig).apply {
                fontScale = appFontScale
            }

            // 4. Inyectamos AMBOS valores
            CompositionLocalProvider(
                LocalDensity provides newDensity,
                LocalConfiguration provides newConfiguration
            ) {
                Smov_monasterioTheme {
                    // 👇 PASAMOS 'this' (la actividad)
                    MonasteryAppScreen(activity = this@MainActivity)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonasteryAppScreen(activity: AppCompatActivity) { // 👈 Recibimos la actividad
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ViewModel Auth
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)

    // Estado global edición
    var isEditing by remember { mutableStateOf(false) }

    val currentTitle = remember { mutableStateOf(context.getString(R.string.title_inicio)) }

    // Detectar ruta actual
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val isPanorama = currentRoute?.startsWith(AppRoutes.PANORAMA) == true
    val isVirtualVisit = currentRoute == AppRoutes.VIRTUAL_VISIT
    val isPin360 = currentRoute?.startsWith(AppRoutes.PIN_360) == true
    val isEdicion = currentRoute == AppRoutes.EDICION_PINES

    val isImmersive = isPanorama || isVirtualVisit || isPin360 || isEdicion

    val gesturesEnabled = when(currentRoute){
        AppRoutes.VIRTUAL_VISIT -> false
        else -> true
    }

    // Actualizar título
    LaunchedEffect(navController, currentRoute) {
        val routeBase = currentRoute?.split("/")?.firstOrNull() ?: currentRoute
        currentTitle.value = when (routeBase) {
            AppRoutes.INFO -> context.getString(R.string.title_info_general)
            AppRoutes.HISTORIA -> context.getString(R.string.title_history)
            AppRoutes.GALERIA -> context.getString(R.string.title_gallery)
            AppRoutes.PERFIL -> context.getString(R.string.title_profile)
            AppRoutes.AJUSTES -> context.getString(R.string.title_settings)
            AppRoutes.OPCIONES_RESERVA,
            AppRoutes.RESERVA,
            AppRoutes.CONFIRMACION_RESERVA -> context.getString(R.string.title_appointment)
            AppRoutes.VIRTUAL_VISIT -> context.getString(R.string.title_monasterio)
            AppRoutes.MODO_NINYOS -> context.getString(R.string.child_mode_view)
            AppRoutes.VIDEO_NINYOS -> context.getString(R.string.video_view)
            AppRoutes.JUEGO_NINYOS -> context.getString(R.string.game_view)
            AppRoutes.MODO_EDICION -> context.getString(R.string.edit_mode)
            AppRoutes.PIN_ENTRADA_MONASTERIO -> context.getString(R.string.title_entrada)
            else -> context.getString(R.string.title_inicio)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                navController = navController,
                scope = scope,
                drawerState = drawerState
            )
        },
        gesturesEnabled = gesturesEnabled && !isImmersive
    ) {
        Scaffold(
            topBar = {
                if (!isImmersive) {
                    CenterAlignedTopAppBar(
                        title = { Text(currentTitle.value) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MonasteryRed,
                            titleContentColor = White,
                            navigationIconContentColor = White,
                            actionIconContentColor = White
                        ),
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }) {
                                val iconRes = if (drawerState.isOpen) R.drawable.menu_close else R.drawable.ic_menu_24
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = stringResource(id = R.string.navigation_drawer_open)
                                )
                            }
                        },
                        actions = {
                            // 👇 1. SELECTOR DE IDIOMA LIMPIO
                            MainLanguageSelector(activity)

                            // Icono Editar
                            if (currentUser != null) {
                                IconButton(onClick = {
                                    isEditing = !isEditing
                                    val message = if (isEditing) context.getString(R.string.edit_mode_activate_message) else context.getString(R.string.edit_mode_deactivate_message)
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.lapiz),
                                        contentDescription = if (isEditing) stringResource(id = R.string.edit_mode_deactivate_icon) else stringResource(id = R.string.edit_mode_activate_icon),
                                        tint = White
                                    )
                                }
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            AppNavigationHost(
                authViewModel = authViewModel,
                navController = navController,
                isEditing = isEditing,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

// --- COMPONENTES DE IDIOMA PARA LA BARRA ---

@Composable
fun MainLanguageSelector(activity: AppCompatActivity) {
    var expanded by remember { mutableStateOf(false) }

    // Obtenemos idioma actual
    val currentLanguageCode = activity.resources.configuration.locales[0].language

    // Determinamos bandera actual
    val currentFlag = when (currentLanguageCode) {
        "de" -> R.drawable.alemania
        "en" -> R.drawable.reinounido
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
        }
    }
}

@Composable
fun MainLanguageItem(
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
                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = name, style = MaterialTheme.typography.bodyMedium)
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