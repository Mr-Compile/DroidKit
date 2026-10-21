package io.github.droidkit.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract for the humanized renderer: markdown markers become typed
 * spans/blocks, no symbol can leak to the screen, and status keywords map
 * to cues the UI turns into tinted icons.
 */
class AiTextFormatTest {

    private fun spansText(spans: List<AiSpan>) = spans.joinToString("") { it.text }

    private fun allText(blocks: List<AiBlock>): String = blocks.joinToString("\n") { b ->
        when (b) {
            is AiBlock.Paragraph -> spansText(b.spans)
            is AiBlock.Heading -> spansText(b.spans)
            is AiBlock.Bullet -> spansText(b.spans)
            is AiBlock.Numbered -> spansText(b.spans)
            is AiBlock.Verdict -> b.label + " " + spansText(b.rest)
            is AiBlock.Mono -> b.text
            AiBlock.Divider -> "|"
        }
    }

    @Test
    fun `bold and emphasis markers become styled spans with no stars left`() {
        val p = AiTextFormat.parse("Battery is **normal** today, _really_.")
            .single() as AiBlock.Paragraph
        assertTrue(p.spans.any { it is AiSpan.Strong && it.text == "normal" })
        assertTrue(p.spans.any { it is AiSpan.Emphasis && it.text == "really" })
        assertFalse(spansText(p.spans).contains("*"))
        assertFalse(spansText(p.spans).contains("_"))
        assertEquals("Battery is normal today, really.", spansText(p.spans))
    }

    @Test
    fun `double underscores become strong while snake_case survives`() {
        val p = AiTextFormat.parse("__ro.build.id__ uses snake_case_names")
            .single() as AiBlock.Paragraph
        assertTrue(p.spans.any { it is AiSpan.Strong && it.text == "ro.build.id" })
        assertTrue(spansText(p.spans).contains("snake_case_names"))
    }

    @Test
    fun `hash lines become headings without hashes`() {
        val blocks = AiTextFormat.parse("## Battery\nBody text")
        assertTrue(blocks[0] is AiBlock.Heading)
        assertFalse(allText(blocks).contains("#"))
    }

    @Test
    fun `dash star bullet plus lines become bullets`() {
        val blocks = AiTextFormat.parse("- a\n* b\n• c\n+ d")
        assertEquals(4, blocks.filterIsInstance<AiBlock.Bullet>().size)
        assertFalse(allText(blocks).contains("•"))
    }

    @Test
    fun `numbered lines keep their numbers`() {
        val blocks = AiTextFormat.parse("1. first\n2. second")
        assertEquals(1, (blocks[0] as AiBlock.Numbered).n)
        assertEquals(2, (blocks[1] as AiBlock.Numbered).n)
    }

    @Test
    fun `rule lines become dividers and double hyphens become em dashes`() {
        val blocks = AiTextFormat.parse("top\n---\n___\n***\nnormal -- for now")
        assertEquals(3, blocks.count { it is AiBlock.Divider })
        val tail = allText(blocks)
        assertFalse(tail.contains("--"))
        assertTrue(tail.contains("normal — for now"))
    }

    @Test
    fun `verdict openers map to cues and labels`() {
        val good = AiTextFormat.parse("ALL GOOD — battery is healthy")[0] as AiBlock.Verdict
        assertEquals(StatusCue.POSITIVE, good.cue)
        assertEquals("All good", good.label)
        val warn = AiTextFormat.parse("WORTH CHECKING: one odd value")[0] as AiBlock.Verdict
        assertEquals(StatusCue.WARNING, warn.cue)
        val bad = AiTextFormat.parse("needs attention\n- Touch grid failed")[0] as AiBlock.Verdict
        assertEquals(StatusCue.NEGATIVE, bad.cue)
    }

