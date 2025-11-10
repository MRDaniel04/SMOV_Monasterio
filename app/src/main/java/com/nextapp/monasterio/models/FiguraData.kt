package com.nextapp.monasterio.models

data class FiguraData(
    val id: String = "",
    val nombre: String = "",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val colorResaltado: Int = 0,
    val tipoDestino: String = "",
    val valorDestino: String = "",
    val path: List<Point> = emptyList()
) {
    data class Point(
        val x: Float = 0f,
        val y: Float = 0f
    )
}