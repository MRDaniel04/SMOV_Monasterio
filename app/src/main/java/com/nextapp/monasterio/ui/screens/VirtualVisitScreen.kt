package com.nextapp.monasterio.ui.screens

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
    const val DETALLE_COLEGIO = "detalle_colegio"
    const val DETALLE_ARCO_MUDEJAR = "detalle_arco_mudejar"
    const val DETALLE_CLAUSTRO = "detalle_claustro"
}

@Composable
fun VirtualVisitScreen(navController: NavHostController? = null) {
    val localNavController = rememberNavController()

    NavHost(
        navController = localNavController,
        startDestination = VirtualVisitRoutes.PLANO
    ) {
        composable(VirtualVisitRoutes.PLANO) {
            // 👇 Le pasamos el navController principal para navegar al detalle inmersivo
            PlanoInteractivoScreen(
                navController = localNavController,
                rootNavController = navController // nuevo parámetro
            )
        }

        composable(VirtualVisitRoutes.DETALLE_MONASTERIO) {
            MonasterioDetalleScreen(
                navController = localNavController,
                rootNavController = navController // nuevo parámetro
            )
        }

        composable(VirtualVisitRoutes.DETALLE_IGLESIA) {
            IglesiaDetalleScreen(navController = localNavController)
        }

        composable(VirtualVisitRoutes.DETALLE_COLEGIO) {
            ColegioDetalleScreen(navController = localNavController)
        }

        composable(VirtualVisitRoutes.DETALLE_ARCO_MUDEJAR) {
            ArcoMudejarDetalleScreen(navController = localNavController)
        }

        composable(VirtualVisitRoutes.DETALLE_CLAUSTRO) {
            ClaustroDetalleScreen(navController = localNavController)
        }
    }
}