package io.github.droidkit.core.privileges

import kotlin.random.Random
import kotlinx.coroutines.flow.StateFlow

/** An ADB service endpoint resolved through mDNS/NSD. */
data class AdbEndpoint(
    val host: String,
    val port: Int,
    val serviceName: String,
)

/**
 * Payload of an Android wireless-debugging pairing QR code —
 * `WIFI:T:ADB;S:<service-name>;P:<secret>;;` (the Android Studio format).
 *
 * Two directions share the same shape:
 *  - We display the QR: another device's dev-settings scanner reads it and
 *    starts a pairing server advertised as [serviceName]; we pair with [secret].
 *  - A device displays the QR: we decode it (e.g. from a screenshot), discover
 *    the advertised pairing service, and pair with [secret].
 *
 * The secret is a transient pairing password — never persist or log it.
 */
data class AdbQrPayload(
    val serviceName: String,
    val secret: String,
) {
    fun encode(): String = "WIFI:T:ADB;S:$serviceName;P:$secret;;"

    companion object {
        private val PAYLOAD_RE = Regex("^WIFI:T:ADB;S:([^;]+);P:([^;]+);;?$")
        private const val NAME_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789"
        private const val SECRET_ALPHABET =
            "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789"

        fun generate(
            prefix: String = "droidkit",
            random: Random = Random,
        ): AdbQrPayload = AdbQrPayload(
            serviceName = "$prefix-" + randomName(random, 6),
            secret = randomName(random, 10, SECRET_ALPHABET),
        )

        private fun randomName(random: Random, len: Int, alphabet: String = NAME_ALPHABET): String =
            buildString(len) { repeat(len) { append(alphabet[random.nextInt(alphabet.length)]) } }

        fun parse(qrText: String): AdbQrPayload? =
            PAYLOAD_RE.matchEntire(qrText.trim())?.let { m ->
                AdbQrPayload(
                    serviceName = m.groupValues[1],
                    secret = m.groupValues[2],
                )
            }
    }
}

/** Stages of the guided (mDNS-assisted) wireless-ADB pairing flow. */
sealed interface AdbPairingState {
    /** Nothing running. */
    data object Idle : AdbPairingState

    /** Discovering the pairing service (`_adb-tls-pairing._tcp`). */
    data object SearchingPairing : AdbPairingState

    /** A pairing endpoint was resolved — the user can enter the code. */
    data class EndpointFound(val endpoint: AdbEndpoint) : AdbPairingState

    /** Handshake in flight. */
    data object Pairing : AdbPairingState

    /** Pairing handshake succeeded — device trusts our ADB key now. */
    data class Paired(val summary: String) : AdbPairingState

    /** Discovering the connect service (`_adb-tls-connect._tcp`). */
    data object SearchingConnect : AdbPairingState

    /** Opening the ADB session. */
    data object Connecting : AdbPairingState

    /** Session live. */
    data class Connected(val summary: String) : AdbPairingState

    /** Something failed — [message] is user-safe (no secrets). */
    data class Failed(val message: String) : AdbPairingState
}
