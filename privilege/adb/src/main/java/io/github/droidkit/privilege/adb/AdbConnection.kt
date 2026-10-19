package io.github.droidkit.privilege.adb

import io.github.droidkit.privilege.adb.AdbProtocol.A_AUTH
import io.github.droidkit.privilege.adb.AdbProtocol.A_CLSE
import io.github.droidkit.privilege.adb.AdbProtocol.A_CNXN
import io.github.droidkit.privilege.adb.AdbProtocol.A_OKAY
import io.github.droidkit.privilege.adb.AdbProtocol.A_OPEN
import io.github.droidkit.privilege.adb.AdbProtocol.A_STLS
import io.github.droidkit.privilege.adb.AdbProtocol.A_VERSION
import io.github.droidkit.privilege.adb.AdbProtocol.A_WRTE
import io.github.droidkit.privilege.adb.AdbProtocol.AUTH_RSAPUBLICKEY
import io.github.droidkit.privilege.adb.AdbProtocol.AUTH_SIGNATURE
import io.github.droidkit.privilege.adb.AdbProtocol.AUTH_TOKEN
import io.github.droidkit.privilege.adb.AdbProtocol.AdbMessage
import io.github.droidkit.privilege.adb.AdbProtocol.MAX_PAYLOAD
import io.github.droidkit.privilege.adb.AdbProtocol.STLS_VERSION
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.X509TrustManager

/**
 * A live ADB session — TCP or TLS (wireless debugging). One connection runs
 * one command at a time; [AdbProvider] serializes callers.
 */
class AdbConnection(private val keys: AdbKeys) {

    class ShellResult(
        val exitCode: Int,
        val stdout: String,
    )

    private var socket: Socket? = null
    private var input: DataInputStream? = null
    private var output: java.io.OutputStream? = null
    private var localIdCounter = 1

    var systemBanner: String = ""
        private set

    val isConnected: Boolean
        get() = socket?.isConnected == true && socket?.isClosed == false

    @Synchronized
    fun connect(host: String, port: Int, useTls: Boolean, timeoutMs: Int = 15_000) {
        close()
        val sock = Socket()
        sock.soTimeout = timeoutMs
        sock.connect(InetSocketAddress(host, port), timeoutMs)
        socket = sock
        input = DataInputStream(sock.getInputStream())
        output = sock.getOutputStream()

        if (useTls) upgradeTls(sock, timeoutMs)
        handshake()
        // Keep reads blocking with a moderate timeout for command streams.
        socket?.soTimeout = 10_000
    }

    /** STLS upgrade — wireless debugging wraps the channel in TLS first. */
    private fun upgradeTls(plain: Socket, timeoutMs: Int) {
        send(AdbMessage(A_STLS, STLS_VERSION, 0, ByteArray(0)))
        val reply = read()
        if (reply.command != A_STLS) {
            throw IOException("adb: expected STLS, got 0x${reply.command.toString(16)}")
        }
        val tm = object : X509TrustManager {
            override fun checkClientTrusted(c: Array<X509Certificate>?, a: String?) {}
            override fun checkServerTrusted(c: Array<X509Certificate>?, a: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        val ctx = SSLContext.getInstance("TLS")
        ctx.init(null, arrayOf(tm), SecureRandom())
        val tls = ctx.socketFactory.createSocket(
            plain, plain.inetAddress.hostAddress, plain.port, true,
        ) as SSLSocket
        tls.soTimeout = timeoutMs
        tls.startHandshake()
        socket = tls
        input = DataInputStream(tls.getInputStream())
        output = tls.getOutputStream()
    }

    /** CNXN / AUTH key exchange — signs tokens; offers our public key once. */
    private fun handshake() {
        send(AdbMessage(A_CNXN, A_VERSION, MAX_PAYLOAD, BANNER.toByteArray()))
        var offeredKey = false
        repeat(8) {
            val msg = read()
            when {
                msg.command == A_CNXN -> {
                    systemBanner = String(msg.payload, Charsets.UTF_8)
                    return
                }
                msg.command == A_AUTH && msg.arg0 == AUTH_TOKEN -> {
                    send(AdbMessage(A_AUTH, AUTH_SIGNATURE, 0, keys.signToken(msg.payload)))
                }
                msg.command == A_AUTH && !offeredKey -> {
                    send(AdbMessage(A_AUTH, AUTH_RSAPUBLICKEY, 0, keys.publicKeyPayload()))
                    offeredKey = true
                }
                else -> throw IOException(
                    "adb: unexpected auth message 0x${msg.command.toString(16)} arg0=${msg.arg0}",
                )
            }
        }
        throw IOException("adb: authentication failed — device did not accept the key")
    }

    /**
     * Runs [command] through the `shell:` service. Exit code is recovered via
     * an `echo __DKX_<code>` trailer; stderr is merged into stdout by adbd.
     */
    @Synchronized
    fun shell(command: String, timeoutMs: Long = 30_000): ShellResult {
        check(isConnected) { "adb: not connected" }
        val localId = localIdCounter++
        val wrapped = "$command; echo ${EXIT_MARKER}$?"
        send(AdbMessage(A_OPEN, localId, 0, "shell:$wrapped".toByteArray() + byteArrayOf(0)))

        val stdout = ByteArrayOutputStream()
        var remoteId = -1
        var opened = false
        var exitCode = -1
        val deadline = System.currentTimeMillis() + timeoutMs

        while (true) {
            if (System.currentTimeMillis() > deadline) {
                runCatching {
                    send(AdbMessage(A_CLSE, localId, remoteId.coerceAtLeast(0), ByteArray(0)))
                }
                throw IOException("adb: command timed out")
            }
            val msg = try {
                read()
            } catch (e: java.net.SocketTimeoutException) {
                continue
            }
            when (msg.command) {
                A_OKAY -> {
                    if (msg.arg1 == localId) { remoteId = msg.arg0; opened = true }
                }
                A_WRTE -> if (msg.arg1 == localId) {
                    stdout.write(msg.payload)
                    send(AdbMessage(A_OKAY, localId, msg.arg0, ByteArray(0)))
                }
                A_CLSE -> {
                    if (msg.arg1 == localId || (opened && msg.arg0 == remoteId)) {
                        send(AdbMessage(A_CLSE, localId, remoteId.coerceAtLeast(0), ByteArray(0)))
                        break
                    }
                }
            }
        }

        val text = stdout.toString(Charsets.UTF_8.name())
        val markerIdx = text.lastIndexOf(EXIT_MARKER)
        if (markerIdx >= 0) {
            val code = text.substring(markerIdx + EXIT_MARKER.length)
                .lineSequence().firstOrNull()?.trim()?.toIntOrNull()
            if (code != null) {
                exitCode = code
                return ShellResult(exitCode, text.substring(0, markerIdx).trimEnd())
            }
        }
        return ShellResult(exitCode, text)
    }

    @Synchronized
    fun close() {
        runCatching { socket?.close() }
        socket = null
        input = null
        output = null
        systemBanner = ""
    }

    private fun send(msg: AdbMessage) {
        AdbProtocol.writeMessage(output ?: throw IOException("adb: closed"), msg)
    }

    private fun read(): AdbMessage =
        AdbProtocol.readMessage(input ?: throw EOFException("adb: closed"))

    private companion object {
        const val EXIT_MARKER = "__DKX_"
        const val BANNER = "host::features=cmd,shell_v2,stat_v2"
    }
}
