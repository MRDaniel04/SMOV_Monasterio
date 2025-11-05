package com.nextapp.monasterio

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
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

    val currentTitle = remember { mutableStateOf(context.getString(R.string.title_inicio)) }

    // ✅ Detectar ruta actual
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
    // En lugar de una lista, comprobamos si la ruta "empieza por" la ruta base,
    // ya que ahora tiene argumentos (ej: "panorama/monastery_1")

    // Comprueba si la ruta actual empieza por "panorama"
    val isPanorama = currentRoute?.startsWith(AppRoutes.PANORAMA) == true
    // Comprueba si la ruta actual es exactamente "pin_detalle"
    val isPinDetalle = currentRoute == AppRoutes.PIN_DETALLE

    // La vista es inmersiva si CUALQUIERA de las dos es verdadera
    val isImmersive = isPanorama || isPinDetalle
    // --- FIN DE LA CORRECCIÓN ---


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
                            IconButton(onClick = {
                                Toast.makeText(context, "Modo edición (próximamente)", Toast.LENGTH_SHORT).show()
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