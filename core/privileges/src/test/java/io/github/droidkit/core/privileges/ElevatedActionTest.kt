package io.github.droidkit.core.privileges

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElevatedActionTest {

    @Test
    fun `appops get renders validated package`() {
        assertEquals(
            "appops get com.example.app",
            ElevatedAction.GetAppOps("com.example.app").command(),
        )
    }

    @Test
    fun `appops set renders op and mode`() {
        assertEquals(
            "appops set com.example.app RUN_IN_BACKGROUND deny",
            ElevatedAction.SetAppOp("com.example.app", "RUN_IN_BACKGROUND", AppOpMode.DENY).command(),
        )
    }

    @Test
    fun `package enable and disable-user commands`() {
        assertEquals(
            "pm enable com.example.app",
            ElevatedAction.SetPackageEnabled("com.example.app", enabled = true).command(),
        )
        assertEquals(
            "pm disable-user --user 0 com.example.app",
            ElevatedAction.SetPackageEnabled("com.example.app", enabled = false).command(),
        )
    }

    @Test
    fun `package data and uninstall commands are user-scoped`() {
        assertEquals(
            "pm install-existing com.example.app",
            ElevatedAction.InstallExisting("com.example.app").command(),
        )
        assertEquals(
            "pm clear --user 0 com.example.app",
            ElevatedAction.ClearPackageData("com.example.app").command(),
        )
        assertEquals(
            "pm uninstall -k --user 0 com.example.app",
            ElevatedAction.UninstallForUser("com.example.app").command(),
        )
    }

    @Test
    fun `getprop and logcat and pm list commands`() {
        assertEquals("getprop ro.build.version.sdk", ElevatedAction.GetProp("ro.build.version.sdk").command())
        assertEquals("logcat -d -t 200", ElevatedAction.ReadLog(200).command())
        assertEquals("logcat -d -t 50 -s MyTag:*", ElevatedAction.ReadLog(50, "MyTag").command())
        assertEquals("pm list packages -d -e", ElevatedAction.ListPackages().command())
        assertEquals("pm list packages", ElevatedAction.ListPackages(includeDisabled = false).command())
    }

    @Test
    fun `identity and reboot commands`() {
        assertEquals("id", ElevatedAction.WhoAmI.command())
        assertEquals("svc power reboot", ElevatedAction.Reboot.command())
    }

    @Test
    fun `injection through package name is rejected`() {
        listOf("com.app;rm -rf /", "com.app && id", "com.app\nevil", "com.app$(id)").forEach { bad ->
            val threw = runCatching { ElevatedAction.GetAppOps(bad).command() }.isFailure
            assertTrue("expected rejection of $bad", threw)
        }
    }

    @Test
    fun `injection through op name is rejected`() {
        val threw = runCatching {
            ElevatedAction.SetAppOp("com.example.app", "OP;id", AppOpMode.ALLOW).command()
        }.isFailure
        assertTrue(threw)
    }

    @Test
    fun `injection through prop key and log tag is rejected`() {
        assertTrue(runCatching { ElevatedAction.GetProp("ro.prop;id").command() }.isFailure)
        assertTrue(runCatching { ElevatedAction.ReadLog(10, "tag;id").command() }.isFailure)
        assertTrue(runCatching { ElevatedAction.ReadLog(0).command() }.isFailure)
        assertTrue(runCatching { ElevatedAction.ReadLog(5001).command() }.isFailure)
    }
}
