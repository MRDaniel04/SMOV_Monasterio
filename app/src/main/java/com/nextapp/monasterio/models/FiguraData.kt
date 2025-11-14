package com.nextapp.monasterio.models

data class FiguraData(
    var id: String = "",
    var nombre: String = "",
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var scale: Float = 1f,
    var colorResaltado: Long = 0,
    var tipoDestino: String = "",
    var valorDestino: String = "",
    var path: List<Point> = emptyList()
) {
    data class Point(
        val x: Float = 0f,
        val y: Float = 0f
    )
}