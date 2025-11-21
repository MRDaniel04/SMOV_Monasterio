package com.nextapp.monasterio.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository private constructor(private val context: Context) {

    private object PreferencesKeys {
        val TUTORIAL_MAIN_MAP = booleanPreferencesKey("tut_main_map") // Mapa General
        val TUTORIAL_SUB_MAP = booleanPreferencesKey("tut_sub_map")   // Interiores (Claustro, Iglesia...)
        val TUTORIAL_PIN = booleanPreferencesKey("tut_pin_detail")    // Detalle del Pin
    }

    // --- FLUJOS DE LECTURA ---
    val isMainMapTutorialDismissed: Flow<Boolean> = context.dataStore.data
        .map { it[PreferencesKeys.TUTORIAL_MAIN_MAP] ?: false }

    val isSubMapTutorialDismissed: Flow<Boolean> = context.dataStore.data
        .map { it[PreferencesKeys.TUTORIAL_SUB_MAP] ?: false }

    val isPinTutorialDismissed: Flow<Boolean> = context.dataStore.data
        .map { it[PreferencesKeys.TUTORIAL_PIN] ?: false }

    // --- FUNCIONES DE ESCRITURA ---
    suspend fun dismissMainMapTutorial() {
        context.dataStore.edit { it[PreferencesKeys.TUTORIAL_MAIN_MAP] = true }
    }

    suspend fun dismissSubMapTutorial() {
        context.dataStore.edit { it[PreferencesKeys.TUTORIAL_SUB_MAP] = true }
    }

    suspend fun dismissPinTutorial() {
        context.dataStore.edit { it[PreferencesKeys.TUTORIAL_PIN] = true }
    }

    companion object {
        @Volatile private var INSTANCE: UserPreferencesRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) { INSTANCE = UserPreferencesRepository(context.applicationContext) }
            }
        }
        val instance: UserPreferencesRepository
            get() = INSTANCE ?: throw IllegalStateException("UserPreferencesRepository no inicializado.")
    }
}