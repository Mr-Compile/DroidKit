package io.github.droidkit.feature.audio

import android.media.AudioManager
import kotlin.math.log10
import kotlin.math.sin

/**
 * Pure helpers for the audio lab — tone synthesis, metering math, and
 * stream/channel naming. All JVM-testable: no AudioTrack/AudioRecord objects
 * are needed to verify any of it.
 */
object AudioInfo {

    const val TONE_SAMPLE_RATE = 44100

    fun streamName(streamType: Int): String = when (streamType) {
        AudioManager.STREAM_VOICE_CALL -> "Voice call"
        AudioManager.STREAM_SYSTEM -> "System"
        AudioManager.STREAM_RING -> "Ring"
        AudioManager.STREAM_MUSIC -> "Music"
        AudioManager.STREAM_ALARM -> "Alarm"
        AudioManager.STREAM_NOTIFICATION -> "Notification"
        AudioManager.STREAM_DTMF -> "DTMF"
        AudioManager.STREAM_ACCESSIBILITY -> "Accessibility"
        else -> "Stream $streamType"
    }

    /** Streams worth listing in a volume inventory, in display order. */
    fun volumeStreams(): List<Int> = listOf(
        AudioManager.STREAM_MUSIC,
        AudioManager.STREAM_RING,
        AudioManager.STREAM_NOTIFICATION,
        AudioManager.STREAM_ALARM,
        AudioManager.STREAM_VOICE_CALL,
        AudioManager.STREAM_SYSTEM,
        AudioManager.STREAM_DTMF,
        AudioManager.STREAM_ACCESSIBILITY,
    )

    fun channelName(channel: Int): String = when (channel) {
        CHANNEL_LEFT -> "Left"
        CHANNEL_RIGHT -> "Right"
        else -> "Both"
    }

    const val CHANNEL_BOTH = 0
    const val CHANNEL_LEFT = 1
    const val CHANNEL_RIGHT = 2

    /**
     * Interleaved stereo 16-bit PCM sine burst. [channel] selects which side
     * carries the signal — [CHANNEL_BOTH], [CHANNEL_LEFT] or [CHANNEL_RIGHT] —
     * so a stereo crosstalk check needs nothing but this buffer.
     */
    fun sinePcm16(
        freqHz: Double,
        durationMs: Int,
        sampleRate: Int = TONE_SAMPLE_RATE,
        channel: Int = CHANNEL_BOTH,
    ): ShortArray {
        require(freqHz > 0) { "freqHz must be > 0" }
        require(durationMs > 0) { "durationMs must be > 0" }
        val frames = (sampleRate * durationMs / 1000).coerceAtLeast(1)
        val pcm = ShortArray(frames * 2)
        val step = 2.0 * Math.PI * freqHz / sampleRate
        val amp = (Short.MAX_VALUE * 0.6).toInt()
        for (i in 0 until frames) {
            val s = (sin(i * step) * amp).toInt().toShort()
            if (channel != CHANNEL_RIGHT) pcm[i * 2] = s
            if (channel != CHANNEL_LEFT) pcm[i * 2 + 1] = s
        }
        return pcm
    }

    /**
     * RMS of a 16-bit PCM buffer → dBFS. Silence returns [DB_FLOOR] rather
     * than -Infinity so meters can render it.
     */
    const val DB_FLOOR = -96.0

    fun dbfs(pcm: ShortArray, length: Int = pcm.size): Double {
        if (length <= 0) return DB_FLOOR
        var sum = 0.0
        for (i in 0 until length) {
            val v = pcm[i].toDouble()
            sum += v * v
        }
        val rms = kotlin.math.sqrt(sum / length)
        if (rms <= 0.0) return DB_FLOOR
        return (20.0 * log10(rms / Short.MAX_VALUE)).coerceIn(DB_FLOOR, 0.0)
    }

    /** Meter fraction 0..1 for a dBFS reading. */
    fun dbToFraction(db: Double): Float =
        ((db - DB_FLOOR) / (0.0 - DB_FLOOR)).coerceIn(0.0, 1.0).toFloat()

    fun dbLabel(db: Double): String = "%.1f dBFS".format(db)
}
