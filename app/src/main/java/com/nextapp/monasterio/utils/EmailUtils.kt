package com.nextapp.monasterio.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun Context.crearCorreo(nombre:String, email:String, fecha: String, hora:String){
    val correoMonasterio = "smrhv@huelgasreales.es"
    val asunto = "Solicitud de Reserva de Vista - $nombre"
    val mensaje = """
    Hola,
    
    Me gustaría solicitar una reserva para una visita al monasterio con los siguientes datos:
    
    * Nombre: $nombre
    * Email: $email
    * Fecha de la visita: $fecha
    * Hora de la visita: $hora
    
    Quedo a la espera de su amable confirmación.
    
    Muchas gracias,
    Atentamente,
    
    $nombre
    """.trimIndent()
    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        type="message/rfc822"
        putExtra(Intent.EXTRA_EMAIL,arrayOf(correoMonasterio))
        putExtra(Intent.EXTRA_SUBJECT,asunto)
        putExtra(Intent.EXTRA_TEXT,mensaje)
    }

    if (emailIntent.resolveActivity(this.packageManager) !=null){
        this.startActivity(emailIntent)
    }else{
        Toast.makeText(this,"No se encontró ninguna app de correo",Toast.LENGTH_SHORT).show()
    }
}