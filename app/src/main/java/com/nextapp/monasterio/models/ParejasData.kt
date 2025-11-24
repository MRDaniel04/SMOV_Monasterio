package com.nextapp.monasterio.models

import java.util.ArrayList
import com.nextapp.monasterio.R


data class Posicion(val x : Int, val y : Int)

data class ParejasSize(val rows:Int, val columns : Int)

data class ParejasPieza(
    val id: Int,
    val imagen : Int,
    val estaVolteada : Boolean,
    val conPareja : Boolean,
    val posicion: Posicion
)

object ParejasData{
    val IMAGENES_NIVEL1 = listOf(
        R.drawable.pj1_1,
        R.drawable.pj1_2,
        R.drawable.pj1_3
    )
    val IMAGENES_NIVEL2 = listOf(
        R.drawable.pj2_1,
        R.drawable.pj2_2,
        R.drawable.pj2_3,
        R.drawable.pj2_4,
    )
    val IMAGENES_NIVEL3 = listOf(
        R.drawable.pj3_1,
        R.drawable.pj3_2,
        R.drawable.pj3_3,
        R.drawable.pj3_4,
        R.drawable.pj3_5
    )
    val IMAGENES_NIVEL4 = listOf(
        R.drawable.pj4_1,
        R.drawable.pj4_2,
        R.drawable.pj4_3,
        R.drawable.pj4_4,
        R.drawable.pj4_5,
        R.drawable.pj4_6,
    )
}

class ParejasManager(val size: ParejasSize){

    private val allPositions : List<Posicion> = generarPosiciones()


    fun inicializarParejaPiezas(imagenes: List<Int>) : List<ParejasPieza>{
        require(imagenes.size == size.rows*size.columns/2)
        val imagenesDuplicadas = (imagenes+imagenes).shuffled()
        val piezas = imagenesDuplicadas.mapIndexed { index, resourceId ->
            val row = index/size.columns
            val col = index%size.columns
            val posicionPieza = Posicion(row,col)
            ParejasPieza(
                id=index,
                imagen=resourceId,
                estaVolteada = false,
                conPareja = false,
                posicion=posicionPieza
            )
        }
        return piezas
    }


    fun generarPosiciones(): List<Posicion>{
        val posiciones = ArrayList<Posicion>(size.rows*size.columns)
        for(i in 0 until size.rows){
            for(j in 0 until size.columns){
                val posicion= Posicion(i,j)
                posiciones.add(posicion)
            }
        }
        return posiciones
    }
}
