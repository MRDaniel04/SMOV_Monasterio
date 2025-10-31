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


    object iglesia {
        // 🔹 Ajustes de posición y escala del path sobre el plano general
        private val OFFSET_X = 0.2f
        private val OFFSET_Y = 0.63f
        private val SCALE_FACTOR = 0.2f

        // 🔹 Coordenadas normalizadas (0–1) del contorno de la iglesia
        //    Estas coordenadas provienen de la figura con dimensiones 170x190
        //    y se han normalizado tomando como punto de origen (3988.33, 13747.94)
        private val coords_iglesia = listOf(
            0.01f to 0.885f, //Pt1
            0.157f to 0.97f, //Pt2
            0.16f to 0.94f,
            0.25f to 1.01f,
            0.4f to 1.04f,
            0.45f to 0.89f,
            0.69f to 1.003f,
            0.78f to 0.89f,
            0.84f to 0.69f,
            0.71f to 0.60f,
            0.74f to 0.53f,
            0.84f to 0.182f,
            0.70f to 0.1f,
            0.74f to 0.0f,
            0.5945f to -0.08f,
            0.44f to -0.15f,
            0.4f to -0.03f,
            0.3f to -0.08f,
            0.18f to 0.3f,
            0.28f to 0.37f,
            0.17f to 0.75f,
            0.08f to 0.68f,
            0.0637f to 0.7360f,
            0.0480f to 0.7311f
        )

        // 🔹 Path del área interactiva (contorno de la iglesia)
        val path: Path by lazy {
            val p = createPathFromPoints(coords_iglesia)
            val matrix = Matrix()
            matrix.setScale(SCALE_FACTOR, SCALE_FACTOR)
            matrix.postTranslate(OFFSET_X, OFFSET_Y)
            p.transform(matrix)
            p
        }

        // 🔹 Pin principal asociado a esta área (si aplica)
        val pinX = 0.40f
        val pinY = 0.65f
        val pinTapRadiusNormalized = 0.03f
    }

}
