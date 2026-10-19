package io.github.droidkit.privilege.adb

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class Spake2Test {

    @Test
    fun `basepoint encodes to canonical ed25519 form`() {
        // The standard Ed25519 basepoint compressed encoding.
        assertEquals(
            "5866666666666666666666666666666666666666666666666666666666666666",
            Ed25519.encodePoint(Ed25519.BASE).toHex(),
        )
    }

    @Test
    fun `group order times base is identity`() {
        val p = Ed25519.scalarMult(Ed25519.BASE, Ed25519.L)
        assertEquals(Ed25519.encodePoint(Ed25519.IDENTITY).toHex(), Ed25519.encodePoint(p).toHex())
    }

    @Test
    fun `mask points decode and re-encode identically`() {
        assertArrayEquals(Spake2.SPAKE_M, Ed25519.encodePoint(Ed25519.decodePoint(Spake2.SPAKE_M)))
        assertArrayEquals(Spake2.SPAKE_N, Ed25519.encodePoint(Ed25519.decodePoint(Spake2.SPAKE_N)))
    }

    @Test
    fun `matching passwords derive identical keys`() {
        val password = "123456".toByteArray()
        val alice = Spake2(password, isClient = true)
        val bob = Spake2(password, isClient = false)
        val aliceMsg = alice.generateMsg()
        val bobMsg = bob.generateMsg()
        val aliceKey = alice.processMsg(bobMsg)
        val bobKey = bob.processMsg(aliceMsg)
        assertArrayEquals(aliceKey, bobKey)
        assertEquals(32, aliceKey.size)
    }

    @Test
    fun `different passwords derive different keys`() {
        val alice = Spake2("123456".toByteArray(), isClient = true)
        val bob = Spake2("654321".toByteArray(), isClient = false)
        val aliceMsg = alice.generateMsg()
        val bobMsg = bob.generateMsg()
        val aliceKey = alice.processMsg(bobMsg)
        val bobKey = bob.processMsg(aliceMsg)
        assertFalse(aliceKey.contentEquals(bobKey))
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }
}
