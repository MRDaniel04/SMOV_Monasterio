package com.nextapp.monasterio.models

enum class ImageTag(val firestoreValue: String, val displayName: String) {
    // AÑADIDO: displayName
    PINTURA("Pintura", "Pintura"),
    ESCULTURA("Escultura", "Escultura"),
    ARQUITECTURA("Arquitectura", "Arquitec."),
    OTRO("Otro", "Otro");


    fun toFirestoreString(): String = firestoreValue

    companion object {
        fun fromFirestoreString(value: String?): ImageTag? =
            entries.find { it.firestoreValue == value }
    }
}