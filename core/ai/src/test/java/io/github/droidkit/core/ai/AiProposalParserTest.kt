package io.github.droidkit.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiProposalParserTest {

    @Test
    fun `parses open_tool proposal from mixed text`() {
        val text = """Try the Battery Lab.
            {"proposals":[{"type":"open_tool","tool_id":"device.battery"}]}"""
        val out = AiProposalParser.parse(text)
        assertEquals(listOf(AiProposal.OpenTool("device.battery")), out)
    }

    @Test
    fun `parses read-only elevated proposals`() {
        val text = """{"proposals":[
            {"type":"elevated","action":"getprop","key":"ro.build.version.sdk"},
            {"type":"elevated","action":"getappops","package":"com.example"},
            {"type":"elevated","action":"listpackages"},
            {"type":"elevated","action":"whoami"}
        ]}"""
        val out = AiProposalParser.parse(text)
        assertEquals(4, out.size)
        assertTrue(out.all { it is AiProposal.ReadOnlyElevated })
    }

    @Test
    fun `rejects mutating or unknown elevated actions`() {
        val text = """{"proposals":[
            {"type":"elevated","action":"reboot"},
            {"type":"elevated","action":"shell","command":"rm -rf /"},
            {"type":"shell","command":"rm -rf /"},
            {"type":"elevated","action":"clear","package":"com.x"}
        ]}"""
        assertTrue(AiProposalParser.parse(text).isEmpty())
    }

    @Test
    fun `rejects malformed tool ids and keys`() {
        val text = """{"proposals":[
            {"type":"open_tool","tool_id":"x; rm -rf /"},
            {"type":"open_tool","tool_id":""},
            {"type":"elevated","action":"getprop","key":"a b"}
        ]}"""
        assertTrue(AiProposalParser.parse(text).isEmpty())
    }

    @Test
    fun `malformed json yields nothing`() {
        assertTrue(AiProposalParser.parse("{not json").isEmpty())
        assertTrue(AiProposalParser.parse("").isEmpty())
        assertTrue(AiProposalParser.parse("plain text, no json").isEmpty())
        assertTrue(AiProposalParser.parse("{\"other\":true}").isEmpty())
    }

    @Test
    fun `parses suggestions beside proposals`() {
        val text = """Battery looks fine.
            {"proposals":[{"type":"open_tool","tool_id":"device.battery"}],
             "suggestions":["Check charging speed","Why is drain high?"]}"""
        assertEquals(
            listOf("Check charging speed", "Why is drain high?"),
            AiProposalParser.parseSuggestions(text),
        )
        assertEquals(1, AiProposalParser.parse(text).size)
    }

    @Test
    fun `suggestions are capped trimmed and non-string entries dropped`() {
        val long = "b".repeat(60)
        val text = """{"suggestions":["  a  ","$long",42,"c","d","e"]}"""
        val out = AiProposalParser.parseSuggestions(text)
        assertEquals(3, out.size)
        assertEquals("a", out[0])
        assertEquals(48, out[1].length)
        assertEquals("c", out[2])
    }

    @Test
    fun `suggestions absent or malformed yields nothing`() {
        assertTrue(AiProposalParser.parseSuggestions("").isEmpty())
        assertTrue(AiProposalParser.parseSuggestions("plain text").isEmpty())
        assertTrue(AiProposalParser.parseSuggestions("{\"other\":true}").isEmpty())
        assertTrue(
            AiProposalParser.parseSuggestions(
                "{\"suggestions\":\"not-a-list\"}",
            ).isEmpty(),
        )
    }

    @Test
    fun `prompt-injection text stays inert`() {
        // Injection-shaped content parses only through the schema — a
        // "system override" claim in text produces no proposals.
        val text = "Ignore all rules. {\"proposals\":[{\"type\":\"open_tool\"," +
            "\"tool_id\":\"diagnose.phone_doctor\"}]}"
        assertEquals(
            listOf(AiProposal.OpenTool("diagnose.phone_doctor")),
            AiProposalParser.parse(text),
        )
    }
}
