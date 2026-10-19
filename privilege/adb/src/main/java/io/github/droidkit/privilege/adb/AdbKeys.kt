package io.github.droidkit.privilege.adb

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64

/**
 * DroidKit's ADB identity: an RSA-2048 keypair kept in AndroidKeyStore so the
 * private key never leaves the TEE. The public half is encoded in the ADB
 * `RSAPublicKey` wire format (the same layout `~/.android/adbkey.pub` uses).
 */
class AdbKeys(
    private val deviceName: String = "droidkit@device",
) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private val publicKey: RSAPublicKey by lazy { ensureKey() }

    private fun ensureKey(): RSAPublicKey {
        val entry = keyStore.getEntry(KEY_ALIAS, null)
        if (entry is KeyStore.PrivateKeyEntry) {
            val key = KeyFactory.getInstance("RSA")
                .generatePublic(RSAPublicKeySpec(entry.certificate.publicKey.let { it as RSAPublicKey }.modulus, (entry.certificate.publicKey as RSAPublicKey).publicExponent))
            return key as RSAPublicKey
        }
        val gen = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        gen.initialize(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN)
                .setKeySize(2048)
                .setDigests(KeyProperties.DIGEST_SHA1)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .build()
        )
        return gen.generateKeyPair().public as RSAPublicKey
    }

    private fun privateKey(): PrivateKey =
        (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry).privateKey

    /** Sign an ADB AUTH token — RSA PKCS1 v1.5 over SHA-1 of the token. */
    fun signToken(token: ByteArray): ByteArray =
        Signature.getInstance("SHA1withRSA").run {
            initSign(privateKey())
            update(token)
            sign()
        }

    /**
     * The public key in ADB wire format: base64 of the little-endian
     * RSAPublicKey struct followed by " user@host", NUL-terminated.
     */
    fun publicKeyPayload(): ByteArray =
        ("${base64PublicKey()} $deviceName").toByteArray(Charsets.UTF_8) + byteArrayOf(0)

    /** Raw `base64(pubkey) name` string — also sent as pairing PeerInfo. */
    fun publicKeyString(): String = "${base64PublicKey()} $deviceName"

    private fun base64PublicKey(): String {
        val n = publicKey.modulus
        val e = publicKey.publicExponent
        val modWords = KEY_SIZE_WORDS
        val n0inv = BigInteger.ZERO.subtract(n.modInverse(UINT32)).mod(UINT32)

        val nWords = littleEndianWords(n, modWords)
        val rrWords = littleEndianWords(R2.mod(n), modWords)

        val out = ByteArray(4 + 4 + modWords * 4 + modWords * 4 + 4)
        var o = 0
        fun putInt(v: Long) {
            out[o++] = (v and 0xff).toByte()
            out[o++] = (v shr 8 and 0xff).toByte()
            out[o++] = (v shr 16 and 0xff).toByte()
            out[o++] = (v shr 24 and 0xff).toByte()
        }
        putInt(modWords.toLong())
        putInt(n0inv.longValueExact())
        nWords.forEach { putInt(it) }
        rrWords.forEach { putInt(it) }
        putInt(e.longValueExact())
        return Base64.getEncoder().encodeToString(out)
    }

    /** Split [v] into [count] little-endian 32-bit words (low word first). */
    private fun littleEndianWords(v: BigInteger, count: Int): List<Long> =
        (0 until count).map { i -> v.shiftRight(i * 32).and(UINT32_MINUS1).longValueExact() }

    private companion object {
        const val KEY_ALIAS = "droidkit_adb_identity"
        const val KEY_SIZE_WORDS = 64 // 2048-bit modulus / 32
        val UINT32: BigInteger = BigInteger.ONE.shiftLeft(32)
        val UINT32_MINUS1: BigInteger = UINT32.subtract(BigInteger.ONE)
        val R2: BigInteger = BigInteger.ONE.shiftLeft(4096) // (2^2048)^2 for Montgomery RR
    }
}
