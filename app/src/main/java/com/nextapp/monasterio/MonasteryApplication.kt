package com.nextapp.monasterio

import android.app.Application
import com.nextapp.monasterio.repository.UserPreferencesRepository // 1. Importar

class MonasteryApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 2. Añade esta línea para inicializar el repositorio
        UserPreferencesRepository.initialize(this)
    }
}