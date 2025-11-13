package com.nextapp.monasterio.models

import androidx.compose.ui.graphics.Color

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
    val iconRes: Int? = null,
    val imagenes: List<String> = emptyList(), // referencias a las imágenes
    val imagenesDetalladas: List<ImagenData> = emptyList(), // 🔥 cargadas desde Firebase
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
