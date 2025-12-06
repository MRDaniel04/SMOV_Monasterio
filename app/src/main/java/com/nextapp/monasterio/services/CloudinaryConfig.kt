package com.nextapp.monasterio.services

/**
 * Configuración de Cloudinary para la aplicación
 */
object CloudinaryConfig {
    const val CLOUD_NAME = "drx7mujrv"
    const val UPLOAD_PRESET = "android_monasterio_app"
    const val UPLOAD_IMAGE_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    const val UPLOAD_AUDIO_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/video/upload"

}
