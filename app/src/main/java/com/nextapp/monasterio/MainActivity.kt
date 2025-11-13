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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nextapp.monasterio.utils.FontSize
import com.nextapp.monasterio.viewModels.AjustesViewModel
import com.nextapp.monasterio.viewModels.AuthViewModel

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

    //ViewModel para tener el estado del usuario autenticado
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    // Estado global para el modo edición
    var isEditing by remember { mutableStateOf(false) }

    // ... (Tu lógica de idioma se queda igual)
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


    val isPanorama = currentRoute?.startsWith(AppRoutes.PANORAMA) == true

    // Comprueba si la ruta ES "virtual_visit" (esto cubre el mapa Y los pines anidados)
    val isVirtualVisit = currentRoute == AppRoutes.VIRTUAL_VISIT

    // Comprueba si la ruta EMPIEZA POR "pin_360" (la nueva vista 360)
    val isPin360 = currentRoute?.startsWith(AppRoutes.PIN_360) == true

    // La vista es inmersiva si es CUALQUIERA de las tres
    val isImmersive = isPanorama || isVirtualVisit || isPin360


    val gesturesEnabled = when(currentRoute){
        AppRoutes.VIRTUAL_VISIT -> false
        else -> true
    }

    // ✅ Actualizar título (Tu lógica se queda igual)
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val route = backStackEntry.destination.route
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
                AppRoutes.PIN_ENTRADA_MONASTERIO -> context.getString(R.string.title_entrada)
                else -> context.getString(R.string.title_inicio)
            }
        }
    }

    // --- ¡¡2. ESTRUCTURA REFACTORIZADA (UN SOLO NAVHOST)!! ---
    // (Hemos eliminado el 'if (isImmersive)' que duplicaba el NavHost)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                navController = navController,
                scope = scope,
                drawerState = drawerState
            )
        },
        // Desactivamos gestos en el mapa Y en pantallas inmersivas
        gesturesEnabled = gesturesEnabled && !isImmersive
    ) {
        Scaffold(
            topBar = {
                // Solo mostramos la barra roja si NO estamos en una ruta inmersiva
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
                            // ... (Tu lógica de idioma y Toast se queda igual)
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

                            // Icono Editar: solo disponible para un usuario autenticado
                            if (currentUser != null) {
                                IconButton(onClick = {
                                    isEditing = !isEditing // Toggle del modo edición
                                    val message = if (isEditing) context.getString(R.string.edit_mode_activate_message) else context.getString(R.string.edit_mode_deactivate_message)
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(
                                        painter = painterResource(id = if (isEditing) R.drawable.lapiz else R.drawable.lapiz),
                                        contentDescription = if (isEditing) stringResource(id = R.string.edit_mode_deactivate_icon) else  stringResource(id = R.string.edit_mode_activate_icon)
                                    )
                                }
                            }

                        }
                    )
                }
            }
        ) { paddingValues ->
            // ¡¡UN SOLO AppNavigationHost!!
            // (Si es inmersiva, paddingValues será cero porque no hay TopBar)
            AppNavigationHost(
                authViewModel = authViewModel,
                navController = navController,
                isEditing = isEditing,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}