package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nextapp.monasterio.R
import com.nextapp.monasterio.utils.crearCorreo
import kotlinx.coroutines.delay

@Composable
fun ConfirmacionReservaScreen(
    navController: NavController,
    nombre: String,
    email:String,
    fecha: String,
    hora:String
){
    val context= LocalContext.current
    LaunchedEffect(key1=true) {
        delay(500L)
        context.crearCorreo(nombre,email,fecha,hora)
    }

    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {

        }
    }
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
                contentDescription = stringResource(R.string.contentimage_confirmation),
                modifier = Modifier.size(210.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.confirmation_appointment),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.details_appointment),
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(0.65f))
        }
    }
}