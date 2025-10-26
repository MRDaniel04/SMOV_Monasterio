package com.nextapp.monasterio

import android.app.Application

/**
 * Esta clase se ejecuta ANTES que cualquier Activity.
 * Es el lugar perfecto para configurar cosas de toda la app, como el idioma.
 */
class MonasteryApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Carga el idioma guardado en el momento en que la app arranca.
        // Esto garantiza que CUALQUIER activity que se inicie ya tenga el idioma correcto.
        LanguageHelper.loadLocale(this)
    }
}