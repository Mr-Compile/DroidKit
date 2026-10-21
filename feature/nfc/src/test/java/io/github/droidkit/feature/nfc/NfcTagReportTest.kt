package io.github.droidkit.feature.nfc

import io.github.droidkit.feature.nfc.NfcViewModel.DoctorCheck
import io.github.droidkit.feature.nfc.NfcViewModel.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcTagReportTest {

    private fun tagInfo(
        records: List<NdefRecordInfo> = emptyList(),
        ndefCapable: Boolean = true,
        writable: Boolean? = true,
    ) = NfcTagInfo(
        idHex = "04 A3 22 9A 4B 61 80",
        techList = listOf("Ndef", "NfcA"),
        ndefCapable = ndefCapable,
        ndefWritable = writable,
        ndefMaxSize = 144,
        ndefType = "NFC Forum Type 2",
        canMakeReadOnly = false,
        formatable = false,
        records = records,
        parseError = null,
        rawMessage = null,
    )

    @Test
    fun `report contains identity tech and record lines`() {
        val info = tagInfo(
            records = listOf(
                NdefRecordInfo(
                    tnfName = "Well-known",
                    typeHex = "55",
                    payloadHex = "04 64 72",
                    decoded = "https://droidkit.dev",
                    decodedKind = "uri",
                ),
            ),
        )
        val checks = listOf(DoctorCheck("Readable", Verdict.PASS, "ok"))
        val sections = listOf(
            TechSection("NfcA", listOf("SAK" to "0x00"), emptyList()),
        )
        val report = buildTagReport(info, checks, sections)
        assertTrue(report.contains("Tag ID: 04 A3 22 9A 4B 61 80"))
        assertTrue(report.contains("Technologies: Ndef, NfcA"))
        assertTrue(report.contains("Capacity: 144 bytes"))
        assertTrue(report.contains("NfcA"))
        assertTrue(report.contains("SAK: 0x00"))
        assertTrue(report.contains("#1 Well-known 55 — https://droidkit.dev"))
        assertTrue(report.contains("Readable=PASS"))
    }

    @Test
    fun `report handles missing values without placeholders for nulls`() {
        val info = tagInfo().copy(
            idHex = null, ndefMaxSize = null, ndefType = null,
            ndefWritable = null,
        )
        val report = buildTagReport(info, emptyList(), emptyList())
        assertTrue(report.contains("Tag ID: —"))
        assertTrue(report.contains("Writable: unknown"))
        assertTrue(!report.contains("Capacity:"))
        assertTrue(!report.contains("NDEF type:"))
        assertTrue(report.endsWith("Tag Doctor: \n"))
    }

    @Test
    fun `report never includes raw payloads or secrets`() {
        val info = tagInfo(
            records = listOf(
                NdefRecordInfo(
                    tnfName = "MIME media",
                    typeHex = "61 70",
                    payloadHex = "DE AD BE EF",
                    decoded = "Wi-Fi \"Home\" (WPA2) — key stored",
                    decodedKind = "wifi",
                ),
            ),
        )
        val report = buildTagReport(info, emptyList(), emptyList())
        // decoded summary is fine; payload hex must not leak
        assertTrue(report.contains("Wi-Fi \"Home\""))
        assertTrue(!report.contains("DE AD BE EF"))
    }
}
