package com.nextapp.monasterio

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageHelper {

    private const val PREFS_NAME = "language_prefs"
    private const val PREF_KEY_LANGUAGE = "language_code"

    private fun getPrefs(context: Context): SharedPreferences {
        // --- INICIO DE LA CORRECCIÓN ---
        // Usa SIEMPRE el contexto de la aplicación para evitar inconsistencias
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // --- FIN DE LA CORRECCIÓN ---
    }

    // 1. Carga el idioma guardado al iniciar la app
    fun loadLocale(context: Context) {
        val language = getPrefs(context).getString(PREF_KEY_LANGUAGE, null)
        if (language != null) {
            val appLocale = LocaleListCompat.forLanguageTags(language)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    // 2. Guarda el nuevo idioma cuando el usuario lo cambia
    fun saveLocale(context: Context, language: String) {
        getPrefs(context).edit().putString(PREF_KEY_LANGUAGE, language).commit()
        val appLocale = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}