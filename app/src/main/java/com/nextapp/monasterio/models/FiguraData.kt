package com.nextapp.monasterio.models

data class FiguraData(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    // Cambiamos a List<String> para guardar solo los IDs
    val imagenes: List<String> = emptyList(),

    // Textos multilenguaje
    val info_es: String = "",
    val info_en: String = "",
    val info_de: String = "",
    val info_fr: String = "",

    // Geometría
    val path: List<Punto> = emptyList(),
    val escala: Float = 1f,
    val colorResaltado: Long = 0xFFFFFFFF,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,

    // Navegación
    val tipoDestino: String = "",
    val valorDestino: String = "",

    // Audio
    val audioUrl_es: String? = null,
    val audioUrl_en: String? = null,
    val audioUrl_de: String? = null,
    val audioUrl_fr: String? = null,

    // 👇👇 AÑADE ESTO PARA ARREGLAR EL ERROR "Unresolved reference" 👇👇
    val vista360Url: String? = null
)

data class Punto(
    val x: Float = 0f,
    val y: Float = 0f
)