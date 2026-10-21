package io.github.droidkit.core.ai

/**
 * Status mood of one rendered line — drives the tinted icon the UI puts in
 * front of it. Detection is keyword-based and cosmetic: a miss renders a
 * plain line, a false hit only costs an icon.
 */
enum class StatusCue { POSITIVE, WARNING, NEGATIVE, INFO, NONE }

/** Inline span of human-facing text after markdown markers are resolved. */
sealed interface AiSpan {
    val text: String

    data class Text(override val text: String) : AiSpan
    data class Strong(override val text: String) : AiSpan
    data class Emphasis(override val text: String) : AiSpan
    data class Code(override val text: String) : AiSpan
}

/** One display block of a model reply. */
sealed interface AiBlock {
    data class Paragraph(val spans: List<AiSpan>, val cue: StatusCue) : AiBlock
    data class Heading(val spans: List<AiSpan>) : AiBlock
    data class Bullet(val spans: List<AiSpan>, val cue: StatusCue) : AiBlock
    data class Numbered(val n: Int, val spans: List<AiSpan>, val cue: StatusCue) : AiBlock

    /** Leading verdict line (ALL GOOD / WORTH CHECKING / NEEDS ATTENTION). */
    data class Verdict(val cue: StatusCue, val label: String, val rest: List<AiSpan>) : AiBlock

    /** Fenced ``` block — rendered monospace, never re-parsed. */
    data class Mono(val text: String) : AiBlock
    data object Divider : AiBlock
}

/**
 * Parses raw model output into display blocks. Markdown markers never reach
 * the screen: paired markers become styled spans, unpaired markers are
 * dropped, `--` becomes an em dash and list/heading lines become real
 * blocks. Pure Kotlin so unit tests exercise the real path.
 */
object AiTextFormat {

    fun parse(raw: String): List<AiBlock> {
        val blocks = mutableListOf<AiBlock>()
        raw.split("```").forEachIndexed { index, segment ->
            if (index % 2 == 1) {
                val code = stripLangTag(segment.trim())
                if (code.isNotEmpty()) blocks += AiBlock.Mono(code)
            } else {
                parseProse(segment, blocks)
            }
        }
        return blocks
    }

    private fun parseProse(segment: String, out: MutableList<AiBlock>) {
        val paragraph = mutableListOf<String>()
        fun flushParagraph() {
            if (paragraph.isEmpty()) return
            val joined = paragraph.joinToString(" ")
            paragraph.clear()
            val cue = detectCue(joined)
            val body = if (cue != StatusCue.NONE) stripLeadEmoji(joined) else joined
            val spans = inline(body)
            if (spans.isNotEmpty()) out += AiBlock.Paragraph(spans, cue)
        }

        for (line in segment.lines()) {
            val t = line.trim()
            if (t.isEmpty()) {
                flushParagraph()
                continue
            }
            when {
                DIVIDER_RE.matches(t) -> {
                    flushParagraph()
                    out += AiBlock.Divider
                }
                HEADING_RE.matches(t) -> {
                    flushParagraph()
                    val body = HEADING_RE.matchEntire(t)!!.groupValues[1]
                    out += AiBlock.Heading(inline(cleanedBody(body, detectCue(body))))
                }
                BULLET_RE.matches(t) -> {
                    flushParagraph()
                    val body = BULLET_RE.matchEntire(t)!!.groupValues[1]
                    val cue = detectCue(body)
                    out += AiBlock.Bullet(inline(cleanedBody(body, cue)), cue)
                }
                NUMBERED_RE.matches(t) -> {
                    flushParagraph()
                    val m = NUMBERED_RE.matchEntire(t)!!
                    val body = m.groupValues[2]
                    val cue = detectCue(body)
                    out += AiBlock.Numbered(
                        n = m.groupValues[1].toIntOrNull() ?: 1,
                        spans = inline(cleanedBody(body, cue)),
                        cue = cue,
                    )
                }
                VERDICT_RE.matches(t) -> {
                    flushParagraph()
                    val m = VERDICT_RE.matchEntire(t)!!
                    val phrase = m.groupValues[1]
                    val cue = when (phrase.lowercase()) {
                        "all good" -> StatusCue.POSITIVE
                        "worth checking" -> StatusCue.WARNING
                        else -> StatusCue.NEGATIVE
                    }
                    val label = phrase.lowercase().replaceFirstChar(Char::uppercaseChar)
                    out += AiBlock.Verdict(cue, label, inline(stripLeadEmoji(m.groupValues[2])))
                }
                else -> paragraph += t.trimStart('#').trim()
            }
        }
        flushParagraph()
    }

