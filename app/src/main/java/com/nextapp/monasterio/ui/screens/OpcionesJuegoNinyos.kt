package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange
import com.nextapp.monasterio.ui.theme.MonasteryRed


@Composable
fun OpcionesJuegoNinyos(navController:NavController,modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        onDispose {

        }
    }

    ConstraintLayout(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        val (btnMemory,btnPuzle,btnDiferencias,background) = createRefs()
        createVerticalChain(btnPuzle,btnMemory,btnDiferencias, chainStyle = ChainStyle.Packed)

        Image(
            painter = painterResource(R.drawable.fondo),
            contentDescription = "Fondo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .constrainAs(background){
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
        )

        Button(
            onClick = {
                navController.navigate(AppRoutes.JUEGO_PUZZLE)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(bottom=40.dp)
                .constrainAs(btnPuzle) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                    width = Dimension.fillToConstraints
                }
        ) {
            Row(
                modifier=Modifier
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            )  {
                Icon(
                    painter = painterResource(id = R.drawable.puzzle),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 18.dp)
                        .size(48.dp)
                )
                Text(
                    stringResource(id = R.string.puzzle_option),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Button(
            onClick = {
                navController.navigate(AppRoutes.JUEGO_PAREJAS)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryRed),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(bottom=40.dp)
                .constrainAs(btnMemory) {
                    start.linkTo(parent.start, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                    width = Dimension.fillToConstraints
                }
        ) {
            Row(
                modifier=Modifier
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.memory),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(48.dp)

                )
                Text(
                    stringResource(id = R.string.memory_option),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Button(
            onClick = {
                navController.navigate(AppRoutes.JUEGO_DIFERENCIAS)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryOrange),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(bottom=40.dp)
                .constrainAs(btnDiferencias) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                    width = Dimension.fillToConstraints
                }
        ) {
            Row(
                modifier=Modifier
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            )  {
                Icon(
                    painter = painterResource(id = R.drawable.lupa),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 18.dp)
                        .size(48.dp)
                )
                Text(
                    stringResource(id = R.string.differences_game),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
