package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.nextapp.monasterio.AppRoutes
import com.nextapp.monasterio.R
import com.nextapp.monasterio.data.ImagenRepository
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



@Composable
fun HomeScreenContent(navController:NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val activity = (context as? Activity)
    val repo = ImagenRepository()
    var imagenFondoInicio by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val data = repo.getImagenFondoInicio()   // ← trae 'imagen_fondo_inicio'
        imagenFondoInicio = data?.url
    }


    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        onDispose {

        }
    }

    ConstraintLayout(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        val (background, crest, title, btnVisit, btnChild,btnBook, btnEdit) = createRefs()
        if (imagenFondoInicio != null) {
            AsyncImage(
                model = imagenFondoInicio,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.constrainAs(background) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.monastery_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.constrainAs(background) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
            )
        }

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
                    height = Dimension.wrapContent
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
                    bottom.linkTo(btnChild.top, margin = 48.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                    width = Dimension.fillToConstraints
                }
        ) {
            Row(
                modifier=Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            )  {
                Icon(
                    painter = painterResource(id = R.drawable.ic_map_24),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(48.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    stringResource(id = R.string.virtual_visit),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(64.dp).weight(1f))
            }
        }
        Button(
            onClick = {
                navController.navigate(AppRoutes.MODO_NINYOS)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6EB017)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .constrainAs(btnChild) {
                    bottom.linkTo(btnBook.top, margin = 48.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                    width = Dimension.fillToConstraints
                }
        ) {
            Row(
                modifier=Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            )  {
                Icon(
                    painter = painterResource(id = R.drawable.outline_account_child_invert_24),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(48.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    stringResource(id = R.string.child_mode),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(64.dp).weight(1f))
            }
        }
        Button(
            onClick = {
                navController.navigate(AppRoutes.OPCIONES_RESERVA)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .constrainAs(btnBook) {
                    bottom.linkTo(btnEdit.top, margin = 48.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                    width = Dimension.fillToConstraints
                }
        ) {
            Row(
                modifier=Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_time_24),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(48.dp)

                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    stringResource(id = R.string.book_appointment),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.width(64.dp).weight(1f))
            }
        }

        Button(
            onClick = {
                navController.navigate(AppRoutes.MODO_EDICION)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)), // Púrpura de ejemplo
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .constrainAs(btnEdit) {
                    bottom.linkTo(parent.bottom, margin = 120.dp)
                    start.linkTo(parent.start, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                    width = Dimension.fillToConstraints
                }

        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.lapiz), // Ícono provisional
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(48.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    stringResource(id = R.string.edit_mode),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(64.dp).weight(1f))
            }
        }
    }
}
