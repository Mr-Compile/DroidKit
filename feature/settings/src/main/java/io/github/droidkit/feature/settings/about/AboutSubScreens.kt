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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.ui.components.SectionHeader

/** Shared secondary-screen scaffold: standard app bar with a back control. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutSubScreen(
    title: String,
    onBack: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
fun AboutWhatIsScreen(onBack: () -> Unit) {
    AboutSubScreen(title = "What is DroidKit?", onBack = onBack) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "DroidKit brings useful Android tools into one organized " +
                            "experience. It is designed to make common device utilities " +
                            "easier to discover and use without unnecessary complexity.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "It includes device inspection, guided diagnostics, hardware " +
                            "labs, app and permission inspection, connectivity tools " +
                            "and report export — all kept honest about what the device " +
                            "actually reports.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun AboutFeaturesScreen(
    onBack: () -> Unit,
    onOpenTool: (String) -> Unit,
    viewModel: AboutViewModel = hiltViewModel(),
) {
    AboutSubScreen(title = "Features", onBack = onBack) {
        viewModel.toolsByCategory.forEach { (category, tools) ->
            item { SectionHeader(category.label()) }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        tools.forEach { tool ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenTool(tool.route) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        tool.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        tool.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ToolCategory.label(): String = when (this) {
    ToolCategory.DEVICE -> "Device"
    ToolCategory.DIAGNOSTICS -> "Diagnostics"
    ToolCategory.APPS -> "Apps"
    ToolCategory.CONNECTIVITY -> "Connectivity"
    ToolCategory.HARDWARE -> "Hardware"
    ToolCategory.DEVELOPER -> "Developer"
    ToolCategory.REPORTS -> "Reports"
}

@Composable
fun AboutWhatsNewScreen(onBack: () -> Unit) {
    AboutSubScreen(title = "What's New", onBack = onBack) {
        items(ReleaseNotes.releases, key = { it.version }) { release ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "DroidKit ${release.version}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        release.dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    release.highlights.forEach { highlight ->
                        Text(
                            "• $highlight",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutHelpScreen(onBack: () -> Unit) {
    AboutSubScreen(title = "Help & FAQ", onBack = onBack) {
        HelpContent.categories.forEach { category ->
            item { SectionHeader(category.title) }
            items(category.entries, key = { it.question }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            entry.question,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            entry.answer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutLicensesScreen(onBack: () -> Unit) {
    AboutSubScreen(title = "Open-Source Licenses", onBack = onBack) {
        item {
            Text(
                "DroidKit ships with these third-party components.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        items(OpenSourceLicenses.entries, key = { it.component }) { entry ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(entry.component, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "© ${entry.copyrightHolder} — ${entry.license}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
