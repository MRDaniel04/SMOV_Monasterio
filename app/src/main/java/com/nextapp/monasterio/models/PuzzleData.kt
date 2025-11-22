package com.nextapp.monasterio.models

import java.util.ArrayList
import com.nextapp.monasterio.R


data class PuzzleSize(val rows:Int, val columns:Int)

data class GridPosicion(val row:Int, val column: Int)

data class PiezaPuzzle(
    val id:Int,
    val posicionCorrecta: GridPosicion,
    val posicionActual: GridPosicion,
    val imagen: Int,
    val encajada:Boolean=false
)

object PuzzleData{
    val IMAGENES_NIVEL1 = listOf(
        R.drawable.p1_1,
        R.drawable.p1_2,
        R.drawable.p1_3,
        R.drawable.p1_4
    )
    val IMAGENES_NIVEL2 = listOf(
        R.drawable.p2_1,
        R.drawable.p2_2,
        R.drawable.p2_3,
        R.drawable.p2_4,
        R.drawable.p2_5,
        R.drawable.p2_6,
        R.drawable.p2_7,
        R.drawable.p2_8,
        R.drawable.p2_9
    )
    val IMAGENES_NIVEL3 = listOf(
        R.drawable.p3_1,
        R.drawable.p3_2,
        R.drawable.p3_3,
        R.drawable.p3_4,
        R.drawable.p3_5,
        R.drawable.p3_6,
        R.drawable.p3_7,
        R.drawable.p3_8,
        R.drawable.p3_9,
        R.drawable.p3_10,
        R.drawable.p3_11,
        R.drawable.p3_12,
        R.drawable.p3_13,
        R.drawable.p3_14,
        R.drawable.p3_15,
        R.drawable.p3_16,
    )
    val IMAGENES_NIVEL4 = listOf(
        R.drawable.p4_1,
        R.drawable.p4_2,
        R.drawable.p4_3,
        R.drawable.p4_4,
        R.drawable.p4_5,
        R.drawable.p4_6,
        R.drawable.p4_7,
        R.drawable.p4_8,
        R.drawable.p4_9,
        R.drawable.p4_10,
        R.drawable.p4_11,
        R.drawable.p4_12,
        R.drawable.p4_13,
        R.drawable.p4_14,
        R.drawable.p4_15,
        R.drawable.p4_16,
        R.drawable.p4_17,
        R.drawable.p4_18,
        R.drawable.p4_19,
        R.drawable.p4_20,
        R.drawable.p4_21,
        R.drawable.p4_22,
        R.drawable.p4_23,
        R.drawable.p4_24,
        R.drawable.p4_25
    )
}

class PuzzleManager(val size: PuzzleSize){
    private val allPositions: List<GridPosicion> = generarPosiciones()

    fun inicializarPiezas(imagenesPiezas: List<Int>):List<PiezaPuzzle>{
        require(imagenesPiezas.size == size.rows*size.columns){

        }
        val piezasOrdenadas = imagenesPiezas.mapIndexed { index,resourceId ->
            val row = index / size.columns
            val column= index % size.columns
            val correctaPosicion = GridPosicion(row,column)

            PiezaPuzzle(
                id=index,
                posicionCorrecta = correctaPosicion,
                posicionActual = correctaPosicion,
                imagen = resourceId
            )
        }
        val posicionesDesordenadas = allPositions.shuffled()
        return piezasOrdenadas.mapIndexed { index,pieza->
            pieza.copy(posicionActual = posicionesDesordenadas[index])
        }
    }


    private fun generarPosiciones(): List<GridPosicion>{
        val posiciones = ArrayList<GridPosicion>(size.rows*size.columns)
        for(i in 0 until size.rows){
            for (j in 0 until size.columns){
                val posicion= GridPosicion(i,j)
                posiciones.add(posicion)
            }
        }
        return posiciones
    }
}