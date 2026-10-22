package io.github.droidkit.feature.diagnostics.tests

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import io.github.droidkit.core.ui.components.DroidKitButton
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest
import java.io.File

private const val RECORD_MS = 3000L

@Composable
fun MicrophoneTestScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
    autoResult: Boolean = true,
) {
    PermissionGate(permission = test.permission!!, onResult = onResult, autoResult = autoResult) {
        MicContent(test, onResult)
    }
}

@Composable
private fun MicContent(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    val context = LocalContext.current
    var recording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            recorder?.release()
            player?.release()
        }
    }

    fun startRecording() {
        error = null
        val file = File(context.cacheDir, "mic_test.m4a")
        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            rec.setAudioSource(MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            rec.setOutputFile(file.absolutePath)
            rec.prepare()
            rec.start()
            recorder = rec
            recording = true
            recordedFile = file
        } catch (e: Exception) {
            error = "Recorder failed to start: ${e.message ?: e.javaClass.simpleName}"
            rec.release()
        }
    }

    fun stopRecording() {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            error = "Recording stopped with error: ${e.message ?: e.javaClass.simpleName}"
        }
        recorder?.release()
        recorder = null
        recording = false
    }

    fun playRecording() {
        val file = recordedFile ?: return
        player?.release()
        val p = MediaPlayer()
        try {
            p.setDataSource(file.absolutePath)
            p.prepare()
            p.start()
            player = p
        } catch (e: Exception) {
            error = "Playback failed: ${e.message ?: e.javaClass.simpleName}"
            p.release()
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DroidKitButton(
                    onClick = { if (recording) stopRecording() else startRecording() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (recording) "Stop" else "Record ${RECORD_MS / 1000}s+")
                }
                DroidKitOutlinedButton(
                    onClick = ::playRecording,
                    enabled = recordedFile != null && !recording,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Play back")
                }
            }
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val detail = listOfNotNull(
                    note,
                    error?.let { "error: $it" },
                    if (recordedFile != null) "recording captured" else "no recording captured",
                ).joinToString("; ")
                onResult(state, detail)
            },
        )
    }
}
