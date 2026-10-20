package io.github.droidkit.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

/**
 * Entry in a lab index screen. [testId] is the Phone Doctor test id the card
 * deep-links into (route `diagnose/test/<testId>`), [title]/[blurb] render on
 * the card.
 */
data class LabEntry(
    val testId: String,
    val title: String,
    val blurb: String,
)

/**
 * Landing screen for a lab area (Audio, Camera, Display). Lists the lab's
 * Phone Doctor tests as tappable cards; each test owns its own permission
 * and hardware-absence states, so no capability resolution happens here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabIndexScreen(
    title: String,
    subtitle: String,
    entries: List<LabEntry>,
    onOpenTest: (String) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(topBar = { DroidKitTopAppBar(title, onBack = onBack) }) { padding ->
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
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            items(entries, key = { it.testId }) { entry ->
                Card(
                    onClick = { onOpenTest(entry.testId) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(entry.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            entry.blurb,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
