package io.github.droidkit.core.ai

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.ui.components.DroidKitHairline
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.theme.DroidKitTheme
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** One rendered chat turn; assistant turns may carry proposals + follow-up chips. */
data class ChatTurn(
    val role: String,
    val text: String,
    val proposals: List<ValidatedProposal> = emptyList(),
    val suggestions: List<String> = emptyList(),
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
                    val raw = result.text.orEmpty()
                    val parsed = AiProposalParser.parse(raw)
                    val suggestions = AiProposalParser.parseSuggestions(raw)
                    var text = AiProposalParser.stripStructuredBlocks(raw)
                    if (text.isBlank() && (parsed.isNotEmpty() || suggestions.isNotEmpty())) {
                        text = "Here's what I found:"
                    }
                    ChatTurn(
                        role = "assistant",
                        text = text,
                        proposals = if (parsed.isEmpty()) emptyList()
                        else validator.validateAll(parsed),
                        suggestions = suggestions,
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

    /** Display label for a proposal chip — resolves tool ids to titles. */
    fun proposalLabel(vp: ValidatedProposal): String = when (val p = vp.proposal) {
        is AiProposal.OpenTool ->
            validator.toolTitle(p.toolId)?.let { "Open $it" } ?: "Open ${p.toolId}"
        is AiProposal.ReadOnlyElevated -> p.action.command()
    }

    private fun errorText(e: AiError) = e.describe()
}

private val USER_BUBBLE_SHAPE = RoundedCornerShape(16.dp, 16.dp, 5.dp, 16.dp)
private val AI_BUBBLE_SHAPE = RoundedCornerShape(16.dp, 16.dp, 16.dp, 5.dp)

/** Hero prompts — each exercises a real chat capability (tools + read-only elevated). */
private val STARTER_PROMPTS = listOf(
    "Check my battery health",
    "Why is my phone running slow?",
    "Which apps can see my location?",
    "Check my network connection",
    "What's using my storage?",
    "What can you do?",
)

/** Follow-up chips shown when the model returned no "suggestions" of its own. */
private val FALLBACK_FOLLOWUPS = listOf("Tell me more", "What else can you check?")

/** Language tags the model may put on fenced blocks — stripped before display. */
private val CODE_LANG_TAGS = setOf(
    "json", "kotlin", "java", "bash", "sh", "shell", "xml", "yaml", "text",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onOpenAiSettings: (() -> Unit)? = null,
    viewModel: AiChatViewModel = hiltViewModel(),
) {
    val turns by viewModel.turns.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val gate by viewModel.gate.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(turns.size, busy) {
        if (turns.isNotEmpty()) {
            listState.animateScrollToItem(turns.size - 1 + if (busy) 1 else 0)
        }
    }

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.SmartToy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text("DroidKit AI", modifier = Modifier.padding(start = 8.dp))
                    }
                },
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val currentGate = gate
            if (currentGate != null) {
                AiChatGate(
                    error = currentGate,
                    onOpenAiSettings = onOpenAiSettings,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    if (turns.isEmpty()) {
                        AiChatHero(onPrompt = viewModel::send)
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            itemsIndexed(turns) { index, turn ->
                                ChatBubble(
                                    turn = turn,
                                    showSuggestions = index == turns.lastIndex && !busy,
                                    onSuggestion = viewModel::send,
                                    onProposal = { vp ->
                                        viewModel.runProposal(turn, vp, onNavigate)
                                    },
                                    labelFor = viewModel::proposalLabel,
                                )
                            }
                            if (busy) {
                                item { TypingIndicator() }
                            }
                        }
                    }
                }
                AiChatComposer(
                    input = input,
                    onInputChange = { input = it },
                    canSend = input.isNotBlank() && !busy,
                    onSend = {
                        viewModel.send(input)
                        input = ""
                    },
                )
            }
        }
    }
}

