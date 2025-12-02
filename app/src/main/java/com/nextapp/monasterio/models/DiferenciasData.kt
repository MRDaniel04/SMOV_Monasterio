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

    fun marcarEncontrada() {
        if (!encontrada) {
            encontrada = true
        }
    }
    fun tocoDiferencia(touchX: Float, touchY: Float): Boolean {
        return touchX >= rectX &&
                touchX <= rectX + width &&
                touchY >= rectY &&
                touchY <= rectY + height
    }
}

data class NivelJuego(
    val id: Int,
    @DrawableRes val imagenOriginal : Int,
    @DrawableRes val imagenModificada : Int,
    val diferencias : List<Diferencia>
)

fun obtenerPares() : List<NivelJuego>{
    return listOf(
        NivelJuego(
            id=1,
            imagenOriginal = R.drawable.d1,
            imagenModificada = R.drawable.d1m,
            diferencias = mutableStateListOf(
                Diferencia(101, 234, 136, 80, 80), // Pájaro
                Diferencia(102, 488, 183, 236, 155),  // Bandera
                Diferencia(103, 533, 657, 100, 100),  // Leon en el caballo
                Diferencia(104, 313, 570, 80, 80),// No leon en la pechera
                Diferencia(105, 199, 626, 206, 190),  // Espada del caballero
            )
        ),
        NivelJuego(
            id=2,
            imagenOriginal = R.drawable.d2,
            imagenModificada = R.drawable.d2m,
            diferencias = mutableStateListOf(
                Diferencia(201, 227, 281, 50, 110), // Crucifijo del cura
                Diferencia(202, 388, 250, 60, 40), // Collar de la reina
                Diferencia(203, 538, 146, 60, 50), // Corona del rey
                Diferencia(204, 512, 319, 110, 70),  // Cinturon del rey
                Diferencia(205, 779, 313, 110, 100), // Instrumento
            )
        ),
        NivelJuego(
            id=3,
            imagenOriginal = R.drawable.d3,
            imagenModificada = R.drawable.d3m,
            diferencias = mutableStateListOf(
                Diferencia(301, 331, 0, 350, 230), // Lampara
                Diferencia(302, 496, 571, 170, 180),  // Cinturon del niño
                Diferencia(303, 381, 939, 250, 85), // Lapiz en el suelo
                Diferencia(304, 853, 799, 155, 110), // Gato en la cesta
                Diferencia(305, 842, 59, 140, 100), // Gato arriba
            )
        ),
        NivelJuego(
            id=4,
            imagenOriginal = R.drawable.d4,
            imagenModificada = R.drawable.d4m,
            diferencias = mutableStateListOf(
                Diferencia(401, 24, 897, 185, 126), // Cesta añadida
                Diferencia(402, 394, 568, 120, 120),  // Collar de la abuela
                Diferencia(403, 557, 750, 140, 100),   // Masa del niño
                Diferencia(404, 649, 522, 140, 205), // Vestido mujer con instrumento
                Diferencia(405, 594, 324, 220, 85),   // Banderines eliminados
            )
        ),
        NivelJuego(
            id=5,
            imagenOriginal = R.drawable.d5,
            imagenModificada = R.drawable.d5m,
            diferencias = mutableStateListOf(
                Diferencia(501, 151, 590, 100, 90), // Abeja
                Diferencia(502, 487, 671, 100, 130), // Rosa en la mano de la monja
                Diferencia(503, 521, 86, 50, 50), // Cruz de la iglesia eliminada
                Diferencia(504, 715, 653, 200, 250),  // Niño en lugar de niña
                Diferencia(505, 898, 771, 110, 110),   // Regaderla eliminada
            )
        )
    )
}