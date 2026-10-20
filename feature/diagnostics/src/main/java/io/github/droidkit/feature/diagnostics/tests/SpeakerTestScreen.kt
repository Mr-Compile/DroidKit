package io.github.droidkit.feature.diagnostics.tests

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest
import kotlin.math.sin

private const val SAMPLE_RATE = 22050
private const val TONE_HZ = 440.0
private const val DURATION_S = 1

/** Plays a generated 440 Hz sine via AudioTrack — no bundled assets. */
@Composable
fun SpeakerTestScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    var playCount by remember { mutableIntStateOf(0) }

    fun playTone() {
        val samples = SAMPLE_RATE * DURATION_S
        val pcm = ShortArray(samples) { i ->
            (sin(2.0 * Math.PI * TONE_HZ * i / SAMPLE_RATE) * Short.MAX_VALUE * 0.5).toInt()
                .toShort()
        }
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(pcm, 0, pcm.size)
        track.play()
        playCount++
        // Track releases when GC'd; static mode plays to completion (~1 s).
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(test.title, style = MaterialTheme.typography.titleMedium)
            Text(
                test.instructions,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Button(onClick = ::playTone, modifier = Modifier.fillMaxWidth()) {
                Text("Play 440 Hz tone (1 s)")
            }
            if (playCount > 0) {
                Text(
                    "Played $playCount time(s)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val played =
                    if (playCount == 0) "tone never played" else "tone played $playCount time(s)"
                onResult(state, listOfNotNull(note, played).joinToString("; "))
            },
        )
    }
}
