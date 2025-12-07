package com.nextapp.monasterio.models

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nextapp.monasterio.R

data class Diferencia(
    val id: Int,
    val rectX: Int,
    val rectY: Int,
    val width: Int,
    val height: Int,
    private var _encontrada: Boolean = false
){
    var encontrada: Boolean by mutableStateOf(_encontrada)

    fun tocoDiferencia(touchX: Float, touchY: Float): Boolean {
        return touchX >= rectX &&
                touchX <= rectX + width &&
                touchY >= rectY &&
                touchY <= rectY + height
    }
}

data class Pista(
    val idDiferencia : Int,
    val idRecurso : Int,
)


data class NivelJuego(
    val id: Int,
    @DrawableRes val imagenOriginal : Int,
    @DrawableRes val imagenModificada : Int,
    val diferencias : List<Diferencia>,
    val pistas : List<Pista>
)

fun obtenerPares() : List<NivelJuego>{
    var indiceBase=0
    return listOf(
        NivelJuego(
            id=1,
            imagenOriginal = R.drawable.d1,
            imagenModificada = R.drawable.d1m,
            diferencias = listOf(
                Diferencia(101, 234, 136, 80, 80), // Pájaro
                Diferencia(102, 488, 183, 236, 155),  // Bandera
                Diferencia(103, 533, 657, 100, 100),  // Leon en el caballo
                Diferencia(104, 313, 570, 80, 80),// No leon en la pechera
                Diferencia(105, 199, 626, 206, 190),  // Espada del caballero
            ),
            pistas=listOf(
                Pista(idDiferencia = 101,indiceBase++),
                Pista(idDiferencia = 102,indiceBase++),
                Pista(idDiferencia = 103,indiceBase++),
                Pista(idDiferencia = 104,indiceBase++),
                Pista(idDiferencia = 105,indiceBase++),
            )
        ),
        NivelJuego(
            id=2,
            imagenOriginal = R.drawable.d2,
            imagenModificada = R.drawable.d2m,
            diferencias = listOf(
                Diferencia(201, 311, 366, 50, 70), // Crucifijo del cura
                Diferencia(202, 513, 328, 100, 60), // Collar de la reina
                Diferencia(203, 1034, 406, 125, 150), // Instrumento
                Diferencia(204, 706, 196, 100, 60), // Corona del rey
                Diferencia(205, 678, 425, 150, 80),  // Cinturon del rey
            ),
            pistas=listOf(
                Pista(idDiferencia = 201,indiceBase++),
                Pista(idDiferencia = 202,indiceBase++),
                Pista(idDiferencia = 203,indiceBase++),
                Pista(idDiferencia = 204,indiceBase++),
                Pista(idDiferencia = 205,indiceBase++),
            )
        ),
        NivelJuego(
            id=3,
            imagenOriginal = R.drawable.d3,
            imagenModificada = R.drawable.d3m,
            diferencias = listOf(
                Diferencia(301, 331, 0, 350, 230), // Lampara
                Diferencia(302, 853, 799, 155, 110), // Gato en la cesta
                Diferencia(303, 842, 59, 140, 100), // Gato arriba
                Diferencia(304, 381, 939, 250, 85), // Lapiz en el suelo
                Diferencia(305, 496, 571, 170, 180),  // Cinturon del niño

            ),
            pistas=listOf(
                Pista(idDiferencia = 301,indiceBase++),
                Pista(idDiferencia = 302,indiceBase++),
                Pista(idDiferencia = 303,indiceBase++),
                Pista(idDiferencia = 304,indiceBase++),
                Pista(idDiferencia = 305,indiceBase++),
            )
        ),
        NivelJuego(
            id=4,
            imagenOriginal = R.drawable.d4,
            imagenModificada = R.drawable.d4m,
            diferencias = listOf(
                Diferencia(401, 557, 750, 140, 100),   // Masa del niño
                Diferencia(402, 649, 522, 140, 205), // Vestido mujer con instrumento
                Diferencia(403, 594, 324, 220, 85),   // Banderines eliminados
                Diferencia(404, 24, 897, 185, 126), // Cesta añadida
                Diferencia(405, 394, 568, 120, 120),  // Collar de la abuela
            ),
            pistas=listOf(
                Pista(idDiferencia = 401,indiceBase++),
                Pista(idDiferencia = 402,indiceBase++),
                Pista(idDiferencia = 403,indiceBase++),
                Pista(idDiferencia = 404,indiceBase++),
                Pista(idDiferencia = 405,indiceBase++),
            )
        ),
        NivelJuego(
            id=5,
            imagenOriginal = R.drawable.d5,
            imagenModificada = R.drawable.d5m,
            diferencias = listOf(
                Diferencia(501, 898, 771, 110, 110),   // Regaderla eliminada
                Diferencia(502, 140, 652, 80, 80), // Abeja
                Diferencia(503, 521, 86, 50, 50), // Cruz de la iglesia eliminada
                Diferencia(504, 702, 647, 170, 150),  // Niño en lugar de niña
                Diferencia(505, 487, 671, 100, 130), // Rosa en la mano de la monja
            ),
            pistas=listOf(
                Pista(idDiferencia = 501,indiceBase++),
                Pista(idDiferencia = 502,indiceBase++),
                Pista(idDiferencia = 503,indiceBase++),
                Pista(idDiferencia = 504,indiceBase++),
                Pista(idDiferencia = 505,indiceBase++),
            )
        )
    )
}