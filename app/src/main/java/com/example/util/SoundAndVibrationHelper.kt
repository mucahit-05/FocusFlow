package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundAndVibrationHelper(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playSessionCompleteAlert(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
                    delay(400)
                    toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 400)
                    delay(500)
                    toneGen.release()
                } catch (_: Exception) {
                    // Fallback gracefully if audio output is unavailable
                }
            }
        }

        if (vibrationEnabled) {
            try {
                vibrator?.let { v ->
                    if (v.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val timings = longArrayOf(0, 200, 150, 300)
                            val amplitudes = intArrayOf(0, 180, 0, 255)
                            v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                        } else {
                            @Suppress("DEPRECATION")
                            v.vibrate(longArrayOf(0, 200, 150, 300), -1)
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore vibration failure
            }
        }
    }

    fun triggerClickHaptic() {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(30)
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore
        }
    }
}
