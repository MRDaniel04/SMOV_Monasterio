package com.nextapp.monasterio.ui.screens.pinCreation.state

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

open class TranslationFieldState(
    es: String = "",
    en: String = "",
    de: String = "",
    fr: String = ""
) {
    var es by mutableStateOf(es)
    var en by mutableStateOf(en)
    var de by mutableStateOf(de)
    var fr by mutableStateOf(fr)
}
