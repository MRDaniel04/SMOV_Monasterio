package com.nextapp.monasterio.ui.screens
/**
 * 🔹 Pantalla principal del módulo de "Visita Virtual".
 *
 * Contiene un `NavHost` interno que gestiona la navegación local entre:
 *   - `PlanoInteractivoScreen`: vista principal del plano con zonas y pines.
 *   - `FiguraDetalleScreen`: detalle mostrado al tocar una figura del plano.
 *   - `PinDetalleScreen`: detalle mostrado al tocar un pin interactivo.
 *
 * Este módulo se integra en la navegación general de la app mediante la ruta `AppRoutes.VIRTUAL_VISIT`.
 */


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nextapp.monasterio.ui.virtualvisit.screens.PlanoInteractivoScreen
import com.nextapp.monasterio.ui.virtualvisit.screens.FiguraDetalleScreen
import com.nextapp.monasterio.ui.virtualvisit.screens.PinDetalleScreen

object VirtualVisitRoutes {
    const val PLANO = "plano"
    const val DETALLE_FIGURA = "detalle_figura"
    const val DETALLE_PIN = "detalle_pin"
}

@Composable
fun VirtualVisitScreen(navController: NavHostController? = null) {
    // Este NavHost es interno SOLO para la sección de visita virtual
    val localNavController = androidx.navigation.compose.rememberNavController()

    NavHost(
        navController = localNavController,
        startDestination = VirtualVisitRoutes.PLANO
    ) {
        composable(VirtualVisitRoutes.PLANO) {
            PlanoInteractivoScreen(navController = localNavController)
        }
        composable(VirtualVisitRoutes.DETALLE_FIGURA) {
            FiguraDetalleScreen()
        }
        composable(VirtualVisitRoutes.DETALLE_PIN) {
            PinDetalleScreen()
        }
    }
}
