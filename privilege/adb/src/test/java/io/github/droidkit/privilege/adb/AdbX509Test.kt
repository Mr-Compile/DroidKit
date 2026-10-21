package io.github.droidkit.privilege.adb

import java.io.ByteArrayInputStream
import java.security.KeyPairGenerator
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdbX509Test {

    private val keyPair = KeyPairGenerator.getInstance("RSA").run {
        initialize(2048)
        generateKeyPair()
    }

    private fun cert(): X509Certificate {
        val der = AdbX509.selfSigned(
            publicKey = keyPair.public as RSAPublicKey,
            notBefore = Instant.EPOCH,
            notAfter = Instant.ofEpochSecond(2461449600L),
            privateKey = keyPair.private,
        )
        return CertificateFactory.getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(der)) as X509Certificate
    }

    @Test
    fun `cert parses as valid X509`() {
        val c = cert()
        assertEquals(3, c.version)
        assertEquals("SHA256WITHRSA", c.sigAlgName.uppercase().replace("-", ""))
        assertEquals("CN=00", c.subjectX500Principal.name)
        assertEquals("CN=00", c.issuerX500Principal.name)
    }

    @Test
    fun `cert carries the RSA key and verifies`() {
        val c = cert()
        assertEquals(
            (keyPair.public as RSAPublicKey).modulus,
            (c.publicKey as RSAPublicKey).modulus,
        )
        c.verify(keyPair.public) // throws if the self-signature is wrong
    }

    @Test
    fun `cert validity covers the asked window`() {
        val c = cert()
        assertTrue(c.notBefore.time == 0L)
        assertTrue(c.notAfter.time == 2461449600L * 1000)
    }
}
