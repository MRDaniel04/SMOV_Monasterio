package com.nextapp.monasterio.models

/**
 * Modelo de datos para la información general del monasterio
 */
data class InfoModel(
    val id: String = "general",
    val mainContent: Map<String, String> = emptyMap(),
    val location: String = "",
    val hours: Map<String, String> = emptyMap(),
    val email: String = "",
    val phone: String = ""
)
