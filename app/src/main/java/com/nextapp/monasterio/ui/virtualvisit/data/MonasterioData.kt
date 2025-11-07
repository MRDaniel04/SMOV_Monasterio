package com.nextapp.monasterio.ui.virtualvisit.data

import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.*

/* TODO */
object MonasterioData {

    val pines = listOf(
        PinData(
            id = "pin1",
            titulo = "Retablo del nacimiento",
            ubicacion = Ubicacion.IGLESIA,
            x = 0.85f,
            y = 0.8f,
            tema = Tema.PINTURA_Y_ARTE_VISUAL,
            imagenes = listOf(R.drawable.retablo1, R.drawable.retablo2),
            descripcion = "Existe un segundo retablo del mismo autor conocido como Retablo del Nacimiento (1614), en la capilla que fuera de San Juan, junto al coro, que da a la Sacristía. \n" +
                    "        En el centro del relieve está el Niño en cuna, la Virgen lo adora con las manos plegadas y hay un pastor ofreciendo un cordero. \n" +
                    "        Junto al Niño hay un ángel de rodillas y más figuras. \n" +
                    "        La escena está tallada con gran detalle y expresividad, destacando por su realismo y dinamismo."
        ),
        PinData(
            id = "pin2",
            titulo = "Claustro principal",
            ubicacion = Ubicacion.CLAUSTRO,
            x = 0.35f,
            y = 0.15f,
            tema = Tema.ESPACIOS_ARQUITECTONICOS,
            imagenes = listOf(R.drawable.claustro1),
            descripcion = "El claustro principal es uno de los espacios más emblemáticos del monasterio."
        )
    )
}
