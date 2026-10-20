package io.github.droidkit.feature.diagnostics.tests

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.nfc.NfcManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest

private data class CapabilityReading(
    val supported: Boolean,
    val detail: String,
    val stateLabel: String,
    val stateValue: String?,
)

/**
 * Objective capability check: hardware absent → NOT_SUPPORTED; hardware
 * present → PASS with the enabled/disabled state recorded in the note.
 * These are capability reads, not sensory judgments — auto-emitted in the
 * guided runner ([autoResult] = true). Standalone keeps the reading on
 * screen and lets the user back out.
 */
@Composable
fun ConnectivityCheckScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
    autoResult: Boolean = true,
) {
    val context = LocalContext.current
    val reading = remember(test.capabilityKey) { readCapability(context, test.capabilityKey) }

    if (autoResult) {
        LaunchedEffect(test.id) {
            if (reading.supported) {
                onResult(
                    TestResultState.PASS,
                    "${reading.stateLabel}: ${reading.stateValue ?: "unknown"}",
                )
            } else {
                onResult(TestResultState.NOT_SUPPORTED, reading.detail)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(test.title, style = MaterialTheme.typography.titleMedium)
        Text(
            test.instructions,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row {
                    Text("Hardware capability", modifier = Modifier.weight(1f))
                    Text(if (reading.supported) "Present" else "Absent")
                }
                Row {
                    Text(reading.stateLabel, modifier = Modifier.weight(1f))
                    Text(reading.stateValue ?: "Not applicable")
                }
                Text(
                    if (autoResult) {
                        if (reading.supported) "Result recorded: PASS"
                        else "Result recorded: NOT SUPPORTED"
                    } else {
                        if (reading.supported) "Result: PASS" else "Result: NOT SUPPORTED"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun readCapability(context: Context, key: String?): CapabilityReading {
    val pm = context.packageManager
    return when (key) {
        "wifi" -> CapabilityReading(
            supported = pm.hasSystemFeature(PackageManager.FEATURE_WIFI),
            detail = "Device reports no Wi-Fi feature",
            stateLabel = "Radio enabled",
            stateValue = (context.getSystemService(Context.WIFI_SERVICE) as? WifiManager)
                ?.isWifiEnabled?.let { if (it) "yes" else "no" },
        )
        "bluetooth" -> CapabilityReading(
            supported = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            detail = "Device reports no Bluetooth feature",
            stateLabel = "Adapter enabled",
            stateValue = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
                ?.adapter?.isEnabled?.let { if (it) "yes" else "no" },
        )
        "location" -> CapabilityReading(
            supported = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS),
            detail = "Device reports no GNSS location feature",
            stateLabel = "Location enabled",
            stateValue = (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
                ?.isLocationEnabled?.let { if (it) "yes" else "no" },
        )
        "nfc" -> CapabilityReading(
            supported = pm.hasSystemFeature(PackageManager.FEATURE_NFC),
            detail = "Device reports no NFC feature",
            stateLabel = "NFC enabled",
            stateValue = (context.getSystemService(Context.NFC_SERVICE) as? NfcManager)
                ?.defaultAdapter?.isEnabled?.let { if (it) "yes" else "no" },
        )
        "usb" -> {
            val host = pm.hasSystemFeature(PackageManager.FEATURE_USB_HOST)
            val accessory = pm.hasSystemFeature(PackageManager.FEATURE_USB_ACCESSORY)
            CapabilityReading(
                supported = host || accessory,
                detail = "Device reports no USB host or accessory feature",
                stateLabel = "USB modes",
                stateValue = buildList {
                    if (host) add("host")
                    if (accessory) add("accessory")
                }.joinToString(", ").ifBlank { null },
            )
        }
        else -> CapabilityReading(
            supported = false,
            detail = "Unknown capability key: $key",
            stateLabel = "State",
            stateValue = null,
        )
    }
}
