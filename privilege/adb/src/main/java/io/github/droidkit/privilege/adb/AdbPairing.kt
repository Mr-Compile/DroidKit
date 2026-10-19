package io.github.droidkit.privilege.adb

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.X509TrustManager

/**
 * ADB wireless-debugging pairing client — the on-device equivalent of
 * `adb pair host:port <code>`. Mirrors AOSP `pairing_connection.cpp`:
 * TLS socket -> SPAKE2_MSG exchange -> AES-128-GCM'd PEER_INFO exchange.
 */
object AdbPairing {

    private const val PACKET_VERSION = 1
    private const val TYPE_SPAKE2_MSG = 0
    private const val TYPE_PEER_INFO = 1
    private const val PEER_INFO_SIZE = 8192
    private const val PEER_INFO_TYPE_RSA_PUB_KEY = 0
    private const val AES_GCM_INFO = "adb pairing_auth aes-128-gcm key"
    private const val CONNECT_TIMEOUT_MS = 15_000

    data class PairResult(
        val peerInfoType: Int,
        val peerInfoSummary: String,
    )

    /**
     * Runs the pairing handshake. On success the device's adbd has stored our
     * public key and [AdbConnection] can connect on the normal service port.
     */
    fun pair(host: String, port: Int, code: String, keys: AdbKeys): PairResult {
        val socket = trustAllSocket(host, port)
        try {
            val input = DataInputStream(socket.getInputStream())
            val output = DataOutputStream(socket.getOutputStream())

            val spake2 = Spake2(code.toByteArray(Charsets.UTF_8))
            writePacket(output, TYPE_SPAKE2_MSG, spake2.generateMsg())

            val their = readPacket(input)
            check(their.type == TYPE_SPAKE2_MSG) { "expected SPAKE2_MSG, got ${their.type}" }
            val keyMaterial = spake2.processMsg(their.payload)
            val aesKey = HkdfSha256.derive(
                keyMaterial, AES_GCM_INFO.toByteArray(Charsets.UTF_8), 16,
            )

            val peerInfo = ByteArray(PEER_INFO_SIZE)
            peerInfo[0] = PEER_INFO_TYPE_RSA_PUB_KEY.toByte()
            val pubBytes = keys.publicKeyString().toByteArray(Charsets.UTF_8)
            check(pubBytes.size < PEER_INFO_SIZE - 1) { "public key too large" }
            System.arraycopy(pubBytes, 0, peerInfo, 1, pubBytes.size)
            writePacket(output, TYPE_PEER_INFO, gcm(aesKey, 0, Cipher.ENCRYPT_MODE, peerInfo))

            val theirInfo = readPacket(input)
            check(theirInfo.type == TYPE_PEER_INFO) { "expected PEER_INFO, got ${theirInfo.type}" }
            val decrypted = gcm(aesKey, 0, Cipher.DECRYPT_MODE, theirInfo.payload)
            check(decrypted.size == PEER_INFO_SIZE) {
                "peer info size ${decrypted.size} != $PEER_INFO_SIZE"
            }
            val theirType = decrypted[0].toInt() and 0xff
            val summary = String(decrypted, 1, printableLen(decrypted), Charsets.UTF_8).trim()
            return PairResult(theirType, summary.ifEmpty { "peer registered" })
        } finally {
            runCatching { socket.close() }
        }
    }

    private fun printableLen(buf: ByteArray): Int {
        var end = 0
        for (i in 1 until buf.size) if (buf[i] != 0.toByte()) end = i
        return end.coerceAtLeast(1)
    }

    /** AES-128-GCM with a 12-byte nonce = 8-byte LE sequence counter. */
    private fun gcm(key: ByteArray, sequence: Long, mode: Int, data: ByteArray): ByteArray {
        val nonce = ByteArray(12)
        var s = sequence
        for (i in 0 until 8) { nonce[i] = (s and 0xff).toByte(); s = s shr 8 }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(mode, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
        return cipher.doFinal(data)
    }

    /**
     * TLS to the pairing service. The device presents a self-signed cert;
     * authenticity is established by SPAKE2 (the pairing code) and the later
     * ADB RSA auth, not by CA — same trust model as `adb pair`.
     */
    private fun trustAllSocket(host: String, port: Int): SSLSocket {
        val tm = object : X509TrustManager {
            override fun checkClientTrusted(certs: Array<X509Certificate>?, auth: String?) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>?, auth: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        val ctx = SSLContext.getInstance("TLS")
        ctx.init(null, arrayOf(tm), SecureRandom())
        val socket = ctx.socketFactory.createSocket() as SSLSocket
        socket.soTimeout = CONNECT_TIMEOUT_MS
        socket.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
        socket.startHandshake()
        return socket
    }

    private class Packet(val type: Int, val payload: ByteArray)

    private fun writePacket(out: DataOutputStream, type: Int, payload: ByteArray) {
        val header = ByteArray(6)
        header[0] = PACKET_VERSION.toByte()
        header[1] = type.toByte()
        val len = payload.size
        header[2] = (len shr 24 and 0xff).toByte()
        header[3] = (len shr 16 and 0xff).toByte()
        header[4] = (len shr 8 and 0xff).toByte()
        header[5] = (len and 0xff).toByte()
        out.write(header)
        out.write(payload)
        out.flush()
    }

    private fun readPacket(input: DataInputStream): Packet {
        val header = ByteArray(6)
        input.readFully(header)
        val version = header[0].toInt() and 0xff
        check(version == PACKET_VERSION) { "unsupported pairing packet version $version" }
        val type = header[1].toInt() and 0xff
        val len = ((header[2].toInt() and 0xff) shl 24) or
            ((header[3].toInt() and 0xff) shl 16) or
            ((header[4].toInt() and 0xff) shl 8) or
            (header[5].toInt() and 0xff)
        check(len in 1..PEER_INFO_SIZE * 2) { "bad payload size $len" }
        val payload = ByteArray(len)
        input.readFully(payload)
        return Packet(type, payload)
    }
}
