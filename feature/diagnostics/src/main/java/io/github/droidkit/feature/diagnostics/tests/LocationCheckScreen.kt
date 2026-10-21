package io.github.droidkit.feature.diagnostics.tests

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.core.permissions.RuntimePermissionState
import io.github.droidkit.core.ui.components.rememberRuntimePermission
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

private const val FIX_TIMEOUT_MS = 20_000L

private sealed interface LiveFix {
    data object Idle : LiveFix
    data object Running : LiveFix
    data class Got(val location: Location) : LiveFix
    data object TimedOut : LiveFix
    data object NoProvider : LiveFix
}

/**
 * Location / GNSS check. Capability and radio state are objective reads; the
 * cached fix and the on-demand live fix need a location grant, so they sit
 * inside PermissionGate. Coarse is enough for a network fix — precise is an
 * in-screen upgrade (Android 12+ grants approximate separately).
 * Guided mode ([autoResult]) emits instantly from capability + cached state —
 * it never waits for a live fix. Raw coordinates render on screen only;
 * result notes carry provider, accuracy and age so reports/AI never see a
 * position.
 */
@Composable
fun LocationCheckScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
    autoResult: Boolean = true,
) {
    val context = LocalContext.current
    val hasGnss = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    }

    if (!hasGnss) {
        if (autoResult) {
            LaunchedEffect(test.id) {
                onResult(
                    TestResultState.NOT_SUPPORTED,
                    "Device reports no GNSS location feature",
                )
            }
        }
        UnsupportedLocationCard(test, autoResult)
        return
    }

    PermissionGate(
        permission = Manifest.permission.ACCESS_COARSE_LOCATION,
        onResult = onResult,
        autoResult = autoResult,
    ) {
        LocationCheckBody(test, onResult, autoResult)
    }
}

