package io.github.droidkit.feature.reports

import io.github.droidkit.core.database.DeviceSnapshotEntity
import io.github.droidkit.core.database.DiagnosticResultEntity
import io.github.droidkit.core.database.DiagnosticSessionEntity
import java.text.DateFormat
import java.util.Date

/**
 * Builds the canonical text report. Pure Kotlin — no Android deps — so the
 * structure is unit-tested and the PDF exporter renders the same lines.
 */
object ReportBuilder {

    private const val DISCLAIMER =
        "This report was generated locally by DroidKit. Manual tests reflect the " +
            "technician's judgment at test time; hardware reported as NOT SUPPORTED " +
            "was absent or unexposed by Android APIs, not failed."

    fun build(
        session: DiagnosticSessionEntity,
        snapshot: DeviceSnapshotEntity?,
        results: List<DiagnosticResultEntity>,
    ): String = buildString {
        val df = DateFormat.getDateTimeInstance()
        appendLine("DroidKit Diagnostic Report")
        appendLine("==========================")
        appendLine()
        appendLine("Report ID: ${session.id}")
        appendLine("Date: ${df.format(Date(session.startedAtEpochMs))}")
        session.completedAtEpochMs?.let { appendLine("Completed: ${df.format(Date(it))}") }
        appendLine("App version: ${session.appVersion}")
        appendLine("Preset: ${session.preset.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}")
        session.customerLabel?.takeIf { it.isNotBlank() }?.let { appendLine("Customer: $it") }
        session.deviceLabel?.takeIf { it.isNotBlank() }?.let { appendLine("Device label: $it") }
        session.technicianName?.takeIf { it.isNotBlank() }?.let { appendLine("Technician: $it") }
        appendLine()

        appendLine("Device summary")
        appendLine("--------------")
        if (snapshot == null) {
            appendLine("No device snapshot captured.")
        } else {
            appendLine("Device: ${snapshot.manufacturer} ${snapshot.model}")
            appendLine("Android ${snapshot.androidRelease} (API ${snapshot.apiLevel})" +
                (snapshot.securityPatch?.let { ", patch $it" } ?: ""))
            appendLine("ABI: ${snapshot.primaryAbi}")
            appendLine("RAM: ${snapshot.totalRamMb} MB")
            appendLine(
                "Storage: ${snapshot.storageFreeBytes / (1024 * 1024)} MB free of " +
                    "${snapshot.storageTotalBytes / (1024 * 1024)} MB",
            )
            appendLine("Display: ${snapshot.displaySummary}")
            appendLine("Providers: ${snapshot.capabilitySummary}")
        }
        appendLine()

        appendLine("Overall status: ${session.overallStatus}")
        appendLine()

        appendLine("Results")
        appendLine("-------")
        if (results.isEmpty()) {
            appendLine("No test results recorded.")
        } else {
            val counts = results.groupingBy { it.state }.eachCount()
            appendLine(
                counts.entries.joinToString(" · ") { "${it.key.replace('_', ' ')}: ${it.value}" },
            )
            appendLine()
            val grouped = results.groupBy { it.category }
            grouped.forEach { (category, rows) ->
                appendLine("[$category]")
                rows.forEach { r ->
                    appendLine("  ${r.testId}: ${r.state.replace('_', ' ')}")
                    r.userNote?.takeIf { n -> n.isNotBlank() }?.let { appendLine("    note: $it") }
                }
                appendLine()
            }
            val failures = results.filter { it.state == "FAIL" }
            if (failures.isNotEmpty()) {
                appendLine("Failures")
                appendLine("--------")
                failures.forEach { appendLine("  ${it.testId}") }
                appendLine()
            }
            val skipped = results.filter {
                it.state == "NOT_SUPPORTED" || it.state == "NOT_TESTED" ||
                    it.state == "PERMISSION_REQUIRED"
            }
            if (skipped.isNotEmpty()) {
                appendLine("Unsupported / skipped")
                appendLine("---------------------")
                skipped.forEach { appendLine("  ${it.testId}: ${it.state.replace('_', ' ')}") }
                appendLine()
            }
        }

        session.notes?.takeIf { it.isNotBlank() }?.let {
            appendLine("Technician notes")
            appendLine("----------------")
            appendLine(it)
            appendLine()
        }

        appendLine("Disclaimer")
        appendLine("----------")
        appendLine(DISCLAIMER)
    }

