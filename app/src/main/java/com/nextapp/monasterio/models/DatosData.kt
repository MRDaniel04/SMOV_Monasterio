package com.nextapp.monasterio.models

data class DatosData(
    val id: String = "",
    val diasVisitas : List<String> = emptyList(),
    val email : String = "",
    val horasVisitas : List<String> = emptyList(),
    val telefono : String = "",
    val ubicacion: String = ""
)