@SuppressLint("MissingPermission") // reads run inside PermissionGate(ACCESS_COARSE_LOCATION)
@Composable
private fun LocationCheckBody(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
    autoResult: Boolean,
) {
    val context = LocalContext.current
    val lm = remember { context.getSystemService(LocationManager::class.java) }
    val locationEnabled = remember { lm?.isLocationEnabled == true }
    val providers = remember { providerRows(lm) }
    val cachedFix = remember { newestFix(lm) }
    val finePerm = rememberRuntimePermission(Manifest.permission.ACCESS_FINE_LOCATION)
    val precise = finePerm.state == RuntimePermissionState.GRANTED
    val scope = rememberCoroutineScope()
    var liveFix by remember { mutableStateOf<LiveFix>(LiveFix.Idle) }

    if (autoResult) {
        LaunchedEffect(test.id) {
            onResult(TestResultState.PASS, guidedNote(locationEnabled, cachedFix))
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Text(test.title, style = MaterialTheme.typography.titleMedium)
        Text(
            test.instructions,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Hardware capability", "Present")
                InfoRow("Location enabled", if (locationEnabled) "yes" else "no")
                providers.forEach { (name, on) -> InfoRow("$name provider", if (on) "on" else "off") }
                InfoRow("Precise location", if (precise) "granted" else "approximate only")
                InfoRow("Cached fix", cachedFix?.let(::describeFix) ?: "No cached fix")
                when (val f = liveFix) {
                    LiveFix.Idle -> Unit
                    LiveFix.Running -> InfoRow("Live fix", "Requesting (up to ${FIX_TIMEOUT_MS / 1000} s)…")
                    is LiveFix.Got -> InfoRow("Live fix", describeFix(f.location))
                    LiveFix.TimedOut -> InfoRow(
                        "Live fix",
                        "No fix within ${FIX_TIMEOUT_MS / 1000} s — may be indoors or obstructed",
                    )
                    LiveFix.NoProvider -> InfoRow("Live fix", "No provider available")
                }
                Text(
                    if (autoResult) "Result recorded: PASS" else "Result: PASS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!locationEnabled) {
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text("Open location settings")
            }
        }
        if (!precise && finePerm.state != RuntimePermissionState.NOT_DECLARED) {
            TextButton(
                onClick = finePerm.request,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text("Enable precise location")
            }
        }
        Button(
            onClick = {
                val provider = pickProvider(lm, precise)
                if (provider == null) {
                    liveFix = LiveFix.NoProvider
                } else {
                    liveFix = LiveFix.Running
                    scope.launch {
                        val fix = withTimeoutOrNull(FIX_TIMEOUT_MS) { awaitFix(context, lm, provider) }
                        liveFix = fix?.let { LiveFix.Got(it) } ?: LiveFix.TimedOut
                    }
                }
            },
            enabled = locationEnabled && liveFix !is LiveFix.Running,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            Text("Get live fix")
        }
        if (!autoResult) {
            AiSummaryCard(
                screenTitle = test.title,
                fields = buildMap {
                    put("Hardware capability", "present")
                    put("Location enabled", if (locationEnabled) "yes" else "no")
                    providers.forEach { (name, on) -> put("$name provider", if (on) "on" else "off") }
                    put("Precise location", if (precise) "granted" else "approximate only")
                    put("Cached fix", cachedFix?.let(::describeFixBrief) ?: "none")
                    put("Result", "PASS")
                },
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun UnsupportedLocationCard(test: PhoneDoctorTest, autoResult: Boolean) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Text(test.title, style = MaterialTheme.typography.titleMedium)
        Text(
            test.instructions,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Hardware capability", "Absent")
                Text(
                    if (autoResult) "Result recorded: NOT SUPPORTED" else "Result: NOT SUPPORTED",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(label, modifier = Modifier.weight(1f))
        Text(value)
    }
}

@SuppressLint("MissingPermission") // caller is inside PermissionGate
private fun providerRows(lm: LocationManager?): List<Pair<String, Boolean>> = buildList {
    add("GPS" to (lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true))
    add("Network" to (lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        add("Fused" to (lm?.isProviderEnabled(LocationManager.FUSED_PROVIDER) == true))
    }
}

@SuppressLint("MissingPermission") // caller is inside PermissionGate
private fun newestFix(lm: LocationManager?): Location? =
    lm?.getProviders(true)
        ?.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
        ?.maxByOrNull { if (it.elapsedRealtimeNanos > 0L) it.elapsedRealtimeNanos else it.time }

@SuppressLint("MissingPermission") // caller is inside PermissionGate
private fun pickProvider(lm: LocationManager?, precise: Boolean): String? = when {
    precise && lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ->
        LocationManager.GPS_PROVIDER
    lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true ->
        LocationManager.NETWORK_PROVIDER
    else -> null
}

@SuppressLint("MissingPermission") // caller is inside PermissionGate
private suspend fun awaitFix(
    context: Context,
    lm: LocationManager?,
    provider: String,
): Location? {
    if (lm == null) return null
    return suspendCancellableCoroutine { cont ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val signal = CancellationSignal()
            cont.invokeOnCancellation { signal.cancel() }
            lm.getCurrentLocation(provider, signal, context.mainExecutor) { loc ->
                if (cont.isActive) cont.resume(loc)
            }
        } else {
            val listener = LocationListener { loc -> if (cont.isActive) cont.resume(loc) }
            cont.invokeOnCancellation { lm.removeUpdates(listener) }
            @Suppress("DEPRECATION")
            lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        }
    }
}

private fun guidedNote(locationEnabled: Boolean, fix: Location?): String =
    "Location enabled: ${if (locationEnabled) "yes" else "no"} · last fix: " +
        (fix?.let(::describeFixBrief) ?: "none")

/** Quality summary without coordinates — safe for notes, reports and AI fields. */
private fun describeFixBrief(loc: Location): String {
    val acc = if (loc.hasAccuracy()) ", ~${loc.accuracy.toInt()} m" else ""
    return "${loc.provider ?: "unknown"}$acc, ${formatAge(ageSeconds(loc))} ago"
}

/** Full summary including coordinates — screen only, never leaves the device. */
private fun describeFix(loc: Location): String {
    val acc = if (loc.hasAccuracy()) ", ±${loc.accuracy.toInt()} m" else ""
    return "${loc.provider ?: "unknown"}: ${formatCoord(loc.latitude)}, " +
        "${formatCoord(loc.longitude)}$acc, ${formatAge(ageSeconds(loc))} ago"
}

private fun formatCoord(value: Double): String =
    String.format(Locale.US, "%.5f", value)

private fun ageSeconds(loc: Location): Long = when {
    loc.elapsedRealtimeNanos > 0L ->
        ((SystemClock.elapsedRealtimeNanos() - loc.elapsedRealtimeNanos) / 1_000_000_000L)
            .coerceAtLeast(0)
    else -> ((System.currentTimeMillis() - loc.time) / 1000L).coerceAtLeast(0)
}

private fun formatAge(seconds: Long): String = when {
    seconds < 60 -> "${seconds}s"
    seconds < 3600 -> "${seconds / 60}m"
    else -> "${seconds / 3600}h"
}
