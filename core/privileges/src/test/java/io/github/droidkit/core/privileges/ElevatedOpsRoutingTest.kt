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
    fun `unpinned action prefers shizuku over root and adb`() = runBlocking {
        val shizuku = FakeExecutor(ProviderKind.SHIZUKU)
        val root = FakeExecutor(ProviderKind.ROOT)
        val adb = FakeExecutor(ProviderKind.ADB)
        val result = ops(shizuku, root, adb).execute(ElevatedAction.WhoAmI)
        assertEquals(ProviderKind.SHIZUKU, result.provider)
        assertEquals(listOf("id"), shizuku.ran)
        assertTrue(root.ran.isEmpty())
    }

    @Test
    fun `pinned action runs on its provider even when a preferred one is ready`() = runBlocking {
        val shizuku = FakeExecutor(ProviderKind.SHIZUKU)
        val root = FakeExecutor(ProviderKind.ROOT)
        val result = ops(shizuku, root).execute(ElevatedAction.ReadWifiConfigStore)
        assertEquals(ProviderKind.ROOT, result.provider)
        assertEquals(1, root.ran.size)
        assertTrue(shizuku.ran.isEmpty())
    }

    @Test
    fun `pinned action never falls back to a weaker provider`() = runBlocking {
        val shizuku = FakeExecutor(ProviderKind.SHIZUKU)
        val result = ops(shizuku).execute(ElevatedAction.ReadWifiConfigStore)
        assertEquals(ProviderKind.ROOT, result.provider)
        assertEquals(-1, result.exitCode)
        assertTrue(result.stderr.contains("requires ROOT"))
        assertTrue(shizuku.ran.isEmpty())
    }

    @Test
    fun `no provider available returns standard failure`() = runBlocking {
        val result = ops().execute(ElevatedAction.WhoAmI)
        assertEquals(ProviderKind.STANDARD, result.provider)
        assertEquals(-1, result.exitCode)
    }
}