    /** Body text for spans — a leading status emoji is consumed by the icon. */
    private fun cleanedBody(body: String, cue: StatusCue): String =
        if (cue != StatusCue.NONE) stripLeadEmoji(body) else body

    private fun inline(s: String): List<AiSpan> {
        val spans = mutableListOf<AiSpan>()
        var cursor = 0
        for (m in INLINE_RE.findAll(s)) {
            emitText(spans, s.substring(cursor, m.range.first))
            val g = m.groupValues
            when {
                g[1].isNotEmpty() -> spans += AiSpan.Strong(g[1])
                g[2].isNotEmpty() -> spans += AiSpan.Strong(g[2])
                g[3].isNotEmpty() -> spans += AiSpan.Emphasis(g[3])
                g[4].isNotEmpty() -> spans += AiSpan.Emphasis(g[4])
                g[5].isNotEmpty() -> spans += AiSpan.Code(g[5])
            }
            cursor = m.range.last + 1
        }
        emitText(spans, s.substring(cursor))
        return trimEdgeSpaces(spans)
    }

    private fun emitText(spans: MutableList<AiSpan>, raw: String) {
        val cleaned = cleanDanglers(raw)
        if (cleaned.isNotEmpty()) spans += AiSpan.Text(cleaned)
    }

    /** First/last text spans lose their outer space — interiors keep theirs. */
    private fun trimEdgeSpaces(spans: MutableList<AiSpan>): List<AiSpan> {
        (spans.firstOrNull() as? AiSpan.Text)?.let { first ->
            val t = first.text.trimStart()
            if (t.isEmpty()) spans.removeAt(0) else spans[0] = AiSpan.Text(t)
        }
        (spans.lastOrNull() as? AiSpan.Text)?.let { last ->
            val t = last.text.trimEnd()
            if (t.isEmpty()) spans.removeLastOrNull() else spans[spans.lastIndex] = AiSpan.Text(t)
        }
        return spans
    }

    /**
     * Removes marker characters that did not form a pair so no literal
     * `*`, `_` or backtick leaks through. Characters flanked by letters or
     * digits on both sides (snake_case, 2*3) survive. Also converts `--`
     * runs to a real em dash and collapses stray double spaces.
     */
    private fun cleanDanglers(s: String): String {
        val dashed = s.replace(DASH_RUN, "—")
        val sb = StringBuilder(dashed.length)
        for (i in dashed.indices) {
            val c = dashed[i]
            if (c == '*' || c == '_' || c == '`') {
                val wordLeft = i > 0 && dashed[i - 1].isLetterOrDigit()
                val wordRight = i + 1 < dashed.length && dashed[i + 1].isLetterOrDigit()
                if (wordLeft && wordRight) sb.append(c)
            } else {
                sb.append(c)
            }
        }
        return sb.toString().replace(SPACE_RUN, " ")
    }

    private fun detectCue(text: String): StatusCue {
        // Negated keywords flip meaning: "nothing abnormal" is good news,
        // "not normal" is soft-bad. Scrub them before keyword matching.
        val scrubbed = NEGATED_GOOD.replace(NEGATED_BAD.replace(text, " "), " ")
        val cue = when {
            CUE_NEGATIVE.containsMatchIn(scrubbed) -> StatusCue.NEGATIVE
            CUE_WARNING.containsMatchIn(scrubbed) -> StatusCue.WARNING
            CUE_POSITIVE.containsMatchIn(scrubbed) -> StatusCue.POSITIVE
            CUE_INFO.containsMatchIn(scrubbed) -> StatusCue.INFO
            else -> StatusCue.NONE
        }
        return when {
            cue != StatusCue.NONE -> cue
            NEGATED_GOOD.containsMatchIn(text) -> StatusCue.WARNING
            NEGATED_BAD.containsMatchIn(text) -> StatusCue.POSITIVE
            else -> StatusCue.NONE
        }
    }

