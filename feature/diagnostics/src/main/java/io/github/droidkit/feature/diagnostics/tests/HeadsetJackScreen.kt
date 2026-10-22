package io.github.droidkit.feature.diagnostics.tests

import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import io.github.droidkit.core.ui.components.DroidKitCard
import androidx.compose.material3.MaterialTheme
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

/** Friendly name for a wired audio device type — pure, JVM-testable. */
fun wiredAudioTypeName(type: Int): String? = when (type) {
    AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired headset (with mic)"
    AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired headphones"
    AudioDeviceInfo.TYPE_USB_HEADSET -> "USB-C headset"
    AudioDeviceInfo.TYPE_USB_DEVICE -> "USB audio device"
    AudioDeviceInfo.TYPE_AUX_LINE -> "Aux line"
    else -> null
}

/**
 * Wired-headset insert/remove detection. Watches [AudioDeviceCallback] for
 * wired output devices appearing and disappearing, then asks the technician
 * to confirm — automated detection alone can't prove the jack works.
 */
@Composable
fun HeadsetJackScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(AudioManager::class.java) }
    var wiredNow by remember { mutableStateOf<List<String>>(emptyList()) }
    var events by remember { mutableStateOf<List<String>>(emptyList()) }
    var sawAttach by remember { mutableStateOf(false) }
    var sawDetach by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        fun wiredNames(devices: Array<AudioDeviceInfo>) = devices.mapNotNull {
            wiredAudioTypeName(it.type)
        }
        val callback = if (audioManager == null) {
            null
        } else {
            object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
                    val names = wiredNames(addedDevices)
                    if (names.isEmpty()) return
                    sawAttach = true
                    events = events + names.map { "Detected: $it" }
                    wiredNow = wiredNames(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
                    val names = wiredNames(removedDevices)
                    if (names.isEmpty()) return
                    sawDetach = true
                    events = events + names.map { "Removed: $it" }
                    wiredNow = wiredNames(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))
                }
            }
        }
        if (audioManager != null && callback != null) {
            wiredNow = wiredNames(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))
            audioManager.registerAudioDeviceCallback(callback, null)
        }
        onDispose {
            if (audioManager != null && callback != null) {
                audioManager.unregisterAudioDeviceCallback(callback)
            }
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
        DroidKitCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (audioManager == null) {
                    Text("Audio service unavailable — mark not supported.")
                } else {
                    Row { Text("Wired audio now", Modifier.weight(1f)); Text(wiredNow.joinToString(", ").ifBlank { "None" }) }
                    Row { Text("Insert seen", Modifier.weight(1f)); Text(if (sawAttach) "Yes" else "Waiting…") }
                    Row { Text("Remove seen", Modifier.weight(1f)); Text(if (sawDetach) "Yes" else "Waiting…") }
                    if (events.isNotEmpty()) {
                        Text(
                            "Events",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        events.takeLast(8).forEach {
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val detail = listOfNotNull(
                    note,
                    if (sawAttach || sawDetach) {
                        "events: ${events.joinToString(", ")}"
                    } else {
                        null
                    },
                ).joinToString("; ")
                onResult(state, detail.ifBlank { null })
            },
        )
    }
}
