package com.nextapp.monasterio.ui.screens.pinCreation.state

class DescripcionState(
    es: String = "",
    en: String = "",
    de: String = "",
    fr: String = "",
    onChanged: (() -> Unit)? = null
) : TranslationFieldState(es, en, de, fr, onChanged)
