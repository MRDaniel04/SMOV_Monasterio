package com.nextapp.monasterio.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.screens.* // Asegúrate de importar PanoramaScreen y GaleriaScreen
import com.nextapp.monasterio.ui.theme.MonasteryRed
import com.nextapp.monasterio.ui.theme.White
import com.nextapp.monasterio.viewModels.AjustesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppNavigationHost(
    ajustesViewModel: AjustesViewModel = viewModel(),
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.INICIO,
        modifier = modifier
    ) {
        composable(AppRoutes.INICIO)   { HomeScreenContent(navController = navController) }
        composable(AppRoutes.INFO)     { InfoScreen() }
        composable(AppRoutes.HISTORIA) { HistoriaScreen() }
        composable(AppRoutes.GALERIA)  { GaleriaScreen(navController = navController) }
        composable(AppRoutes.PERFIL)   { ProfileScreen() }
        composable(AppRoutes.AJUSTES)  { AjustesScreen(viewModel = ajustesViewModel) }

        // Ruta de Panorama 360 (Inmersiva, desde Galería / res/raw)
        composable(
            route = AppRoutes.PANORAMA + "/{vistaId}",
            arguments = listOf(navArgument("vistaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vistaId = backStackEntry.arguments?.getString("vistaId")
            if (vistaId != null) {
                PanoramaScreen(
                    vistaId = vistaId,
                    navController = navController
                )
            }
        }

        // Ruta de Visita Virtual (Mapa)
        composable(AppRoutes.VIRTUAL_VISIT) {
            VirtualVisitScreen(navController = navController,viewModel = ajustesViewModel)
        }

        // --- ¡¡AQUÍ ESTÁ LA NUEVA RUTA!! ---
        // Ruta inmersiva para el 360 de un Pin (desde Firebase URL)
        composable(
            route = AppRoutes.PIN_360 + "/{pinId}", // Recibe el ID del Pin
            arguments = listOf(navArgument("pinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pinId = backStackEntry.arguments?.getString("pinId")
            if (pinId != null) {
                // Llama a la NUEVA pantalla 360
                Pin360Screen(
                    pinId = pinId,
                    navController = navController
                )
            }
        }
        // --- FIN DE LA MODIFICACIÓN ---

        // (La ruta PIN_DETALLE se queda eliminada de aquí, ¡está bien!)

        // --- RESTO DE TUS RUTAS ---
        composable(AppRoutes.OPCIONES_RESERVA) { OpcionesReservaScreen(navController = navController) }
        composable(AppRoutes.RESERVA) { ReservaScreen(navController = navController) }
        composable(
            route = AppRoutes.CONFIRMACION_RESERVA + "/{nombre}/{email}/{fecha}/{hora}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("fecha") { type = NavType.StringType },
                navArgument("hora") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
            val hora = backStackEntry.arguments?.getString("hora") ?: ""
            ConfirmacionReservaScreen(
                navController = navController,
                nombre = nombre,
                email = email,
                fecha = fecha,
                hora = hora
            )
        }
    }
}

//
// --- EL RESTO DEL ARCHIVO (AppDrawerContent y DrawerMenuItem) SE QUEDA IGUAL ---
//

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    navController: NavHostController,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
        scope.launch { drawerState.close() }
    }

    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = MonasteryRed,
        drawerContentColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            IconButton(onClick = { scope.launch { drawerState.close() } }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu_24),
                    contentDescription = stringResource(id = R.string.navigation_drawer_close)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            DrawerMenuItem(text = stringResource(id = R.string.title_inicio)) { navigateTo(AppRoutes.INICIO) }
            DrawerMenuItem(text = stringResource(id = R.string.menu_info)) { navigateTo(AppRoutes.INFO) }
            DrawerMenuItem(text = stringResource(id = R.string.menu_history)) { navigateTo(AppRoutes.HISTORIA) }
            DrawerMenuItem(text = stringResource(id = R.string.menu_gallery)) { navigateTo(AppRoutes.GALERIA) }
            DrawerMenuItem(text = stringResource(id = R.string.menu_profile)) { navigateTo(AppRoutes.PERFIL) }
            DrawerMenuItem(text = stringResource(id = R.string.menu_settings)) { navigateTo(AppRoutes.AJUSTES) }
        }
    }
}

@Composable
fun DrawerMenuItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.arrow_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = White, style = MaterialTheme.typography.bodyMedium)
    }
}