    @Test
    fun `status keywords drive line cues`() {
        fun cueOf(line: String) =
            (AiTextFormat.parse(line).single() as? AiBlock.Bullet)?.cue
                ?: (AiTextFormat.parse(line).single() as? AiBlock.Paragraph)?.cue
        assertEquals(StatusCue.NEGATIVE, cueOf("- Touch grid failed"))
        assertEquals(StatusCue.POSITIVE, cueOf("- Voltage normal"))
        assertEquals(StatusCue.WARNING, cueOf("- Temperature high"))
        assertEquals(StatusCue.INFO, cueOf("Note: values are estimated"))
        assertEquals(StatusCue.NONE, cueOf("The quick brown fox"))
    }

    @Test
    fun `negated keywords flip the cue`() {
        fun cueOf(line: String) =
            (AiTextFormat.parse(line).single() as? AiBlock.Bullet)?.cue
                ?: (AiTextFormat.parse(line).single() as? AiBlock.Paragraph)?.cue
        assertEquals(StatusCue.POSITIVE, cueOf("- Nothing abnormal detected"))
        assertEquals(StatusCue.POSITIVE, cueOf("- No issues found"))
        assertEquals(StatusCue.WARNING, cueOf("- Temperature not normal"))
        assertEquals(StatusCue.NEGATIVE, cueOf("- Sensor not working"))
        assertEquals(StatusCue.NEGATIVE, cueOf("- Not normal and still failing"))
        assertEquals(StatusCue.WARNING, cueOf("- Proximity unusual: absent (not a failure)"))
        assertEquals(StatusCue.POSITIVE, cueOf("- Never a problem in testing"))
        assertEquals(StatusCue.WARNING, cueOf("- Not a good sign for the cell"))
    }

    @Test
    fun `unmatched markers are dropped not shown`() {
        val text = allText(AiTextFormat.parse("half **bold and a _ tail **"))
        assertFalse(text.contains("*"))
        assertFalse(text.contains("_"))
        assertTrue(text.contains("bold"))
    }

    @Test
    fun `leading status emoji is consumed mid-line emoji stays`() {
        val lead = AiTextFormat.parse("- ✅ Battery normal")
            .single() as AiBlock.Bullet
        assertFalse(spansText(lead.spans).contains("✅"))
        val mid = AiTextFormat.parse("- Battery ✅ normal")
            .single() as AiBlock.Bullet
        assertTrue(spansText(mid.spans).contains("✅"))
    }

    @Test
    fun `fenced blocks become mono and drop the lang tag`() {
        val blocks = AiTextFormat.parse("Raw output:\n```json\n{\"a\":1}\n```")
        val mono = blocks.filterIsInstance<AiBlock.Mono>().single()
        assertTrue(mono.text.contains("{\"a\":1}"))
        assertFalse(mono.text.startsWith("json"))
    }

    @Test
    fun `proposal json stripped upstream never reaches blocks`() {
        val raw = "Battery looks fine. " +
            "{\"proposals\":[{\"type\":\"open_tool\",\"tool_id\":\"battery\"}]}"
        val blocks = AiTextFormat.parse(AiProposalParser.stripStructuredBlocks(raw))
        assertFalse(allText(blocks).contains("proposals"))
        assertTrue(allText(blocks).contains("Battery looks fine"))
    }

    @Test
    fun `empty and blank input yield no blocks`() {
        assertTrue(AiTextFormat.parse("").isEmpty())
        assertTrue(AiTextFormat.parse("\n \n").isEmpty())
    }

    @Test
    fun `prompt contract bans markdown and keeps verdict labels`() {
        val base = PromptRegistry.forAssistant(AiAssistant.DEVICE_EXPLAINER)
        assertTrue(base.contains("Never use Markdown"))
        assertTrue(base.contains("normal, unusual, high, low, failing"))
        val sum = PromptRegistry.SUMMARIZE_VALUES
        assertTrue(sum.contains("ALL GOOD"))
        assertTrue(sum.contains("WORTH CHECKING"))
        assertTrue(sum.contains("NEEDS ATTENTION"))
        assertTrue(sum.contains("'- '"))
    }
}
