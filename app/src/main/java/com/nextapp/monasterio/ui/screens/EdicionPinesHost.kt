package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.AppRoutes

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
        startDestination = "editor"
    ) {
        composable("editor") {
            EdicionPines(navController = localNav, rootNavController = navController)
        }


        composable(AppRoutes.CREACION_PINES) {
            CreacionPines(navController = localNav)
        }

    }
}
