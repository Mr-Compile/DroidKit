package io.github.droidkit.feature.settings.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.common.DLog
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.DroidKitWordmark
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.theme.DroidKitMark
import kotlinx.coroutines.launch

internal fun openExternalUrl(context: Context, url: String): Boolean = try {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    true
} catch (e: ActivityNotFoundException) {
    DLog.w("AboutScreen", "No handler for $url", e)
    false
} catch (e: RuntimeException) {
    DLog.w("AboutScreen", "Could not open $url", e)
    false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenWhatIs: () -> Unit,
    onOpenFeatures: () -> Unit,
    onOpenWhatsNew: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenLicenses: () -> Unit,
    viewModel: AboutViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val updateState by viewModel.updateState.collectAsState()

    fun notify(text: String) = scope.launch { snackbar.showSnackbar(text) }
    fun openUrl(url: String) {
        if (!openExternalUrl(context, url)) {
            notify("Couldn't open this page. Please try again.")
        }
    }

    Scaffold(
        topBar = { DroidKitTopAppBar("About DroidKit", onBack = onBack) },
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
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DroidKitWordmark(markSize = 40.dp)
                    Text(
                        "Your Android utility toolkit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            item {
                Text(
                    "DroidKit is an Android utility toolkit designed to bring useful " +
                        "device tools, diagnostics, file utilities, and everyday Android " +
                        "features together in one clean and easy-to-use application.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            item { SectionHeader("App information") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AboutValueRow("Version", viewModel.appInfo.versionName)
                        AboutValueRow("Build number", viewModel.appInfo.versionCode.toString())
                        AboutActionRow(
                            title = "Check for updates",
                            subtitle = updateSubtitle(updateState),
                            trailing = if (
                                (updateState as? UpdateUiState.Result)?.result
                                is UpdateCheckResult.UpdateAvailable
                            ) {
                                Icons.AutoMirrored.Filled.OpenInNew
                            } else {
                                Icons.AutoMirrored.Filled.KeyboardArrowRight
                            },
                            onClick = {
                                val result = (updateState as? UpdateUiState.Result)?.result
                                if (result is UpdateCheckResult.UpdateAvailable) {
                                    openUrl(result.releaseUrl)
                                } else {
                                    viewModel.checkForUpdates()
                                }
                            },
                        )
                    }
                }
            }

            item { SectionHeader("DroidKit") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AboutActionRow("What is DroidKit?", onClick = onOpenWhatIs)
                        AboutActionRow("Features", onClick = onOpenFeatures)
                        AboutActionRow("What's New", onClick = onOpenWhatsNew)
                    }
                }
            }

            item { SectionHeader("Support") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AboutActionRow("Help & FAQ", onClick = onOpenHelp)
                        AboutActionRow(
                            title = "Report a problem",
                            subtitle = "GitHub Issues",
                            trailing = Icons.AutoMirrored.Filled.OpenInNew,
                            onClick = { openUrl(AboutLinks.GITHUB_ISSUES_NEW) },
                        )
                        AboutActionRow(
                            title = "Send feedback",
                            subtitle = "GitHub Issues",
                            trailing = Icons.AutoMirrored.Filled.OpenInNew,
                            onClick = { openUrl(AboutLinks.GITHUB_ISSUES) },
                        )
                    }
                }
            }

            item { SectionHeader("Legal") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val privacy = AboutLinks.privacyPolicyUrl
                        AboutActionRow(
                            title = "Privacy Policy",
                            trailing = if (privacy != null) {
                                Icons.AutoMirrored.Filled.OpenInNew
                            } else {
                                Icons.AutoMirrored.Filled.KeyboardArrowRight
                            },
                            onClick = {
                                if (privacy != null) {
                                    openUrl(privacy)
                                } else {
                                    notify("The privacy policy isn't published yet.")
                                }
                            },
                        )
                        val terms = AboutLinks.termsOfUseUrl
                        AboutActionRow(
                            title = "Terms of Use",
                            trailing = if (terms != null) {
                                Icons.AutoMirrored.Filled.OpenInNew
                            } else {
                                Icons.AutoMirrored.Filled.KeyboardArrowRight
                            },
                            onClick = {
                                if (terms != null) {
                                    openUrl(terms)
                                } else {
                                    notify("Terms of use aren't published yet.")
                                }
                            },
                        )
                        AboutActionRow("Open-Source Licenses", onClick = onOpenLicenses)
                    }
                }
            }

            item { SectionHeader("Developer") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AboutValueRow("Developer", AboutLinks.DEVELOPER_NAME)
                        val website = AboutLinks.websiteUrl
                        if (website != null) {
                            AboutActionRow(
                                title = "Website",
                                trailing = Icons.AutoMirrored.Filled.OpenInNew,
                                onClick = { openUrl(website) },
                            )
                        }
                        AboutActionRow(
                            title = "GitHub",
                            subtitle = "Source and releases",
                            trailing = Icons.AutoMirrored.Filled.OpenInNew,
                            onClick = { openUrl(AboutLinks.GITHUB_REPO) },
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        imageVector = DroidKitMark,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        "DroidKit",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        "Built to make Android utilities simpler.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Version ${viewModel.appInfo.versionName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

private fun updateSubtitle(state: UpdateUiState): String = when (state) {
    UpdateUiState.Idle -> "Check GitHub Releases for a newer version"
    UpdateUiState.Checking -> "Checking for updates…"
    is UpdateUiState.Result -> when (val r = state.result) {
        UpdateCheckResult.UpToDate -> "You're on the latest version."
        is UpdateCheckResult.UpdateAvailable -> "Update available: ${r.latestTag}"
        UpdateCheckResult.NoPublishedReleases -> "No published updates found yet."
        UpdateCheckResult.Offline ->
            "Couldn't check for updates. Check your internet connection and try again."
        UpdateCheckResult.Failed -> "Couldn't check for updates. Please try again later."
    }
}

/** Tappable row: whole area is one click target with merged semantics. */
@Composable
private fun AboutActionRow(
    title: String,
    subtitle: String? = null,
    trailing: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = trailing,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Non-interactive label/value row for static metadata. */
@Composable
private fun AboutValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
