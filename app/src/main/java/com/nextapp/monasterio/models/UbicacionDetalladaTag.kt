package com.nextapp.monasterio.models

import androidx.annotation.StringRes
import com.nextapp.monasterio.R


/**
 * Representa las opciones predefinidas para el desplegable de Ubicación Detallada.
 */
enum class UbicacionDetalladaTag(val displayName: String, @StringRes val stringResId: Int) { // 👈 AÑADIMOS stringResId
    CRUCERO("Crucero", R.string.loc_crucero),
    LADO_EPISTOLA("Lado de la epistola", R.string.loc_lado_epistola),
    TRASCORO("Trascoro", R.string.loc_trascoro),
    CORO("Coro", R.string.loc_coro),
    CAPILLA_NACIMIENTO("Capilla del nacimiento", R.string.loc_capilla_nacimiento),
    OTRA("Otra", R.string.loc_other);

    companion object {
        fun fromDisplayName(name: String): UbicacionDetalladaTag? =
            entries.find { it.displayName == name }
    }
}