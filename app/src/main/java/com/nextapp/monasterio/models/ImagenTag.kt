package com.nextapp.monasterio.models

enum class ImageTag(val firestoreValue: String, val displayName: String) {
    // AÑADIDO: displayName
    PINTURA("Pintura", "Pintura"),
    ESCULTURA("Escultura", "Escultura"),
    ARQUITECTURA("Arquitectura", "Arquitec."),
    OTRO("Otro", "Otro");


    fun toFirestoreString(): String = firestoreValue

    companion object {
        fun fromFirestoreString(value: String?): ImageTag? {

            // 1. MANEJA NULO/VACÍO: Si no hay valor en DB, asigna "Otro" para que la imagen se cargue.
            if (value.isNullOrBlank()) {
                return OTRO
            }

            return entries.find { it.firestoreValue.equals(value, ignoreCase = true) }
        }
    }
}