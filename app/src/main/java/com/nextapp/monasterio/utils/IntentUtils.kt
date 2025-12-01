package com.nextapp.monasterio.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nextapp.monasterio.R
import android.widget.Toast
import com.nextapp.monasterio.models.Ubicacion

fun Context.llamarTelefono(telefono : String){
    val telefonoMonasterio = telefono
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$telefonoMonasterio")
    this.startActivity(intent)
}

fun Context.abrirUbicacion(ubicacion: String){
    val encodedLocation = Uri.encode(ubicacion)
    val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedLocation")
    val mapIntent = Intent(Intent.ACTION_VIEW,gmmIntentUri)
    try {
        this.startActivity(mapIntent)
    }catch(e:Exception){
        Toast.makeText(this,getString(R.string.location_error),Toast.LENGTH_SHORT).show()
    }
}

fun Context.crearCorreo(nombre:String, email:String, fecha: String, hora:String,necesitaTexto: Boolean){
    /*val correoMonasterio = "smrhv@huelgasreales.es"*/
    val correoMonasterio = "fransanse18@gmail.com"
    val asunto = getString(R.string.email_subject,nombre)
    val mensaje =getString(R.string.email_message,nombre,email,fecha,hora).trimIndent()
    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        type="message/rfc822"
        putExtra(Intent.EXTRA_EMAIL,arrayOf(correoMonasterio))
        if(necesitaTexto) {
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
    }

    if (emailIntent.resolveActivity(this.packageManager) !=null){
        this.startActivity(emailIntent)
    }else{
        Toast.makeText(this,getString(R.string.email_error),Toast.LENGTH_SHORT).show()
    }
}