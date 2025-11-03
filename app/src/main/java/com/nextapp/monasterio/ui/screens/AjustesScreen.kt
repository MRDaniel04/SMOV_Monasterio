package com.nextapp.monasterio.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextapp.monasterio.LanguageHelper
import com.nextapp.monasterio.R
import com.nextapp.monasterio.utils.FontSize

@Composable
fun AjustesScreen() {
    // ✅ Usa LocalActivity en lugar de LocalContext
    val activity = LocalActivity.current as? AppCompatActivity ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                FontSize.guardarFontScale(activity, 0.85f)
                activity.recreate()
            }
        ) {
            Text(
                text = stringResource(R.string.font_small),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                FontSize.guardarFontScale(activity, 1.25f)
                activity.recreate()
            }
        ) {
            Text(
                text = stringResource(R.string.font_big),
                textAlign = TextAlign.Center
            )
        }
    }
}
