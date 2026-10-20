package io.github.droidkit.feature.network

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.UiProblemCard
import io.github.droidkit.feature.network.SavedNetworksViewModel.UiState

/**
 * Saved Wi-Fi networks + passwords via the root provider. The screen holds
 * the OS flag that blocks screenshots and recents thumbnails, keeps secrets
 * masked until a per-network tap, and never persists or shares them — this
 * is owner-facing credential recovery for devices (e.g. ColorOS builds)
 * that hide saved passwords in Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedNetworksScreen(
    onBack: () -> Unit,
    viewModel: SavedNetworksViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    Scaffold(topBar = { DroidKitTopAppBar("Saved Wi-Fi Networks", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Passwords stay on this screen — never stored, logged, shared, or sent to AI.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when (val s = state) {
                UiState.Loading -> item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        Text("Reading saved networks via root…")
                    }
                }
                is UiState.Failed -> item {
                    UiProblemCard(problem = s.problem, onRecovery = viewModel::onRecovery)
                }
                UiState.Dismissed -> item {
                    SectionCard(title = "Requires root") {
                        Text(
                            "Saved Wi-Fi passwords need root access on this device.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(onClick = { viewModel.refresh() }) { Text("Try again") }
                    }
                }
                UiState.Empty -> item {
                    SectionCard(title = "No saved networks") {
                        Text(
                            "The Wi-Fi store was read but no saved networks were found.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                is UiState.Ready -> {
                    s.result.sourcePath?.let { path ->
                        item {
                            Text(
                                "Source: $path",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    itemsIndexed(s.result.networks) { _, network ->
                        SavedNetworkCard(network)
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedNetworkCard(network: SavedNetwork) {
    var revealed by remember { mutableStateOf(false) }

    SectionCard(title = network.ssid) {
        InfoRow("Security", network.security, copyable = false)
        val secret = network.psk
        when {
            secret == null ->
                InfoRow("Password", "None (open / enterprise-managed)", copyable = false)
            network.pskIsHex -> {
                InfoRow(
                    "PSK (hex)",
                    if (revealed) secret else "••••••••",
                    copyable = false,
                )
                Text(
                    "Stored as the derived key — not a typed passphrase.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else ->
                InfoRow(
                    "Password",
                    if (revealed) secret else "••••••••",
                    copyable = false,
                )
        }
        if (secret != null) {
            TextButton(onClick = { revealed = !revealed }) {
                Text(if (revealed) "Hide" else "Reveal")
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
