package com.nextapp.monasterio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R

@Composable
fun OpcionesReservaScreen(navController: NavController){
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Button(
            onClick = {navController.navigate(AppRoutes.RESERVA)},
            enabled = true,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E8E3E),
                contentColor = Color.Black,
            ),
            modifier = Modifier.fillMaxWidth().height(120.dp)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Left,
                modifier = Modifier.fillMaxWidth(),
            ){
                Icon(
                    painter = painterResource(R.drawable.telefono),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(Modifier.width(32.dp))
                Text(
                    text = "Reservar Cita",
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = {},
            enabled = false,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF303F9F),
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF303F9F).copy(alpha = 0.7f),
                disabledContentColor = Color.Black.copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth().height(120.dp)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Left,
                modifier = Modifier.fillMaxWidth(),
            ){
                Icon(
                    painter = painterResource(R.drawable.calendario),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(Modifier.width(32.dp))
                Text(
                    text = "No Disponible",
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = {},
            enabled = false,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA16B23),
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFFA16B23).copy(alpha = 0.7f),
                disabledContentColor = Color.Black.copy(alpha = 0.8f)
            ),
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Left,
                modifier = Modifier.fillMaxWidth(),
            ){
                Icon(
                    painter = painterResource(R.drawable.mail),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(Modifier.width(32.dp))
                Text(
                    text = "No Disponible",
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
