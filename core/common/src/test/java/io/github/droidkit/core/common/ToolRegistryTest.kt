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
}
