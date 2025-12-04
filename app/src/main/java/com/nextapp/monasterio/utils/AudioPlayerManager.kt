package com.nextapp.monasterio.utils

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AudioPlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private var currentUrl: String? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun initialize(context: Context) {
        if (exoPlayer == null) {
            Log.d("AudioPlayer", "🛠 Creando nueva instancia de ExoPlayer")
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                        _isPlaying.value = isPlayingValue
                        Log.d("AudioPlayer", "Estado playing cambiado: $isPlayingValue")
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            seekTo(0)
                            pause()
                            _isPlaying.value = false
                            Log.d("AudioPlayer", "Audio finalizado")
                        }
                    }

                    // 👇 ESTO ES LO NUEVO: Captura de errores
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("AudioPlayer", "❌ ERROR CRÍTICO: ${error.message}", error)
                    }
                })
            }
        } else {
            Log.d("AudioPlayer", "♻️ ExoPlayer ya estaba inicializado")
        }
    }

    fun playOrPause(url: String) {
        val player = exoPlayer
        if (player == null) {
            Log.e("AudioPlayer", "⚠️ Error: Intentando reproducir sin inicializar (player es null)")
            return
        }

        Log.d("AudioPlayer", "Solicitud playOrPause. URL actual: $currentUrl | Nueva URL: $url")

        if (currentUrl != url) {
            Log.d("AudioPlayer", "🎵 Cargando nuevo audio...")
            currentUrl = url
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        } else {
            if (player.isPlaying) {
                Log.d("AudioPlayer", "⏸ Pausando")
                player.pause()
            } else {
                Log.d("AudioPlayer", "▶️ Reanudando")
                player.seekTo(0)
                player.play()
            }
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun release() {
        Log.d("AudioPlayer", "🛑 Liberando recursos")
        exoPlayer?.release()
        exoPlayer = null
        currentUrl = null
        _isPlaying.value = false
    }
}