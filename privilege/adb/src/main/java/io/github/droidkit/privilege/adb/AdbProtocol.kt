package io.github.droidkit.privilege.adb

import java.io.DataInputStream
import java.io.IOException
import java.io.OutputStream

/** ADB wire protocol framing — 24-byte little-endian header + payload. */
object AdbProtocol {
    const val A_CNXN = 0x4e584e43
    const val A_AUTH = 0x48545541
    const val A_OPEN = 0x4e45504f
    const val A_OKAY = 0x59414b4f
    const val A_WRTE = 0x45545257
    const val A_CLSE = 0x45534c43
    const val A_STLS = 0x534c5453

    const val A_VERSION = 0x01000001
    const val MAX_PAYLOAD = 0x00100000
    const val STLS_VERSION = 1

    const val AUTH_TOKEN = 1
    const val AUTH_SIGNATURE = 2
    const val AUTH_RSAPUBLICKEY = 3

    data class AdbMessage(
        val command: Int,
        val arg0: Int,
        val arg1: Int,
        val payload: ByteArray,
    ) {
        override fun equals(other: Any?): Boolean =
            other is AdbMessage && command == other.command && arg0 == other.arg0 &&
                arg1 == other.arg1 && payload.contentEquals(other.payload)

        override fun hashCode(): Int =
            31 * (31 * (31 * command + arg0) + arg1) + payload.contentHashCode()
    }

    fun writeMessage(out: OutputStream, msg: AdbMessage) {
        val header = ByteArray(24)
        putInt(header, 0, msg.command)
        putInt(header, 4, msg.arg0)
        putInt(header, 8, msg.arg1)
        putInt(header, 12, msg.payload.size)
        putInt(header, 16, payloadChecksum(msg.payload))
        putInt(header, 20, msg.command xor -0x1)
        out.write(header)
        out.write(msg.payload)
        out.flush()
    }

    fun readMessage(input: DataInputStream): AdbMessage {
        val header = ByteArray(24)
        input.readFully(header)
        val command = getInt(header, 0)
        val arg0 = getInt(header, 4)
        val arg1 = getInt(header, 8)
        val length = getInt(header, 12)
        val checksum = getInt(header, 16)
        val magic = getInt(header, 20)
        if (magic != command xor -0x1) {
            throw IOException("adb: bad magic for command 0x${command.toString(16)}")
        }
        if (length < 0 || length > MAX_PAYLOAD * 4) {
            throw IOException("adb: invalid payload length $length")
        }
        val payload = ByteArray(length)
        input.readFully(payload)
        if (payloadChecksum(payload) != checksum) {
            throw IOException("adb: payload checksum mismatch")
        }
        return AdbMessage(command, arg0, arg1, payload)
    }

    fun payloadChecksum(data: ByteArray): Int {
        var sum = 0L
        for (b in data) sum += b.toLong() and 0xff
        return (sum and 0xffffffffL).toInt()
    }

    private fun putInt(buf: ByteArray, off: Int, v: Int) {
        buf[off] = (v and 0xff).toByte()
        buf[off + 1] = (v shr 8 and 0xff).toByte()
        buf[off + 2] = (v shr 16 and 0xff).toByte()
        buf[off + 3] = (v shr 24 and 0xff).toByte()
    }

    private fun getInt(buf: ByteArray, off: Int): Int =
        (buf[off].toInt() and 0xff) or
            (buf[off + 1].toInt() and 0xff shl 8) or
            (buf[off + 2].toInt() and 0xff shl 16) or
            (buf[off + 3].toInt() and 0xff shl 24)
}
