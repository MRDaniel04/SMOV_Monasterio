package com.nextapp.monasterio.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.nextapp.monasterio.AppointmentActivity
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.virtualvisit.VirtualVisitActivity
import com.nextapp.monasterio.ui.theme.Black
import com.nextapp.monasterio.ui.theme.MonasteryBlue
import com.nextapp.monasterio.ui.theme.MonasteryOrange
import com.nextapp.monasterio.ui.theme.White

@Composable
fun HomeScreenContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current

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
            painter = painterResource(id = R.drawable.escudo),
            contentDescription = "Escudo",
            modifier = Modifier
                .size(80.dp)
                .constrainAs(crest) {
                    top.linkTo(parent.top, margin = 32.dp)
                    start.linkTo(parent.start, margin = 24.dp)
                }
        )
        Text(
            text = stringResource(id = R.string.monastery_name),
            color = White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp,
            style = LocalTextStyle.current.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Black,
                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
            modifier = Modifier.constrainAs(title) {
                top.linkTo(crest.top)
                bottom.linkTo(crest.bottom)
                start.linkTo(crest.end, margin = 16.dp)
                end.linkTo(parent.end, margin = 24.dp)
                width = androidx.constraintlayout.compose.Dimension.fillToConstraints
            }
        )
        Button(
            onClick = {
                context.startActivity(Intent(context, VirtualVisitActivity::class.java))
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryOrange),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .constrainAs(btnVisit) {
                    bottom.linkTo(btnBook.top, margin = 16.dp)
                    start.linkTo(parent.start, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                    width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }
                .height(60.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_map_24),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(id = R.string.virtual_visit))
        }
        Button(
            onClick = {
                context.startActivity(Intent(context, AppointmentActivity::class.java))
            },
            colors = ButtonDefaults.buttonColors(containerColor = MonasteryBlue),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .constrainAs(btnBook) {
                    bottom.linkTo(parent.bottom, margin = 80.dp)
                    start.linkTo(parent.start, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                    width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }
                .height(60.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_time_24),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(id = R.string.book_appointment))
        }
    }
}