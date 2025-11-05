package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.R

@Composable
fun HistoriaScreen() {

    val context = LocalContext.current

    val activity = (context as? Activity)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ExpandableHistoryCard(
            title = stringResource(id = R.string.history_1800),
            content = stringResource(id = R.string.history_content_placeholder)
        )
        ExpandableHistoryCard(
            title = stringResource(id = R.string.history_1900),
            content = stringResource(id = R.string.history_content_placeholder)
        )
        ExpandableHistoryCard(
            title = stringResource(id = R.string.history_1950),
            content = stringResource(id = R.string.history_content_placeholder)
        )
        ExpandableHistoryCard(
            title = stringResource(id = R.string.history_actualidad),
            content = stringResource(id = R.string.history_content_placeholder)
        )
    }
}

@Composable
fun ExpandableHistoryCard(
    title: String,
    content: String
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.arrow_down),
                contentDescription = "Expandir/Colapsar",
                modifier = Modifier.rotate(rotationAngle)
            )
        }

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        AnimatedVisibility(visible = expanded) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }
    }
}