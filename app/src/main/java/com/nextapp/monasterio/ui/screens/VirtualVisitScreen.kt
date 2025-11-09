package com.nextapp.monasterio.ui.screens // (O donde esté tu archivo)

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType // <-- ¡IMPORTANTE!
import androidx.navigation.navArgument // <-- ¡IMPORTANTE!
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.AppRoutes // <-- ¡IMPORTANTE!
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
fun VirtualVisitScreen(navController: NavHostController? = null) { // <-- Este es el 'navController' RAÍZ
    // Este es el 'localNavController'
    val localNavController = rememberNavController()

    val context = LocalContext.current
    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {}
    }

    NavHost(
        navController = localNavController,
        startDestination = VirtualVisitRoutes.PLANO
    ) {
        composable(VirtualVisitRoutes.PLANO) {
            PlanoInteractivoScreen(
                navController = localNavController,
                rootNavController = navController
            )
        }

        // --- (Tus otras rutas de "sub-parte") ---
        composable(VirtualVisitRoutes.DETALLE_MONASTERIO) {
            MonasterioDetalleScreen(
                navController = localNavController,
                rootNavController = navController // <-- ¡CORRECCIÓN 2: Pasa el raíz!
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
        // --- FIN DE TUS OTRAS RUTAS ---


        composable(
            route = AppRoutes.PIN_DETALLE + "/{pinId}", // Usamos la constante global
            arguments = listOf(navArgument("pinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pinId = backStackEntry.arguments?.getString("pinId") ?: ""
            PinDetalleFirestoreScreen(
                pinId = pinId,
                navController = localNavController, // <-- El local (para onBack)
                rootNavController = navController // <-- ¡CORRECCIÓN 3: Pasa el raíz!
            )
        }
    }
}