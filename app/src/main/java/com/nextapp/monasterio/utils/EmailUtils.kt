package com.nextapp.monasterio.utils

import android.content.Context
import android.content.Intent
import com.nextapp.monasterio.R
import android.widget.Toast

fun Context.crearCorreo(nombre:String, email:String, fecha: String, hora:String){
    val correoMonasterio = "smrhv@huelgasreales.es"
    val asunto = getString(R.string.email_subject,nombre)
    val mensaje =getString(R.string.email_message,nombre,email,fecha,hora).trimIndent()
    val emailIntent = Intent(Intent.ACTION_SEND).apply {
        type="message/rfc822"
        putExtra(Intent.EXTRA_EMAIL,arrayOf(correoMonasterio))
        putExtra(Intent.EXTRA_SUBJECT,asunto)
        putExtra(Intent.EXTRA_TEXT,mensaje)
    }

    if (emailIntent.resolveActivity(this.packageManager) !=null){
        this.startActivity(emailIntent)
    }else{
        Toast.makeText(this,getString(R.string.email_error),Toast.LENGTH_SHORT).show()
    }
}