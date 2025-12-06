package com.nextapp.monasterio.models

data class PinData(
    val id: String,

    // 🆕 UBICACIÓN (Compleja)
    val ubicacion_es: String? = null, // Antes 'ubicacion'
    val ubicacion_en: String? = null, // Antes 'ubicacionIngles'
    val ubicacion_de: String? = null, // Antes 'ubicacionAleman'
    val ubicacion_fr: String? = null, // Antes 'ubicacionFrances'

    // 🆕 ÁREA (Simple)
    val area_es: String? = null, // Antes 'area'
    val area_en: String? = null, // Antes 'areaIngles'
    val area_de: String? = null, // Antes 'areaAleman'
    val area_fr: String? = null, // Antes 'areaFrances'

    val x: Float,
    val y: Float,
    val iconRes: Int? = null,
    val imagenes: List<String> = emptyList(),
    val imagenesDetalladas: List<ImagenData> = emptyList(),

    // 🆕 DESCRIPCIÓN
    val descripcion_es: String? = null, // Antes 'descripcion'
    val descripcion_en: String? = null, // Antes 'descripcionIngles'
    val descripcion_de: String? = null, // Antes 'descripcionAleman'
    val descripcion_fr: String?=null, // Antes 'descripcionFrances'

    // 🔹 Nuevo: destino dinámico leído de Firebase
    val tipoDestino: String? = null,
    val valorDestino: String? = null,
    val destino: DestinoPin = DestinoPin.Detalle("pin_detalle"),

    val tapRadius: Float = 0.06f,
    val vista360Url: String? = null,

    // Estos ya usan el sufijo, se mantienen igual
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