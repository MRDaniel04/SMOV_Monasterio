package com.nextapp.monasterio.ui.screens.pinEdition

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.ui.screens.pinCreation.CreacionPinesScreen


@Composable
fun EdicionPinesHost(navController: NavHostController) {
    val localNav = rememberNavController()
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {}
    }

    NavHost(
        navController = localNav,
        startDestination = "pins_graph"
    ) {

        navigation(
            startDestination = "editor",
            route = "pins_graph"
        ) {

            composable("editor") {
                EdicionPines(
                    navController = localNav,
                    rootNavController = navController
                )
            }

            composable(AppRoutes.CREACION_PINES) {
                CreacionPinesScreen(navController = localNav)
            }
        }
    }



}
