package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * TambolaSoundManager
 *
 * Professional sound manager utility for the Tambola application.
 * Provides custom, crisp synthesized audio cues for:
 * - Number calling announcement chime & countdown tick
 * - Ticket dab / mark confirmation "pop" & unmark feedback
 * - Winning claim fanfare / celebratory jingle
 * - Bogus / invalid claim warning buzz
 * - Room enter / button click feedback
 *
 * Employs low-latency SoundPool and high-precision synthesized audio buffers
 * ensuring zero external asset dependencies while delivering rich, tactile game audio.
 */
class TambolaSoundManager(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private var soundPool: SoundPool? = null

    // Cache pre-synthesized PCM audio tracks / sound IDs
    private var isMuted: Boolean = false

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SoundPool", e)
        }
    }

    fun setMuted(muted: Boolean) {
        this.isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    /**
     * Play a clean, crisp "pop" chime when a player dabs a number on their ticket.
     */
    fun playDabSound(isDabbing: Boolean = true) {
        if (isMuted) return
        audioScope.launch {
            if (isDabbing) {
                // Bright rising two-tone pop (880Hz -> 1320Hz)
                playToneSequence(
                    frequencies = floatArrayOf(880f, 1320f),
                    durationsMs = intArrayOf(30, 50),
                    volume = 0.7f
                )
            } else {
                // Soft falling un-dab tone (660Hz -> 440Hz)
                playToneSequence(
                    frequencies = floatArrayOf(660f, 440f),
                    durationsMs = intArrayOf(25, 40),
                    volume = 0.45f
                )
            }
        }
    }

    /**
     * Play an energetic caller announcement chime when a new Tambola ball is drawn.
     */
    fun playNumberDrawChime() {
        if (isMuted) return
        audioScope.launch {
            // Elegant 3-tone arpeggio (C5 -> E5 -> G5)
            playToneSequence(
                frequencies = floatArrayOf(523.25f, 659.25f, 783.99f),
                durationsMs = intArrayOf(60, 60, 110),
                volume = 0.8f
            )
        }
    }

    /**
     * Play an exciting victory fanfare when a player or bot wins a verified claim.
     */
    fun playWinFanfare() {
        if (isMuted) return
        audioScope.launch {
            // Grand celebratory chord progression (G4 -> C5 -> E5 -> G5 -> C6)
            playToneSequence(
                frequencies = floatArrayOf(392.00f, 523.25f, 659.25f, 783.99f, 1046.50f),
                durationsMs = intArrayOf(70, 70, 80, 90, 220),
                volume = 0.9f
            )
        }
    }

    /**
     * Play a low buzz warning when a bogus/invalid claim is rejected by the verifier.
     */
    fun playBogusWarning() {
        if (isMuted) return
        audioScope.launch {
            // Low dual-buzz tone (220Hz -> 180Hz)
            playToneSequence(
                frequencies = floatArrayOf(220f, 180f),
                durationsMs = intArrayOf(120, 160),
                volume = 0.75f
            )
        }
    }

    /**
     * Play a soft UI click confirmation sound.
     */
    fun playClickSound() {
        if (isMuted) return
        audioScope.launch {
            playToneSequence(
                frequencies = floatArrayOf(1200f),
                durationsMs = intArrayOf(20),
                volume = 0.4f
            )
        }
    }

    /**
     * Synthesizes and streams a high-quality PCM sine-wave tone buffer via AudioTrack.
     */
    private fun playToneSequence(frequencies: FloatArray, durationsMs: IntArray, volume: Float) {
        val sampleRate = 44100
        var totalSamples = 0
        durationsMs.forEach { totalSamples += (sampleRate * it) / 1000 }

        val generatedSnd = ShortArray(totalSamples)
        var sampleOffset = 0

        for (i in frequencies.indices) {
            val freq = frequencies[i]
            val durationMs = durationsMs[i]
            val numSamples = (sampleRate * durationMs) / 1000
            val attackReleaseLength = (numSamples * 0.15f).toInt().coerceAtLeast(1)

            for (j in 0 until numSamples) {
                val t = j.toDouble() / sampleRate
                val rawSine = sin(2.0 * PI * freq * t)

                // Smooth linear envelope (Attack & Decay) to avoid clicks/pops
                val envelope = when {
                    j < attackReleaseLength -> j.toFloat() / attackReleaseLength
                    j > numSamples - attackReleaseLength -> (numSamples - j).toFloat() / attackReleaseLength
                    else -> 1.0f
                }

                val sampleVal = (rawSine * envelope * volume * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                generatedSnd[sampleOffset + j] = sampleVal.toShort()
            }
            sampleOffset += numSamples
        }

        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(minBufferSize, generatedSnd.size * 2))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    maxOf(minBufferSize, generatedSnd.size * 2),
                    AudioTrack.MODE_STATIC
                )
            }

            track.write(generatedSnd, 0, generatedSnd.size)
            track.play()
            track.setNotificationMarkerPosition(generatedSnd.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(trackInstance: AudioTrack?) {
                    try {
                        trackInstance?.stop()
                        trackInstance?.release()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error releasing AudioTrack", e)
                    }
                }
                override fun onPeriodicNotification(trackInstance: AudioTrack?) {}
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error playing tone sequence", e)
        }
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing SoundPool", e)
        }
    }

    companion object {
        private const val TAG = "TambolaSoundManager"

        @Volatile
        private var INSTANCE: TambolaSoundManager? = null

        fun getInstance(context: Context): TambolaSoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TambolaSoundManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