/** Centered gate state — AI off or no key; offers a jump to Settings. */
@Composable
private fun AiChatGate(
    error: AiError,
    onOpenAiSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AssistantAvatar(size = 56.dp, iconSize = 30.dp)
        Text(
            when (error) {
                AiError.DISABLED ->
                    "DroidKit Intelligence is off — enable it in Settings to chat."
                AiError.NO_KEY ->
                    "Add your Groq API key in Settings to start chatting."
                else -> "DroidKit AI is unavailable right now."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
        if (onOpenAiSettings != null) {
            TextButton(onClick = onOpenAiSettings, modifier = Modifier.padding(top = 8.dp)) {
                Text("Open AI settings")
            }
        }
    }
}

/** Meta-style empty state: avatar, greeting and capability prompt chips. */
@Composable
private fun AiChatHero(onPrompt: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AssistantAvatar(size = 64.dp, iconSize = 36.dp)
        Text(
            "Ask DroidKit anything",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            "Device answers, tool shortcuts and read-only checks — " +
                "powered by your Groq key.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Column(
            modifier = Modifier.padding(top = 24.dp).widthIn(max = 340.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            STARTER_PROMPTS.forEach { prompt ->
                PromptChip(
                    text = prompt,
                    onClick = { onPrompt(prompt) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** One message row — bubble plus proposal chips and follow-up suggestions. */
@Composable
private fun ChatBubble(
    turn: ChatTurn,
    showSuggestions: Boolean,
    onSuggestion: (String) -> Unit,
    onProposal: (ValidatedProposal) -> Unit,
    labelFor: (ValidatedProposal) -> String,
) {
    val isUser = turn.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUser) {
            AssistantAvatar()
            Spacer(Modifier.width(8.dp))
        }
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        ) {
            if (turn.text.isNotBlank()) {
                MessageBubble(text = turn.text, isUser = isUser)
            }
            turn.proposals.forEach { vp ->
                ProposalChip(
                    proposal = vp,
                    label = labelFor(vp),
                    onClick = { onProposal(vp) },
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            if (!isUser && showSuggestions) {
                val chips = turn.suggestions.ifEmpty { FALLBACK_FOLLOWUPS }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    items(chips) { s ->
                        PromptChip(text = s, onClick = { onSuggestion(s) })
                    }
                }
            }
        }
    }
}

/** Chat bubble surface (mockup `.msg`): brand gradient for the user, faint glass tile + hairline for the AI. */
@Composable
private fun MessageBubble(text: String, isUser: Boolean) {
    val shape = if (isUser) USER_BUBBLE_SHAPE else AI_BUBBLE_SHAPE
    val c = DroidKitTheme.colors
    Column(
        modifier = Modifier
            .clip(shape)
            .background(if (isUser) c.brandGradient else SolidColor(c.aiBubble))
            .border(1.dp, if (isUser) Color.Transparent else c.lineNeutral, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        MessageText(text = text, onUserBubble = isUser)
    }
}

/** Plain text with ```-fenced spans rendered as monospace blocks. */
@Composable
private fun MessageText(text: String, onUserBubble: Boolean) {
    text.split("```").forEachIndexed { index, part ->
        val trimmed = part.trim()
        if (index % 2 == 1) {
            val code = trimmed.let { t ->
                val first = t.substringBefore('\n')
                if (first.lowercase() in CODE_LANG_TAGS) t.substringAfter('\n').trim() else t
            }
            if (code.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Text(
                        code,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        } else if (trimmed.isNotEmpty()) {
            if (onUserBubble) {
                Text(
                    trimmed,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = DroidKitTheme.colors.onBrand,
                )
            } else {
                AiRichText(trimmed)
            }
        }
    }
}

/** Action chip under an assistant bubble — one tap navigates or runs the read-only action. */
@Composable
private fun ProposalChip(
    proposal: ValidatedProposal,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isNav = proposal.proposal is AiProposal.OpenTool
    val c = DroidKitTheme.colors
    val fg = if (proposal.allowed) c.brandPrimary else c.textSecondary
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (proposal.allowed) c.raise else c.panel,
        border = BorderStroke(1.dp, c.lineNeutral),
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = proposal.allowed, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isNav) {
                Icon(
                    Icons.Outlined.Terminal,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp),
                    fontFamily = if (isNav) null else FontFamily.Monospace,
                    color = fg,
                    maxLines = 2,
                )
                proposal.blockedReason?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = c.statusError,
                    )
                }
            }
            Icon(
                if (isNav) Icons.AutoMirrored.Outlined.ArrowForward
                else Icons.Outlined.PlayArrow,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** AI glyph tile (mockup `.avatar`): gradient wash + brand hairline, rounded square. */
@Composable
private fun AssistantAvatar(size: Dp = 28.dp, iconSize: Dp = 16.dp) {
    val c = DroidKitTheme.colors
    val corner = when {
        size >= 64.dp -> 18.dp
        size >= 56.dp -> 16.dp
        else -> 10.dp
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(c.tileGradient)
            .border(1.dp, c.lineBrand, RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.SmartToy,
            contentDescription = null,
            tint = c.brandPrimary,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** Meta-style three-dot waiting bubble shown while a reply is in flight. */
@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "phase",
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        AssistantAvatar()
        Spacer(Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .clip(AI_BUBBLE_SHAPE)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            repeat(3) { i ->
                val wave = ((sin(2 * PI * (phase + i * 0.25f)) + 1f) / 2f).toFloat()
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            DroidKitTheme.colors.textSecondary
                                .copy(alpha = 0.25f + 0.75f * wave),
                        ),
                )
            }
        }
    }
}

/** Suggestion chip (mockup `.chip`): panel tile with a neutral hairline. */
@Composable
private fun PromptChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = DroidKitTheme.colors
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = c.panel,
        border = BorderStroke(1.dp, c.lineNeutral),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = c.textPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

/** Meta-style pill composer: rounded field plus a circular send button. */
@Composable
private fun AiChatComposer(
    input: String,
    onInputChange: (String) -> Unit,
    canSend: Boolean,
    onSend: () -> Unit,
) {
    val c = DroidKitTheme.colors
    // mockup .chatform — panel bar under a hairline.
    Surface(color = c.panel) {
        Column {
            DroidKitHairline()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                TextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask DroidKit…") },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = c.field,
                        unfocusedContainerColor = c.field,
                        focusedTextColor = c.textPrimary,
                        unfocusedTextColor = c.textPrimary,
                        cursorColor = c.brandPrimary,
                        focusedPlaceholderColor = c.faint,
                        unfocusedPlaceholderColor = c.faint,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { if (canSend) onSend() },
                    ),
                )
                // mockup .sendbtn — raise tile; gradient + glow once sendable.
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (canSend) c.brandGradient
                            else SolidColor(c.raise),
                        )
                        .border(
                            1.dp,
                            if (canSend) Color.Transparent else c.lineNeutral,
                            RoundedCornerShape(12.dp),
                        )
                        .clickable(enabled = canSend, onClick = onSend),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Send",
                        tint = if (canSend) c.onBrand else c.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
