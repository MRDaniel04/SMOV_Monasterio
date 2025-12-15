package com.nextapp.monasterio.utils

import android.content.Context
import android.media.MediaPlayer
import com.nextapp.monasterio.R

object SoundManager {
    private var mediaPlayer: MediaPlayer? = null

    fun playClickSound(context: Context) {
        // Si no existe, lo creamos
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.botones)
        }

        // Si está sonando, lo rebobinamos
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.seekTo(0)
        }

        mediaPlayer?.start()
    }
}