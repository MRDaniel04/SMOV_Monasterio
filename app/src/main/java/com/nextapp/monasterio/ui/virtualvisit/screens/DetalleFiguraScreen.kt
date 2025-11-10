
package com.nextapp.monasterio.ui.virtualvisit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nextapp.monasterio.R

@Composable
fun DetalleFiguraScreen(
    navController: NavHostController,
    nombre: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "DETALLE DE $nombre".uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF8F00)
            )
            Text(
                text = "Vista interactiva del área de $nombre.",
                fontSize = 16.sp,
                color = Color(0xFFFF8F00)
            )
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Volver",
                tint = Color.White
            )
        }
    }
}
