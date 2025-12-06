package com.nextapp.monasterio.services

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Servicio para subir imágenes a Cloudinary
 */
object CloudinaryService {

    private const val TAG = "CloudinaryService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Sube una imagen a Cloudinary
     * @param imageUri URI de la imagen seleccionada
     * @param context Contexto de la aplicación
     * @return Result con la URL de la imagen subida o error
     */
    suspend fun uploadImage(imageUri: Uri, context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando subida de imagen: $imageUri")

            // Convertir URI a File temporal
            val file = uriToFile(imageUri, context)
                ?: return@withContext Result.failure(Exception("No se pudo leer la imagen"))

            // Crear request multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                .build()

            val request = Request.Builder()
                .url(CloudinaryConfig.UPLOAD_IMAGE_URL)
                .post(requestBody)
                .build()

            // Ejecutar request
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e(TAG, "Error en la respuesta: ${response.code}")
                return@withContext Result.failure(Exception("Error al subir imagen: ${response.code}"))
            }

            // Parsear respuesta JSON con JSONObject nativo
            val responseBody = response.body?.string()
            Log.d(TAG, "Respuesta de Cloudinary: $responseBody")

            val jsonResponse = JSONObject(responseBody ?: "{}")
            val imageUrl = jsonResponse.getString("secure_url")

            // Limpiar archivo temporal
            file.delete()

            Log.d(TAG, "Imagen subida exitosamente: $imageUrl")
            Result.success(imageUrl)

        } catch (e: Exception) {
            Log.e(TAG, "Error al subir imagen", e)
            Result.failure(e)
        }
    }

    /**
     * Convierte un URI a File temporal
     */
    private fun uriToFile(uri: Uri, context: Context): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir URI a File", e)
            null
        }
    }


    suspend fun uploadFile(file: File, mimeType: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando subida de archivo: ${file.name} con MIME: $mimeType")

            // Crear request multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody(mimeType.toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                .build()

            val request = Request.Builder()
                .url(CloudinaryConfig.UPLOAD_AUDIO_URL) // Ahora usa /auto/upload
                .post(requestBody)
                .build()

            // Ejecutar request
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e(TAG, "Error en la respuesta: ${response.code}")
                return@withContext Result.failure(Exception("Error al subir archivo: ${response.code}"))
            }

            // Parsear respuesta JSON
            val responseBody = response.body?.string()
            val jsonResponse = JSONObject(responseBody ?: "{}")
            val fileUrl = jsonResponse.getString("secure_url")

            Log.d(TAG, "Archivo subido exitosamente: $fileUrl")
            Result.success(fileUrl)

        } catch (e: Exception) {
            Log.e(TAG, "Error al subir archivo", e)
            Result.failure(e)
        }
    }



}
