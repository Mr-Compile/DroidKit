package io.github.droidkit.feature.audio

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import io.github.droidkit.core.ui.components.DroidKitButton
import androidx.compose.material3.ExperimentalMaterial3Api
import io.github.droidkit.core.ui.components.DroidKitFilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

private class TonePlayer {
    private var track: AudioTrack? = null

    fun play(pcm: ShortArray, sampleRate: Int) {
        stop()
        val t = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build(),
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        t.write(pcm, 0, pcm.size)
        t.play()
        track = t
    }

    fun stop() {
        track?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        track = null
    }
}

/**
 * Tone generator + channel check + mic meter. Frequencies are plain sine
 * bursts through the music stream; channel check puts signal on one side only
 * to expose swapped/dead channels. Mic meter records on-device only — nothing
 * is stored or sent anywhere.
 */
@SuppressLint("MissingPermission") // mic section is gated by a runtime grant
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioLabScreen(onBack: () -> Unit) {
    var freq by remember { mutableStateOf(440) }
    var channel by remember { mutableStateOf(AudioInfo.CHANNEL_BOTH) }
    var playing by remember { mutableStateOf(false) }
    var micOn by remember { mutableStateOf(false) }
    var micDb by remember { mutableDoubleStateOf(AudioInfo.DB_FLOOR) }
    var micDenied by remember { mutableStateOf(false) }
    val player = remember { TonePlayer() }

    DisposableEffect(Unit) { onDispose { player.stop(); micOn = false } }

    val micPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) micOn = true else micDenied = true }

    LaunchedEffect(micOn) {
        if (!micOn) { micDb = AudioInfo.DB_FLOOR; return@LaunchedEffect }
        withContext(Dispatchers.IO) {
            val minBuf = AudioRecord.getMinBufferSize(
                16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            ).coerceAtLeast(1024)
            val rec = runCatching {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC, 16000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuf * 2,
                )
            }.getOrNull()
            if (rec == null) { micOn = false; return@withContext }
            val buf = ShortArray(512)
            runCatching { rec.startRecording() }
            try {
                while (isActive) {
                    val n = rec.read(buf, 0, buf.size)
                    if (n > 0) micDb = AudioInfo.dbfs(buf, n)
                }
            } finally {
                runCatching { rec.stop() }
                rec.release()
            }
        }
    }

    Scaffold(topBar = { DroidKitTopAppBar("Audio Lab", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Tone generator") {
                    Text(
                        "Sine burst through the music stream — confirms the " +
                            "output path is alive and roughly how loud it is.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        listOf(125, 440, 1000, 4000).forEach { f ->
                            DroidKitFilterChip(
                                selected = freq == f,
                                onClick = { freq = f },
                                label = { Text("$f Hz") },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (playing) {
                            DroidKitOutlinedButton(onClick = { player.stop(); playing = false }) {
                                Text("Stop")
                            }
                        } else {
                            DroidKitButton(onClick = {
                                player.play(
                                    AudioInfo.sinePcm16(freq.toDouble(), 1000, channel = channel),
                                    AudioInfo.TONE_SAMPLE_RATE,
                                )
                                playing = true
                            }) { Text("Play 1 s") }
                        }
                    }
                }
            }
            item {
                SectionCard(title = "Channel check") {
                    Text(
                        "Plays the tone on one side only — swapped or dead " +
                            "stereo channels show up immediately on headphones/speakers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        listOf(
                            AudioInfo.CHANNEL_BOTH to "Both",
                            AudioInfo.CHANNEL_LEFT to "Left only",
                            AudioInfo.CHANNEL_RIGHT to "Right only",
                        ).forEach { (c, label) ->
                            DroidKitFilterChip(
                                selected = channel == c,
                                onClick = { channel = c },
                                label = { Text(label) },
                            )
                        }
                    }
                    Text(
                        "Applies to the next tone you play.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                SectionCard(title = "Microphone meter") {
                    Text(
                        "Live input level in dBFS. Recording is on-device only " +
                            "and stops when you leave or tap Stop.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (micDenied) {
                        Text(
                            "Microphone permission denied — the meter cannot run.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { AudioInfo.dbToFraction(micDb) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(5.dp)),
                    )
                    Text(
                        AudioInfo.dbLabel(micDb),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (micOn) {
                            DroidKitOutlinedButton(onClick = { micOn = false }) { Text("Stop") }
                        } else {
                            DroidKitButton(onClick = {
                                micPerm.launch(Manifest.permission.RECORD_AUDIO)
                            }) { Text("Start meter") }
                        }
                    }
                }
            }
        }
    }
}

