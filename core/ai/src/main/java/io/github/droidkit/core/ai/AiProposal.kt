package io.github.droidkit.core.ai

import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.privileges.BlockedReason
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ElevatedAction
import io.github.droidkit.core.privileges.ToolResolution
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A structured action the model may propose. Only these shapes exist —
 * anything else in model output is discarded. Proposals are data: the UI
 * shows them verbatim and they execute only on explicit user confirmation.
 */
sealed interface AiProposal {
    /** Navigate to a registered tool's route. */
    data class OpenTool(val toolId: String) : AiProposal

    /** Read-only elevated actions only — mutations are never AI-triggerable. */
    data class ReadOnlyElevated(val action: ElevatedAction) : AiProposal
}

/** Result of validating a proposal against the live tool/capability graph. */
data class ValidatedProposal(
    val proposal: AiProposal,
    val allowed: Boolean,
    val blockedReason: String?,
)

/**
 * Parses model JSON into [AiProposal]s. Malformed output yields nothing.
 * Uses a minimal internal JSON reader so the parser is pure-JVM unit-testable.
 */
object AiProposalParser {

    /** Extracts proposals from text that may contain a {...} JSON block. */
    fun parse(text: String): List<AiProposal> {
        var start = text.indexOf('{')
        while (start >= 0) {
            val end = balancedEnd(text, start)
            if (end < 0) return emptyList()
            val obj = MiniJson.parseObject(text.substring(start, end + 1))
            if (obj != null && obj["proposals"] is List<*>) {
                return (obj["proposals"] as List<*>).mapNotNull(::toProposal)
            }
            start = text.indexOf('{', start + 1)
        }
        return emptyList()
    }

    /**
     * Extracts follow-up prompt strings from a {"suggestions":[...]} array
     * that may sit beside "proposals" in the same JSON block. Suggestions
     * are plain text — the UI only sends one back as user input on tap.
     */
    fun parseSuggestions(text: String): List<String> {
        var start = text.indexOf('{')
        while (start >= 0) {
            val end = balancedEnd(text, start)
            if (end < 0) return emptyList()
            val obj = MiniJson.parseObject(text.substring(start, end + 1))
            val list = obj?.get("suggestions") as? List<*>
            if (list != null) {
                return list.filterIsInstance<String>()
                    .map { it.trim().take(SUGGESTION_MAX_LEN) }
                    .filter { it.isNotEmpty() }
                    .take(SUGGESTION_MAX_COUNT)
            }
            start = text.indexOf('{', start + 1)
        }
        return emptyList()
    }

    /**
     * Removes JSON blocks carrying "proposals"/"suggestions" payloads so
     * chat bubbles show only the human-facing reply text. Braces that are
     * not structured payloads are left untouched.
     */
    fun stripStructuredBlocks(text: String): String {
        val sb = StringBuilder()
        var cursor = 0
        var start = text.indexOf('{')
        while (start >= 0) {
            val end = balancedEnd(text, start)
            if (end < 0) break
            val obj = MiniJson.parseObject(text.substring(start, end + 1))
            if (obj != null && (obj["proposals"] is List<*> || obj["suggestions"] is List<*>)) {
                sb.append(text, cursor, start)
                cursor = end + 1
                start = text.indexOf('{', cursor)
            } else {
                start = text.indexOf('{', start + 1)
            }
        }
        sb.append(text, cursor, text.length)
        return sb.toString().trim()
    }

    private fun toProposal(el: Any?): AiProposal? {
        val obj = el as? Map<*, *> ?: return null
        return when (obj.str("type")) {
            "open_tool" -> obj.str("tool_id")
                ?.takeIf { it.matches(TOOL_ID_RE) }
                ?.let(AiProposal::OpenTool)
            "elevated" -> elevated(obj)
            else -> null // unknown types rejected — no arbitrary commands
        }
    }

    private fun elevated(obj: Map<*, *>): AiProposal? = when (
        obj.str("action")
    ) {
        "getprop" -> obj.str("key")
            ?.takeIf { it.matches(KEY_RE) }
            ?.let { AiProposal.ReadOnlyElevated(ElevatedAction.GetProp(it)) }
        "getappops" -> obj.str("package")
            ?.takeIf { it.matches(PKG_RE) }
            ?.let { AiProposal.ReadOnlyElevated(ElevatedAction.GetAppOps(it)) }
        "listpackages" ->
            AiProposal.ReadOnlyElevated(ElevatedAction.ListPackages())
        "whoami" -> AiProposal.ReadOnlyElevated(ElevatedAction.WhoAmI)
        else -> null
    }

    private fun Map<*, *>.str(key: String): String? = this[key] as? String

