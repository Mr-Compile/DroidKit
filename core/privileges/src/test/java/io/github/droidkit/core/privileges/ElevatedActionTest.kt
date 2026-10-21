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
    fun `suspend and unsuspend commands are user-scoped`() {
        assertEquals(
            "pm suspend --user 0 com.example.app",
            ElevatedAction.SuspendPackage("com.example.app", suspended = true).command(),
        )
        assertEquals(
            "pm unsuspend --user 0 com.example.app",
            ElevatedAction.SuspendPackage("com.example.app", suspended = false).command(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `suspend rejects injection`() {
        ElevatedAction.SuspendPackage("com.a;b", suspended = true).command()
    }

    @Test
    fun `read file head is bounded and quoted`() {
        assertEquals(
            "head -c 4096 '/sdcard/Android/data/com.a/log.txt'",
            ElevatedAction.ReadFileHead("/sdcard/Android/data/com.a/log.txt", 4096).command(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `read file head rejects private path`() {
        ElevatedAction.ReadFileHead("/data/data/com.a/secret.db").command()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `search rejects shell metachars`() {
        ElevatedAction.SearchStorage("/sdcard", "a'; rm -rf /").command()
    }

    @Test
    fun `search renders validated pattern`() {
        val cmd = ElevatedAction.SearchStorage("/sdcard/Android/data", "*.log").command()
        assertTrue(cmd.contains("find '/sdcard/Android/data' -maxdepth 5"))
        assertTrue(cmd.contains("-iname '*[*.log]*'") || cmd.contains("*.log"))
    }

    @Test
    fun `runtime permission commands grant and revoke`() {
        assertEquals(
            "pm grant com.example.app android.permission.CAMERA",
            ElevatedAction.SetRuntimePermission(
                "com.example.app",
                "android.permission.CAMERA",
                granted = true,
            ).command(),
        )
        assertEquals(
            "pm revoke com.example.app android.permission.RECORD_AUDIO",
            ElevatedAction.SetRuntimePermission(
                "com.example.app",
                "android.permission.RECORD_AUDIO",
                granted = false,
            ).command(),
        )
        assertEquals(
            "dumpsys package com.example.app",
            ElevatedAction.GetRuntimePermissions("com.example.app").command(),
        )
    }

    @Test
    fun `runtime permission rejects bad names`() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            ElevatedAction.SetRuntimePermission("com.app", "CAMERA; rm -rf /", true)
        }
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            ElevatedAction.GetRuntimePermissions("bad pkg name").command()
        }
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
    fun `wifi config read is root-pinned with a fixed command`() {
        val action = ElevatedAction.ReadWifiConfigStore
        assertEquals(ProviderKind.ROOT, action.requiredProvider)
        val cmd = action.command()
        assertTrue(cmd.contains("/data/misc/apexdata/com.android.wifi/WifiConfigStore.xml"))
        assertTrue(cmd.contains("/data/misc/wifi/WifiConfigStore.xml"))
        assertTrue(cmd.contains("/data/misc/wifi/wpa_supplicant.conf"))
    }

    @Test
    fun `read-only dump actions are unpinned and param-free`() {
        listOf(
            ElevatedAction.DumpBatteryDetail,
            ElevatedAction.ReadLearnedBatteryCapacity,
            ElevatedAction.DumpWifiLink,
            ElevatedAction.ReadSystemProps,
            ElevatedAction.ReadThermalZones,
            ElevatedAction.ReadCpuInfo,
            ElevatedAction.ReadMeminfo,
            ElevatedAction.ReadPartitions,
        ).forEach { action ->
            org.junit.Assert.assertNull(action.requiredProvider)
            assertTrue(action.command().isNotBlank())
        }
        assertEquals("getprop", ElevatedAction.ReadSystemProps.command())
        assertEquals("cat /proc/cpuinfo", ElevatedAction.ReadCpuInfo.command())
        assertEquals("cat /proc/meminfo", ElevatedAction.ReadMeminfo.command())
        assertEquals("cat /proc/partitions", ElevatedAction.ReadPartitions.command())
    }

    @Test
    fun `device-identity reads are unpinned and param-free`() {
        listOf(
            ElevatedAction.GetSeLinuxStatus,
            ElevatedAction.ReadLinkAddresses,
            ElevatedAction.ReadDeviceSettings,
            ElevatedAction.DumpPhoneIdentity,
        ).forEach { action ->
            org.junit.Assert.assertNull(action.requiredProvider)
            assertTrue(action.command().isNotBlank())
        }
        assertEquals("getenforce", ElevatedAction.GetSeLinuxStatus.command())
        assertTrue(ElevatedAction.ReadLinkAddresses.command().contains("/sys/class/net/"))
        val settings = ElevatedAction.ReadDeviceSettings.command()
        assertTrue(settings.contains("global device_name"))
        assertTrue(settings.contains("secure bluetooth_address"))
        assertTrue(ElevatedAction.DumpPhoneIdentity.command().startsWith("dumpsys phone"))
    }

    @Test
    fun `power-supply read is a fixed allow-listed dump`() {
        val cmd = ElevatedAction.ReadPowerSupply.command()
        assertTrue(cmd.startsWith("grep -H . /sys/class/power_supply/"))
        assertTrue(cmd.endsWith("2>/dev/null"))
        // No parameter injection is possible: the command takes no input.
        assertTrue(listOf("charge_full", "charge_full_design", "cycle_count").all { it in cmd })
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

    @Test
    fun `storage listing quotes the validated path and stats each entry`() {
        val cmd = ElevatedAction.ListStorageDir("/sdcard/Android/data/com.app").command()
        assertTrue(cmd.startsWith("cd '/sdcard/Android/data/com.app' &&"))
        assertTrue(cmd.contains("stat -Lc '%F|%s|%Y|%n'"))
        assertTrue(cmd.contains("2>/dev/null"))
    }

    @Test
    fun `storage paths outside shared storage are rejected`() {
        listOf(
            "/data/data/com.app",
            "/sdcard/../data/data",
            "/sdcard/Android/../../etc/passwd",
            "sdcard/Android/data", // not absolute
            "/sdcardAndroid/data", // prefix trick — no separator
            "",
        ).forEach { bad ->
            assertTrue(
                "expected rejection of '$bad'",
                runCatching { ElevatedAction.ListStorageDir(bad).command() }.isFailure,
            )
            assertTrue(
                "expected rejection of '$bad'",
                runCatching { ElevatedAction.CopyToDownload(bad).command() }.isFailure,
            )
        }
    }

    @Test
    fun `storage paths with shell-breaking characters are rejected`() {
        listOf(
            "/sdcard/Android/data/it's",
            "/sdcard/Android/data/x\ny",
            "/sdcard/" + "a".repeat(600),
        ).forEach { bad ->
            assertTrue(
                "expected rejection",
                runCatching { ElevatedAction.ListStorageDir(bad).command() }.isFailure,
            )
        }
    }

    @Test
    fun `storage roots and valid paths are accepted`() {
        listOf(
            "/sdcard",
            "/storage/emulated/0",
            "/sdcard/Android/data",
            "/storage/emulated/0/Android/obb",
            "/sdcard/Download/a file.txt",
        ).forEach { good ->
            assertTrue(
                "expected acceptance of '$good'",
                runCatching { ElevatedAction.ListStorageDir(good).command() }.isSuccess,
            )
        }
    }

    @Test
    fun `copy-out writes only to the fixed Download destination`() {
        val cmd = ElevatedAction.CopyToDownload("/sdcard/Android/data/com.app/files").command()
        assertTrue(cmd.startsWith("mkdir -p '/sdcard/Download/DroidKit'"))
        assertTrue(cmd.contains("cp -r '/sdcard/Android/data/com.app/files' '/sdcard/Download/DroidKit/'"))
        // The destination is fixed — callers cannot redirect it.
        assertTrue(cmd.endsWith("echo \"== /sdcard/Download/DroidKit/\""))
    }
}
