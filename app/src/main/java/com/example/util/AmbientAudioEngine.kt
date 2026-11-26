package com.example.util

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

enum class AmbientSoundType(val iconEmoji: String, val rawResId: Int?) {
    NONE("", null),
    RAIN("", R.raw.sound_rain),
    FOREST("", R.raw.sound_forest),
    OCEAN("", R.raw.sound_ocean),
    CAMPFIRE("", R.raw.sound_campfire),
    NIGHT("", R.raw.sound_night),
    CAFE("", R.raw.sound_cafe),
    WHITE_NOISE("", R.raw.sound_white_noise),
    ZEN("", R.raw.sound_zen);

    fun getVectorIcon(): ImageVector = when (this) {
        NONE -> Icons.Rounded.VolumeOff
        RAIN -> Icons.Rounded.WaterDrop
        FOREST -> Icons.Rounded.Forest
        OCEAN -> Icons.Rounded.Waves
        CAMPFIRE -> Icons.Rounded.LocalFireDepartment
        NIGHT -> Icons.Rounded.NightsStay
        CAFE -> Icons.Rounded.Coffee
        WHITE_NOISE -> Icons.Rounded.GraphicEq
        ZEN -> Icons.Rounded.SelfImprovement
    }

    fun getDisplayName(strings: AppStrings): String = when (this) {
        NONE -> strings.ambientSoundNone
        RAIN -> strings.ambientSoundRain
        FOREST -> strings.ambientSoundForest
        OCEAN -> strings.ambientSoundOcean
        CAMPFIRE -> strings.ambientSoundCampfire
        NIGHT -> strings.ambientSoundNight
        CAFE -> strings.ambientSoundCafe
        WHITE_NOISE -> strings.ambientSoundWhiteNoise
        ZEN -> strings.ambientSoundZen
    }

    fun getDescription(strings: AppStrings): String = when (this) {
        NONE -> strings.ambientDescNone
        RAIN -> strings.ambientDescRain
        FOREST -> strings.ambientDescForest
        OCEAN -> strings.ambientDescOcean
        CAMPFIRE -> strings.ambientDescCampfire
        NIGHT -> strings.ambientDescNight
        CAFE -> strings.ambientDescCafe
        WHITE_NOISE -> strings.ambientDescWhiteNoise
        ZEN -> strings.ambientDescZen
    }
}

class AmbientAudioEngine(private val context: Context) {

    @Volatile
    private var mediaPlayer: MediaPlayer? = null

    @Volatile
    private var currentSound = AmbientSoundType.NONE

    @Volatile
    private var currentVolume = 0.6f

    @Volatile
    private var isPlaying = false

    @Synchronized
    fun setSound(soundType: AmbientSoundType) {
        // Immediate clean shutdown of any existing playback
        stopAndRelease()

        if (soundType == AmbientSoundType.NONE || soundType.rawResId == null) {
            currentSound = AmbientSoundType.NONE
            return
        }

        currentSound = soundType
        try {
            val mp = MediaPlayer.create(context, soundType.rawResId)
            if (mp != null) {
                mp.isLooping = true
                mp.setVolume(currentVolume, currentVolume)
                mp.start()
                mediaPlayer = mp
                isPlaying = true
            }
        } catch (_: Exception) {
            stopAndRelease()
        }
    }

    fun getSound(): AmbientSoundType = currentSound

    @Synchronized
    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        try {
            if (currentVolume <= 0.005f) {
                mediaPlayer?.setVolume(0f, 0f)
            } else {
                mediaPlayer?.setVolume(currentVolume, currentVolume)
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    fun getVolume(): Float = currentVolume

    fun isCurrentlyPlaying(): Boolean = isPlaying && mediaPlayer != null

    @Synchronized
    fun play() {
        if (currentSound == AmbientSoundType.NONE || currentSound.rawResId == null) return
        val player = mediaPlayer
        if (player == null) {
            setSound(currentSound)
        } else {
            try {
                if (!player.isPlaying) {
                    player.start()
                }
                isPlaying = true
            } catch (_: Exception) {
                setSound(currentSound)
            }
        }
    }

    @Synchronized
    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        } catch (_: Exception) {
            // Ignore
        } finally {
            isPlaying = false
        }
    }

    @Synchronized
    fun stopAndRelease() {
        val player = mediaPlayer
        mediaPlayer = null
        currentSound = AmbientSoundType.NONE
        isPlaying = false

        if (player != null) {
            try {
                player.setVolume(0f, 0f)
            } catch (_: Exception) {
                // Ignore
            }
            try {
                if (player.isPlaying) {
                    player.pause()
                }
            } catch (_: Exception) {
                // Ignore
            }
            try {
                player.stop()
            } catch (_: Exception) {
                // Ignore
            }
            try {
                player.reset()
            } catch (_: Exception) {
                // Ignore
            }
            try {
                player.release()
            } catch (_: Exception) {
                // Ignore
            }
        }
    }
}
