package com.nextapp.monasterio.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nextapp.monasterio.models.*
import kotlinx.coroutines.tasks.await

object PinRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pines")

    // -----------------------
    // CREATE
    // -----------------------
    suspend fun createPinAutoId(pinPayload: Map<String, Any?>): String {
        val docRef = collection.add(pinPayload).await()
        val generatedId = docRef.id
        docRef.set(mapOf("id" to generatedId), SetOptions.merge()).await()
        return generatedId
    }

    suspend fun createPin(pin: PinData): String {
        val payload = mapOf(
            "titulo" to pin.titulo,
            "ubicacion" to pin.ubicacion?.name,
            "x" to pin.x.toDouble(),
            "y" to pin.y.toDouble(),
            "tema" to pin.tema?.name,
            "imagenes" to pin.imagenes,
            "descripcion" to pin.descripcion,
            "tapRadius" to pin.tapRadius.toDouble(),
            "vista360Url" to pin.vista360Url,
            "audioUrl_es" to pin.audioUrl_es,
            "audioUrl_en" to pin.audioUrl_en,
            "audioUrl_ge" to pin.audioUrl_ge
        )
        return createPinAutoId(payload)
    }

    // -----------------------
// CREATE desde formulario (EdicionPines)
// -----------------------
    suspend fun createPinFromForm(
        titulo: String,
        descripcion: String,
        imagenes: List<String>,
        imagenes360: List<String>,
        ubicacion: String,
        x: Float,
        y: Float
    ): String {

        val payload = mapOf(
            "titulo" to titulo,
            "tituloIngles" to "",
            "tituloAleman" to "",

            "descripcion" to descripcion,
            "descripcionIngles" to "",
            "descripcionAleman" to "",

            "ubicacion" to ubicacion,
            "ubicacionIngles" to ubicacion,
            "ubicacionAleman" to ubicacion,

            "x" to x.toDouble(),
            "y" to y.toDouble(),

            // Imágenes: convertimos a referencias de Firestore
            "imagenes" to imagenes.map {
                firestore.collection("imagenes").document().path
            },

            // Por ahora guardamos solo como URL (si Cloudinary ya devuelve URL)
            "vista360Url" to imagenes360.firstOrNull(),

            "tipoDestino" to "detalle",
            "valorDestino" to "pin_detalle"
        )

        return createPinAutoId(payload)
    }


    // -----------------------
    // READ: todos los pines
    // -----------------------
    suspend fun getAllPins(): List<PinData> {
        val snapshot = collection.get().await()
        Log.d("PinRepository", "📦 getAllPins() → ${snapshot.size()} documentos")
        return snapshot.documents.mapNotNull { doc ->
            mapDocToPinData(doc.id, doc.data)
        }
    }

    // -----------------------
    // READ: pin individual
    // -----------------------
    suspend fun getPinById(id: String): PinData? {
        Log.d("PinRepository", "🔍 getPinById($id)")
        val doc = collection.document(id).get().await()

        if (!doc.exists()) {
            Log.e("PinRepository", "❌ Documento de pin no encontrado: $id")
            return null
        }

        val data = doc.data
        Log.d("PinRepository", "📄 Datos del pin recuperados: ${data?.keys}")

        val basePin = mapDocToPinData(doc.id, data)
        if (basePin == null) {
            Log.e("PinRepository", "❌ Fallo al mapear el pin $id")
            return null
        }

        if (basePin.imagenes.isEmpty()) {
            Log.w("PinRepository", "⚠️ El pin $id no tiene referencias de imágenes")
        } else {
            Log.d("PinRepository", "🔗 Referencias detectadas: ${basePin.imagenes}")
        }

        val imagenesDetalladas = mutableListOf<ImagenData>()

        for (ref in basePin.imagenes) {
            try {
                val imageId = ref.substringAfterLast("/")
                val imageDoc = firestore.collection("imagenes").document(imageId).get().await()
                if (imageDoc.exists()) {
                    val url = imageDoc.getString("url") ?: ""
                    val etiqueta = imageDoc.getString("etiqueta") ?: ""
                    val titulo = imageDoc.getString("titulo") ?: ""
                    val tituloIngles = imageDoc.getString("tituloIngles") ?: ""
                    val tituloAleman = imageDoc.getString("tituloAleman") ?: ""

                    Log.d(
                        "PinRepository",
                        "✅ Imagen encontrada '$imageId' → etiqueta='$etiqueta', titulo='$titulo', url='$url'"
                    )

                    if (url.isNotBlank()) {
                        imagenesDetalladas.add(
                            ImagenData(
                                id = imageId,
                                url = url,
                                etiqueta = etiqueta,
                                titulo = titulo,
                                tituloIngles = tituloIngles,
                                tituloAleman = tituloAleman
                            )
                        )
                    }
                } else {
                    Log.w("PinRepository", "⚠️ Documento no encontrado en /imagenes/: $imageId")
                }
            } catch (e: Exception) {
                Log.e("PinRepository", "❌ Error al obtener imagen referenciada para $ref", e)
            }
        }

        return basePin.copy(imagenesDetalladas = imagenesDetalladas)
    }

    suspend fun deletePin(pinId: String): Boolean {
        return try {
            collection.document(pinId)
                .delete()
                .await()
            Log.d("PinRepository", "✅ Pin '$pinId' eliminado correctamente.")
            true
        } catch (e: Exception) {
            Log.e("PinRepository", "❌ Error al eliminar el pin '$pinId'", e)
            false
        }
    }

    suspend fun updatePinPosition(pinId: String, newX: Float, newY: Float) {
        val payload = mapOf(
            "x" to newX.toDouble(), // Firebase usa Double para números
            "y" to newY.toDouble()
        )
        try {
            collection.document(pinId)
                .update(payload)
                .await()

        } catch (e: Exception) {

            throw e // Propagar el error para manejo en la UI
        }
    }

    // -----------------------
    // Helper: mapear doc -> PinData
    // -----------------------
    private fun mapDocToPinData(docId: String, data: Map<String, Any>?): PinData? {
        if (data == null) return null

        return try {
            val titulo = data["titulo"] as? String ?: ""
            val tituloIngles = data["tituloIngles"] as? String ?: ""
            val tituloAleman = data["tituloAleman"] as? String ?: ""

            val ubicacion = (data["ubicacion"] as? String)?.let { safeUbicacionOf(it) }
            val ubicacionIngles = (data["ubicacionIngles"] as? String)?.let { safeUbicacionOf(it) }
            val ubicacionAleman = (data["ubicacionAleman"] as? String)?.let { safeUbicacionOf(it) }

            val x = (data["x"] as? Number)?.toFloat() ?: 0f
            val y = (data["y"] as? Number)?.toFloat() ?: 0f
            val temaStr = data["tema"] as? String
            val tema = temaStr?.let { safeTemaOf(it) } ?: Tema.PINTURA_Y_ARTE_VISUAL

            // 🧩 Referencias de imágenes
            val imagenes: List<String> = when (val raw = data["imagenes"]) {
                is List<*> -> raw.mapNotNull {
                    when (it) {
                        is String -> it
                        is DocumentReference -> it.path
                        else -> null
                    }
                }
                else -> emptyList()
            }

            val descripcion = data["descripcion"] as? String
            val descripcionIngles = data["descripcionIngles"] as? String
            val descripcionAleman = data["descripcionAleman"] as? String
            val tapRadius = (data["tapRadius"] as? Number)?.toFloat() ?: 0.04f
            val vista360Url = data["vista360Url"] as? String

            val audioUrl_es = data["audioUrl_es"] as? String
            val audioUrl_en = data["audioUrl_en"] as? String
            val audioUrl_ge = data["audioUrl_ge"] as? String

            // 🔹 NUEVO: leer los campos de destino
            val tipoDestino = data["tipoDestino"] as? String
            val valorDestino = data["valorDestino"] as? String

            // 🔹 Crear el destino dinámicamente
            val destino = when (tipoDestino?.lowercase()) {
                "ruta" -> DestinoPin.Ruta(valorDestino ?: "")
                "detalle" -> DestinoPin.Detalle(valorDestino ?: docId)
                "popup" -> DestinoPin.Popup(valorDestino ?: "Sin mensaje")
                else -> DestinoPin.Detalle(docId)
            }

            Log.d("PinRepository", "📍 Mapeando pin '$titulo' ($docId) → tipoDestino=$tipoDestino, valor=$valorDestino")

            PinData(
                id = docId,
                titulo = titulo,
                tituloIngles = tituloIngles,
                tituloAleman = tituloAleman,
                ubicacion = ubicacion,
                ubicacionIngles = ubicacionIngles,
                ubicacionAleman = ubicacionAleman,
                x = x,
                y = y,
                tema = tema,
                color = null,
                iconRes = null,
                imagenes = imagenes,
                imagenesDetalladas = emptyList(),
                descripcion = descripcion,
                descripcionIngles = descripcionIngles,
                descripcionAleman = descripcionAleman,
                tipoDestino = tipoDestino,
                valorDestino = valorDestino,
                destino = destino,
                tapRadius = tapRadius,
                vista360Url = vista360Url,
                audioUrl_es = audioUrl_es,
                audioUrl_en = audioUrl_en,
                audioUrl_ge = audioUrl_ge
            )
        } catch (e: Exception) {
            Log.e("PinRepository", "❌ Error mapeando pin $docId", e)
            null
        }
    }


    private fun safeTemaOf(name: String): Tema {
        return try {
            Tema.valueOf(name)
        } catch (_: Exception) {
            Tema.PINTURA_Y_ARTE_VISUAL
        }
    }

    private fun safeUbicacionOf(name: String): Ubicacion? {
        return try {
            Ubicacion.valueOf(name)
        } catch (_: Exception) {
            null
        }
    }
}
