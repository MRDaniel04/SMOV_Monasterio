package com.nextapp.monasterio.models

import androidx.compose.ui.graphics.Color

/**
 * Representa un punto de interés individual mostrado como un "pin" sobre un plano.
 * Ahora admite URLs de imágenes (Cloudinary).
 */
data class PinData(
    val id: String,
    val titulo: String,
    val tituloIngles: String,
    val tituloAleman: String,
    val ubicacion: Ubicacion? = null,
    val ubicacionIngles: Ubicacion? = null,
    val ubicacionAleman: Ubicacion? = null,
    val x: Float,
    val y: Float,
    val tema: Tema,
    val color: Color? = null,
    val iconRes: Int? = null, // si algún día quieres usar íconos locales
    val imagenes: List<String> = emptyList(), // ✅ URLs de Cloudinary
    val descripcion: String? = null,
    val descripcionIngles: String? = null,
    val descripcionAleman: String? = null,
    val destino: DestinoPin = DestinoPin.Detalle("pin_detalle"),
    val tapRadius: Float = 0.04f,
    val vista360Url: String? = null
)

sealed class DestinoPin {
    data class Detalle(val idDetalle: String) : DestinoPin()
    data class NavegarPlano(val planoId: String) : DestinoPin()
    data class Popup(val mensaje: String) : DestinoPin()
}