    /**
     * Formal inspection certificate — the technician-facing document for
     * used-phone sales and repair handover. Presents the same data as the
     * diagnostic report in a certificate layout; still honest about limits.
     */
    fun buildCertificate(
        session: DiagnosticSessionEntity,
        snapshot: DeviceSnapshotEntity?,
        results: List<DiagnosticResultEntity>,
    ): String = buildString {
        val df = DateFormat.getDateTimeInstance()
        appendLine("DroidKit Device Inspection Certificate")
        appendLine("======================================")
        appendLine()
        appendLine("Certificate ID: ${session.id}")
        appendLine("Issued: ${df.format(Date(session.startedAtEpochMs))}")
        session.technicianName?.takeIf { it.isNotBlank() }?.let { appendLine("Technician: $it") }
        session.customerLabel?.takeIf { it.isNotBlank() }?.let { appendLine("Customer / ticket: $it") }
        session.deviceLabel?.takeIf { it.isNotBlank() }?.let { appendLine("Device label: $it") }
        appendLine("Inspection preset: ${session.preset.replace('_', ' ')}")
        appendLine()

        appendLine("Device")
        appendLine("------")
        if (snapshot == null) {
            appendLine("No device snapshot captured.")
        } else {
            appendLine("${snapshot.manufacturer} ${snapshot.model} · " +
                "Android ${snapshot.androidRelease} (API ${snapshot.apiLevel})" +
                (snapshot.securityPatch?.let { ", patch $it" } ?: ""))
            appendLine("RAM ${snapshot.totalRamMb} MB · " +
                "Storage ${snapshot.storageFreeBytes / (1024 * 1024)} MB free of " +
                "${snapshot.storageTotalBytes / (1024 * 1024)} MB")
        }
        appendLine()

        appendLine("Inspection checklist")
        appendLine("--------------------")
        if (results.isEmpty()) {
            appendLine("No inspection results recorded.")
        } else {
            val counts = results.groupingBy { it.state }.eachCount()
            appendLine(
                "Tally: " + counts.entries.joinToString(" · ") {
                    "${it.key.replace('_', ' ')}: ${it.value}"
                },
            )
            appendLine()
            results.groupBy { it.category }.forEach { (category, rows) ->
                appendLine("[$category]")
                rows.forEach { r ->
                    appendLine("  ${r.testId}: ${r.state.replace('_', ' ')}")
                }
            }
        }
        appendLine()

        session.notes?.takeIf { it.isNotBlank() }?.let {
            appendLine("Technician notes")
            appendLine("----------------")
            appendLine(it)
            appendLine()
        }

        appendLine("Signature")
        appendLine("---------")
        appendLine(session.technicianName?.takeIf { it.isNotBlank() } ?: "(unsigned)")
        appendLine()
        appendLine("Certificate notice")
        appendLine("------------------")
        appendLine(
            "This certificate records the observable results of a DroidKit " +
                "inspection at the stated date. It is not a guarantee of device " +
                "condition: manual checks reflect the technician's judgment, and " +
                "NOT SUPPORTED entries mean the hardware or value was not exposed " +
                "to Android — not that it failed.",
        )
    }

    /** One-line summary for clipboard/share previews. */
    fun buildSummary(
        session: DiagnosticSessionEntity,
        results: List<DiagnosticResultEntity>,
    ): String {
        val counts = results.groupingBy { it.state }.eachCount()
        val tally = counts.entries.joinToString(" ") { "${it.key.replace('_', ' ')} ${it.value}" }
        return "DroidKit report ${session.id.take(8)} — ${session.overallStatus} — " +
            "${results.size} tests ($tally)"
    }
}
