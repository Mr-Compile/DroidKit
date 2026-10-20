package io.github.droidkit.feature.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

private fun deviceTypeName(type: Int): String = when (type) {
    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Earpiece"
    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Built-in speaker"
    AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired headset"
    AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired headphones"
    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
    AudioDeviceInfo.TYPE_HDMI -> "HDMI"
    AudioDeviceInfo.TYPE_HDMI_ARC -> "HDMI ARC"
    AudioDeviceInfo.TYPE_USB_DEVICE -> "USB device"
    AudioDeviceInfo.TYPE_USB_HEADSET -> "USB headset"
    AudioDeviceInfo.TYPE_USB_ACCESSORY -> "USB accessory"
    AudioDeviceInfo.TYPE_BUILTIN_MIC -> "Built-in mic"
    AudioDeviceInfo.TYPE_TELEPHONY -> "Telephony"
    AudioDeviceInfo.TYPE_BLE_HEADSET -> "BLE headset"
    AudioDeviceInfo.TYPE_BLE_SPEAKER -> "BLE speaker"
    AudioDeviceInfo.TYPE_BLE_BROADCAST -> "BLE broadcast"
    else -> "Type $type"
}

private fun modeName(mode: Int): String = when (mode) {
    AudioManager.MODE_NORMAL -> "Normal"
    AudioManager.MODE_RINGTONE -> "Ringtone"
    AudioManager.MODE_IN_CALL -> "In call"
    AudioManager.MODE_IN_COMMUNICATION -> "In communication"
    AudioManager.MODE_CALL_SCREENING -> "Call screening"
    else -> "Mode $mode"
}

// isSpeakerphoneOn / isBluetoothScoOn are deprecated on API 31+ but remain the
// only reads on API 28–30; still accurate where the field exists.
@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRoutingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    Scaffold(topBar = { DroidKitTopAppBar("Audio Routing", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                if (am == null) {
                    SectionCard(title = "Audio") {
                        Text(
                            "AudioManager unavailable (NOT_SUPPORTED).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                } else {
                    SectionCard(title = "Active state") {
                        InfoRow("Mode", modeName(am.mode), copyable = false)
                        InfoRow("Speakerphone", am.isSpeakerphoneOn.toString(), copyable = false)
                        InfoRow("Bluetooth SCO", am.isBluetoothScoOn.toString(), copyable = false)
                        InfoRow(
                            "Music stream",
                            "${am.getStreamVolume(AudioManager.STREAM_MUSIC)}/${am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)}",
                            copyable = false,
                        )
                        InfoRow("Music muted", am.isStreamMute(AudioManager.STREAM_MUSIC).toString(), copyable = false)
                        val caps = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                            .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                        val rates = caps?.sampleRates?.takeIf { it.isNotEmpty() }
                            ?.joinToString { "$it Hz" }
                        InfoRow("Speaker sample rates", rates)
                    }
                }
            }
            if (am != null) {
                val outputs = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
                val inputs = am.getDevices(AudioManager.GET_DEVICES_INPUTS).toList()
                item {
                    SectionCard(title = "Output devices (${outputs.size})") {
                        outputs.forEach { d ->
                            InfoRow(
                                deviceTypeName(d.type),
                                listOfNotNull(
                                    d.productName?.toString()?.takeIf { it.isNotBlank() },
                                    "id ${d.id}",
                                ).joinToString(" · "),
                                copyable = false,
                            )
                        }
                    }
                }
                item {
                    SectionCard(title = "Input devices (${inputs.size})") {
                        inputs.forEach { d ->
                            InfoRow(
                                deviceTypeName(d.type),
                                listOfNotNull(
                                    d.productName?.toString()?.takeIf { it.isNotBlank() },
                                    "id ${d.id}",
                                ).joinToString(" · "),
                                copyable = false,
                            )
                        }
                    }
                }
                item {
                    SectionCard(title = "Communication device") {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val current = runCatching { am.communicationDevice }.getOrNull()
                            InfoRow(
                                "Current",
                                current?.let { "${deviceTypeName(it.type)} — ${it.productName ?: ""}" },
                                copyable = false,
                            )
                        } else {
                            Text(
                                "Communication-device reporting requires API 31+.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
                item {
                    AiSummaryCard(
                        screenTitle = "Audio routing",
                        fields = mapOf(
                            "Mode" to modeName(am.mode),
                            "Speakerphone" to am.isSpeakerphoneOn.toString(),
                            "Bluetooth SCO" to am.isBluetoothScoOn.toString(),
                            "Music stream" to "${am.getStreamVolume(AudioManager.STREAM_MUSIC)}/${am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)}",
                            "Music muted" to am.isStreamMute(AudioManager.STREAM_MUSIC).toString(),
                            "Output devices" to outputs.joinToString { deviceTypeName(it.type) },
                            "Input devices" to inputs.joinToString { deviceTypeName(it.type) },
                        ),
                    )
                }
            }
        }
    }
}
