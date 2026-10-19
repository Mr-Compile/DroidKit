package io.github.droidkit.privilege.adb

import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Minimal Edwards-curve math on edwards25519 — only what ADB's SPAKE2 pairing
 * needs: point decode/encode, add/sub, scalar multiply. Extended homogeneous
 * coordinates (Hisil formulas, a = -1).
 */
object Ed25519 {
    private val P: BigInteger = BigInteger.ONE.shiftLeft(255).subtract(BigInteger.valueOf(19))
    private val D: BigInteger = BigInteger.valueOf(-121665)
        .multiply(BigInteger.valueOf(121666).modInverse(P)).mod(P)
    private val SQRT_M1: BigInteger = BigInteger.valueOf(2)
        .modPow(P.subtract(BigInteger.ONE).shiftRight(2), P)

    /** Group order l = 2^252 + 27742317777372353535851937790883648493. */
    val L: BigInteger = BigInteger.ONE.shiftLeft(252)
        .add(BigInteger("27742317777372353535851937790883648493"))

    data class Point(val x: BigInteger, val y: BigInteger, val z: BigInteger, val t: BigInteger) {
        fun affine(): Pair<BigInteger, BigInteger> {
            val zi = z.modInverse(P)
            return x.multiply(zi).mod(P) to y.multiply(zi).mod(P)
        }
    }

    private val BY: BigInteger = BigInteger.valueOf(4)
        .multiply(BigInteger.valueOf(5).modInverse(P)).mod(P)
    private val BX: BigInteger = recoverX(BY, 0)
    val BASE = Point(BX, BY, BigInteger.ONE, BX.multiply(BY).mod(P))
    val IDENTITY = Point(BigInteger.ZERO, BigInteger.ONE, BigInteger.ONE, BigInteger.ZERO)

    private fun recoverX(y: BigInteger, sign: Int): BigInteger {
        val y2 = y.multiply(y).mod(P)
        val u = y2.subtract(BigInteger.ONE).mod(P)
        val v = D.multiply(y2).add(BigInteger.ONE).mod(P)
        var x = u.multiply(v.pow(3)).multiply(u.multiply(v.pow(7)).modPow(P.subtract(BigInteger.valueOf(5)).shiftRight(3), P)).mod(P)
        if (x.multiply(x).subtract(v.modInverse(P).multiply(u)).mod(P) != BigInteger.ZERO) {
            x = x.multiply(SQRT_M1).mod(P)
        }
        if (x.testBit(0) != (sign == 1)) x = P.subtract(x)
        return x
    }

    fun decodePoint(bytes: ByteArray): Point {
        require(bytes.size == 32) { "point must be 32 bytes" }
        val yBytes = bytes.copyOf()
        val sign = (yBytes[31].toInt() shr 7) and 1
        yBytes[31] = (yBytes[31].toInt() and 0x7f).toByte()
        val y = BigInteger(1, yBytes.reversedArray())
        val x = recoverX(y, sign)
        return Point(x, y, BigInteger.ONE, x.multiply(y).mod(P))
    }

    fun encodePoint(p: Point): ByteArray {
        val (x, y) = p.affine()
        val b = y.toByteArray().reversedArray()
        val out = ByteArray(32)
        // toByteArray is big-endian, possibly with a sign byte — keep low 32 LE bytes.
        for (i in 0 until minOf(b.size, 32)) out[i] = b[i]
        if (x.testBit(0)) out[31] = (out[31].toInt() or 0x80).toByte()
        return out
    }

    fun add(p: Point, q: Point): Point {
        val a = p.x.multiply(q.x).mod(P)
        val b = p.y.multiply(q.y).mod(P)
        val c = D.multiply(p.t).mod(P).multiply(q.t).mod(P)
        val d = p.z.multiply(q.z).mod(P)
        val e = p.x.add(p.y).mod(P)
            .multiply(q.x.add(q.y).mod(P)).mod(P)
            .subtract(a).subtract(b).mod(P)
        val f = d.subtract(c).mod(P)
        val g = d.add(c).mod(P)
        val h = b.add(a).mod(P)
        return Point(
            x = e.multiply(f).mod(P),
            y = g.multiply(h).mod(P),
            z = f.multiply(g).mod(P),
            t = e.multiply(h).mod(P),
        )
    }

    fun subtract(p: Point, q: Point): Point = add(p, negate(q))

    fun negate(p: Point): Point =
        Point(P.subtract(p.x).mod(P), p.y, p.z, P.subtract(p.t).mod(P))

    fun scalarMult(p: Point, scalar: BigInteger): Point {
        var s = scalar.mod(L.multiply(BigInteger.valueOf(16))) // bound work; keeps multiples intact
        var result = IDENTITY
        var addend = p
        while (s > BigInteger.ZERO) {
            if (s.testBit(0)) result = add(result, addend)
            addend = add(addend, addend)
            s = s.shiftRight(1)
        }
        return result
    }

    /** 32-byte little-endian scalar -> BigInteger. */
    fun scalarFromLe(bytes: ByteArray): BigInteger =
        BigInteger(1, bytes.reversedArray())

    /** Interpret [bytes] (little-endian) mod L. */
    fun reduceLe(bytes: ByteArray): BigInteger =
        BigInteger(1, bytes.reversedArray()).mod(L)
}

