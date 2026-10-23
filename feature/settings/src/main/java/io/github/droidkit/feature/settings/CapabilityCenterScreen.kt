package io.github.droidkit.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.common.Capability
import io.github.droidkit.core.privileges.AdbPairingState
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.components.DroidKitCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.components.StatusBadge
import io.github.droidkit.core.ui.components.StatusKind
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Capability Center (spec `23_CAPABILITY_ADVANCED_ACCESS_V2`): one screen
 * listing every registered capability provider — status, what it can serve,
 * limitations and the path to set it up. Renders only real providers; the
 * spec's Shizuku row is superseded by the native wireless-ADB provider.
 */
@Composable
fun CapabilityCenterScreen(
    onBack: () -> Unit = {},
    onOpenAdb: () -> Unit = {},
    viewModel: CapabilityCenterViewModel = hiltViewModel(),
) {
    val statuses by viewModel.providerStatuses.collectAsState()
    val grantBusy by viewModel.grantBusy.collectAsState()
    val adbPairing by viewModel.adbPairingState.collectAsState()
    val adbAutoConnect by viewModel.adbAutoConnect.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(topBar = { DroidKitTopAppBar("Capability Center", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().contentWidthCap(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "DroidKit routes every elevated action through a capability — " +
                        "tools never pick a provider themselves.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DroidKitTheme.colors.textSecondary,
                )
            }

            ProviderKind.entries.forEach { kind ->
                val status = statuses[kind] ?: return@forEach
                item {
                    ProviderCard(
                        kind = kind,
                        status = status,
                        capabilities = viewModel.providerCapabilities[kind].orEmpty(),
                        adbPairing = if (kind == ProviderKind.ADB) adbPairing else null,
                        adbAutoConnect = if (kind == ProviderKind.ADB) adbAutoConnect else null,
                        adbSessionSummary = if (kind == ProviderKind.ADB) {
                            viewModel.adbSessionSummary()
                        } else {
                            null
                        },
                        grantBusy = grantBusy,
                        onGrant = { viewModel.requestGrant(kind) },
                        onOpenAdb = onOpenAdb,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderCard(
    kind: ProviderKind,
    status: ProviderStatus,
    capabilities: Set<Capability>,
    adbPairing: AdbPairingState?,
    adbAutoConnect: Boolean?,
    adbSessionSummary: String?,
    grantBusy: Boolean,
    onGrant: () -> Unit,
    onOpenAdb: () -> Unit,
) {
    val c = DroidKitTheme.colors
    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        kind.displayName(),
                        style = MaterialTheme.typography.titleSmall,
                        color = c.textPrimary,
                    )
                }
                StatusBadge(status.kindBadge(), status.shortLabel())
            }

            status.detailLine()?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                )
            }

            if (capabilities.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SectionHeader("Can serve")
                    capabilities.sortedBy { it.name }.forEach { cap ->
                        Text(
                            "· ${cap.displayName()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary,
                        )
                    }
                }
            }

            // ADB extras — live session + auto-connect state.
            if (kind == ProviderKind.ADB) {
                adbSessionSummary?.let {
                    Text(
                        "Session: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
                adbPairing?.guidedLine()?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
                adbAutoConnect?.let {
                    Text(
                        "Auto-connect ${if (it) "on" else "off"} — reconnects when " +
                            "wireless debugging is available",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.faint,
                    )
                }
            }

            kind.limitations()?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.faint,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (status is ProviderStatus.NeedsSetup) {
                    TextButton(onClick = onGrant, enabled = !grantBusy) {
                        Text(if (grantBusy) "Working…" else "Grant")
                    }
                }
                if (kind == ProviderKind.ADB) {
                    TextButton(onClick = onOpenAdb) { Text("Open ADB Lab") }
                }
            }
        }
    }
}

private fun ProviderKind.displayName(): String = when (this) {
    ProviderKind.STANDARD -> "Standard Android"
    ProviderKind.ADB -> "Wireless ADB"
    ProviderKind.ROOT -> "Root"
}

private fun ProviderStatus.kindBadge(): StatusKind = when (this) {
    ProviderStatus.Ready -> StatusKind.Success
    is ProviderStatus.NeedsSetup -> StatusKind.SetupRequired
    is ProviderStatus.Unavailable -> StatusKind.Unsupported
}

private fun ProviderStatus.shortLabel(): String = when (this) {
    ProviderStatus.Ready -> "Ready"
    is ProviderStatus.NeedsSetup -> "Setup required"
    is ProviderStatus.Unavailable -> "Unavailable"
}

private fun ProviderStatus.detailLine(): String? = when (this) {
    ProviderStatus.Ready -> null
    is ProviderStatus.NeedsSetup -> instructions
    is ProviderStatus.Unavailable -> reason
}

private fun Capability.displayName(): String = when (this) {
    Capability.STANDARD_API -> "Standard Android APIs"
    Capability.RUNTIME_PERMISSION -> "Runtime permissions"
    Capability.ELEVATED_SHELL -> "Elevated shell actions"
    Capability.ADB_SHELL -> "ADB shell"
    Capability.ROOT -> "Root shell"
}

private fun ProviderKind.limitations(): String? = when (this) {
    ProviderKind.STANDARD ->
        "Runs with no extra access — read-only device and app information. " +
            "Cannot perform elevated actions."
    ProviderKind.ADB ->
        "Needs Wireless debugging enabled and a one-time pairing. Commands run " +
            "as the ADB shell user — broad but not full system access."
    ProviderKind.ROOT ->
        "Only on rooted devices. Broadest access; used only when a tool " +
            "explicitly needs it."
}

private fun AdbPairingState.guidedLine(): String? = when (this) {
    is AdbPairingState.Connected -> "Connected — $summary"
    is AdbPairingState.SearchingPairing -> "Looking for the pairing service…"
    is AdbPairingState.SearchingConnect -> "Looking for the connect service…"
    is AdbPairingState.EndpointFound -> "Endpoint found — ready to pair"
    is AdbPairingState.Pairing -> "Pairing…"
    is AdbPairingState.Paired -> "Paired — $summary"
    is AdbPairingState.Connecting -> "Connecting…"
    is AdbPairingState.Failed -> "Last attempt failed — $message"
    AdbPairingState.Idle -> null
}