    private fun stripLeadEmoji(s: String): String =
        s.replace(LEAD_STATUS_EMOJI, "").trimStart()

    private fun stripLangTag(code: String): String {
        val first = code.substringBefore('\n')
        return if (first.lowercase() in CODE_LANG_TAGS) code.substringAfter('\n').trim() else code
    }

    private val DIVIDER_RE = Regex("^\\s*([\\-*_])(\\s*\\1){2,}\\s*$")
    private val HEADING_RE = Regex("^#{1,6}\\s+(.+)$")
    private val BULLET_RE = Regex("^[-*+•]\\s+(.+)$")
    private val NUMBERED_RE = Regex("^(\\d{1,2})[.)]\\s+(.+)$")
    private val VERDICT_RE = Regex(
        "^(all good|worth checking|needs attention)\\b[\\s:—–.,!-]*(.*)$",
        RegexOption.IGNORE_CASE,
    )

    // **bold** / __bold__ / *em* / _em_ / `code`; underscores require a
    // non-word outer context so snake_case is never treated as emphasis.
    private val INLINE_RE = Regex(
        "\\*\\*([^*\\s](?:.*?[^*\\s])?)\\*\\*" +
            "|(?<![A-Za-z0-9_])__([^_\\s](?:.*?[^_\\s])?)__(?![A-Za-z0-9_])" +
            "|\\*([^*\\s](?:.*?[^*\\s])?)\\*" +
            "|(?<![A-Za-z0-9_])_([^_\\s](?:.*?[^_\\s])?)_(?![A-Za-z0-9_])" +
            "|`([^`]+)`",
    )

    private val DASH_RUN = Regex("--+")
    private val SPACE_RUN = Regex(" {2,}")

    private val CUE_NEGATIVE = Regex(
        "(?i)\\b(fail(ed|ing|s|ure)?|critical|needs attention|errors?|dead|broken|not working)\\b" +
            "|[❌✗✘🔴]",
    )
    private val CUE_WARNING = Regex(
        "(?i)\\b(worth checking|unusual|warnings?|warn|abnormal|elevated|high|low|overheat(ing)?|hot)\\b" +
            "|[⚠🟡🟠]",
    )
    private val CUE_POSITIVE = Regex(
        "(?i)\\b(all good|normals?|pass(ed)?|healthy|ok(ay)?|in range|within (the )?normal" +
            "|looks good|no issues?)\\b|[✅✓✔🟢]",
    )
    private val CUE_INFO = Regex("(?i)^(note|tip)\\b|[ℹ💡]")

    private val NEGATED_BAD = Regex(
        "(?i)\\b(no|not|nothing|without|never|hardly|barely|no signs of)\\s+" +
            "(?:(?:a|an|the|any|signs of)\\s+)?" +
            "(abnormal\\w*|unusual|fail(ed|ing|s|ure)?|failures?|issues?|problems?|errors?" +
            "|critical|wrong|high|low|hot|bad)\\b",
    )
    private val NEGATED_GOOD = Regex(
        "(?i)\\b(no|not|nothing|isn't|isnt|without|never|hardly|barely)\\s+" +
            "(?:(?:a|an|the|any)\\s+)?" +
            "(normal|healthy|good|fine|ok(ay)?|passed?|in range|within (the )?normal)\\b",
    )

    private val LEAD_STATUS_EMOJI = Regex("^[\\s✅❌⚠✓✔✗✘🟢🔴🟡🟠ℹ💡🔥⭐\\uFE0F]+")

    private val CODE_LANG_TAGS = setOf(
        "json", "kotlin", "java", "bash", "sh", "shell", "xml", "yaml", "text",
    )
}
