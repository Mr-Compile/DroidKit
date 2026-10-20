package io.github.droidkit.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolRegistryTest {

    private val contributions = setOf(
        object : FeatureContribution {
            override val tools = listOf(
                Tool("a", "USB Devices", "List USB", ToolCategory.HARDWARE, "usb"),
                Tool("b", "Sensor List", "Show sensors", ToolCategory.DEVICE, "sensors"),
            )
        },
        object : FeatureContribution {
            override val tools = listOf(
                Tool("c", "AppOps", "App ops", ToolCategory.APPS, "appops"),
            )
        },
    )

    private val registry = ToolRegistry(contributions)

    @Test
    fun `registry flattens contributions sorted by title`() {
        assertEquals(listOf("AppOps", "Sensor List", "USB Devices"), registry.tools.map { it.title })
    }

    @Test
    fun `search matches title case-insensitively`() {
        assertEquals(listOf("a"), registry.search("usb").map { it.id })
    }

    @Test
    fun `byCategory filters`() {
        assertEquals(1, registry.byCategory(ToolCategory.APPS).size)
    }

    @Test
    fun `byId returns tool or null`() {
        assertEquals("USB Devices", registry.byId("a")?.title)
        assertTrue(registry.byId("zzz") == null)
    }

    @Test
    fun `tool rejects parameterized launch routes`() {
        // Registry routes are navigated verbatim via nav.navigate(tool.route);
        // nav-argument placeholders must live only in NavGraph composable patterns.
        listOf("sensors/live/{type}", "apps/detail/{packageName}", "{a}/b").forEach { route ->
            runCatching { Tool("t", "T", "d", ToolCategory.DEVICE, route) }
                .onSuccess { throw AssertionError("placeholder route accepted: $route") }
                .onFailure { assertTrue(it is IllegalArgumentException) }
        }
    }

    @Test
    fun `registry launch routes are all directly navigable`() {
        registry.tools.forEach { assertTrue(it.route.none { c -> c == '{' || c == '}' }) }
    }

    @Test
    fun `search ranks title hits above keyword and description hits`() {
        val reg = ToolRegistry(
            setOf(
                object : FeatureContribution {
                    override val tools = listOf(
                        Tool("desc", "Zeta", "wifi analyzer inside", ToolCategory.DEVICE, "a"),
                        Tool("kw", "Beta", "x", ToolCategory.DEVICE, "b", keywords = setOf("wifi")),
                        Tool("title", "Wi-Fi Lab", "x", ToolCategory.DEVICE, "c"),
                    )
                },
            ),
        )
        assertEquals(listOf("title", "kw", "desc"), reg.search("wifi").map { it.id })
    }

    @Test
    fun `search ranks exact title above prefix and contains`() {
        val reg = ToolRegistry(
            setOf(
                object : FeatureContribution {
                    override val tools = listOf(
                        Tool("mid", "My Ping Tool", "x", ToolCategory.DEVICE, "a"),
                        Tool("exact", "Ping", "x", ToolCategory.DEVICE, "b"),
                        Tool("prefix", "Ping Extended", "x", ToolCategory.DEVICE, "c"),
                    )
                },
            ),
        )
        assertEquals(listOf("exact", "prefix", "mid"), reg.search("ping").map { it.id })
    }

    @Test
    fun `registry rejects duplicate tool ids`() {
        val dupe = object : FeatureContribution {
            override val tools = listOf(
                Tool("same", "A", "a", ToolCategory.DEVICE, "a"),
                Tool("same", "B", "b", ToolCategory.DEVICE, "b"),
            )
        }
        runCatching { ToolRegistry(setOf(dupe)) }
            .onSuccess { throw AssertionError("duplicate tool ids accepted") }
            .onFailure { assertTrue(it is IllegalArgumentException) }
    }

    @Test
    fun `tool rejects unknown icon keys`() {
        runCatching { Tool("t", "T", "d", ToolCategory.DEVICE, "r", iconKey = "notAKey") }
            .onSuccess { throw AssertionError("unknown iconKey accepted") }
            .onFailure { assertTrue(it is IllegalArgumentException) }
        // Blank falls back to the category icon — allowed.
        Tool("t", "T", "d", ToolCategory.DEVICE, "r", iconKey = "")
        Tool("t", "T", "d", ToolCategory.DEVICE, "r", iconKey = ToolIconKeys.BATTERY)
    }

    @Test
    fun `searchEntries ranks and filters contributed entries`() {
        val reg = ToolRegistry(
            setOf(
                object : FeatureContribution {
                    override val tools = emptyList<Tool>()
                    override val searchEntries = listOf(
                        SearchEntry("p1", "Privacy Center", "sub", "settings/privacy",
                            SearchResultType.SETTING, ToolIconKeys.PRIVACY,
                            keywords = setOf("data")),
                        SearchEntry("d1", "Speaker", "Audio test", "diagnose/test/audio.speaker",
                            SearchResultType.DIAGNOSTIC, ToolIconKeys.SPEAKER),
                    )
                },
            ),
        )
        assertEquals(listOf("d1"), reg.searchEntries("speaker").map { it.id })
        assertEquals(listOf("p1"), reg.searchEntries("privacy").map { it.id })
        assertTrue(reg.searchEntries("").isEmpty())
        assertTrue(reg.searchEntries("zzzz").isEmpty())
    }

    @Test
    fun `searchEntry rejects placeholder routes and unknown icons`() {
        runCatching {
            SearchEntry("e", "t", "s", "r/{arg}")
        }.onSuccess { throw AssertionError("placeholder route accepted") }
            .onFailure { assertTrue(it is IllegalArgumentException) }
        runCatching {
            SearchEntry("e", "t", "s", "r", iconKey = "notAKey")
        }.onSuccess { throw AssertionError("unknown iconKey accepted") }
            .onFailure { assertTrue(it is IllegalArgumentException) }
    }
}
