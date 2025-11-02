package com.nextapp.monasterio.utils

import android.content.Context
import android.content.SharedPreferences

object FontSize{
    private const val PREFERENCES_FILE = "font_size_prefs"
    private const val KEY_FONT_SCALE = "font_scale"

    fun guardarFontScale(context: Context, scale: Float){
        val prefs: SharedPreferences = context.getSharedPreferences(PREFERENCES_FILE,Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_FONT_SCALE,scale).apply()
    }

    fun devolverFontScale(context: Context): Float{
        val prefs: SharedPreferences = context.getSharedPreferences(PREFERENCES_FILE,Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_FONT_SCALE,1.0f)
    }
}