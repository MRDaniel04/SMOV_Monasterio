package com.nextapp.monasterio.models

data class PinData(
    val id: String,

    val ubicacion_es: String? = null,
    val ubicacion_en: String? = null,
    val ubicacion_de: String? = null,
    val ubicacion_fr: String? = null,

    val area_es: String? = null,
    val area_en: String? = null,
    val area_de: String? = null,
    val area_fr: String? = null,

    val x: Float,
    val y: Float,
    val iconRes: Int? = null,
    val imagenes: List<String> = emptyList(),
    val imagenesDetalladas: List<ImagenData> = emptyList(),

    val descripcion_es: String? = null,
    val descripcion_en: String? = null,
    val descripcion_de: String? = null,
    val descripcion_fr: String?=null,

    val tipoDestino: String? = null,
    val valorDestino: String? = null,
    val destino: DestinoPin = DestinoPin.Detalle("pin_detalle"),

    val tapRadius: Float = 0.06f,
    val vista360Url: String? = null,

    val audioUrl_es: String? = null,
    val audioUrl_en: String? = null,
    val audioUrl_de: String? = null,
    val audioUrl_fr: String? = null
)

sealed class DestinoPin {
    data class Detalle(val idDetalle: String) : DestinoPin()
    data class Ruta(val nombreRuta: String) : DestinoPin()
    data class Popup(val mensaje: String) : DestinoPin()
}