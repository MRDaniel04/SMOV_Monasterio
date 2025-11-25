package com.nextapp.monasterio.models

/**
 * Modelo de datos para un período histórico
 */
data class HistoriaPeriod(
    val id: String = "",
    val title: String = "",
    val content: Map<String, String> = emptyMap(),
    val imageUrls: List<String> = emptyList(),
    val order: Int = 0
)
