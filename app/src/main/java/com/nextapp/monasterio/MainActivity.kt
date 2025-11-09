package com.nextapp.monasterio


import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.ui.navigation.AppDrawerContent
import com.nextapp.monasterio.ui.navigation.AppNavigationHost
import com.nextapp.monasterio.ui.theme.*
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nextapp.monasterio.utils.FontSize

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        LanguageHelper.loadLocale(this)
        setContent {
            val appFontScale = FontSize.devolverFontScale(this)
            val currentDensity = LocalDensity.current
            val newDensity= Density(
                density= currentDensity.density,
                fontScale = currentDensity.fontScale*appFontScale
            )
            CompositionLocalProvider(LocalDensity provides newDensity) {
                Smov_monasterioTheme {
                    MonasteryAppScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonasteryAppScreen() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val stringEspanyol= "Español"
    val stringAleman= "Deutsch"
    val stringIngles= "English"


    var mostrarIdioma by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val currentLanguageCode = configuration.locales[0].language

    var idioma by remember { mutableStateOf(currentLanguageCode) }

    var idiomasDisponibles = listOf(stringEspanyol,stringIngles,stringAleman)

    val currentTitle = remember { mutableStateOf(context.getString(R.string.title_inicio)) }

    // ✅ Detectar ruta actual
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    // --- ¡¡CORRECCIÓN DEFINITIVA!! ---

// Comprueba si la ruta actual EMPIEZA POR "panorama"
    val isPanorama = currentRoute?.startsWith(AppRoutes.PANORAMA) == true

// Comprueba si la ruta actual ES "virtual_visit"
    val isVirtualVisit = currentRoute == AppRoutes.VIRTUAL_VISIT

// La vista es inmersiva si es PANORAMA o CUALQUIER COSA DENTRO de VIRTUAL_VISIT
    val isImmersive = isPanorama || isVirtualVisit


    val gesturesEnabled = when(currentRoute){
        AppRoutes.VIRTUAL_VISIT -> false
        else -> true
    }

    // ✅ Actualizar título
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val route = backStackEntry.destination.route
            // --- MODIFICACIÓN SUGERIDA ---
            // Añadimos esto para que no intente buscar un título para la ruta con argumento
            val routeBase = route?.split("/")?.firstOrNull() ?: route

            currentTitle.value = when (routeBase) { // <-- Usamos routeBase
                AppRoutes.INFO -> context.getString(R.string.title_info_general)
                AppRoutes.HISTORIA -> context.getString(R.string.title_history)
                AppRoutes.GALERIA -> context.getString(R.string.title_gallery)
                AppRoutes.PERFIL -> context.getString(R.string.title_profile)
                AppRoutes.AJUSTES -> context.getString(R.string.title_settings)
                AppRoutes.OPCIONES_RESERVA,
                AppRoutes.RESERVA,
                AppRoutes.CONFIRMACION_RESERVA -> context.getString(R.string.title_appointment)
                AppRoutes.VIRTUAL_VISIT -> context.getString(R.string.title_monasterio)
                // (No es necesario añadir PANORAMA aquí, ya que será inmersiva y no mostrará título)
                else -> context.getString(R.string.title_inicio)
            }
        }
    }

    if (isImmersive) {
        // 🌌 Vista inmersiva sin barra ni menú
        AppNavigationHost(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // 🧱 Vista normal con barra y menú
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    navController = navController,
                    scope = scope,
                    drawerState = drawerState
                )
            },
            gesturesEnabled = gesturesEnabled
        ) {
            Scaffold(
                topBar = {
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
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
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
                            Box {
                                OutlinedIconButton(onClick = {
                                    mostrarIdioma = true
                                }) {
                                    val iconRes =
                                        if (idioma == "es" || idioma == stringEspanyol) R.drawable.espanya else if (idioma == "de" || idioma == stringAleman) R.drawable.alemania else R.drawable.reinounido
                                    Image(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = if (idioma == "es" || idioma == stringEspanyol ) stringEspanyol
                                         else if (idioma == "de" || idioma == stringAleman) stringAleman else stringIngles,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                DropdownMenu(
                                    expanded = mostrarIdioma,
                                    onDismissRequest = { mostrarIdioma = false },
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                ) {
                                    idiomasDisponibles.forEach { idiomaSlot ->
                                        DropdownMenuItem(
                                            text = { Text(idiomaSlot) },
                                            onClick = {
                                                idioma = idiomaSlot
                                                mostrarIdioma = false
                                                if(idiomaSlot == stringEspanyol)
                                                    LanguageHelper.saveLocale(context, "es")
                                                else if(idiomaSlot == stringAleman){
                                                    LanguageHelper.saveLocale(context, "de")
                                                } else LanguageHelper.saveLocale(context, "en")
                                            }
                                        )
                                    }
                                }
                            }
                                IconButton(onClick = {
                                    Toast.makeText(
                                        context,
                                        "Modo edición (próximamente)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.lapiz),
                                        contentDescription = stringResource(id = R.string.edit_mode)
                                    )
                                }
                            }
                    )
                }
            ) { paddingValues ->
                AppNavigationHost(
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
