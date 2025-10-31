package com.nextapp.monasterio.ui.virtualvisit.data
/**
 * 🔹 Modelo base para cualquier elemento interactivo del plano.
 *
 * Define una jerarquía común:
 *   - `InteractiveFigure`: figuras irregulares definidas por Path.
 *   - `InteractivePin`: pines con posición y área de toque.
 *
 * Permite tratar figuras y pines de forma unificada y escalable.
 * Ideal para listas dinámicas o carga desde JSON / base de datos.
 *
 *
 */



import android.graphics.Path


// 🔹 Clase base: cualquier elemento interactivo (una figura o un pin)
sealed class InteractiveItem {
    abstract val id: String          // Ejemplo: "pin1" o "salaA"
    abstract val name: String        // Ejemplo: "Pin Azul" o "Sala Irregular"
}

// 🔸 Figura irregular dibujada con Path
data class InteractiveFigure(
    override val id: String,         // Identificador único (por ejemplo, "figura1")
    override val name: String,       // Nombre visible o lógico
    val path: Path,                  // El contorno de la figura (zona interactiva)
    val highlightColor: Int,         // Color al tocar (ej. Color.YELLOW)
    val detailRoute: String          // Ruta a la pantalla de detalle
) : InteractiveItem()

// 🔹 Pin (un punto interactivo con icono)
data class InteractivePin(
    override val id: String,         // Identificador único (por ejemplo, "pin1")
    override val name: String,       // Nombre del pin
    val x: Float,                    // Posición X normalizada (0.0–1.0)
    val y: Float,                    // Posición Y normalizada (0.0–1.0)
    val iconId: Int,                 // ID del icono (ej. R.drawable.pin_azul)
    val tapRadius: Float,            // Radio de detección del toque
    val detailRoute: String          // Pantalla a la que lleva
) : InteractiveItem()
