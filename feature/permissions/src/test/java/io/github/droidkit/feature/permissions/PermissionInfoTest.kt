package io.github.droidkit.feature.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionInfoTest {

    private fun app(label: String, vararg perms: Pair<String, Boolean>) =
        PermAppSource(label, "pkg.${label.lowercase()}", perms.toList())

    @Test
    fun groupsOnlyDangerousAndSortsByGranted() {
        val groups = PermissionInfo.buildGroups(
            listOf(
                app("Alpha", "a.CAMERA" to true, "a.NORMAL" to true),
                app("Beta", "a.CAMERA" to false, "a.LOCATION" to true),
                app("Gamma", "a.CAMERA" to true, "a.LOCATION" to true),
            ),
        ) { it != "a.NORMAL" }

        assertEquals(listOf("a.CAMERA", "a.LOCATION"), groups.map { it.permission })
        val camera = groups.first()
        assertEquals(2, camera.grantedCount)
        assertEquals(3, camera.apps.size)
        // entries sorted by label
        assertEquals(listOf("Alpha", "Beta", "Gamma"), camera.apps.map { it.label })
    }

    @Test
    fun emptyInput() {
        assertTrue(PermissionInfo.buildGroups(emptyList()) { true }.isEmpty())
    }

    @Test
    fun specialOpsAreStable() {
        assertEquals(7, PermissionInfo.specialOps.size)
        PermissionInfo.specialOps.forEach {
            assertTrue(it.op.startsWith("android:"))
            assertTrue(it.label.isNotBlank())
        }
    }

    @Test
    fun shortName() {
        assertEquals("CAMERA", PermissionInfo.shortName("android.permission.CAMERA"))
        assertEquals("PERMIT", PermissionInfo.shortName("custom.PERMIT"))
    }
}
