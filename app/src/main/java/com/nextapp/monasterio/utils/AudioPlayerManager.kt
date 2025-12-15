package com.nextapp.monasterio.utils

import android.content.Context
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
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                        _isPlaying.value = isPlayingValue
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            seekTo(0)
                            pause()
                            _isPlaying.value = false
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                    }
                })
            }
        }
    }

    fun playOrPause(url: String) {
        val player = exoPlayer
        if (player == null) {
            return
        }


        if (currentUrl != url) {
            currentUrl = url
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        } else {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.seekTo(0)
                player.play()
            }
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

}