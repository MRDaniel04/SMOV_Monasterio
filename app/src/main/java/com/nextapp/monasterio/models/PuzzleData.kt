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

data class PuzzleSet(
    val imagenCompleta : Int,
    val piezas : List<Int>
)

object PuzzleData{
    val PUZZLES_NIVEL1 = listOf(
        PuzzleSet(
            imagenCompleta = R.drawable.p1_0,
            piezas = listOf(R.drawable.p1_0_1,R.drawable.p1_0_2,R.drawable.p1_0_3,R.drawable.p1_0_4)
        ),
        PuzzleSet(
            imagenCompleta = R.drawable.p1_1,
            piezas = listOf(R.drawable.p1_1_1,R.drawable.p1_1_2,R.drawable.p1_1_3,R.drawable.p1_1_4)
        ),
        PuzzleSet(
            imagenCompleta = R.drawable.p1_2,
            piezas = listOf(R.drawable.p1_2_1,R.drawable.p1_2_2,R.drawable.p1_2_3,R.drawable.p1_2_4)
        ),
        PuzzleSet(
            imagenCompleta = R.drawable.p1_3,
            piezas = listOf(R.drawable.p1_3_1,R.drawable.p1_3_2,R.drawable.p1_3_3,R.drawable.p1_3_4)
        )

    )
    val PUZZLES_NIVEL2 = listOf(
        PuzzleSet(
            imagenCompleta = R.drawable.p2_0,
            piezas = listOf(R.drawable.p2_0_1,R.drawable.p2_0_2,R.drawable.p2_0_3,R.drawable.p2_0_4,R.drawable.p2_0_5,R.drawable.p2_0_6,R.drawable.p2_0_7,R.drawable.p2_0_8,R.drawable.p2_0_9)
        ),
        PuzzleSet(
            imagenCompleta = R.drawable.p2_1,
            piezas = listOf(R.drawable.p2_1_1,R.drawable.p2_1_2,R.drawable.p2_1_3,R.drawable.p2_1_4,R.drawable.p2_1_5,R.drawable.p2_1_6,R.drawable.p2_1_7,R.drawable.p2_1_8,R.drawable.p2_1_9)
        ),
        PuzzleSet(
            imagenCompleta = R.drawable.p2_2,
            piezas = listOf(R.drawable.p2_2_1,R.drawable.p2_2_2,R.drawable.p2_2_3,R.drawable.p2_2_4,R.drawable.p2_2_5,R.drawable.p2_2_8,R.drawable.p2_2_7,R.drawable.p2_2_6,R.drawable.p2_2_9)
        ),
        PuzzleSet(
            imagenCompleta = R.drawable.p2_3,
            piezas = listOf(R.drawable.p2_3_1,R.drawable.p2_3_2,R.drawable.p2_3_3,R.drawable.p2_3_4,R.drawable.p2_3_5,R.drawable.p2_3_6,R.drawable.p2_3_7,R.drawable.p2_3_8,R.drawable.p2_3_9)
        ),
    )
    val PUZZLES_NIVEL3 = listOf(
        PuzzleSet(
            imagenCompleta = R.drawable.p3_0,
            piezas = listOf(R.drawable.p3_0_1,R.drawable.p3_0_2,R.drawable.p3_0_3,R.drawable.p3_0_4,R.drawable.p3_0_5,R.drawable.p3_0_6,R.drawable.p3_0_7,R.drawable.p3_0_8,R.drawable.p3_0_9,R.drawable.p3_0_10,R.drawable.p3_0_11,R.drawable.p3_0_12,R.drawable.p3_0_13,R.drawable.p3_0_14,R.drawable.p3_0_15,R.drawable.p3_0_16)
        ),
        PuzzleSet(
            imagenCompleta = R.drawable.p3_1,
            piezas = listOf(R.drawable.p3_1_1,R.drawable.p3_1_2,R.drawable.p3_1_3,R.drawable.p3_1_4,R.drawable.p3_1_5,R.drawable.p3_1_6,R.drawable.p3_1_7,R.drawable.p3_1_8,R.drawable.p3_1_9,R.drawable.p3_1_10,R.drawable.p3_1_11,R.drawable.p3_1_12,R.drawable.p3_1_13,R.drawable.p3_1_14,R.drawable.p3_1_15,R.drawable.p3_1_16)
        ),
        PuzzleSet(
            imagenCompleta = R.drawable.p3_2,
            piezas = listOf(R.drawable.p3_2_1,R.drawable.p3_2_2,R.drawable.p3_2_3,R.drawable.p3_2_4,R.drawable.p3_2_5,R.drawable.p3_2_6,R.drawable.p3_2_7,R.drawable.p3_2_8,R.drawable.p3_2_9,R.drawable.p3_2_10,R.drawable.p3_2_11,R.drawable.p3_2_12,R.drawable.p3_2_13,R.drawable.p3_2_14,R.drawable.p3_2_15,R.drawable.p3_2_16)
        )
    )
    val PUZZLES_NIVEL4 = listOf(
        PuzzleSet(
            imagenCompleta = R.drawable.p4_0,
            piezas = listOf(R.drawable.p4_0_1,R.drawable.p4_0_2,R.drawable.p4_0_3,R.drawable.p4_0_4,R.drawable.p4_0_5,R.drawable.p4_0_6,R.drawable.p4_0_7,R.drawable.p4_0_8,R.drawable.p4_0_9,R.drawable.p4_0_10,R.drawable.p4_0_11,R.drawable.p4_0_12,R.drawable.p4_0_13,R.drawable.p4_0_14,R.drawable.p4_0_15,R.drawable.p4_0_16,R.drawable.p4_0_17,R.drawable.p4_0_18,R.drawable.p4_0_19,R.drawable.p4_0_20,R.drawable.p4_0_21,R.drawable.p4_0_22,R.drawable.p4_0_23,R.drawable.p4_0_24,R.drawable.p4_0_25)
        ),
        PuzzleSet(
            imagenCompleta = R.drawable.p4_1,
            piezas = listOf(R.drawable.p4_1_1,R.drawable.p4_1_2,R.drawable.p4_1_3,R.drawable.p4_1_4,R.drawable.p4_1_5,R.drawable.p4_1_6,R.drawable.p4_1_7,R.drawable.p4_1_8,R.drawable.p4_1_9,R.drawable.p4_1_10,R.drawable.p4_1_11,R.drawable.p4_1_12,R.drawable.p4_1_13,R.drawable.p4_1_14,R.drawable.p4_1_15,R.drawable.p4_1_16,R.drawable.p4_1_17,R.drawable.p4_1_18,R.drawable.p4_1_19,R.drawable.p4_1_20,R.drawable.p4_1_21,R.drawable.p4_1_22,R.drawable.p4_1_23,R.drawable.p4_1_24,R.drawable.p4_1_25)
        ),
        PuzzleSet(
            imagenCompleta = R.drawable.p4_2,
            piezas = listOf(R.drawable.p4_2_1,R.drawable.p4_2_2,R.drawable.p4_2_3,R.drawable.p4_2_4,R.drawable.p4_2_5,R.drawable.p4_2_6,R.drawable.p4_2_7,R.drawable.p4_2_8,R.drawable.p4_2_9,R.drawable.p4_2_10,R.drawable.p4_2_11,R.drawable.p4_2_12,R.drawable.p4_2_13,R.drawable.p4_2_14,R.drawable.p4_2_15,R.drawable.p4_2_16,R.drawable.p4_2_17,R.drawable.p4_2_18,R.drawable.p4_2_19,R.drawable.p4_2_20,R.drawable.p4_2_21,R.drawable.p4_2_22,R.drawable.p4_2_23,R.drawable.p4_2_24,R.drawable.p4_2_25)
        )
    )
}

object PuzzleRotador{
    private val ultimoIndice = mutableMapOf<Int,Int>()

    fun getSiguienteIndice(tamaño: Int):Int{
        val indiceActual = ultimoIndice[tamaño] ?: -1
        val siguienteIndice = (indiceActual + 1 ) % tamaño

        ultimoIndice[tamaño] = siguienteIndice

        return siguienteIndice
    }
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