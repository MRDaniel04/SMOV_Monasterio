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
import androidx.navigation.compose.currentBackStackEntryAsState

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        LanguageHelper.loadLocale(this)
        setContent {
            Smov_monasterioTheme {
                MonasteryAppScreen()
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

    // ✅ Rutas inmersivas
    val immersiveRoutes = listOf(AppRoutes.PIN_DETALLE)
    val isImmersive = currentRoute in immersiveRoutes

    // ✅ Actualizar título
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val route = backStackEntry.destination.route
            currentTitle.value = when (route) {
                AppRoutes.INFO -> context.getString(R.string.title_info_general)
                AppRoutes.HISTORIA -> context.getString(R.string.title_history)
                AppRoutes.GALERIA -> context.getString(R.string.title_gallery)
                AppRoutes.PERFIL -> context.getString(R.string.title_profile)
                AppRoutes.AJUSTES -> context.getString(R.string.title_settings)
                AppRoutes.OPCIONES_RESERVA,
                AppRoutes.RESERVA,
                AppRoutes.CONFIRMACION_RESERVA -> context.getString(R.string.title_appointment)
                AppRoutes.VIRTUAL_VISIT -> context.getString(R.string.title_monasterio)
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
            }
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