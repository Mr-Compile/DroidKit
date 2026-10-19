package io.github.droidkit.core.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.privileges.ElevatedOps
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** One rendered chat turn; assistant turns may carry validated proposals. */
data class ChatTurn(
    val role: String,
    val text: String,
    val proposals: List<ValidatedProposal> = emptyList(),
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val useCase: AiUseCase,
    private val store: AiSettingsStore,
    private val validator: AiActionValidator,
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    private val _turns = MutableStateFlow<List<ChatTurn>>(emptyList())
    val turns: StateFlow<List<ChatTurn>> = _turns.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _gate = MutableStateFlow<AiError?>(null)
    val gate: StateFlow<AiError?> = _gate.asStateFlow()

    init {
        viewModelScope.launch {
            store.enabled.collect { enabled ->
                _gate.value = when {
                    !enabled -> AiError.DISABLED
                    !store.hasKey.first() -> AiError.NO_KEY
                    else -> null
                }
            }
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _busy.value = true
            _turns.value += ChatTurn("user", trimmed)
            val history = _turns.value.map { AiMessage(it.role, it.text) }
            val result = useCase.chat(history)
            val turn = when {
                result.error != null -> ChatTurn("assistant", errorText(result.error))
                else -> {
                    val parsed = AiProposalParser.parse(result.text.orEmpty())
                    ChatTurn(
                        role = "assistant",
                        text = result.text.orEmpty(),
                        proposals = if (parsed.isEmpty()) emptyList()
                        else validator.validateAll(parsed),
                    )
                }
            }
            _turns.value += turn
            _busy.value = false
        }
    }

    /** Confirmed proposal tap — navigate for tools, run for read-only elevated. */
    fun runProposal(turn: ChatTurn, vp: ValidatedProposal, onNavigate: (String) -> Unit) {
        when (val p = vp.proposal) {
            is AiProposal.OpenTool ->
                validator.toolRoute(p.toolId)?.let(onNavigate)
            is AiProposal.ReadOnlyElevated -> viewModelScope.launch {
                val r = elevatedOps.execute(p.action)
                _turns.value += ChatTurn(
                    "assistant",
                    if (r.ok) "```\n${r.stdout.take(1_500)}\n```"
                    else "Command failed (exit ${r.exitCode}): ${r.stderr.take(300)}",
                )
            }
        }
    }

    private fun errorText(e: AiError) = when (e) {
        AiError.DISABLED -> "AI is disabled — enable it in Settings."
        AiError.NO_KEY -> "No API key stored — add one in Settings."
        AiError.OFFLINE -> "Device is offline."
        AiError.AUTH_ERROR -> "Authentication failed — check the key."
        AiError.RATE_LIMITED -> "Rate limited — try again later."
        AiError.MALFORMED_RESPONSE -> "Unexpected response shape."
        AiError.PROVIDER_ERROR -> "Provider error."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    onNavigate: (String) -> Unit,
    viewModel: AiChatViewModel = hiltViewModel(),
) {
    val turns by viewModel.turns.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val gate by viewModel.gate.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(turns.size) {
        if (turns.isNotEmpty()) listState.animateScrollToItem(turns.size - 1)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("DroidKit Chat") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (gate != null) {
                Text(
                    when (gate) {
                        AiError.DISABLED -> "Enable DroidKit Intelligence in Settings to chat."
                        AiError.NO_KEY -> "Add a Groq API key in Settings to chat."
                        else -> "AI unavailable."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
                return@Column
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(turns) { turn ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                if (turn.role == "user") "You" else "DroidKit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(turn.text, style = MaterialTheme.typography.bodyMedium)
                            turn.proposals.forEach { vp ->
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        when (val p = vp.proposal) {
                                            is AiProposal.OpenTool -> "Open ${p.toolId}"
                                            is AiProposal.ReadOnlyElevated -> p.action.command()
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                    )
                                    TextButton(
                                        onClick = { viewModel.runProposal(turn, vp, onNavigate) },
                                        enabled = vp.allowed,
                                    ) { Text(if (vp.allowed) "Confirm" else "Blocked") }
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
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask about this device…") },
                    enabled = !busy,
                    singleLine = true,
                )
                OutlinedButton(
                    onClick = {
                        viewModel.send(input)
                        input = ""
                    },
                    enabled = !busy && input.isNotBlank(),
                ) { Text(if (busy) "…" else "Send") }
            }
        }
    }
}
