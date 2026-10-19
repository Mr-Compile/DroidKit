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
}
