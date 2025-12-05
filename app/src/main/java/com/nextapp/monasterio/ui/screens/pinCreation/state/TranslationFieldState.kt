package com.nextapp.monasterio.ui.screens.pinCreation.state

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

open class TranslationFieldState(
    es: String = "",
    en: String = "",
    de: String = "",
    fr: String = "",
    private val onChanged: (() -> Unit)? = null
) {
    // ⚠️ ALMACENAMIENTO INTERNO: La variable mutable real
    private var _es by mutableStateOf(es)
    private var _en by mutableStateOf(en)
    private var _de by mutableStateOf(de)
    private var _fr by mutableStateOf(fr)


    // 🚀 EXPOSICIÓN PÚBLICA (Uso idiomático en Compose)
    // El getter permite leer el valor directamente.
    // El setter se usa para mutar el valor (aunque será privado en este caso).
    var es: String
        get() = _es
        private set(value) { _es = value } // <- Mutación controlada internamente

    var en: String
        get() = _en
        private set(value) { _en = value }

    var de: String
        get() = _de
        private set(value) { _de = value }

    var fr: String
        get() = _fr
        private set(value) { _fr = value }

    // --- FUNCIÓN RESET ---
    fun reset() {
        _es = ""
        _en = ""
        _de = ""
        _fr = ""
        onChanged?.invoke()
    }

    // --- FUNCIONES UPDATE ---
    // Estas funciones ahora modifican las variables internas (`_es`) y notifican el cambio.

    fun updateEs(value: String) {
        _es = value // ⬅️ Modificamos la variable privada
        onChanged?.invoke()
    }

    fun updateEn(value: String) {
        _en = value
        onChanged?.invoke()
    }

    fun updateDe(value: String) {
        _de = value
        onChanged?.invoke()
    }

    fun updateFr(value: String) {
        _fr = value
        onChanged?.invoke()
    }
}