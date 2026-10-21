package io.github.droidkit.core.ui

import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
import io.github.droidkit.core.ui.components.ToolIcons
import io.github.droidkit.core.ui.theme.DroidKitCategoryColors
import org.junit.Assert.assertNotNull
import org.junit.Test

class ToolIconsTest {

    @Test
    fun `every ToolIconKeys value maps to a vector`() {
        ToolIconKeys.ALL.forEach { key -> assertNotNull(ToolIcons.of(key)) }
    }

    @Test
    fun `every category has a fallback icon`() {
        ToolCategory.entries.forEach { assertNotNull(ToolIcons.forCategory(it)) }
    }

    @Test
    fun `tool without iconKey falls back to the category icon`() {
        val tool = Tool("t", "T", "d", ToolCategory.DEVICE, "r")
        assertNotNull(ToolIcons.forTool(tool))
    }

    @Test
    fun `every category has a dark and light accent`() {
        ToolCategory.entries.forEach { category ->
            assertNotNull(DroidKitCategoryColors.accent(category, darkTheme = true))
            assertNotNull(DroidKitCategoryColors.accent(category, darkTheme = false))
        }
    }
}
