package io.github.droidkit.feature.audio

import android.media.AudioManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class AudioInfoTest {

    @Test
    fun streamNames() {
        assertEquals("Music", AudioInfo.streamName(AudioManager.STREAM_MUSIC))
        assertEquals("Voice call", AudioInfo.streamName(AudioManager.STREAM_VOICE_CALL))
        assertEquals("Alarm", AudioInfo.streamName(AudioManager.STREAM_ALARM))
        assertEquals("Stream 99", AudioInfo.streamName(99))
    }

    @Test
    fun volumeStreamsCoversAllStandardTypes() {
        val streams = AudioInfo.volumeStreams()
        assertTrue(streams.contains(AudioManager.STREAM_MUSIC))
        assertTrue(streams.contains(AudioManager.STREAM_RING))
        assertTrue(streams.contains(AudioManager.STREAM_ACCESSIBILITY))
        assertEquals(streams.size, streams.distinct().size)
    }

    @Test
    fun channelNames() {
        assertEquals("Both", AudioInfo.channelName(AudioInfo.CHANNEL_BOTH))
        assertEquals("Left", AudioInfo.channelName(AudioInfo.CHANNEL_LEFT))
        assertEquals("Right", AudioInfo.channelName(AudioInfo.CHANNEL_RIGHT))
    }

    @Test
    fun sinePcmLengthAndRange() {
        val pcm = AudioInfo.sinePcm16(440.0, 1000)
        // 1 s stereo @ 44100 Hz
        assertEquals(44100 * 2, pcm.size)
        assertTrue(pcm.all { abs(it.toInt()) <= Short.MAX_VALUE })
    }

    @Test
    fun sinePcmChannelIsolation() {
        val leftOnly = AudioInfo.sinePcm16(440.0, 50, channel = AudioInfo.CHANNEL_LEFT)
        // every right-channel sample silent, some left samples non-zero
        var leftNonZero = false
        for (i in leftOnly.indices) {
            if (i % 2 == 1) assertEquals(0, leftOnly[i].toInt())
            else if (leftOnly[i].toInt() != 0) leftNonZero = true
        }
        assertTrue(leftNonZero)
        val rightOnly = AudioInfo.sinePcm16(440.0, 50, channel = AudioInfo.CHANNEL_RIGHT)
        for (i in rightOnly.indices) {
            if (i % 2 == 0) assertEquals(0, rightOnly[i].toInt())
        }
    }

    @Test
    fun sinePcmRejectsBadInput() {
        assertThrows(IllegalArgumentException::class.java) { AudioInfo.sinePcm16(0.0, 100) }
        assertThrows(IllegalArgumentException::class.java) { AudioInfo.sinePcm16(440.0, 0) }
    }

    @Test
    fun dbfsMath() {
        assertEquals(AudioInfo.DB_FLOOR, AudioInfo.dbfs(ShortArray(0)), 0.01)
        assertEquals(AudioInfo.DB_FLOOR, AudioInfo.dbfs(ShortArray(64)), 0.01)
        // full-scale square wave ≈ 0 dBFS
        val loud = ShortArray(128) { Short.MAX_VALUE }
        assertEquals(0.0, AudioInfo.dbfs(loud), 0.01)
        // full-scale sine ≈ -3.01 dBFS; quarter amplitude adds ~-12 dB
        val quarter = ShortArray(512) { i ->
            (Short.MAX_VALUE * 0.25 * kotlin.math.sin(i * 0.3)).toInt().toShort()
        }
        val db = AudioInfo.dbfs(quarter)
        assertTrue(db in -16.0..-14.0)
    }

    @Test
    fun meterFractionAndLabel() {
        assertEquals(0f, AudioInfo.dbToFraction(AudioInfo.DB_FLOOR))
        assertEquals(1f, AudioInfo.dbToFraction(0.0))
        assertEquals(0.5f, AudioInfo.dbToFraction(AudioInfo.DB_FLOOR / 2), 0.01f)
        assertEquals("-48.0 dBFS", AudioInfo.dbLabel(-48.0))
    }
}
