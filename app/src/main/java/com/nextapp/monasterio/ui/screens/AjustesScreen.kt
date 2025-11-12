package com.nextapp.monasterio.ui.screens

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.LanguageHelper
import com.nextapp.monasterio.R
import com.nextapp.monasterio.utils.FontSize
import com.nextapp.monasterio.viewModels.AjustesViewModel

@Composable
fun AjustesScreen(viewModel: AjustesViewModel) {
    // ✅ Usa LocalActivity en lugar de LocalContext
    val activity = LocalActivity.current as? AppCompatActivity ?: return
    val botonesVisibles by viewModel.botonesVisibles.collectAsState()

    DisposableEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.language_select),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para Español
        Button(onClick = {
            LanguageHelper.saveLocale(activity, "es")
            activity.recreate()
        }) {
            Text("Español")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para Inglés
        Button(onClick = {
            LanguageHelper.saveLocale(activity, "en")
            activity.recreate()
        }) {
            Text("English")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para Alemán
        Button(onClick = {
            LanguageHelper.saveLocale(activity, "de")
            activity.recreate()
        }) {
            Text("Deutsch")
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.font_size_select),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))


        Button(
            onClick = {
                FontSize.guardarFontScale(activity, 1.0f)
                activity.recreate()
            }
        ) {
            Text(
                text = stringResource(R.string.font_normal),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                FontSize.guardarFontScale(activity, 1.3f)
                activity.recreate()
            }
        ) {
            Text(
                text = stringResource(R.string.font_big),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                FontSize.guardarFontScale(activity, 1.6f)
                activity.recreate()
            }
        ) {
            Text(
                text = stringResource(R.string.font_extrabig),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ){
            Text(
                text=stringResource(R.string.navegation_button),
                style = MaterialTheme.typography.headlineMedium,
            )
            Checkbox(
                checked = !botonesVisibles,
                onCheckedChange = {nuevoValor ->
                    viewModel.setBotonesVisibles(!nuevoValor)
                }
            )
        }
    }
}
