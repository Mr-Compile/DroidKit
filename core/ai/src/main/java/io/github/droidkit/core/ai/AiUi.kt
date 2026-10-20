package io.github.droidkit.core.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

/**
 * Drop-in "Explain with AI" affordance. Renders nothing when AI is off or
 * no key is stored; otherwise asks the assistant over [fields] and shows the
 * result (plus any validated action proposals) in a dialog.
 */
@Composable
fun AiExplainButton(
    assistant: AiAssistant,
    fields: Map<String, String?>,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AiAssistViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    if (!state.enabled || !state.hasKey) return

    OutlinedButton(
        onClick = { viewModel.ask(assistant, fields) },
        enabled = !state.busy,
        modifier = modifier,
    ) {
        Text(if (state.busy) "Asking…" else "Explain with AI")
    }

    AiResultDialog(viewModel = viewModel, onNavigate = onNavigate)
}

/**
 * Inline "AI summary" card for result/diagnostic screens. Renders nothing
 * when AI is off or no key is stored. Auto mode (Settings → Intelligence →
 * Auto-summarize) fires once per screen view on the first populated
 * [fields] snapshot; otherwise a manual button is shown. Refresh re-runs
 * against the values currently on screen.
 */
@Composable
fun AiSummaryCard(
    screenTitle: String,
    fields: Map<String, String?>,
    modifier: Modifier = Modifier,
    viewModel: AiAssistViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    if (!state.enabled || !state.hasKey) return

    Card(modifier = modifier.fillMaxWidth()) {
        AiSummaryContent(
            screenTitle,
            fields,
            viewModel = viewModel,
            modifier = Modifier.padding(16.dp),
        )
    }
}

/**
 * Card-free variant of [AiSummaryCard] for embedding inside an existing
 * card/section. Same gating and behavior; renders nothing when AI is off.
 */
@Composable
fun AiSummaryContent(
    screenTitle: String,
    fields: Map<String, String?>,
    modifier: Modifier = Modifier,
    viewModel: AiAssistViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val summary by viewModel.summary.collectAsState()
    if (!state.enabled || !state.hasKey) return

    val hasFields = fields.values.any { !it.isNullOrBlank() }
    LaunchedEffect(state.autoSummarize, hasFields) {
        if (state.autoSummarize && hasFields) {
            viewModel.summarize(screenTitle, fields, auto = true)
        }
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.SmartToy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                "AI summary",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            )
            IconButton(
                onClick = { viewModel.summarize(screenTitle, fields) },
                enabled = hasFields && !summary.busy,
            ) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = "Refresh AI summary",
                )
            }
        }
        when {
            summary.busy -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("Summarizing…", style = MaterialTheme.typography.bodyMedium)
            }
            summary.result?.text != null -> Text(
                summary.result!!.text!!,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            summary.result?.error != null -> Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    summary.result!!.error!!.describe(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(
                    onClick = { viewModel.summarize(screenTitle, fields) },
                    enabled = hasFields,
                ) { Text("Try again") }
            }
            else -> OutlinedButton(
                onClick = { viewModel.summarize(screenTitle, fields) },
                enabled = hasFields,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(if (hasFields) "Summarize with AI" else "Nothing to summarize yet")
            }
        }
    }
}

/** Result + proposal dialog shared by the button and the chat screen. */
@Composable
fun AiResultDialog(
    viewModel: AiAssistViewModel,
    onNavigate: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val proposals by viewModel.proposals.collectAsState()
    val elevatedOutcome by viewModel.elevatedOutcome.collectAsState()

    val result = state.result ?: return
    AlertDialog(
        onDismissRequest = { viewModel.dismissResult() },
        title = { Text("DroidKit Intelligence") },
        text = {
            Column {
                result.error?.let {
                    Text(
                        it.describe(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                result.text?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
                if (proposals.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        proposals.forEach { vp ->
                            val label = when (val p = vp.proposal) {
                                is AiProposal.OpenTool -> "Open ${p.toolId}"
                                is AiProposal.ReadOnlyElevated -> p.action.command()
                            }
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(
                                    onClick = { viewModel.executeProposal(vp, onNavigate) },
                                    enabled = vp.allowed,
                                ) {
                                    Text(if (vp.allowed) "Run" else "Blocked")
                                }
                            }
                            vp.blockedReason?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
                elevatedOutcome?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.dismissResult() }) { Text("Close") }
        },
    )
}
