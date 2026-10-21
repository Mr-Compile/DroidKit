package io.github.droidkit.core.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Guards the bottom-bar ↔ rail rule the NavigationSuiteScaffold applies:
 * rail only when width ≥600dp AND height ≥480dp and no tabletop posture.
 */
class NavChromeTest {

    @Test
    fun `compact portrait phone shows bottom bar`() {
        assertEquals(NavChrome.BottomBar, navChromeFor(widthDp = 411, heightDp = 891))
    }

    @Test
    fun `compact landscape phone keeps bottom bar`() {
        assertEquals(NavChrome.BottomBar, navChromeFor(widthDp = 891, heightDp = 411))
    }

    @Test
    fun `medium width shows rail`() {
        assertEquals(NavChrome.Rail, navChromeFor(widthDp = 600, heightDp = 960))
    }

    @Test
    fun `expanded width shows rail`() {
        assertEquals(NavChrome.Rail, navChromeFor(widthDp = 840, heightDp = 900))
    }

    @Test
    fun `width boundary just below medium stays bottom bar`() {
        assertEquals(NavChrome.BottomBar, navChromeFor(widthDp = 599, heightDp = 960))
    }

    @Test
    fun `compact height forces bottom bar even at wide width`() {
        assertEquals(NavChrome.BottomBar, navChromeFor(widthDp = 800, heightDp = 479))
    }

    @Test
    fun `tabletop posture forces bottom bar`() {
        assertEquals(NavChrome.BottomBar, navChromeFor(widthDp = 840, heightDp = 900, tabletop = true))
    }
}
