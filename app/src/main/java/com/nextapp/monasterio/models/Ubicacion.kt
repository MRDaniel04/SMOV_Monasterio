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
    EXTERIOR("Exterior"),
    OTRO("Otro")
}
