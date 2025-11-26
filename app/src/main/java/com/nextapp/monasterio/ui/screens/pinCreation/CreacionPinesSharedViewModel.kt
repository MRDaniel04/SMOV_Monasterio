package com.nextapp.monasterio.ui.screens.pinCreation

import androidx.lifecycle.ViewModel
import com.nextapp.monasterio.ui.screens.pinCreation.state.*

class CreacionPinSharedViewModel : ViewModel() {

    // Estados del formulario
    val titulo = TituloState()
    val descripcion = DescripcionState()
    val imagenes = ImagenesState()
    val imagenes360 = ImagenesState()
    val ubicacion = UbicacionState()

    // Indicador: ¿estamos moviendo un pin nuevo?
    var modoMoverPin: Boolean = false

    // 🔥 Se usa para decirle a EdicionPines que volvemos del formulario
    var formSubmitted: Boolean = false

    // Resultado de la fase mover pin
    var coordenadasFinales: Pair<Float, Float>? = null

    // Reset completo (solo al terminar o cancelar definitivamente)
    fun reset() {
        titulo.es = ""; titulo.en = ""; titulo.de = ""
        descripcion.es = ""; descripcion.en = ""; descripcion.de = ""
        imagenes.uris = emptyList()
        imagenes360.uris = emptyList()
        ubicacion.displayName = ""
        modoMoverPin = false
        coordenadasFinales = null
        formSubmitted = false   // <-- IMPORTANTE
    }
}
