package com.nextapp.monasterio.ui.screens

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
