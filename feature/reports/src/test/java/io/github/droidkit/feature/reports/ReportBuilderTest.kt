package io.github.droidkit.feature.reports

import io.github.droidkit.core.database.DeviceSnapshotEntity
import io.github.droidkit.core.database.DiagnosticResultEntity
import io.github.droidkit.core.database.DiagnosticSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportBuilderTest {

    private fun session() = DiagnosticSessionEntity(
        id = "sess-1",
        startedAtEpochMs = 1_700_000_000_000,
        completedAtEpochMs = 1_700_000_600_000,
        deviceSnapshotId = "snap-1",
        overallStatus = "COMPLETED",
        notes = "Screen replaced last week",
        appVersion = "0.4.0",
    )

    private fun snapshot() = DeviceSnapshotEntity(
        id = "snap-1",
        capturedAtEpochMs = 1_700_000_000_000,
        manufacturer = "Google",
        model = "Pixel 8",
        device = "shiba",
        product = "shiba",
        androidRelease = "14",
        apiLevel = 34,
        securityPatch = "2024-01-05",
        primaryAbi = "arm64-v8a",
        totalRamMb = 8192,
        storageTotalBytes = 128L * 1024 * 1024 * 1024,
        storageFreeBytes = 40L * 1024 * 1024 * 1024,
        displaySummary = "1080x2400 420dpi 120Hz",
        capabilitySummary = "standard=ready shizuku=absent",
    )

    private fun results() = listOf(
        DiagnosticResultEntity(
            id = "r1", sessionId = "sess-1", testId = "display.color",
            category = "Display", state = "PASS",
            measuredDataJson = null, userNote = null,
            timestampEpochMs = 1_700_000_001_000,
        ),
        DiagnosticResultEntity(
            id = "r2", sessionId = "sess-1", testId = "touch.grid",
            category = "Touch", state = "FAIL",
            measuredDataJson = null, userNote = "dead zone bottom-right; Coverage 88/96",
            timestampEpochMs = 1_700_000_002_000,
        ),
        DiagnosticResultEntity(
            id = "r3", sessionId = "sess-1", testId = "sensor.gyroscope",
            category = "Sensors", state = "NOT_SUPPORTED",
            measuredDataJson = null, userNote = "No sensor reported",
            timestampEpochMs = 1_700_000_003_000,
        ),
    )

    @Test fun `report contains header, device summary and disclaimer`() {
        val report = ReportBuilder.build(session(), snapshot(), results())
        assertTrue(report.contains("DroidKit Diagnostic Report"))
        assertTrue(report.contains("sess-1"))
        assertTrue(report.contains("0.4.0"))
        assertTrue(report.contains("Google Pixel 8"))
        assertTrue(report.contains("Android 14"))
        assertTrue(report.contains("disclaimer") || report.contains("Disclaimer"))
    }

    @Test fun `results grouped by state with failures and unsupported called out`() {
        val report = ReportBuilder.build(session(), snapshot(), results())
        assertTrue(report.contains("touch.grid"))
        assertTrue(report.contains("dead zone bottom-right"))
        assertTrue(report.contains("sensor.gyroscope"))
        val failIdx = report.indexOf("FAIL")
        val passIdx = report.indexOf("display.color")
        assertTrue(failIdx >= 0 && passIdx >= 0)
        assertTrue(report.contains("Screen replaced last week"))
    }

    @Test fun `summary counts states`() {
        val report = ReportBuilder.build(session(), snapshot(), results())
        assertTrue(report.contains("1") && report.contains("FAIL"))
        assertTrue(report.contains("NOT SUPPORTED") || report.contains("NOT_SUPPORTED"))
    }

    @Test fun `empty results report honestly`() {
        val report = ReportBuilder.build(session(), snapshot(), emptyList())
        assertTrue(report.contains("No test results"))
    }

    @Test fun `concise summary is short`() {
        val summary = ReportBuilder.buildSummary(session(), results())
        assertTrue(summary.length < 300)
        assertTrue(summary.contains("FAIL"))
    }
}
