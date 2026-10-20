package io.github.droidkit.core.ai

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

/** State for the shared "Explain with AI" affordance. */
data class AiAssistState(
    val busy: Boolean = false,
    val enabled: Boolean = false,
    val hasKey: Boolean = false,
    val autoSummarize: Boolean = false,
    val result: AiResult? = null,
)

/** State for the inline [AiSummaryCard] — kept separate so a summary never pops the explain dialog. */
data class AiSummaryState(
    val busy: Boolean = false,
    val result: AiResult? = null,
)

@HiltViewModel
class AiAssistViewModel @Inject constructor(
    private val useCase: AiUseCase,
    private val store: AiSettingsStore,
    private val validator: AiActionValidator,
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    private val _state = MutableStateFlow(AiAssistState())
    val state: StateFlow<AiAssistState> = _state.asStateFlow()

    /** Proposals parsed from the latest assistant/chat reply. */
    private val _proposals = MutableStateFlow<List<ValidatedProposal>>(emptyList())
    val proposals: StateFlow<List<ValidatedProposal>> = _proposals.asStateFlow()

    private val _elevatedOutcome = MutableStateFlow<String?>(null)
    val elevatedOutcome: StateFlow<String?> = _elevatedOutcome.asStateFlow()

    private val _summary = MutableStateFlow(AiSummaryState())
    val summary: StateFlow<AiSummaryState> = _summary.asStateFlow()

    /** Auto summaries fire once per screen view; manual refresh bypasses. */
    private var autoFired = false

    init {
        viewModelScope.launch {
            store.enabled.collect { enabled ->
                _state.value = _state.value.copy(
                    enabled = enabled,
                    hasKey = store.hasKey.first(),
                )
            }
        }
        viewModelScope.launch {
            store.autoSummarize.collect { auto ->
                _state.value = _state.value.copy(autoSummarize = auto)
            }
        }
    }

    /** Run an assistant over screen-supplied fields. */
    fun ask(assistant: AiAssistant, fields: Map<String, String?>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, result = null)
            _proposals.value = emptyList()
            val result = useCase.assist(assistant, fields)
            _state.value = _state.value.copy(busy = false, result = result)
            result.text?.let { parseProposals(it) }
        }
    }

    /**
     * Summarize the values a screen shows. With [auto] = true this runs at
     * most once per view-model instance (and only when the user enabled
     * auto-summarize); manual calls always run.
     */
    fun summarize(screenTitle: String, fields: Map<String, String?>, auto: Boolean = false) {
        if (auto && (autoFired || !_state.value.autoSummarize)) return
        if (auto) autoFired = true
        if (_summary.value.busy) return
        viewModelScope.launch {
            _summary.value = AiSummaryState(busy = true)
            _summary.value = AiSummaryState(result = useCase.summarizeValues(screenTitle, fields))
        }
    }

    private fun parseProposals(text: String) {
        val parsed = AiProposalParser.parse(text)
        if (parsed.isNotEmpty()) {
            _proposals.value = validator.validateAll(parsed)
        }
    }

    /**
     * Executes a confirmed read-only elevated proposal. Navigation proposals
     * return the route instead — the caller navigates.
     */
    fun executeProposal(vp: ValidatedProposal, onNavigate: (String) -> Unit) {
        when (val p = vp.proposal) {
            is AiProposal.OpenTool -> {
                val tool = validator.toolRoute(p.toolId)
                if (tool != null) onNavigate(tool)
            }
            is AiProposal.ReadOnlyElevated -> {
                viewModelScope.launch {
                    val r = elevatedOps.execute(p.action)
                    _elevatedOutcome.value =
                        if (r.ok) r.stdout.take(2_000)
                        else "exit ${r.exitCode}: ${r.stderr.take(400)}"
                }
            }
        }
    }

    fun dismissResult() {
        _state.value = _state.value.copy(result = null)
        _proposals.value = emptyList()
        _elevatedOutcome.value = null
    }
}
