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

        val INSTRUCTIONS_PUZZLE = booleanPreferencesKey("ins_puzzle") // Instrucciones del Puzle
        val INSTRUCTIONS_PAIRS = booleanPreferencesKey("ins_pairs") // Instrucciones de las parejas
        val INSTRUCTIONS_DIFFERENCES = booleanPreferencesKey("ins_differences") // Instrucciones de las parejas
    }

    // --- FLUJOS DE LECTURA ---
    val isMainMapTutorialDismissed: Flow<Boolean> = context.dataStore.data
        .map { it[PreferencesKeys.TUTORIAL_MAIN_MAP] ?: false }

    val isSubMapTutorialDismissed: Flow<Boolean> = context.dataStore.data
        .map { it[PreferencesKeys.TUTORIAL_SUB_MAP] ?: false }

    val isPinTutorialDismissed: Flow<Boolean> = context.dataStore.data
        .map { it[PreferencesKeys.TUTORIAL_PIN] ?: false }

    val isInstructionsPuzzleDismissed: Flow<Boolean> = context.dataStore.data
        .map { it[PreferencesKeys.INSTRUCTIONS_PUZZLE] ?: false }

    val isInstructionsPairsDismissed: Flow<Boolean> = context.dataStore.data
        .map { it[PreferencesKeys.INSTRUCTIONS_PAIRS] ?: false }

    val isInstructionsDifferencesDismissed: Flow<Boolean> = context.dataStore.data
        .map { it[PreferencesKeys.INSTRUCTIONS_DIFFERENCES] ?: false }

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

    suspend fun dismissInstructionsPuzzle() {
        context.dataStore.edit { it[PreferencesKeys.INSTRUCTIONS_PUZZLE] = true }
    }

    suspend fun dismissInstructionsPairs() {
        context.dataStore.edit { it[PreferencesKeys.INSTRUCTIONS_PAIRS] = true }
    }

    suspend fun dismissInstructionsDifferences() {
        context.dataStore.edit { it[PreferencesKeys.INSTRUCTIONS_DIFFERENCES] = true }
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