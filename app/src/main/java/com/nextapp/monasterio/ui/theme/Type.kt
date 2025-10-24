package com.nextapp.monasterio.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nextapp.monasterio.R

// 1. Define tu nueva familia de fuentes apuntando a los archivos en res/font/
// Asegúrate de que los nombres (ej: R.font.cactus_classical_serif_regular)
// coinciden EXACTAMENTE con los archivos que pusiste en la carpeta res/font.
val CactusClassicalSerif = FontFamily(
    Font(R.font.cactus_classical_serif, FontWeight.Normal),
    // Si añadiste más pesos (Medium, Italic, etc.), añádelos aquí
)

// 2. Define el objeto Typography para que use tu nueva fuente por defecto
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = CactusClassicalSerif, // <-- Aplicamos la fuente
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CactusClassicalSerif, // <-- Aplicamos la fuente
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Esto es para el texto de tus botones
    labelLarge = TextStyle(
        fontFamily = CactusClassicalSerif, // <-- Aplicamos la fuente
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // Esto es para los ítems del menú
    bodyMedium = TextStyle(
        fontFamily = CactusClassicalSerif, // <-- Aplicamos la fuente
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp, // Tamaño que definimos en el DrawerMenuItem
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)