package com.nextapp.monasterio.ui.virtualvisit.data

import android.graphics.Matrix
import com.nextapp.monasterio.R
import com.nextapp.monasterio.ui.virtualvisit.utils.createPathFromPoints
import com.nextapp.monasterio.ui.virtualvisit.utils.mergePaths

object PlanoData {

    // 🔹 Lista de figuras interactivas
    val figuras: List<FiguraData> by lazy {
        listOf(
            FiguraData(
                id = "monasterio",
                nombre = "Monasterio",
                path = createTransformedPath(
                    coords = listOf(
                        0.1865f to 0.4054f, 0.2725f to 0.3180f, 0.2608f to 0.3005f,
                        0.2793f to 0.2825f, 0.2545f to 0.2451f, 0.2324f to 0.2656f,
                        0.1400f to 0.1222f, 0.1445f to 0.1215f, 0.1070f to 0.0600f,
                        0.0842f to 0.0795f, 0.0652f to 0.0500f, 0.0182f to 0.1000f,
                        0.0212f to 0.1000f, 0.0011f to 0.1200f, 0.0165f to 0.1464f,
                        0.0164f to 0.1500f, 0.0529f to 0.2040f
                    ),
                    offsetX = 0.164f,
                    offsetY = 0.16f,
                    scale = 1.0f
                ),
                colorResaltado = 0x80FFFF00.toInt(), // amarillo semitransparente
                destino = "detalle_monasterio"
            ),
            FiguraData(
                id = "iglesia",
                nombre = "Iglesia",
                path = createTransformedPath(
                    coords = listOf(
                        0.01f to 0.885f, 0.157f to 0.97f, 0.16f to 0.94f, 0.25f to 1.01f,
                        0.4f to 1.04f, 0.45f to 0.89f, 0.69f to 1.003f, 0.78f to 0.89f,
                        0.84f to 0.69f, 0.71f to 0.60f, 0.74f to 0.53f, 0.84f to 0.182f,
                        0.70f to 0.1f, 0.74f to 0.0f, 0.5945f to -0.08f, 0.44f to -0.15f,
                        0.4f to -0.03f, 0.3f to -0.08f, 0.18f to 0.3f, 0.28f to 0.37f,
                        0.17f to 0.75f, 0.08f to 0.68f, 0.0637f to 0.7360f, 0.0480f to 0.7311f
                    ),
                    offsetX = 0.2f,
                    offsetY = 0.63f,
                    scale = 0.2f
                ),
                colorResaltado = 0x80FFFF00.toInt(),
                destino = "detalle_iglesia"
            ),

            // 🔹 NUEVA FIGURA: COLEGIO DEL MONASTERIO
            FiguraData(
                id = "colegio",
                nombre = "Colegio",
                path = createTransformedPath(
                    coords = listOf(
                        0.03f to 0.81f, // Pt1
                        0.15f to 1.01f, // Pt2
                        0.26f to 0.95f, // Pt3
                        0.34f to 0.85f, // PERFECT
                        0.598f to 0.588f,
                        0.87f to 0.32f,
                        0.785f to 0.22f,
                        0.7f to 0.1f,
                        0.65f to 0.02f,
                        0.68f to -0.025f,
                        0.235f to -0.59f,
                        0.08f to -0.425f,
                        0.12f to -0.36f,
                        0.16f to -0.3f,
                        0.215f to -0.36f,
                        0.43f to -0.08f,
                        0.405f to -0.05f,
                        0.43f to -0.01f,
                        0.385f to 0.045f,
                        0.445f to 0.14f,
                        0.49f to 0.21f,
                        0.42f to 0.28f,
                        0.365f to 0.21f,
                        0.059f to 0.525f,
                        0.105f to 0.595f,
                        0.16f to 0.68f,
                        0.06f to 0.78f
                    ),
                    offsetX = 0.56f,  // estimado: ajustar según posición en el plano
                    offsetY = 0.38f,  // estimado: ajustar visualmente
                    scale = 0.3f     // proporcional al tamaño 250x384 sobre plano
                ),
                colorResaltado = 0x804CAF50.toInt(), // verde semitransparente
                destino = "detalle_colegio"
            ),

            FiguraData(
                id = "arco_mudejar",
                nombre = "Arco Mudéjar",
                path = mergePaths(
                    createTransformedPath(
                        coords = listOf(
                            0.53f to 1.15f,
                            0.9f to 0.68f,
                            0.47f to 0.05f,
                            0.08f to 0.53f,
                            0.53f to 1.15f,
                        ),
                        offsetX = 0.37f,
                        offsetY = 0.65f,
                        scale = 0.06f
                    ),
                    createTransformedPath(
                        coords = listOf(
                            0.52f to 0.98f,
                            0.75f to 0.7f,
                            0.47f to 0.23f,
                            0.19f to 0.54f,
                            0.52f to 0.98f
                        ),
                        offsetX = 0.37f,
                        offsetY = 0.65f,
                        scale = 0.06f
                    )
                ),
                colorResaltado = 0x809C27B0.toInt(),
                destino = "detalle_arco_mudejar"
            ),

                    // 🔹 FIGURA: CLAUSTRO
            FiguraData(
                id = "claustro",
                nombre = "Claustro",
                path = createTransformedPath(
                    coords = listOf(
                        0.000000f to 0.457f,  // V1
                        0.43f to 1.08f,  // V2
                        0.945f to 0.565f,  // V3
                        0.53f to -0.072f   // V4
                    ),
                    // Estos valores son una estimación inicial — ajústalos visualmente si hace falta:
                    offsetX = 0.513f,   // posición X estimada en el plano (ajustar si no queda bien)
                    offsetY = 0.335f,   // posición Y estimada en el plano (ajustar si no queda bien)
                    scale = 0.15f       // escala estimada (ajustar para que el polygon se visualice con el tamaño correcto)
                ),
                colorResaltado = 0x80FFEB3B.toInt(), // amarillo/ámbar semitransparente (puedes cambiar)
                destino = "detalle_claustro"
            )


        )
    }

    // 🔹 Lista de pines
    val pines: List<PinData> = listOf(
        PinData("pin1", 0.40f, 0.65f, 0.03f, R.drawable.pin3, "detalle_pin"),
        PinData("pin2", 0.70f, 0.45f, 0.05f, R.drawable.pin3, "detalle_pin2")
    )

    // 🔹 Utilidad interna para crear paths transformados
    private fun createTransformedPath(coords: List<Pair<Float, Float>>, offsetX: Float, offsetY: Float, scale: Float) =
        createPathFromPoints(coords).apply {
            val matrix = Matrix()
            matrix.setScale(scale, scale)
            matrix.postTranslate(offsetX, offsetY)
            transform(matrix)
        }
}
