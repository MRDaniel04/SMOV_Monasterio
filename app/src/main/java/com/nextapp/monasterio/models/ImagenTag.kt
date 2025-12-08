package com.nextapp.monasterio.models

import androidx.annotation.StringRes
import com.nextapp.monasterio.R

enum class ImageTag(
    val firestoreValue: String,
    val displayName: String,
    @StringRes val stringResId: Int // 👈 NUEVA PROPIEDAD
) {
    // Usamos los strings que nos has proporcionado
    PINTURA("pintura", "Pintura", R.string.tag_pintura),
    ESCULTURA("escultura", "Escultura", R.string.tag_escultura),
    ARQUITECTURA("arquitectura", "Arquitectura", R.string.tag_arquitectura),
    OTRO("otro", "Otro", R.string.tag_otro); // Cambié displayName a "Arquitectura" para consistencia, pero el stringResId es el importante.


    fun toFirestoreString(): String = firestoreValue


    companion object {
        fun fromFirestoreString(value: String?): ImageTag? {


            if (value.isNullOrBlank()) {
                return OTRO
            }

            val foundTag = entries.find {
                it.firestoreValue.lowercase() == value.lowercase()
            }
            return foundTag ?: OTRO
        }
    }

}