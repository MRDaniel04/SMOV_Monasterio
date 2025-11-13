package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.ui.virtualvisit.screens.*
import com.nextapp.monasterio.viewModels.AjustesViewModel

object VirtualVisitRoutes {
    const val PLANO = "plano"
    const val DETALLE_GENERICO = "detalle_generico"
}

@Composable
fun VirtualVisitScreen(navController: NavHostController? = null,viewModel: AjustesViewModel) {
    val localNavController = rememberNavController()

    val context = LocalContext.current
    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {}
    }

    // 🔹 ID inicial del plano por defecto (exterior)
    val initialPlanoId = "monasterio_exterior"

    NavHost(
        navController = localNavController,
        // 👇 Empieza directamente en el plano exterior
        startDestination = "${VirtualVisitRoutes.PLANO}/$initialPlanoId"
    ) {
        // --- 🔹 Ruta base sin argumento (por si alguien navega a "plano" directamente)
        composable(VirtualVisitRoutes.PLANO) {
            PlanoScreen(
                viewModel=viewModel,
                planoId = initialPlanoId,
                navController = localNavController,
                rootNavController = navController
            )
        }

        // --- 🔹 Ruta con parámetro (para navegación entre planos)
        composable(
            route = "${VirtualVisitRoutes.PLANO}/{planoId}",
            arguments = listOf(navArgument("planoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val planoId = backStackEntry.arguments?.getString("planoId") ?: initialPlanoId
            PlanoScreen(
                viewModel=viewModel,
                planoId = planoId,
                navController = localNavController,
                rootNavController = navController
            )
        }

        // --- 🔹 Pantalla de detalle de Pin (igual que antes)
        composable(
            route = AppRoutes.PIN_DETALLE + "/{pinId}",
            arguments = listOf(navArgument("pinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pinId = backStackEntry.arguments?.getString("pinId") ?: ""
            PinDetalleFirestoreScreen(
                pinId = pinId,
                navController = localNavController,
                rootNavController = navController
            )
        }


        // --- 🔹 Pantalla genérica de detalle de Figura
        composable(
            route = "${VirtualVisitRoutes.DETALLE_GENERICO}/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            DetalleFiguraScreen(
                navController = localNavController,
                nombre = nombre
            )
        }
    }
}
