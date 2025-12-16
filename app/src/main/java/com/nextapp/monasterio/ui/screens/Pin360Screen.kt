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
import androidx.compose.foundation.shape.CircleShape
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
import com.nextapp.monasterio.utils.AudioPlayerManager
import androidx.compose.ui.platform.LocalConfiguration
import com.nextapp.monasterio.repository.FiguraRepository
import java.util.Locale

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

    // 1. OBTENER IDIOMA Y URL DE AUDIO
    val configuration = LocalConfiguration.current
    val language = configuration.locales[0].language

    // Calculamos la URL del audio igual que en la pantalla anterior
    val audioUrl = remember(pin, language) {
        pin?.let { p ->
            val selectedUrl = when (language) {
                "es" -> p.audioUrl_es
                "en" -> p.audioUrl_en
                "de" -> p.audioUrl_de
                "fr" -> p.audioUrl_fr
                else -> p.audioUrl_es
            }
            selectedUrl.takeIf { !it.isNullOrBlank() } ?: p.audioUrl_en
        }
    }

    // 2. CONECTAR CON EL GESTOR DE AUDIO GLOBAL
    // Esto nos dice si está sonando ahora mismo para poner el icono de Pausa o Play
    val isAudioPlaying by AudioPlayerManager.isPlaying.collectAsState()
    var hasUserInteracted by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            // Al salir de la pantalla (Back o Destroy), pausamos el audio global
            AudioPlayerManager.pause()
        }
    }
    // 1. Cargar datos del Pin
    // 1. Cargar datos (Buscando en Pines Y en Figuras)
    LaunchedEffect(pinId) {
        isLoading = true
        try {
            // INTENTO 1: Buscar como Pin normal
            val loadedData = PinRepository.getPinById(pinId)

            if (loadedData != null) {
                // Es un Pin real
                pin = loadedData
            } else {
                // INTENTO 2: Buscar como Figura (Arco, Iglesia...)
                val figura = FiguraRepository.getFiguraById(pinId)
                if (figura != null) {
                    // ¡Truco! Creamos un PinData temporal.
                    // Rellenamos los datos obligatorios que faltaban con valores vacíos o ceros.
                    pin = PinData(
                        id = figura.id,

                        // Mapeo de Ubicación (antiguo 'nombre' de Figura -> 'ubicacion_es')
                        ubicacion_es = figura.nombre,
                        ubicacion_en = null,
                        ubicacion_de = null,
                        ubicacion_fr = null,

                        // Mapeo de Descripción (antiguo 'descripcion' de Figura -> 'descripcion_es')
                        descripcion_es = figura.descripcion,
                        descripcion_en = null,
                        descripcion_de = null,
                        descripcion_fr = null,

                        // Campos de Área (nuevos, no existen en Figura, se inicializan a null)
                        area_es = null,
                        area_en = null,
                        area_de = null,
                        area_fr = null,

                        // Campos de coordenadas/radio (se mantienen los defaults)
                        x = 0f,
                        y = 0f,
                        tapRadius = 0f,

                        // Campos de imágenes y 360
                        imagenesDetalladas = emptyList(),
                        vista360Url = figura.vista360Url,

                        // Campos de Audio (ya usan la convención correcta)
                        audioUrl_es = figura.audioUrl_es,
                        audioUrl_en = figura.audioUrl_en,
                        audioUrl_de = figura.audioUrl_de,
                        audioUrl_fr = figura.audioUrl_fr
                    )
                }
            }

            if (pin?.vista360Url.isNullOrBlank()) {
                errorMessage = "No se encontró imagen 360 para este elemento."
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Error de carga: ${e.message}"
            isLoading = false
        }
    }

    // 2. Descargar imagen (Con timeout largo)
    LaunchedEffect(pin) {
        if (pin != null) {
            val url = pin!!.vista360Url

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
                    onSuccess = { _, _ ->  },
                    onError = { _, result ->
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
        if (!audioUrl.isNullOrBlank()) {
            val iconRes = when {
                isAudioPlaying -> R.drawable.pause
                !hasUserInteracted -> R.drawable.sound
                else -> R.drawable.play_arrow
            }
            IconButton(
                onClick = {
                    hasUserInteracted = true
                    AudioPlayerManager.initialize(context)
                    AudioPlayerManager.playOrPause(audioUrl!!)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd) // 👈 Esquina Superior Derecha
                    .statusBarsPadding()
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Audio",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
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
                plManager.onResume()
                invalidate()
                requestLayout()
                retryCount++
                watchdogHandler.postDelayed(this, 500)
            } else {
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
        plManager.onResume()
        startRenderWatchdog()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        watchdogHandler.removeCallbacks(renderRunnable)
        plManager.onPause()
        plManager.onDestroy()
    }

    // 👇👇 AQUÍ ESTÁ LA CORRECCIÓN: Quitamos el '?' de MotionEvent 👇👇
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return try {
            plManager.onTouchEvent(event)
        } catch (e: Exception) {
            true
        }
    }
}