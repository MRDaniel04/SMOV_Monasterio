package com.nextapp.monasterio.ui.virtualvisit.data

/**
 * 🔹 Define la información base del plano del monasterio.
 *
 * Contiene:
 *   - Coordenadas normalizadas (0–1) para construir el Path del monasterio.
 *   - Transformaciones de posición y escala del Path.
 *   - Posición y radio del pin principal.
 *
 * Este archivo puede ampliarse con más planos (ej: iglesia, claustro, jardines...).
 *
 */



import android.graphics.Matrix
import android.graphics.Path
import com.nextapp.monasterio.ui.virtualvisit.utils.createPathFromPoints

object PlanoData {

    object monasterio {
        private val OFFSET_X = 0.164f
        private val OFFSET_Y = 0.16f
        private val SCALE_FACTOR = 1.0f

        // Coordenadas normalizadas (0–1)
        private val coords = listOf(
            0.1865f to 0.4054f,
            0.2725f to 0.3180f,
            0.2608f to 0.3005f,
            0.2793f to 0.2825f,
            0.2545f to 0.2451f,
            0.2324f to 0.2656f,
            0.1400f to 0.1222f,
            0.1445f to 0.1215f,
            0.1070f to 0.0600f,
            0.0842f to 0.0795f,
            0.0652f to 0.0500f,
            0.0182f to 0.1000f,
            0.0212f to 0.1000f,
            0.0011f to 0.1200f,
            0.0165f to 0.1464f,
            0.0164f to 0.1500f,
            0.0529f to 0.2040f
        )

        // Path del monasterio
        val path: Path by lazy {
            val p = createPathFromPoints(coords)
            val matrix = Matrix()
            matrix.setScale(SCALE_FACTOR, SCALE_FACTOR)
            matrix.postTranslate(OFFSET_X, OFFSET_Y)
            p.transform(matrix)
            p
        }

        // Pin principal
        val pinX = 0.40f
        val pinY = 0.65f
        val pinTapRadiusNormalized = 0.03f
    }
}
