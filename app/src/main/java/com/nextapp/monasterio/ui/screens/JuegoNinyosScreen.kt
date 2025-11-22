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


@Composable
fun JuegoNinyosScreen(navController:NavController,modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        onDispose {

        }
    }

    ConstraintLayout(modifier = modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
        val (btn4,btn9,btn16,btn25) = createRefs()
        createVerticalChain(btn4,btn9,btn16,btn25, chainStyle = ChainStyle.Packed)
        Button(
            onClick = {
                navController.navigate(AppRoutes.PUZZLENIVEL1)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(bottom = 40.dp)
                .constrainAs(btn4) {
                    top.linkTo(parent.top)
                    bottom.linkTo(btn9.top, margin = 40.dp)
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
                Text(
                    stringResource(id = R.string.four_pieces),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Button(
            onClick = {
                navController.navigate(AppRoutes.PUZZLENIVEL2)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(bottom = 40.dp)
                .constrainAs(btn9) {
                    top.linkTo(btn4.bottom, margin = 40.dp)
                    bottom.linkTo(btn16.top,margin=40.dp)
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
                Text(
                    stringResource(id = R.string.nine_pieces),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Button(
            onClick = {
                navController.navigate(AppRoutes.PUZZLENIVEL3)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(bottom = 40.dp)
                .constrainAs(btn16) {
                    top.linkTo(btn9.bottom, margin = 40.dp)
                    bottom.linkTo(btn25.top,margin=40.dp)
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
                Text(
                    stringResource(id = R.string.sixteen_pieces),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        Button(
            onClick = {
                navController.navigate(AppRoutes.PUZZLENIVEL4)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .padding(bottom = 40.dp)
                .constrainAs(btn25) {
                    top.linkTo(btn16.bottom, margin = 40.dp)
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
            ) {
                Text(
                    stringResource(id = R.string.twentyfive_pieces),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}