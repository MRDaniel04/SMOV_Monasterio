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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.screens.* // Importa todas tus pantallas
import com.nextapp.monasterio.ui.theme.MonasteryRed
import com.nextapp.monasterio.ui.theme.White
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// --- 7. El Host de Navegación (El "marco") ---
@Composable
fun AppNavigationHost(
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
        composable(AppRoutes.GALERIA)  { GaleriaScreen() }
        composable(AppRoutes.PERFIL)   { PerfilScreen() }
        composable(AppRoutes.AJUSTES)  { AjustesScreen() }
        composable(AppRoutes.OPCIONES_RESERVA) {OpcionesReservaScreen(navController = navController)}
        composable( AppRoutes.RESERVA ) {ReservaScreen()}
    }
}

// --- 8. El contenido del menú lateral (actualizado) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    navController: NavHostController,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    // Función helper para navegar y cerrar el menú
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

            DrawerMenuItem(
                text = stringResource(id = R.string.title_inicio),
                onClick = { navigateTo(AppRoutes.INICIO) }
            )
            DrawerMenuItem(
                text = stringResource(id = R.string.menu_info),
                onClick = { navigateTo(AppRoutes.INFO) }
            )
            DrawerMenuItem(
                text = stringResource(id = R.string.menu_history),
                onClick = { navigateTo(AppRoutes.HISTORIA) }
            )
            DrawerMenuItem(
                text = stringResource(id = R.string.menu_gallery),
                onClick = { navigateTo(AppRoutes.GALERIA) }
            )
            DrawerMenuItem(
                text = stringResource(id = R.string.menu_profile),
                onClick = { navigateTo(AppRoutes.PERFIL) }
            )
            DrawerMenuItem(
                text = stringResource(id = R.string.menu_settings),
                onClick = { navigateTo(AppRoutes.AJUSTES) }
            )
        }
    }
}

// Esta es la plantilla para cada fila del menú
@Composable
fun DrawerMenuItem(
    text: String,
    onClick: () -> Unit
) {
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
        Text(
            text = text,
            color = White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}