/**
 * SPAKE2 over edwards25519 exactly as BoringSSL implements it for ADB pairing
 * (`crypto/curve25519/spake25519.cc` + `pairing_auth.cpp`): client is Alice
 * named "adb pair client", server is Bob named "adb pair server" (both with
 * trailing NULs, matching sizeof() in C).
 */
class Spake2(password: ByteArray, private val isClient: Boolean = true) {
    private val privateScalar: BigInteger
    private val passwordScalar: BigInteger
    private val passwordHash: ByteArray
    private var myMsg: ByteArray = ByteArray(0)

    private val myName = if (isClient) CLIENT_NAME else SERVER_NAME
    private val theirName = if (isClient) SERVER_NAME else CLIENT_NAME
    private val myMaskPoint get() = if (isClient) SPAKE_M else SPAKE_N
    private val theirMaskPoint get() = if (isClient) SPAKE_N else SPAKE_M

    init {
        val rand = ByteArray(64).also { SecureRandom().nextBytes(it) }
        privateScalar = Ed25519.reduceLe(rand).shiftLeft(3)

        passwordHash = sha512(password)
        var w = Ed25519.reduceLe(passwordHash)
        val l = Ed25519.L
        if (w.testBit(0)) w = w.add(l)
        if (w.testBit(1)) w = w.add(l.shiftLeft(1))
        if (w.testBit(2)) w = w.add(l.shiftLeft(2))
        passwordScalar = w
    }

    /** Our 32-byte SPAKE2 message: P* = x·B + w·M (client) or x·B + w·N (server). */
    fun generateMsg(): ByteArray {
        val p = Ed25519.scalarMult(Ed25519.BASE, privateScalar)
        val mask = Ed25519.scalarMult(Ed25519.decodePoint(myMaskPoint), passwordScalar)
        myMsg = Ed25519.encodePoint(Ed25519.add(p, mask))
        return myMsg
    }

    /**
     * Consume the server's message and derive the shared key material.
     * Q* - w·N -> dh = x·Q -> key = SHA512( transcript || dh || pw_hash )[:32]
     */
    fun processMsg(theirMsg: ByteArray): ByteArray {
        require(theirMsg.size == 32) { "peer message must be 32 bytes" }
        require(myMsg.isNotEmpty()) { "generateMsg must be called first" }
        val qStar = Ed25519.decodePoint(theirMsg)
        val peersMask = Ed25519.scalarMult(Ed25519.decodePoint(theirMaskPoint), passwordScalar)
        val q = Ed25519.subtract(qStar, peersMask)
        val dh = Ed25519.encodePoint(Ed25519.scalarMult(q, privateScalar))

        val sha = MessageDigest.getInstance("SHA-512")
        fun lp(data: ByteArray) {
            var l = data.size.toLong()
            val len = ByteArray(8)
            for (i in 0 until 8) { len[i] = (l and 0xff).toByte(); l = l shr 8 }
            sha.update(len); sha.update(data)
        }
        // Transcript order is always (client-name, server-name, client-msg,
        // server-msg) regardless of our role — matches BoringSSL.
        lp(CLIENT_NAME); lp(SERVER_NAME)
        if (isClient) { lp(myMsg); lp(theirMsg) } else { lp(theirMsg); lp(myMsg) }
        lp(dh)
        lp(passwordHash)
        return sha.digest().copyOf(32)
    }

    companion object {
        val CLIENT_NAME: ByteArray = "adb pair client\u0000".toByteArray(Charsets.UTF_8)
        val SERVER_NAME: ByteArray = "adb pair server\u0000".toByteArray(Charsets.UTF_8)

        /** SPAKE2 M point (prover/Alice mask), compressed encoding. */
        val SPAKE_M: ByteArray = hex("5ada7e4bf6ddd9adb6626d32131c6b5c51a1e347a3478f53cfcf441b88eed12e")

        /** SPAKE2 N point (verifier/Bob mask), compressed encoding. */
        val SPAKE_N: ByteArray = hex("10e3df0ae37d8e7a99b5fe74b44672103dbddcbd06af680d71329a11693bc778")

        fun sha512(data: ByteArray): ByteArray =
            MessageDigest.getInstance("SHA-512").digest(data)

        private fun hex(s: String): ByteArray =
            ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }
}

/** HKDF-SHA256 (RFC 5869), single output block as used by pairing_auth. */
object HkdfSha256 {
    fun derive(keyMaterial: ByteArray, info: ByteArray, length: Int): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        // extract: salt = 32 zero bytes (HashLen of SHA-256, salt=null in AOSP)
        mac.init(SecretKeySpec(ByteArray(32), "HmacSHA256"))
        val prk = mac.doFinal(keyMaterial)
        val out = ByteArray(length)
        var prev = ByteArray(0)
        var off = 0
        var i = 1
        while (off < length) {
            mac.init(SecretKeySpec(prk, "HmacSHA256"))
            mac.update(prev)
            mac.update(info)
            mac.update(i.toByte())
            prev = mac.doFinal()
            val n = minOf(prev.size, length - off)
            System.arraycopy(prev, 0, out, off, n)
            off += n; i++
        }
        return out
    }
}
