package io.github.droidkit.privilege.adb

import java.math.BigInteger
import java.security.PrivateKey
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Minimal DER/X.509 writer — just enough to self-sign a certificate that
 * carries our ADB RSA public key. adbd authenticates the TLS *public key*
 * (matched against the paired `adb_keys` store), not the certificate chain,
 * so a bare v3 cert with no extensions is sufficient and avoids a
 * BouncyCastle dependency for ~100 lines of DER.
 */
object AdbX509 {

    /** AlgorithmIdentifier for sha256WithRSAEncryption (1.2.840.113549.1.1.11 + NULL). */
    private val SHA256_WITH_RSA = byteArrayOf(
        0x30, 0x0d, 0x06, 0x09, 0x2a, 0x86.toByte(), 0x48, 0x86.toByte(),
        0xf7.toByte(), 0x0d, 0x01, 0x01, 0x0b, 0x05, 0x00,
    )

    /** RDNSequence `CN=00`: SEQUENCE { SET { SEQ { OID 2.5.4.3, UTF8String "00" } } }. */
    private val NAME_00 = byteArrayOf(
        0x30, 0x0d, 0x31, 0x0b, 0x30, 0x09, 0x06, 0x03,
        0x55, 0x04, 0x03, 0x0c, 0x02, 0x30, 0x30,
    )

    private val UTC = ZoneOffset.UTC

    /**
     * A self-signed X.509v3 certificate for [publicKey], valid between
     * [notBefore] and [notAfter] (both encoded as UTCTime — keep years inside
     * 1950–2049), signed by [privateKey] with SHA256withRSA.
     */
    fun selfSigned(
        publicKey: RSAPublicKey,
        notBefore: Instant,
        notAfter: Instant,
        privateKey: PrivateKey,
    ): ByteArray {
        val tbs = seq(
            explicit(0, integer(BigInteger.valueOf(2))),
            integer(BigInteger.ONE),
            SHA256_WITH_RSA,
            NAME_00,
            seq(utcTime(notBefore), utcTime(notAfter)),
            NAME_00,
            publicKey.encoded,
        )
        val signature = Signature.getInstance("SHA256withRSA").run {
            initSign(privateKey)
            update(tbs)
            sign()
        }
        return seq(tbs, SHA256_WITH_RSA, bitString(signature))
    }

    // ---------- DER encoding ------------------------------------------------

    private fun seq(vararg parts: ByteArray): ByteArray = tlv(0x30, concat(*parts))

    private fun explicit(tag: Int, content: ByteArray): ByteArray = tlv(0xa0 + tag, content)

    private fun integer(v: BigInteger): ByteArray = tlv(0x02, v.toByteArray())

    private fun utcTime(t: Instant): ByteArray =
        tlv(0x17, DateTimeFormatter.ofPattern("yyMMddHHmmss'Z'").withZone(UTC).format(t)
            .toByteArray(Charsets.US_ASCII))

    private fun bitString(content: ByteArray): ByteArray = tlv(0x03, byteArrayOf(0) + content)

    private fun tlv(tag: Int, content: ByteArray): ByteArray {
        val out = ByteArray(1 + lenBytes(content.size).size + content.size)
        out[0] = tag.toByte()
        val lb = lenBytes(content.size)
        System.arraycopy(lb, 0, out, 1, lb.size)
        System.arraycopy(content, 0, out, 1 + lb.size, content.size)
        return out
    }

    private fun lenBytes(len: Int): ByteArray = when {
        len < 0x80 -> byteArrayOf(len.toByte())
        len < 0x100 -> byteArrayOf(0x81.toByte(), len.toByte())
        else -> byteArrayOf(0x82.toByte(), (len shr 8).toByte(), len.toByte())
    }

    private fun concat(vararg parts: ByteArray): ByteArray {
        val out = ByteArray(parts.sumOf { it.size })
        var off = 0
        for (p in parts) {
            System.arraycopy(p, 0, out, off, p.size)
            off += p.size
        }
        return out
    }
}
