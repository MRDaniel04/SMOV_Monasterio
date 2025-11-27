package com.nextapp.monasterio.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.nextapp.monasterio.R
import com.nextapp.monasterio.models.PinData
import com.nextapp.monasterio.repository.PinRepository
import com.panoramagl.PLImage
import com.panoramagl.PLSphericalPanorama
import com.panoramagl.PLManager
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Pin360Screen(
    pinId: String,
    navController: NavController
) {
    val context = LocalContext.current

    // Estados
    var pin by remember { mutableStateOf<PinData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 1. Cargar datos del Pin
    LaunchedEffect(pinId) {
        isLoading = true
        try {
            val loadedPin = PinRepository.getPinById(pinId)
            if (loadedPin?.vista360Url.isNullOrBlank()) {
                errorMessage = "Pin no encontrado o no tiene URL 360"
                isLoading = false
            } else {
                pin = loadedPin
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar pin: ${e.message}"
            isLoading = false
        }
    }

    // 2. Descargar imagen (Con timeout largo)
    LaunchedEffect(pin) {
        if (pin != null) {
            val url = pin!!.vista360Url
            Log.d("Pin360", "Iniciando descarga de: $url")

            val customOkHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val customImageLoader = ImageLoader.Builder(context)
                .okHttpClient(customOkHttpClient)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .build()

            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .listener(
                    onSuccess = { _, _ -> Log.d("Pin360", "✅ Imagen descargada correctamente") },
                    onError = { _, result ->
                        Log.e("Pin360", "❌ Error descarga: ${result.throwable.message}")
                        errorMessage = "Error de carga"
                        isLoading = false
                    }
                )
                .target { drawable ->
                    bitmap = (drawable as BitmapDrawable).bitmap
                    isLoading = false
                }
                .build()

            customImageLoader.execute(request)
        }
    }

    // --- UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 3. VISTA PERSONALIZADA CON "WATCHDOG" (REINTENTOS)
        if (bitmap != null && errorMessage == null) {
            AndroidView(
                factory = { ctx ->
                    PanoramaView(ctx).apply {
                        setBitmap(bitmap!!)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    // Importante: Dejamos que la vista maneje sus toques
                    .pointerInteropFilter { false }
            )
        }

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando experiencia 360...", color = Color.White)
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Volver",
                tint = Color.White
            )
        }
    }
}

// ======================================================================
// CLASE PERSONALIZADA INTELIGENTE (PANORAMA VIEW)
// ======================================================================
class PanoramaView(context: Context) : FrameLayout(context) {
    private val plManager: PLManager = PLManager(context)
    private var isInit = false
    private var currentBitmap: Bitmap? = null

    // Variables para el sistema de reintentos (Watchdog)
    private val watchdogHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var retryCount = 0
    private val maxRetries = 5

    init {
        plManager.setContentView(this)
        plManager.onCreate()
        plManager.setInertiaEnabled(false)
        plManager.isAcceleratedTouchScrollingEnabled = false
    }

    fun setBitmap(bitmap: Bitmap) {
        if (isInit) return
        this.currentBitmap = bitmap

        try {
            loadPanoramaImage()
            isInit = true
            startRenderWatchdog()
        } catch (e: Throwable) {
            Log.e("PanoramaView", "Error inicial setBitmap", e)
        }
    }

    private fun loadPanoramaImage() {
        currentBitmap?.let { bmp ->
            val panorama = PLSphericalPanorama()
            panorama.camera.lookAt(0.0f, 0.0f)
            panorama.setImage(PLImage(bmp, false))
            plManager.panorama = panorama
        }
    }

    private val renderRunnable = object : Runnable {
        override fun run() {
            if (retryCount < maxRetries && isAttachedToWindow) {
                Log.d("PanoramaView", "🔄 Watchdog: Forzando onResume (Intento ${retryCount + 1})")
                plManager.onResume()
                invalidate()
                requestLayout()
                retryCount++
                watchdogHandler.postDelayed(this, 500)
            } else {
                Log.d("PanoramaView", "✅ Watchdog finalizado.")
            }
        }
    }

    private fun startRenderWatchdog() {
        retryCount = 0
        watchdogHandler.removeCallbacks(renderRunnable)
        watchdogHandler.post(renderRunnable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("PanoramaView", "onAttachedToWindow -> Reiniciando Watchdog")
        plManager.onResume()
        startRenderWatchdog()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d("PanoramaView", "onDetachedFromWindow -> Limpiando")
        watchdogHandler.removeCallbacks(renderRunnable)
        plManager.onPause()
        plManager.onDestroy()
    }

    // 👇👇 AQUÍ ESTÁ LA CORRECCIÓN: Quitamos el '?' de MotionEvent 👇👇
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return try {
            plManager.onTouchEvent(event)
        } catch (e: Exception) {
            Log.w("PanoramaView", "Ignorando crash táctil interno: ${e.message}")
            true
        }
    }
}