package com.nextapp.monasterio.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.ui.virtualvisit.screens.*

/**
 * 🔹 Pantalla principal del módulo de "Visita Virtual".
 *
 * Contiene un `NavHost` interno que gestiona la navegación local entre:
 *   - `PlanoInteractivoScreen`: vista principal del plano con zonas y pines.
 *   - Distintas pantallas de detalle según figura o pin seleccionado.
 */
object VirtualVisitRoutes {
    const val PLANO = "plano"
    const val DETALLE_MONASTERIO = "detalle_monasterio"
    const val DETALLE_IGLESIA = "detalle_iglesia"
    const val DETALLE_COLEGIO = "detalle_colegio"
    const val DETALLE_ARCO_MUDEJAR = "detalle_arco_mudejar"
    const val DETALLE_CLAUSTRO = "detalle_claustro"
    const val DETALLE_PIN = "detalle_pin"
    const val DETALLE_PIN2 = "detalle_pin2"
}

@Composable
fun VirtualVisitScreen(navController: NavHostController? = null) {
    // 🔸 NavController interno del módulo "Visita Virtual"
    val localNavController = rememberNavController()

    NavHost(
        navController = localNavController,
        startDestination = VirtualVisitRoutes.PLANO
    ) {
        // 🗺️ Pantalla principal del plano interactivo
        composable(VirtualVisitRoutes.PLANO) {
            PlanoInteractivoScreen(navController = localNavController)
        }

        // 🏛️ Detalle del monasterio
        composable(VirtualVisitRoutes.DETALLE_MONASTERIO) {
            MonasterioDetalleScreen(navController = localNavController)
        }

        // ⛪ Detalle de la iglesia
        composable(VirtualVisitRoutes.DETALLE_IGLESIA) {
            IglesiaDetalleScreen(navController = localNavController)
        }

        // 🏫 Detalle del colegio
        composable(VirtualVisitRoutes.DETALLE_COLEGIO) {
            ColegioDetalleScreen(navController = localNavController)
        }

        // 🧱 Detalle del arco mudéjar
        composable(VirtualVisitRoutes.DETALLE_ARCO_MUDEJAR) {
            ArcoMudejarDetalleScreen(navController = localNavController)
        }

        // 🌿 Detalle del claustro
        composable(VirtualVisitRoutes.DETALLE_CLAUSTRO) {
            ClaustroDetalleScreen(navController = localNavController)
        }

        // 📍 Detalle del primer pin interactivo
        composable(VirtualVisitRoutes.DETALLE_PIN) {
            PinDetalleScreen(navController = localNavController)
        }

        // 📍 Detalle del segundo pin (si lo tienes)
        composable(VirtualVisitRoutes.DETALLE_PIN2) {
            PinDetalleScreen2(navController = localNavController)
        }
    }
}
