package com.nextapp.monasterio.ui.virtualvisit.data

/**
 * 🔹 Representa un pin interactivo sobre el plano
 */
data class PinData1(
    val id: String,
    val x: Float,
    val y: Float,
    val tapRadius: Float,
    val iconRes: Int,
    val destino: String // ruta a navegar
)
