package com.nextapp.monasterio.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.nextapp.monasterio.utils.clickableSingle
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
import com.nextapp.monasterio.models.ParejasData
import com.nextapp.monasterio.models.ParejasSize
import com.nextapp.monasterio.models.PuzzleSize
import com.nextapp.monasterio.models.User
import com.nextapp.monasterio.ui.components.GenericPdfViewer
import com.nextapp.monasterio.ui.screens.*
import com.nextapp.monasterio.ui.screens.pinEdition.EdicionFondoInicio
import com.nextapp.monasterio.ui.screens.pinEdition.EdicionPinesHost
import com.nextapp.monasterio.ui.theme.MonasteryRed
import com.nextapp.monasterio.ui.theme.White
import com.nextapp.monasterio.viewModels.AjustesViewModel
import com.nextapp.monasterio.viewModels.InfoViewModel
import com.nextapp.monasterio.viewModels.HistoriaViewModel
import com.nextapp.monasterio.viewModels.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppNavigationHost(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    historiaViewModel: HistoriaViewModel = viewModel(),
    ajustesViewModel: AjustesViewModel = viewModel(),
    infoViewModel: InfoViewModel = viewModel(),
    navController: NavHostController,
    isEditing: Boolean = false,
    isDiscarding: Boolean = false,
    onKeepEditing: () -> Unit = {},
    scaffoldPadding: PaddingValues = PaddingValues(0.dp)
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.INICIO,
        modifier = modifier
    ) {
        composable(AppRoutes.INICIO)   { HomeScreenContent(navController = navController, topPadding = scaffoldPadding, authViewModel = authViewModel) }
        composable(AppRoutes.INFO) { InfoScreen(isEditing = isEditing, isDiscarding = isDiscarding, onKeepEditing = onKeepEditing, viewModel = infoViewModel, topPadding = scaffoldPadding) }
        composable(AppRoutes.HISTORIA) { HistoriaScreen(isEditing = isEditing, isDiscarding = isDiscarding, onKeepEditing = onKeepEditing, viewModel = historiaViewModel,topPadding = scaffoldPadding) }
        composable(AppRoutes.GALERIA)  { GaleriaScreen(navController = navController) }
        composable(AppRoutes.MODO_NINYOS)   { OpcionesModoNiño(navController = navController, topPadding = scaffoldPadding) }
        composable(AppRoutes.VIDEO_NINYOS)   { VideoNinyosScreen(topPadding = scaffoldPadding) }
        composable(AppRoutes.PUZZLENIVEL1){PuzzleScreen(navController=navController,tamaño = PuzzleSize(2,2))}
        composable(AppRoutes.PUZZLENIVEL2){PuzzleScreen(navController=navController,tamaño = PuzzleSize(3,3))}
        composable(AppRoutes.PUZZLENIVEL3){PuzzleScreen(navController=navController,tamaño = PuzzleSize(4,4))}
        composable(AppRoutes.PUZZLENIVEL4){PuzzleScreen(navController=navController,tamaño= PuzzleSize(5,5))}
        composable(AppRoutes.JUEGO_NINYOS){OpcionesJuegoNinyos(navController=navController, topPadding = scaffoldPadding)}
        composable(route=AppRoutes.MODO_EDICION) { OpcionesModoEdicion(navController = navController)}
        composable(AppRoutes.EDICION_FONDO_INICIO) { EdicionFondoInicio(navController = navController, topPadding = scaffoldPadding) }
        composable(AppRoutes.PERFIL)   { ProfileScreen(isEditing = isEditing, isDiscarding = isDiscarding, onKeepEditing = onKeepEditing, viewModel = authViewModel ) }
        composable(AppRoutes.AJUSTES)  { AjustesScreen(viewModel = ajustesViewModel) }
        composable(AppRoutes.MANUAL) { GenericPdfViewer(pdfFileName = "manual_usuario.pdf", initialZoom = 1.25f)}
        composable (AppRoutes.JUEGO_PUZZLE) {JuegoPuzzleScreen(navController = navController, topPadding = scaffoldPadding)}
        composable(AppRoutes.JUEGO_PAREJAS) {JuegoParejasScreen(navController = navController, topPadding = scaffoldPadding)}
        composable(AppRoutes.PAREJASNIVEL1){ParejasScreen(navController=navController, size = ParejasSize(3,2),imagenes= ParejasData.IMAGENES_NIVEL1)}
        composable(AppRoutes.PAREJASNIVEL2){ParejasScreen(navController=navController,size = ParejasSize(4,2),imagenes= ParejasData.IMAGENES_NIVEL2)}
        composable(AppRoutes.PAREJASNIVEL3){ParejasScreen(navController=navController,size = ParejasSize(5,2),imagenes= ParejasData.IMAGENES_NIVEL3)}
        composable(AppRoutes.PAREJASNIVEL4){ParejasScreen(navController=navController,size= ParejasSize(4,3),imagenes= ParejasData.IMAGENES_NIVEL4)}
        composable(AppRoutes.JUEGO_DIFERENCIAS){DiferenciasScreen(navController=navController)}

        // Ruta de Visita Virtual (Mapa)
        composable(AppRoutes.VIRTUAL_VISIT) {
            VirtualVisitScreen(navController = navController,viewModel = ajustesViewModel, topPadding = scaffoldPadding)
        }

        // Ruta al Edición Pines Host
        composable(AppRoutes.EDICION_PINES) {
            EdicionPinesHost(navController)
        }


        composable(
            route = AppRoutes.PIN_360 + "/{pinId}",
            arguments = listOf(navArgument("pinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pinId = backStackEntry.arguments?.getString("pinId")
            if (pinId != null) {
                Pin360Screen(
                    pinId = pinId,
                    navController = navController
                )
            }
        }



        // --- FIN DE LA MODIFICACIÓN ---

        // (La ruta PIN_DETALLE se queda eliminada de aquí, ¡está bien!)

        // --- RESTO DE TUS RUTAS ---
        composable(AppRoutes.OPCIONES_RESERVA) { OpcionesReservaScreen(navController = navController,viewModel= infoViewModel, topPadding = scaffoldPadding) }
        composable(AppRoutes.RESERVA) { ReservaScreen(navController = navController, viewModel = infoViewModel,topPadding = scaffoldPadding) }
        composable(
            route = AppRoutes.CONFIRMACION_RESERVA + "/{nombre}/{fecha}/{hora}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("fecha") { type = NavType.StringType },
                navArgument("hora") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
            val hora = backStackEntry.arguments?.getString("hora") ?: ""
            ConfirmacionReservaScreen(
                nombre = nombre,
                fecha = fecha,
                hora = hora,
                viewModel = infoViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    navController: NavHostController,
    scope: CoroutineScope,
    drawerState: DrawerState,
    currentUser: User?
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
                .verticalScroll(rememberScrollState())
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
            if(currentUser != null) {
                DrawerMenuItem(text = stringResource(id = R.string.menu_manual_usuario)) {
                    navigateTo(
                        AppRoutes.MANUAL
                    )
                }
            }

        }
    }
}

@Composable
fun DrawerMenuItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableSingle(onClick = onClick)
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