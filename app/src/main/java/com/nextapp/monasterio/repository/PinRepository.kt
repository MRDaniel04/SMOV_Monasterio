package com.nextapp.monasterio.repository

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nextapp.monasterio.models.*
import kotlinx.coroutines.tasks.await

object PinRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pines")
    private val imagenRepository = ImagenRepository(firestore)

    suspend fun getPinById(id: String): PinData? {
        val doc = collection.document(id).get().await()
        if (!doc.exists()) {
            Log.w("PinRepository", "⚠️ Pin $id no encontrado.")
            return null
        }

        val basePin = mapDocToPinData(doc.id, doc.data) ?: return null
        val imagenesDetalladas = imagenRepository.getImagesByIds(basePin.imagenes)

        return basePin.copy(
            imagenesDetalladas = imagenesDetalladas
        )
    }

    suspend fun getAllPins(): List<PinData> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            mapDocToPinData(doc.id, doc.data)
        }
    }

    private fun mapDocToPinData(docId: String, data: Map<String, Any>?): PinData? {
        if (data == null) return null

        return try {

            val ubicacion_es = data["ubicacion"] as? String // FB: "ubicacion"
            val ubicacion_en = data["ubicacionIngles"] as? String // FB: "ubicacionIngles"
            val ubicacion_de = data["ubicacionAleman"] as? String // FB: "ubicacionAleman"
            val ubicacion_fr = data["ubicacionFrances"] as? String // FB: "ubicacionFrances"

            // 2. ÁREA (Simple)
            val area_es = data["area_es"] as? String // FB: "area"
            val area_en = data["area_en"] as? String // FB: "areaIngles"
            val area_de = data["area_de"] as? String // FB: "areaAleman"
            val area_fr = data["area_fr"] as? String // FB: "areaFrances"

            val x = (data["x"] as? Number)?.toFloat() ?: 0f
            val y = (data["y"] as? Number)?.toFloat() ?: 0f


            // 3. DESCRIPCIÓN
            val descripcion_es = data["descripcion"] as? String // FB: "descripcion"
            val descripcion_en = data["descripcionIngles"] as? String // FB: "descripcionIngles"
            val descripcion_de = data["descripcionAleman"] as? String // FB: "descripcionAleman"
            val descripcion_fr = data["descripcionFrances"] as? String // FB: "descripcionFrances"
            val vista360Url = data["vista360Url"] as? String

            val imagenes: List<String> = when (val raw = data["imagenes"]) {
                is List<*> -> raw.mapNotNull { ref ->
                    when (ref) {
                        is DocumentReference -> ref.id
                        else -> null
                    }
                }
                else -> emptyList()
            }


            val audioUrl_es = data["audioUrl_es"] as? String
            val audioUrl_en = data["audioUrl_en"] as? String
            val audioUrl_de = data["audioUrl_de"] as? String
            val audioUrl_fr = data["audioUrl_fr"] as? String


            val tapRadius = (data["tapRadius"] as? Number)?.toFloat() ?: 0.04f
            val tipoDestino = data["tipoDestino"] as? String
            val valorDestino = data["valorDestino"] as? String

            val destino = when (tipoDestino?.lowercase()) {
                "ruta" -> DestinoPin.Ruta(valorDestino ?: "")
                "detalle" -> DestinoPin.Detalle(valorDestino ?: docId)
                "popup" -> DestinoPin.Popup(valorDestino ?: "Sin mensaje")
                else -> DestinoPin.Detalle(docId)
            }

            PinData(
                id = docId,
                ubicacion_es = ubicacion_es, // ⬅️ Nuevo nombre en PinData
                ubicacion_en = ubicacion_en,
                ubicacion_de = ubicacion_de,
                ubicacion_fr = ubicacion_fr,
                area_es = area_es, // ⬅️ Nuevo nombre en PinData
                area_en = area_en,
                area_de = area_de,
                area_fr = area_fr,
                x = x,
                y = y,
                iconRes = null,
                imagenes = imagenes,
                imagenesDetalladas = emptyList(),
                descripcion_es = descripcion_es,
                descripcion_en = descripcion_en,
                descripcion_de = descripcion_de,
                descripcion_fr = descripcion_fr,
                tipoDestino = tipoDestino,
                valorDestino = valorDestino,
                destino = destino,
                tapRadius = tapRadius,
                vista360Url = vista360Url,
                audioUrl_es = audioUrl_es,
                audioUrl_en = audioUrl_en,
                audioUrl_de = audioUrl_de,
                audioUrl_fr = audioUrl_fr
            )

        } catch (e: Exception) {
            Log.e("PinRepository", "❌ Error mapeando pin $docId", e)
            null
        }
    }

    suspend fun createPinAutoId(pinPayload: Map<String, Any?>): String {
        val docRef = collection.document()
        val generatedId = docRef.id
        val finalPayload = pinPayload.toMutableMap()
        finalPayload["id"] = generatedId
        docRef.set(finalPayload).await()
        return generatedId
    }

    suspend fun createPinFromForm(
        // 🆕 UBICACIÓN (Compleja)
        ubicacion_es: String?, // ⬅️ Antes 'titulo'
        ubicacion_en: String? = null,
        ubicacion_de: String? = null,
        ubicacion_fr: String? = null,

        // 🆕 DESCRIPCIÓN
        descripcion_es: String?, // ⬅️ Antes 'descripcion'
        descripcion_en: String? = null,
        descripcion_de: String? = null,
        descripcion_fr: String? = null,

        // 🆕 ÁREA (Simple)
        area_es: String?, // ⬅️ Antes 'ubicacion'
        area_en: String?,
        area_de: String?,
        area_fr: String?,

        imagenes: List<ImagenData>,   // URLs de Cloudinary
        imagen360: String?,       // URL de la imagen 360 (opcional)
        x: Float,
        y: Float,
        tapRadius: Float?,
        audioUrl_es: String? = null,
        audioUrl_en: String? = null,
        audioUrl_de: String? = null,
        audioUrl_fr: String? = null
    ): String {

        Log.d("REPO-DEBUG", "📌 Guardando PIN...")

        val imagenesRefs: List<DocumentReference> = imagenes.map { image ->
            createImagenDocument(image)
        }

        val payload = mapOf(
            // UBICACIÓN (Mapeo de 'ubicacion_es' a "ubicacion" en FB, etc.)
            "ubicacion" to ubicacion_es,
            "ubicacionIngles" to ubicacion_en.orEmpty(),
            "ubicacionAleman" to ubicacion_de.orEmpty(),
            "ubicacionFrances" to ubicacion_fr.orEmpty(),

            // DESCRIPCIÓN
            "descripcion" to descripcion_es,
            "descripcionIngles" to descripcion_en,
            "descripcionAleman" to descripcion_de,
            "descripcionFrances" to descripcion_fr,

            // ÁREA (Mapeo de 'area_es' a "area" en FB, etc.)
            "area" to area_es,
            "areaIngles" to area_en,
            "areaAleman" to area_de,
            "areaFrances" to area_fr,

            "x" to x.toDouble(),
            "y" to y.toDouble(),
            "tapRadius" to tapRadius,
            "imagenes" to imagenesRefs,
            "vista360Url" to imagen360,
            "audioUrl_es" to audioUrl_es,
            "audioUrl_en" to audioUrl_en,
            "audioUrl_de" to audioUrl_de,
            "audioUrl_fr" to audioUrl_fr,
            "tipoDestino" to "detalle",
            "valorDestino" to "auto"
        )

        return createPinAutoId(payload)
    }

    private suspend fun createImagenDocument(image: ImagenData): DocumentReference {
        val imagenesCollection = firestore.collection("imagenes")

        // 1. Determinar DocumentReference (usar ID existente o generar uno nuevo)
        val docRef = if (image.id.isNotBlank()) {
            imagenesCollection.document(image.id)
        } else {
            imagenesCollection.document()
        }

        // 2. Crear Payload con todos los datos, incluidos los títulos
        val payload = mapOf(
            "url" to image.url,
            "etiqueta" to image.etiqueta,
            "titulo" to image.titulo,
            "tituloIngles" to image.tituloIngles,
            "tituloAleman" to image.tituloAleman,
            "tituloFrances" to image.tituloFrances,
            "foco" to image.foco.toDouble(), // Firebase usa Double para números
            "tipo" to image.tipo
        )

        // 3. Guardar/Actualizar el documento
        docRef.set(payload, SetOptions.merge()).await()
        return docRef
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
            Log.e("PinRepository", "❌ Error al actualizar posición del pin $pinId", e)
            throw e // Propagar el error para manejo en la UI
        }
    }


    suspend fun deletePinAndImages(pinId: String): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()

            // 1) Obtener documento del pin
            val pinDoc = firestore.collection("pines").document(pinId).get().await()
            if (!pinDoc.exists()) {
                Log.w("PinRepository", "⚠️ deletePinAndImages: pin $pinId no existe")
                return false
            }

            // 2) Eliminar documentos de la colección "imagenes" referenciados en el pin
            val imagenesRefs = pinDoc.get("imagenes") as? List<DocumentReference> ?: emptyList()
            imagenesRefs.forEach { ref ->
                try {
                    ref.delete().await()
                    Log.d("PinRepository", "🗑 Imagen eliminada: ${ref.path}")
                } catch (e: Exception) {
                    Log.e("PinRepository", "❌ Error eliminando imagen ${ref.path}", e)
                }
            }


            try {
                val pinRef = firestore.collection("pines").document(pinId)
                firestore.collection("planos")
                    .document("monasterio_interior")
                    .update("pines", FieldValue.arrayRemove(pinRef))
                    .await()
                Log.d("PinRepository", "🗑 Referencia del pin $pinId eliminada en plano monastery_interior")
            } catch (e: Exception) {
                Log.e("PinRepository", "❌ Error eliminando referencia en plano para pin $pinId", e)
                // no abortamos: seguimos intentando borrar el documento del pin
            }

            // 4) Eliminar el propio documento del pin
            firestore.collection("pines").document(pinId).delete().await()
            Log.d("PinRepository", "🗑 Pin eliminado correctamente: $pinId")

            true
        } catch (e: Exception) {
            Log.e("PinRepository", "❌ Error eliminando pin completo $pinId", e)
            false
        }
    }


    suspend fun updatePin(
        pinId: String,

        ubicacion_es: String?,
        ubicacion_en: String?,
        ubicacion_de: String?,
        ubicacion_fr: String?,

        // 🆕 DESCRIPCIÓN
        descripcion_es: String?,
        descripcion_en: String?,
        descripcion_de: String?,
        descripcion_fr: String?,

        // 🆕 ÁREA (Simple)
        area_es: String?, // ⬅️ Antes 'ubicacion'
        area_en: String?,
        area_de: String?,
        area_fr: String?,

        audioUrl_es: String?,
        audioUrl_en: String?,
        audioUrl_de: String?,
        audioUrl_fr: String?,
        tapRadius: Float?,
        imagenes: List<ImagenData>,
        imagen360: String?
    ) {
        try {
            val pinRef = collection.document(pinId)
            val pinDoc = pinRef.get().await()

            if (pinDoc.exists()) {
                val imagenesRefsAntiguas = pinDoc.get("imagenes") as? List<DocumentReference> ?: emptyList()
                imagenesRefsAntiguas.forEach { ref ->
                    // ⚠️ NOTA: Si una imagen antigua no está en la nueva lista, aquí se elimina.
                    // Si la quieres reutilizar, deberías omitir la eliminación aquí.
                    try { ref.delete().await() }
                    catch (e: Exception) { Log.e("PinRepository", "❌ Advertencia: No se pudo eliminar la imagen antigua ${ref.id}.", e) }
                }
            }

            // 2. Crear/Actualizar documentos de imagen (con títulos)
            val imagenesRefsNuevas: List<DocumentReference> = imagenes.map { image ->
                createImagenDocument(image) // 🆕 USAMOS LA FUNCIÓN ACTUALIZADA
            }

            val updates = mapOf<String, Any?>(
                // UBICACIÓN (Mapeo de 'ubicacion_es' a "ubicacion" en FB)
                "ubicacion" to ubicacion_es,
                "ubicacionIngles" to ubicacion_en,
                "ubicacionAleman" to ubicacion_de,
                "ubicacionFrances" to ubicacion_fr,

                // DESCRIPCIÓN
                "descripcion" to descripcion_es,
                "descripcionIngles" to descripcion_en,
                "descripcionAleman" to descripcion_de,
                "descripcionFrances" to descripcion_fr,

                // ÁREA (Mapeo de 'area_es' a "area" en FB)
                "area" to area_es,
                "areaIngles" to area_en,
                "areaAleman" to area_de,
                "areaFrances" to area_fr,

                // Otros campos
                "imagenes" to imagenesRefsNuevas,
                "vista360Url" to imagen360,
                "audioUrl_es" to audioUrl_es,
                "audioUrl_en" to audioUrl_en,
                "audioUrl_de" to audioUrl_de,
                "audioUrl_fr" to audioUrl_fr,
                "tapRadius" to tapRadius?.toDouble(),
                "tipoDestino" to "detalle",
                "valorDestino" to "auto"
            )

            pinRef
                .set(updates, SetOptions.merge())
                .await()

            Log.d("PinRepository", "✅ Pin $pinId actualizado correctamente.")

        } catch (e: Exception) {
            Log.e("PinRepository", "❌ Error al actualizar pin $pinId", e)
            throw e
        }
    }

}

