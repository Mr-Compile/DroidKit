package io.github.droidkit.feature.settings.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.SectionHeader
import kotlinx.coroutines.launch

/**
 * Settings → Privacy Center. Every statement here reflects real behavior:
 * local stores (Room + DataStore), the opt-in Groq path through
 * AiContextBuilder/PrivacyFilter, and the manual GitHub update check.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyCenterScreen(
    onBack: () -> Unit,
    onOpenPermissions: () -> Unit,
    viewModel: PrivacyCenterViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val favoritesCount by viewModel.favoritesCount.collectAsState()
    val recentsCount by viewModel.recentsCount.collectAsState()
    val elevatedLogCount by viewModel.elevatedLogCount.collectAsState()
    val aiEnabled by viewModel.aiEnabled.collectAsState()

    fun notify(text: String) = scope.launch { snackbar.showSnackbar(text) }

    Scaffold(
        topBar = { DroidKitTopAppBar("Privacy Center", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionHeader("On this device") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Diagnostic sessions, reports, favorites, recent tools, " +
                                "preferences and the elevated-action history are stored " +
                                "on this device only.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        PrivacyDataRow(
                            label = "Favorite tools",
                            count = favoritesCount,
                            actionLabel = "Clear",
                            onAction = {
                                viewModel.clearFavorites()
                                notify("Favorites cleared")
                            },
                        )
                        PrivacyDataRow(
                            label = "Recent tools and usage counters",
                            count = recentsCount,
                            actionLabel = "Clear",
                            onAction = {
                                viewModel.clearRecents()
                                notify("Recent tools cleared")
                            },
                        )
                        PrivacyDataRow(
                            label = "Elevated action history",
                            count = elevatedLogCount,
                            actionLabel = "Clear",
                            onAction = {
                                viewModel.clearElevatedLog()
                                notify("Elevated history cleared")
                            },
                        )
                        Text(
                            "Sessions and reports are deleted individually from the " +
                                "Reports screen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            item { SectionHeader("Network activity") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PrivacyInfoRow(
                            title = "DroidKit Intelligence (Groq)",
                            detail = if (aiEnabled) {
                                "On. Sends a minimized, redacted context to Groq " +
                                    "only when you use an AI action."
                            } else {
                                "Off. Nothing is sent — enable it in Settings → " +
                                    "DroidKit Intelligence with your own API key."
                            },
                        )
                        PrivacyInfoRow(
                            title = "Update checks",
                            detail = "Manual only — contacts the GitHub Releases API " +
                                "when you tap Check for updates in About DroidKit.",
                        )
                        PrivacyInfoRow(
                            title = "Everything else",
                            detail = "All tools and diagnostics work fully offline. " +
                                "No analytics or crash reporting leave the device.",
                        )
                    }
                }
            }

            item { SectionHeader("AI requests") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "When AI features run, DroidKit sends only the context " +
                                "the action needs — device or test summaries you chose " +
                                "to explain. Keys, tokens, private keys, MAC addresses " +
                                "and serial numbers are stripped before anything leaves.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "Your API key is stored encrypted on this device and is " +
                                "never included in requests to anything but the " +
                                "provider's own API.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            item { SectionHeader("Controls & policies") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PrivacyNavRow(
                            title = "Permissions & Access",
                            subtitle = "See what Android permissions DroidKit uses",
                            onClick = onOpenPermissions,
                        )
                        val privacy = AboutLinks.privacyPolicyUrl
                        PrivacyNavRow(
                            title = "Privacy Policy",
                            subtitle = privacy ?: "Not published yet",
                            external = privacy != null,
                            onClick = {
                                if (privacy != null) {
                                    if (!openExternalUrl(context, privacy)) {
                                        notify("Couldn't open this page. Please try again.")
                                    }
                                } else {
                                    notify("The privacy policy isn't published yet.")
                                }
                            },
                        )
                        Text(
                            "This screen describes real behavior — it is not a legal " +
                                "privacy policy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyDataRow(
    label: String,
    count: Int,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Text(
            "$label ($count)",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAction, enabled = count > 0) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun PrivacyInfoRow(title: String, detail: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text(
            detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrivacyNavRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    external: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = if (external) {
                Icons.AutoMirrored.Filled.OpenInNew
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
