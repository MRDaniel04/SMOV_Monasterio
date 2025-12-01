package com.nextapp.monasterio.models

import com.google.firebase.firestore.DocumentReference

data class FiguraData(
    var id: String = "",
    var nombre: String = "",
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var scale: Float = 1f,
    var colorResaltado: Long = 0,
    var tipoDestino: String = "",
    var valorDestino: String = "",
    var path: List<Point> = emptyList(), //Nuevos campos para DetalleFiguraScreen
    var info_es: String = "",
    var info_en: String = "",
    var info_de: String = "",
    var info_fr: String = "",
    var imagenes: List<DocumentReference> = emptyList() // Array de referencias a documentos en la colección 'imagenes'

) {
    data class Point(
        val x: Float = 0f,
        val y: Float = 0f
    )
}