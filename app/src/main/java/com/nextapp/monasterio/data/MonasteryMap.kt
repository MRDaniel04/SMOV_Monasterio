package com.nextapp.monasterio.data

import com.nextapp.monasterio.R

/**
 * Define la estructura de un Hotspot (una flecha de navegación).
 */
data class Hotspot(
    val id: String,         // ID único para esta flecha (ej: "claustro_a_capilla")
    val pitch: Float,       // Ángulo vertical (arriba/abajo) donde aparece
    val yaw: Float,         // Ángulo horizontal (izq/der) donde aparece
    val targetVistaId: String // El ID de la vista a la que navega (ej: "capilla_mayor")
)

/**
 * Define la estructura de una Vista Panorámica individual.
 */
data class PanoramaVista(
    val id: String,                 // ID único (ej: "monastery_1")
    val nombre: String,             // Nombre para el botón (ej: "Entrada Principal")
    val imagenResId: Int,           // El recurso en R.raw (ej: R.raw.monastery_1)
    val hotspots: List<Hotspot>     // Lista de flechas/hotspots en esta vista
)

/**
 * Repositorio central que contiene el "mapa" de todas las vistas del monasterio.
 *
 * ¡IMPORTANTE!
 * Todas las imágenes (monastery_1.jpg, entrada.jpg, etc.) deben estar copiadas
 * en tu carpeta `app/src/main/res/raw`
 */
object MonasterioMapRepository {

    // Lista completa de las 32 vistas panorámicas
    private val vistas = listOf(
        PanoramaVista(
            id = "monastery_1",
            nombre = "Monastery 1", // <-- Cambia este nombre por el real
            imagenResId = R.raw.monastery_1,
            hotspots = emptyList() // <-- Añadiremos los hotspots después
        ),
        PanoramaVista(
            id = "monastery_2",
            nombre = "Monastery 2",
            imagenResId = R.raw.monastery_2,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_3",
            nombre = "Monastery 3",
            imagenResId = R.raw.monastery_3,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_4",
            nombre = "Monastery 4",
            imagenResId = R.raw.monastery_4,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_5",
            nombre = "Monastery 5",
            imagenResId = R.raw.monastery_5,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_6",
            nombre = "Monastery 6",
            imagenResId = R.raw.monastery_6,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_7",
            nombre = "Monastery 7",
            imagenResId = R.raw.monastery_7,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_8",
            nombre = "Monastery 8",
            imagenResId = R.raw.monastery_8,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_9",
            nombre = "Monastery 9",
            imagenResId = R.raw.monastery_9,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_10",
            nombre = "Monastery 10",
            imagenResId = R.raw.monastery_10,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_11",
            nombre = "Monastery 11",
            imagenResId = R.raw.monastery_11,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_12",
            nombre = "Monastery 12",
            imagenResId = R.raw.monastery_12,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_13",
            nombre = "Monastery 13",
            imagenResId = R.raw.monastery_13,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_14",
            nombre = "Monastery 14",
            imagenResId = R.raw.monastery_14,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_15",
            nombre = "Monastery 15",
            imagenResId = R.raw.monastery_15,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_16",
            nombre = "Monastery 16",
            imagenResId = R.raw.monastery_16,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_17",
            nombre = "Monastery 17",
            imagenResId = R.raw.monastery_17,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_18",
            nombre = "Monastery 18",
            imagenResId = R.raw.monastery_18,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_19",
            nombre = "Monastery 19",
            imagenResId = R.raw.monastery_19,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_20",
            nombre = "Monastery 20",
            imagenResId = R.raw.monastery_20,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_21",
            nombre = "Monastery 21",
            imagenResId = R.raw.monastery_21,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_22",
            nombre = "Monastery 22",
            imagenResId = R.raw.monastery_22,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_23",
            nombre = "Monastery 23",
            imagenResId = R.raw.monastery_23,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_24",
            nombre = "Monastery 24",
            imagenResId = R.raw.monastery_24,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_25",
            nombre = "Monastery 25",
            imagenResId = R.raw.monastery_25,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_26",
            nombre = "Monastery 26",
            imagenResId = R.raw.monastery_26,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_27",
            nombre = "Monastery 27",
            imagenResId = R.raw.monastery_27,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_28",
            nombre = "Monastery 28",
            imagenResId = R.raw.monastery_28,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "monastery_29",
            nombre = "Monastery 29",
            imagenResId = R.raw.monastery_29,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "entrada",
            nombre = "Entrada",
            imagenResId = R.raw.entrada,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "entrada_2",
            nombre = "Entrada 2",
            imagenResId = R.raw.entrada_2,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "entrada_3",
            nombre = "Entrada 3",
            imagenResId = R.raw.entrada_3,
            hotspots = emptyList()
        ),
        PanoramaVista(
            id = "entrada_4",
            nombre = "Entrada 4",
            imagenResId = R.raw.entrada_4,
            hotspots = emptyList()
        )
    )

    /**
     * Devuelve la lista completa de todas las vistas.
     * (Lo usará GaleriaScreen para mostrar todos los botones).
     */
    fun getVistas(): List<PanoramaVista> = vistas

    /**
     * Busca y devuelve una vista específica por su ID.
     * (Lo usará PanoramaScreen para saber qué cargar).
     */
    fun getVistaPorId(id: String): PanoramaVista? = vistas.find { it.id == id }
}