package com.nextapp.monasterio.models


/**
 * Representa las opciones predefinidas para el desplegable de Ubicación Detallada.
 */
enum class UbicacionDetalladaTag(val displayName: String) {
    CRUCERO("Crucero"),
    LADO_EPISTOLA("Lado de la epistola"),
    TRASCORO("Trascoro"),
    CORO("Coro"),
    CAPILLA_NACIMIENTO("Capilla del nacimiento"),
    OTRA("Otra");

    companion object {
        fun fromDisplayName(name: String): UbicacionDetalladaTag? =
            entries.find { it.displayName == name }
    }
}