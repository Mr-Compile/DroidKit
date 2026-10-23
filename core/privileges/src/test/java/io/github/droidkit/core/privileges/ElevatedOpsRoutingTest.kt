package io.github.droidkit.core.privileges

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking

class ElevatedOpsRoutingTest {

    private class FakeExecutor(
        override val kind: ProviderKind,
        private val available: Boolean = true,
    ) : ElevatedExecutor {
        val ran = mutableListOf<String>()
        override suspend fun isAvailable(): Boolean = available
        override suspend fun runShell(command: String, timeoutMs: Long): ElevatedResult {
            ran += command
            return ElevatedResult(kind, 0, "ok", "")
        }
    }

    private fun ops(vararg executors: FakeExecutor) =
        ElevatedOps(executors.toSet(), emptySet(), emptySet())

    @Test
    fun `unpinned action prefers root over adb`() = runBlocking {
        val root = FakeExecutor(ProviderKind.ROOT)
        val adb = FakeExecutor(ProviderKind.ADB)
        val result = ops(adb, root).execute(ElevatedAction.WhoAmI)
        assertEquals(ProviderKind.ROOT, result.provider)
        assertEquals(listOf("id"), root.ran)
        assertTrue(adb.ran.isEmpty())
    }

    @Test
    fun `unpinned action uses adb when root is unavailable`() = runBlocking {
        val root = FakeExecutor(ProviderKind.ROOT, available = false)
        val adb = FakeExecutor(ProviderKind.ADB)
        val result = ops(root, adb).execute(ElevatedAction.WhoAmI)
        assertEquals(ProviderKind.ADB, result.provider)
        assertEquals(listOf("id"), adb.ran)
        assertTrue(root.ran.isEmpty())
    }

    @Test
    fun `pinned action runs on its provider even when a preferred one is ready`() = runBlocking {
        val root = FakeExecutor(ProviderKind.ROOT)
        val adb = FakeExecutor(ProviderKind.ADB)
        val result = ops(adb, root).execute(ElevatedAction.ReadWifiConfigStore)
        assertEquals(ProviderKind.ROOT, result.provider)
        assertEquals(1, root.ran.size)
        assertTrue(adb.ran.isEmpty())
    }

    @Test
    fun `pinned action never falls back to a weaker provider`() = runBlocking {
        val adb = FakeExecutor(ProviderKind.ADB)
        val result = ops(adb).execute(ElevatedAction.ReadWifiConfigStore)
        assertEquals(ProviderKind.ROOT, result.provider)
        assertEquals(-1, result.exitCode)
        assertTrue(result.stderr.contains("requires ROOT"))
        assertTrue(adb.ran.isEmpty())
    }

    @Test
    fun `no provider available returns standard failure`() = runBlocking {
        val result = ops().execute(ElevatedAction.WhoAmI)
        assertEquals(ProviderKind.STANDARD, result.provider)
        assertEquals(-1, result.exitCode)
    }

    @Test
    fun `raw command runs verbatim on the preferred provider`() = runBlocking {
        val adb = FakeExecutor(ProviderKind.ADB)
        val result = ops(adb).runRawCommand("  dumpsys battery  ")
        assertEquals(ProviderKind.ADB, result.provider)
        assertEquals(listOf("dumpsys battery"), adb.ran)
    }

    @Test
    fun `raw command rejects blank multiline and oversized input`() = runBlocking {
        val adb = FakeExecutor(ProviderKind.ADB)
        val ops = ops(adb)
        listOf(
            "   ",
            "id\npm list packages",
            "id\r\nls",
            "x".repeat(ElevatedOps.MAX_RAW_LENGTH + 1),
        ).forEach { bad ->
            val r = ops.runRawCommand(bad)
            assertEquals(-1, r.exitCode)
        }
        assertTrue(adb.ran.isEmpty())
    }

    @Test
    fun `raw command needs a live provider`() = runBlocking {
        val result = ops().runRawCommand("id")
        assertEquals(ProviderKind.STANDARD, result.provider)
        assertEquals(-1, result.exitCode)
    }
}
