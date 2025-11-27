package com.nextapp.monasterio.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.components.EditableText
import com.nextapp.monasterio.ui.theme.MonasteryRed
import com.nextapp.monasterio.ui.theme.White
import com.nextapp.monasterio.viewModels.InfoViewModel

@Composable
fun InfoScreen(
    isEditing: Boolean = false,
    viewModel: InfoViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    val infoState by viewModel.infoState.collectAsState()

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Información General",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            EditableText(
                textMap = infoState.mainContent,
                isEditing = isEditing,
                onTextMapChange = { viewModel.updateMainContent(it) },
                readOnlyStyle = MaterialTheme.typography.bodyLarge
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MonasteryRed
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.general_info),
                    style = MaterialTheme.typography.titleLarge,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                InfoRow(
                    iconResId = R.drawable.location,
                    textMap = infoState.location,
                    isEditing = isEditing,
                    onUpdate = { viewModel.updateLocation(it) },
                    defaultLabel = stringResource(id = R.string.info_location)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoRow(
                    iconResId = R.drawable.ic_time_24,
                    textMap = infoState.hours,
                    isEditing = isEditing,
                    onUpdate = { viewModel.updateHours(it) },
                    defaultLabel = stringResource(id = R.string.info_hours)
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    iconResId: Int,
    textMap: Map<String, String>,
    isEditing: Boolean,
    onUpdate: (Map<String, String>) -> Unit,
    defaultLabel: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        
        // Usamos Box para que el EditableText ocupe el espacio restante
        Box(modifier = Modifier.weight(1f)) {
             EditableText(
                textMap = textMap,
                isEditing = isEditing,
                onTextMapChange = onUpdate,
                readOnlyStyle = MaterialTheme.typography.bodyLarge.copy(color = White),
                editTextColor = White,
                label = defaultLabel
            )
        }
    }
}