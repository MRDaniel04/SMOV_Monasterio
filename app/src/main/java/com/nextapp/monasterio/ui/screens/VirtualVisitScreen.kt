package com.nextapp.monasterio.ui.screens
/**
 * 🔹 Pantalla principal del módulo de "Visita Virtual".
 *
 * Contiene un `NavHost` interno que gestiona la navegación local entre:
 *   - `PlanoInteractivoScreen`: vista principal del plano con zonas y pines.
 *   - `FiguraDetalleScreen`: detalle mostrado al tocar una figura del plano.
 *   - `IglesiaDetalleScreen`: detalle mostrado al tocar la figura de la iglesia.
 *   - `PinDetalleScreen`: detalle mostrado al tocar un pin interactivo.
 *
 * Este módulo se integra en la navegación general de la app mediante la ruta `AppRoutes.VIRTUAL_VISIT`.
 */

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.ui.virtualvisit.screens.*

object VirtualVisitRoutes {
    const val PLANO = "plano"
    const val DETALLE_MONASTERIO = "detalle_monasterio"
    const val DETALLE_IGLESIA = "detalle_iglesia"
    const val DETALLE_COLEGIO = "detalle-colegio"
    const val DETALLE_PIN = "detalle_pin"
    const val DETALLE_PIN2 = "detalle_pin2"
}

@Composable
fun VirtualVisitScreen(navController: NavHostController? = null) {
    // Este NavHost es interno SOLO para la sección de visita virtual
    val localNavController = rememberNavController()

    NavHost(
        navController = localNavController,
        startDestination = VirtualVisitRoutes.PLANO
    ) {
        // 🔹 Pantalla principal del plano interactivo
        composable(VirtualVisitRoutes.PLANO) {
            PlanoInteractivoScreen(navController = localNavController)
        }

        // 🔹 Detalle del monasterio
        composable(VirtualVisitRoutes.DETALLE_MONASTERIO) {
            FiguraDetalleScreen()
        }

        // 🔹 Detalle de la iglesia
        composable(VirtualVisitRoutes.DETALLE_IGLESIA) {
            IglesiaDetalleScreen()
        }

        // 🔹 Detalle del colegio
        composable(VirtualVisitRoutes.DETALLE_COLEGIO) {
            ColegioDetalleScreen()
        }

        // 🔹 Detalle del pin interactivo
        composable(VirtualVisitRoutes.DETALLE_PIN) {
            PinDetalleScreen()
        }

        composable(VirtualVisitRoutes.DETALLE_PIN2) {
            PinDetalleScreen2()
        }

    }
}
