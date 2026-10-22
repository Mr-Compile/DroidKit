package io.github.droidkit.privilege.adb

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Principal
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Signature
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Instant
import java.util.Base64
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.X509ExtendedKeyManager
import javax.net.ssl.X509TrustManager

/**
 * DroidKit's ADB identity: an RSA-2048 keypair kept in AndroidKeyStore so the
 * private key never leaves the TEE. The public half is encoded in the ADB
 * `RSAPublicKey` wire format (the same layout `~/.android/adbkey.pub` uses).
 *
 * The same key doubles as the TLS client certificate on the
 * `_adb-tls-connect` port — adbd matches the certificate's public key against
 * the paired `adb_keys` store. That requires the key to support the digests
 * and paddings TLS 1.3 CertificateVerify can request (RSA-PSS / SHA-2), on
 * top of the classic SHA1+PKCS1 token signature.
 */
class AdbKeys(
    private val deviceName: String = "droidkit@device",
) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    /** Private key from the generator — some OEM keystores never surface the
     * entry via getEntry() (Spreadtrum KeyMint), so keep the generated handle. */
    @Volatile
    private var generatedPrivateKey: PrivateKey? = null

    private val publicKey: RSAPublicKey by lazy { ensureKey() }

    /** Self-signed TLS client cert carrying [publicKey] (CN=00, v3, no extensions). */
    private val certificate: X509Certificate by lazy {
        java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCertificate(
                AdbX509.selfSigned(
                    publicKey = publicKey,
                    notBefore = Instant.EPOCH,
                    notAfter = Instant.ofEpochSecond(2461449600L), // 2048-01-01
                    privateKey = privateKey(),
                ).inputStream(),
            ) as X509Certificate
    }

    private fun ensureKey(): RSAPublicKey {
        // The v1 key was generated with SHA1+PKCS1 only — too narrow for TLS
        // client auth. Rotate to the wider spec under a new alias.
        runCatching { keyStore.deleteEntry(LEGACY_KEY_ALIAS) }
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
                .setDigests(
                    KeyProperties.DIGEST_SHA1,
                    KeyProperties.DIGEST_SHA256,
                    KeyProperties.DIGEST_SHA384,
                    KeyProperties.DIGEST_SHA512,
                )
                .setSignaturePaddings(
                    KeyProperties.SIGNATURE_PADDING_RSA_PKCS1,
                    KeyProperties.SIGNATURE_PADDING_RSA_PSS,
                )
                .build()
        )
        val pair = gen.generateKeyPair()
        generatedPrivateKey = pair.private
        return pair.public as RSAPublicKey
    }

    private fun privateKey(): PrivateKey {
        publicKey // force key generation before entry lookup
        return (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry)?.privateKey
            ?: generatedPrivateKey
            ?: throw IOException("ADB identity key unavailable")
    }

    /** Sign an ADB AUTH token — RSA PKCS1 v1.5 over SHA-1 of the token. */
    fun signToken(token: ByteArray): ByteArray =
        Signature.getInstance("SHA1withRSA").run {
            initSign(privateKey())
            update(token)
            sign()
        }

    /**
     * Probes which signature schemes the ADB key can actually run on this
     * device — TLS 1.3 CertificateVerify needs RSA-PSS, which some OEM
     * keystores declare but can't perform. Diagnostic only.
     */
    fun logTlsSelfTest() {
        for (alg in listOf("SHA256withRSA/PSS", "SHA256withRSA", "SHA1withRSA")) {
            val ok = runCatching {
                Signature.getInstance(alg).run {
                    initSign(privateKey())
                    update(byteArrayOf(1, 2, 3))
                    sign()
                }
            }.isSuccess
            android.util.Log.i("DroidKitAdb", "self-test sign $alg: ${if (ok) "OK" else "FAIL"}")
        }
        runCatching {
            val k = privateKey()
            android.util.Log.i(
                "DroidKitAdb",
                "key class=${k.javaClass.name} encoded=${k.encoded?.size ?: -1}",
            )
        }
    }

    /**
     * The public key in ADB wire format: base64 of the little-endian
     * RSAPublicKey struct followed by " user@host", NUL-terminated.
     */
    fun publicKeyPayload(): ByteArray =
        ("${base64PublicKey()} $deviceName").toByteArray(Charsets.UTF_8) + byteArrayOf(0)

    /** Raw `base64(pubkey) name` string — also sent as pairing PeerInfo. */
    fun publicKeyString(): String = "${base64PublicKey()} $deviceName"

    /**
     * TLS context for both the pairing port and the connect port. Trust-all
     * server side (adbd presents a self-signed cert; authenticity comes from
     * SPAKE2/the paired key, same model as `adb pair`/`adb connect`), plus a
     * key manager presenting our ADB key as a client certificate — the
     * `_adb-tls-connect` port authenticates clients by certificate pubkey.
     */
    fun sslContext(): SSLContext {
        val ctx = SSLContext.getInstance("TLS")
        ctx.init(arrayOf(clientKeyManager()), arrayOf(TrustAll), SecureRandom())
        return ctx
    }

    /**
     * TLS context for the *pairing* port only — no client certificate. The
     * pairing server authenticates via SPAKE2 (the code), not by client cert;
     * presenting our keystore key here only gives a broken OEM keystore a
     * chance to fail the CertificateVerify RSA-PSS sign for no benefit.
     */
    fun pairingSslContext(): SSLContext {
        val ctx = SSLContext.getInstance("TLS")
        ctx.init(null, arrayOf(TrustAll), SecureRandom())
        return ctx
    }

    private fun clientKeyManager(): X509ExtendedKeyManager = object : X509ExtendedKeyManager() {
        private val alias = "droidkit_adb"

        override fun chooseClientAlias(
            keyTypes: Array<out String>,
            issuers: Array<out Principal>?,
            socket: java.net.Socket?,
        ): String? = if ("RSA" in keyTypes) alias else null

        override fun getCertificateChain(alias: String?): Array<X509Certificate>? =
            if (alias == this.alias) arrayOf(certificate) else null

        override fun getPrivateKey(alias: String?): PrivateKey? =
            if (alias == this.alias) privateKey() else null

        override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? = null

        override fun getServerAliases(keyType: String, issuers: Array<out Principal>?): Array<String>? = null

        override fun chooseServerAlias(keyType: String, issuers: Array<out Principal>?, socket: java.net.Socket?): String? = null
    }

    /**
     * RFC 5705 TLS exported keying material — ADB pairing binds the SPAKE2
     * password to the TLS session with `SSL_export_keying_material(ssl, 64,
     * "adb-label\0", no context)`. javax.net.ssl has no equivalent API; reach
     * Conscrypt's. The label includes the trailing NUL to match AOSP's
     * `sizeof(kExportedKeyLabel)` (10 bytes, not 9).
     */
    fun exportedKeyMaterial(socket: SSLSocket, length: Int = EXPORTED_KEY_SIZE): ByteArray {
        // Unlock hidden-API reflection — exportKeyingMaterial is a non-SDK
        // Conscrypt member on some devices. No-op where exempt/unsupported.
        runCatching {
            val rt = Class.forName("dalvik.system.VMRuntime")
            val runtime = rt.getDeclaredMethod("getRuntime").apply { isAccessible = true }
                .invoke(null)
            rt.getDeclaredMethod("setHiddenApiExemptions", Array<String>::class.java)
                .apply { isAccessible = true }
                .invoke(runtime, arrayOf("L"))
        }

        val sig = arrayOf(
            String::class.java, ByteArray::class.java, Int::class.javaPrimitiveType,
        )

        // 1. The Conscrypt socket itself — any visibility, any superclass.
        var cls: Class<*>? = socket.javaClass
        while (cls != null) {
            for (m in cls.declaredMethods) {
                if (m.name == "exportKeyingMaterial" &&
                    m.parameterTypes.contentEquals(sig)
                ) {
                    runCatching { m.isAccessible = true }
                    return m.invoke(socket, EXPORTED_KEY_LABEL, null, length) as ByteArray
                }
            }
            cls = cls.superclass
        }

        // 2. Static Conscrypt helper — platform namespace, then upstream.
        for (name in listOf("com.android.org.conscrypt.Conscrypt", "org.conscrypt.Conscrypt")) {
            runCatching {
                val k = Class.forName(name)
                for (m in k.declaredMethods) {
                    if (m.name == "exportKeyingMaterial" &&
                        m.parameterTypes.size == 4 &&
                        m.parameterTypes[0].isAssignableFrom(SSLSocket::class.java)
                    ) {
                        runCatching { m.isAccessible = true }
                        return m.invoke(null, socket, EXPORTED_KEY_LABEL, null, length) as ByteArray
                    }
                }
            }
        }

        // Diagnostics: log candidates so the failure shows what the device has.
        runCatching {
            val names = socket.javaClass.methods.map { it.name }.distinct()
                .filter { it.contains("xport", true) || it.contains("eying", true) }
            android.util.Log.w("DroidKitAdb", "EKM missing; socket=${socket.javaClass.name} candidates=$names")
        }
        throw IOException("TLS exported keying material unavailable on this device")
    }

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

    private object TrustAll : X509TrustManager {
        override fun checkClientTrusted(certs: Array<X509Certificate>?, auth: String?) {}
        override fun checkServerTrusted(certs: Array<X509Certificate>?, auth: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private companion object {
        const val KEY_ALIAS = "droidkit_adb_identity_v2"
        const val LEGACY_KEY_ALIAS = "droidkit_adb_identity"
        val EXPORTED_KEY_LABEL = "adb-label" + Char(0) // trailing NUL - AOSP passes sizeof(label)
        const val EXPORTED_KEY_SIZE = 64
        const val KEY_SIZE_WORDS = 64 // 2048-bit modulus / 32
        val UINT32: BigInteger = BigInteger.ONE.shiftLeft(32)
        val UINT32_MINUS1: BigInteger = UINT32.subtract(BigInteger.ONE)
        val R2: BigInteger = BigInteger.ONE.shiftLeft(4096) // (2^2048)^2 for Montgomery RR
    }
}
