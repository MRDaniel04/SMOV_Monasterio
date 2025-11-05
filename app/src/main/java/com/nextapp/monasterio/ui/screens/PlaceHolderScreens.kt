// En: ui/screens/PlaceholderScreens.kt (o donde esté GaleriaScreen)

package com.nextapp.monasterio.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn // ¡Importante!
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.data.MonasterioMapRepository // ¡Importante!

@Composable
fun GaleriaScreen(navController: NavController) { // Recuerda que recibe NavController

    // 1. Obtenemos la lista completa de vistas del repositorio
    val vistas = MonasterioMapRepository.getVistas()

    // 2. Usamos LazyColumn para mostrar eficientemente la lista de botones
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp), // Añadimos padding
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3. Creamos un item (un botón) por cada vista en la lista
        items(vistas.size) { index ->
            val vista = vistas[index]

            Button(
                onClick = {
                    // 4. Al hacer clic, navega a PanoramaScreen con el ID de la vista
                    navController.navigate(AppRoutes.PANORAMA + "/${vista.id}")
                },
                modifier = Modifier.fillMaxWidth() // Hacemos que los botones sean anchos
            ) {
                Text(text = vista.nombre) // Mostramos el nombre descriptivo
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ... (tus otros PlaceholderScreens como PerfilScreen, etc.)