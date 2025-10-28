package com.nextapp.monasterio.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextapp.monasterio.R

@Composable
fun ConfirmacionReservaScreen(){
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(R.drawable.monastery_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f))
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Spacer(modifier = Modifier.weight(0.35f))
            Image(
                painter = painterResource(id = R.drawable.check_confirmacion),
                contentDescription = "Confirmado",
                modifier = Modifier.size(210.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "¡CITA CONFIRMADA!",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Tu visita ha sido confirmada con éxito. Recibirás al correo electrónico los detalles.",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(0.65f))
        }
    }
}