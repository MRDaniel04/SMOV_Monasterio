package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange


@Composable
fun HomeScreenContent(navController:NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        onDispose {

        }
    }

    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (background, crest, title, btnVisit, btnBook) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.monastery_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.constrainAs(background) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                height = androidx.constraintlayout.compose.Dimension.fillToConstraints
            }
        )
        Image(
            painter = painterResource(id = R.drawable.huelgas_inicio),
            contentDescription = "Escudo",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .constrainAs(crest) {
                    top.linkTo(parent.top, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
        )
        Button(
            onClick = {
                navController.navigate(AppRoutes.VIRTUAL_VISIT)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryOrange),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .constrainAs(btnVisit) {
                    bottom.linkTo(btnBook.top, margin = 72.dp)
                    start.linkTo(parent.start, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                    width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }
                .height(60.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_map_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(48.dp)
            )
            Text(
                stringResource(id = R.string.virtual_visit),
                fontSize = 22.sp
            )
        }
        Button(
            onClick = {
                navController.navigate(AppRoutes.OPCIONES_RESERVA)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .constrainAs(btnBook) {
                    bottom.linkTo(parent.bottom, margin = 186.dp)
                    start.linkTo(parent.start, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                    width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }
                .height(60.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_time_24),
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp).size(48.dp)

            )
            Text(
                stringResource(id = R.string.book_appointment),
                fontSize = 22.sp
            )
        }
    }
}
