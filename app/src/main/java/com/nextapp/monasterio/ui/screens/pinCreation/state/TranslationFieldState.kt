package com.nextapp.monasterio.ui.screens.pinCreation.state

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.R.attr.value

open class TranslationFieldState(
    es: String = "",
    en: String = "",
    de: String = "",
    fr: String = "",
    private val onChanged: (() -> Unit)? = null
) {

    var es = mutableStateOf(es)
        private set

    var en = mutableStateOf(en)
        private set

    var de = mutableStateOf(de)
        private set

    var fr = mutableStateOf(fr)
        private set

    fun updateEs(value: String) {
        es.value = value
        onChanged?.invoke()
    }

    fun updateEn(value: String) {
        en.value = value
        onChanged?.invoke()
    }

    fun updateDe(value: String) {
        de.value = value
        onChanged?.invoke()
    }

    fun updateFr(value: String) {
        fr.value = value
        onChanged?.invoke()
    }
}
