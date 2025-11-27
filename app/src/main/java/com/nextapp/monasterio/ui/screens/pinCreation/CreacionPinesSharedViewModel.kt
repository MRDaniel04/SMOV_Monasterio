package com.nextapp.monasterio.ui.screens.pinCreation

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.nextapp.monasterio.ui.screens.pinCreation.state.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


class CreacionPinSharedViewModel : ViewModel() {

        val titulo = TituloState()
        val descripcion = DescripcionState()
        val imagenes = ImagenesState()
        var imagen360 by mutableStateOf<Uri?>(null)
        val ubicacion = UbicacionState()

        var modoMoverPin: Boolean = false
        var formSubmitted: Boolean = false
        var coordenadasFinales: Pair<Float, Float>? = null

        fun reset() {
            titulo.es = ""; titulo.en = ""; titulo.de = ""
            descripcion.es = ""; descripcion.en = ""; descripcion.de = ""

            imagenes.uris = emptyList()
            imagen360 = null

            ubicacion.displayName = ""
            modoMoverPin = false
            coordenadasFinales = null
            formSubmitted = false
        }
    }


