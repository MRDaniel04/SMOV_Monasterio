package com.nextapp.monasterio.models

/**
 * Modelo de datos para la información general del monasterio
 */
data class InfoModel(
    val id: String = "general",
    val mainContent: Map<String, String> = emptyMap(),
    val location: Map<String, String> = emptyMap(),
    val hours: Map<String, String> = emptyMap()
)
