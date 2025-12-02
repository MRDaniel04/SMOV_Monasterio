package com.nextapp.monasterio.models

/**
 * Representa las posibles ubicaciones físicas dentro del conjunto monástico.
 *
 * Este enum nos sirve para saber en qué parte se encuentra cada punto de interés.
 * También se puede usar para filtrar o agrupar los pines.
 */
enum class Ubicacion(val displayName: String) {
    IGLESIA("Iglesia"),
    MONASTERIO("Monasterio"),
    CLAUSTRO("Claustro"),
    COLEGIO("Colegio"),
    MUSEO("Museo"),
    EXTERIOR("Curecero"),
    OTRO("Otro");

    companion object { // 👈 INICIO DEL BLOQUE A AÑADIR
        /**
         * Busca un valor de Ubicacion a partir de su nombre de visualización.
         * Devuelve el objeto Enum o null si no se encuentra.
         */
        fun fromDisplayName(name: String): Ubicacion? =
            // 'entries' es una forma concisa de obtener todos los valores del enum en Kotlin 1.9+
            entries.find { it.displayName == name }
    }
}
