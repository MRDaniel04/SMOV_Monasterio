package com.nextapp.monasterio.ui.virtualvisit.utils

import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import kotlin.math.sqrt

/**
 * 🔹 Comprueba si un punto (x, y) está dentro de un Path (figura irregular).
 *
 * @param x Coordenada X normalizada (0–1)
 * @param y Coordenada Y normalizada (0–1)
 * @param path Path del área a comprobar
 *
 * @return true si el punto está dentro del área del Path
 *
 * 🧠 Ejemplo:
 * if (isPointInPath(x, y, myRoomPath)) { /* Navegar o resaltar */ }
 */
fun isPointInPath(x: Float, y: Float, path: Path): Boolean {
    val scale = 10000f
    val scaledPath = Path()
    path.transform(android.graphics.Matrix().apply { setScale(scale, scale) }, scaledPath)

    val boundsRectF = RectF()
    scaledPath.computeBounds(boundsRectF, true)
    val bounds = android.graphics.Rect(
        boundsRectF.left.toInt(),
        boundsRectF.top.toInt(),
        boundsRectF.right.toInt(),
        boundsRectF.bottom.toInt()
    )

    val region = Region(bounds)
    region.setPath(scaledPath, region)

    val scaledX = (x * scale).toInt()
    val scaledY = (y * scale).toInt()

    return region.contains(scaledX, scaledY)
}

/**
 * 🔹 Comprueba si un toque está dentro del área de un pin (círculo invisible de detección).
 *
 * @param tapX X del toque (normalizado 0–1)
 * @param tapY Y del toque (normalizado 0–1)
 * @param pinX X del pin
 * @param pinY Y del pin
 * @param tapRadius Radio de detección
 *
 * @return true si el toque cae dentro del área del pin
 *
 * 🧠 Ejemplo:
 * if (isPointInPinArea(x, y, pin.x, pin.y, 0.03f)) { /* Navegar a vista del pin */ }
 */
fun isPointInPinArea(
    tapX: Float, tapY: Float,
    pinX: Float, pinY: Float,
    tapRadius: Float
): Boolean {
    val dx = tapX - pinX
    val dy = tapY - pinY
    val distance = sqrt(dx * dx + dy * dy)
    return distance < tapRadius
}

/**
 * 🔹 Crea un Path (figura) a partir de una lista de coordenadas normalizadas (0.0–1.0)
 *
 * @param points Lista de coordenadas (pares X,Y)
 * @param close Si es true, cierra la figura automáticamente (forma cerrada)
 *
 * @return Path listo para usar en DebugPhotoView
 *
 * 🧠 Ejemplo:
 * val coords = listOf(0.1f to 0.2f, 0.3f to 0.5f, 0.5f to 0.4f)
 * val path = createPathFromPoints(coords, close = true)
 */
fun createPathFromPoints(points: List<Pair<Float, Float>>, close: Boolean = true): Path {
    val path = Path()
    if (points.isEmpty()) return path

    path.moveTo(points.first().first, points.first().second)
    for (i in 1 until points.size) {
        path.lineTo(points[i].first, points[i].second)
    }

    if (close) path.close()
    return path
}
