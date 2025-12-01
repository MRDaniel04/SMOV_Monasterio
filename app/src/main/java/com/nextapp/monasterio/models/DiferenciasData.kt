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
        /*NivelJuego(
            id=1,
            imagenOriginal = R.drawable.d1,
            imagenModificada = R.drawable.d1m,
            diferencias = mutableStateListOf(
                Diferencia(101, 430, 120, 200, 120), // Estandarte
                Diferencia(102, 710, 150, 90, 70),  // Nube/Emoji
                Diferencia(103, 800, 200, 60, 60),  // Pájaro
                Diferencia(104, 150, 620, 250, 180),// Flores izquierda
                Diferencia(105, 770, 800, 70, 70),  // Mariposa
                Diferencia(106, 420, 580, 120, 60),// Manga de la princesa
                Diferencia(107, 390, 400, 180, 180)
            )
        ),
        NivelJuego(
            id=2,
            imagenOriginal = R.drawable.d2,
            imagenModificada = R.drawable.d2m,
            diferencias = mutableStateListOf(
                Diferencia(201, 390, 440, 250, 250), // Símbolo Túnica
                Diferencia(202, 250, 200, 120, 100), // Pájaro
                Diferencia(203, 720, 180, 150, 100), // Nube/Emoji
                Diferencia(204, 400, 660, 100, 70),  // Hebilla/Cinturón
                Diferencia(205, 150, 850, 100, 100), // Flor Inferior
                Diferencia(206, 350, 300, 50, 200),  // Muros del Castillo
                Diferencia(207, 400, 920, 150, 50)
            )
        ),
        NivelJuego(
            id=3,
            imagenOriginal = R.drawable.d3,
            imagenModificada = R.drawable.d3m,
            diferencias = mutableStateListOf(
                Diferencia(301, 350, 120, 300, 100), // Tocado/Velo (Color)
                Diferencia(302, 480, 360, 60, 60),  // Flor en la trenza
                Diferencia(303, 550, 250, 150, 100), // Manga (Color)
                Diferencia(304, 390, 600, 250, 150), // Posición de Manos/Brazos
                Diferencia(305, 100, 280, 200, 180), // Muros del Castillo (Torre izquierda)
                Diferencia(306, 150, 750, 200, 150), // Flores/Pasto Inferior
                Diferencia(307, 400, 200, 80, 70)
            )
        ),
        NivelJuego(
            id=4,
            imagenOriginal = R.drawable.d4,
            imagenModificada = R.drawable.d4m,
            diferencias = mutableStateListOf(
                Diferencia(401, 480, 250, 120, 100), // Corona del Príncipe
                Diferencia(402, 750, 850, 100, 50),  // Zapatos
                Diferencia(403, 280, 850, 80, 70),   // Juguete/Pato
                Diferencia(404, 210, 190, 180, 100), // Corona del Rey
                Diferencia(405, 830, 200, 80, 80),   // Símbolo Ventana
                Diferencia(406, 850, 500, 50, 80),   // Cruz/Símbolo
                Diferencia(407, 480, 400, 80, 100)
            )
        ),*/
        NivelJuego(
            id=5,
            imagenOriginal = R.drawable.d5,
            imagenModificada = R.drawable.d5m,
            diferencias = mutableStateListOf(
                Diferencia(501, 391, 214, 228, 128), // Tocado del Obispo
                Diferencia(502, 654, 486, 100, 120), // Símbolo en el pecho
                Diferencia(503, 538, 537, 250, 158), // Vestido de la Princesa (Color/Diseño)
                Diferencia(504, 626, 440, 143, 100),  // Collar
                Diferencia(505, 224, 572, 156, 100),   // Hebilla
                Diferencia(506, 0, 0, 1024, 363),  // Confeti
                Diferencia(507, 87, 673, 222, 100)  // Color de la empuñadura de la espada
            )
        ),
        /*NivelJuego(
            id=6,
            imagenOriginal = R.drawable.d6,
            imagenModificada = R.drawable.d6m,
            diferencias = mutableStateListOf(
                Diferencia(801, 500, 200, 120, 100), // Bandera
                Diferencia(802, 100, 50, 800, 200),  // Nubes/Cielo
                Diferencia(803, 680, 820, 80, 70),   // Hongo
                Diferencia(804, 280, 800, 80, 80),   // Flor
                Diferencia(805, 590, 580, 100, 80),  // Manga
                Diferencia(806, 400, 450, 180, 100), // Símbolo sobre el pecho (Estrellas/Corazones)
                Diferencia(807, 480, 520, 60, 60)
            )
        )*/
    )
}