    /**
     * Returns the index of the '}' closing the '{' at [start], skipping
     * braces inside string literals. -1 if unbalanced.
     */
    private fun balancedEnd(text: String, start: Int): Int {
        var depth = 0
        var i = start
        while (i < text.length) {
            when (text[i]) {
                '"' -> {
                    i++
                    while (i < text.length && text[i] != '"') {
                        if (text[i] == '\\') i++ // skip escaped char
                        i++
                    }
                }
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        return -1
    }

    private const val SUGGESTION_MAX_COUNT = 3
    private const val SUGGESTION_MAX_LEN = 48

    private val TOOL_ID_RE = Regex("^[a-z0-9_.-]{1,64}$")
    private val KEY_RE = Regex("^[A-Za-z0-9._\\[\\]-]{1,64}$")
    private val PKG_RE = Regex("^[A-Za-z0-9_.]+$")
}

/**
 * Minimal JSON reader covering objects, arrays, strings, numbers, and
 * literals — enough for model proposal payloads. Returns null on any
 * malformed input. Kept dependency-free so unit tests exercise the real
 * parse path (android.jar's org.json is stubbed in local tests).
 */
private object MiniJson {

    fun parseObject(text: String): Map<String, Any?>? {
        val p = Parser(text)
        val v = p.value()
        p.skipWs()
        return if (v is Map<*, *> && p.eof()) {
            @Suppress("UNCHECKED_CAST")
            v as Map<String, Any?>
        } else null
    }

    private class Parser(val s: String) {
        var pos = 0

        fun eof() = pos >= s.length
        fun skipWs() {
            while (pos < s.length && s[pos].isWhitespace()) pos++
        }

        fun value(): Any? {
            skipWs()
            if (eof()) return null
            return when (s[pos]) {
                '{' -> obj()
                '[' -> arr()
                '"' -> str()
                't' -> lit("true", true)
                'f' -> lit("false", false)
                'n' -> lit("null", null)
                else -> num()
            }
        }

        private fun obj(): Map<String, Any?>? {
            pos++ // {
            val map = LinkedHashMap<String, Any?>()
            skipWs()
            if (!eof() && s[pos] == '}') { pos++; return map }
            while (true) {
                skipWs()
                if (eof() || s[pos] != '"') return null
                val key = str() ?: return null
                skipWs()
                if (eof() || s[pos] != ':') return null
                pos++
                map[key] = value()
                skipWs()
                if (eof()) return null
                when (s[pos]) {
                    ',' -> pos++
                    '}' -> { pos++; return map }
                    else -> return null
                }
            }
        }

        private fun arr(): List<Any?>? {
            pos++ // [
            val list = ArrayList<Any?>()
            skipWs()
            if (!eof() && s[pos] == ']') { pos++; return list }
            while (true) {
                list.add(value())
                skipWs()
                if (eof()) return null
                when (s[pos]) {
                    ',' -> pos++
                    ']' -> { pos++; return list }
                    else -> return null
                }
            }
        }

        private fun str(): String? {
            pos++ // "
            val sb = StringBuilder()
            while (!eof()) {
                when (val c = s[pos]) {
                    '"' -> { pos++; return sb.toString() }
                    '\\' -> {
                        pos++
                        if (eof()) return null
                        when (val e = s[pos]) {
                            'n' -> sb.append('\n')
                            't' -> sb.append('\t')
                            'r' -> sb.append('\r')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'u' -> {
                                if (pos + 4 >= s.length) return null
                                val hex = s.substring(pos + 1, pos + 5)
                                val cp = hex.toIntOrNull(16) ?: return null
                                sb.append(cp.toChar())
                                pos += 4
                            }
                            '"', '\\', '/' -> sb.append(e)
                            else -> return null
                        }
                        pos++
                    }
                    else -> { sb.append(c); pos++ }
                }
            }
            return null // unterminated string
        }

        private fun lit(word: String, result: Any?): Any? {
            if (s.startsWith(word, pos)) {
                pos += word.length
                return result
            }
            return null
        }

        private fun num(): Any? {
            val start = pos
            while (!eof() && s[pos] in "-+0123456789.eE") pos++
            return if (pos > start) {
                s.substring(start, pos).toDoubleOrNull()
            } else null
        }
    }
}

/**
 * Validates parsed proposals against reality: the tool must exist and be
 * resolvable; elevated reads need an active elevated provider.
 */
@Singleton
class AiActionValidator @Inject constructor(
    private val registry: ToolRegistry,
    private val capabilityManager: CapabilityManager,
) {

    fun validateAll(proposals: List<AiProposal>): List<ValidatedProposal> =
        proposals.map(::validate)

    fun validate(p: AiProposal): ValidatedProposal = when (p) {
        is AiProposal.OpenTool -> validateTool(p)
        is AiProposal.ReadOnlyElevated ->
            // Read-only by construction — provider availability is checked
            // again at execution time by ElevatedOps.
            ValidatedProposal(p, allowed = true, blockedReason = null)
    }

    private fun validateTool(p: AiProposal.OpenTool): ValidatedProposal {
        val tool = registry.byId(p.toolId)
            ?: return ValidatedProposal(p, false, "unknown tool")
        return when (val r = capabilityManager.resolve(tool)) {
            is ToolResolution.Ready -> ValidatedProposal(p, true, null)
            is ToolResolution.Blocked ->
                ValidatedProposal(p, false, r.reasons.joinToString { describe(it) })
        }
    }

    /** Route for a known tool id, or null. Used by the confirm-then-navigate path. */
    fun toolRoute(toolId: String): String? = registry.byId(toolId)?.route

    /** Display title for a known tool id, or null. Used for proposal chip labels. */
    fun toolTitle(toolId: String): String? = registry.byId(toolId)?.title

    private fun describe(reason: BlockedReason): String = when (reason) {
        is BlockedReason.MissingPermission -> "missing permission ${reason.permission}"
        is BlockedReason.MissingHardware -> "missing hardware ${reason.feature}"
        is BlockedReason.RequiresApi -> "requires API ${reason.minSdk}"
        is BlockedReason.NeedsProvider -> "needs ${reason.kind.name} provider"
    }
}
