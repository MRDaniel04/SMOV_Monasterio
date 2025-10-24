package com.nextapp.monasterio.ui.virtualvisit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nextapp.monasterio.ui.theme.Smov_monasterioTheme

class VirtualVisitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Smov_monasterioTheme {
                VirtualVisitScreen()
            }
        }
    }
}
