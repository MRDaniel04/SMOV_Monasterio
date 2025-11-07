package com.nextapp.monasterio.models

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.nextapp.monasterio.R

/**
 * Clasificación visual de los temas de puntos de interés.
 * Define el color del pin usado en el mapa y en la pantalla de detalle.
 */
enum class Tema(
    val displayName: String,
    val color: Color,

) {
    ESPACIOS_ARQUITECTONICOS(
        "Espacios arquitectónicos",
        Color(0xFFCBB294)
    ),
    ESCULTURA_Y_ARTE_FUNERARIO(
        "Escultura y arte funerario",
        Color(0xFF6C91BF)
    ),
    PINTURA_Y_ARTE_VISUAL(
        "Pintura y arte visual",
        Color(0xFF9EB384)
    ),
    RELIQUIAS_Y_OBJETOS_HISTORICOS(
        "Reliquias y objetos históricos",
        Color(0xFFE6A157),
    ),
    VIDA_MONASTICA_Y_TRADICION(
        "Vida monástica y tradición",
        Color(0xFFF6D743)
    )
}
