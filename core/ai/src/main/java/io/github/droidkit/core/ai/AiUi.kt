package io.github.droidkit.core.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
