package com.nextapp.monasterio

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageHelper {

    private const val PREFS_NAME = "language_prefs"
    private const val PREF_KEY_LANGUAGE = "language_code"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // 1. Carga el idioma guardado al iniciar la app
    fun loadLocale(context: Context) {
        val language = getPrefs(context).getString(PREF_KEY_LANGUAGE, null)
        if (language != null) {
            val appLocale = LocaleListCompat.forLanguageTags(language)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
        // Si es null, usará el idioma del sistema (que cargará tu 'values' español por defecto)
    }

    // 2. Guarda el nuevo idioma cuando el usuario lo cambia
    fun saveLocale(context: Context, language: String) {
        getPrefs(context).edit().putString(PREF_KEY_LANGUAGE, language).apply()
        val appLocale = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}