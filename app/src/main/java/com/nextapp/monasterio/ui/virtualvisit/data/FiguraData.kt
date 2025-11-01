package com.nextapp.monasterio.ui.virtualvisit.data

import android.graphics.Path

/**
 * 🔹 Representa una figura interactiva del plano (ej: iglesia, claustro, etc.)
 */
data class FiguraData(
    val id: String,
    val nombre: String,
    val path: Path,
    val colorResaltado: Int,
    val destino: String // ruta a la pantalla de detalle
)
