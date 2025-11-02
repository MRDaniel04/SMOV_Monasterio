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
import com.nextapp.monasterio.utils.FontSize

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        LanguageHelper.loadLocale(this)
        setContent {
            val appFontScale = FontSize.devolverFontScale(this)
            val currentDensity = LocalDensity.current
            val newDensity = Density(
                density = currentDensity.density,
                fontScale = currentDensity.fontScale * appFontScale
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

    // --- 2. Estado para guardar el título actual ---
    val currentTitle = remember { mutableStateOf(context.getString(R.string.title_inicio)) }

    // --- 3. Escuchamos los cambios de navegación para actualizar el título ---
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val route = backStackEntry.destination.route
            currentTitle.value = when (route) {
                AppRoutes.INFO -> context.getString(R.string.title_info_general)
                AppRoutes.HISTORIA -> context.getString(R.string.title_history)
                AppRoutes.GALERIA -> context.getString(R.string.title_gallery)
                AppRoutes.PERFIL -> context.getString(R.string.title_profile)
                AppRoutes.AJUSTES -> context.getString(R.string.title_settings)
                AppRoutes.OPCIONES_RESERVA -> context.getString(R.string.title_appointment)
                AppRoutes.RESERVA -> context.getString(R.string.title_appointment)
                AppRoutes.CONFIRMACION_RESERVA -> context.getString(R.string.title_appointment)
                AppRoutes.VIRTUAL_VISIT -> context.getString(R.string.title_monasterio)
                else -> context.getString(R.string.title_inicio)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // --- 4. CONTENIDO DEL MENÚ (AHORA EN ui.navigation) ---
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
                            if (drawerState.isOpen) {
                                Icon(
                                    painter = painterResource(id = R.drawable.menu_close),
                                    contentDescription = stringResource(id = R.string.navigation_drawer_close)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_menu_24),
                                    contentDescription = stringResource(id = R.string.navigation_drawer_open)
                                )
                            }
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
            // --- 6. HOST DE NAVEGACIÓN (AHORA EN ui.navigation) ---
            AppNavigationHost(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}