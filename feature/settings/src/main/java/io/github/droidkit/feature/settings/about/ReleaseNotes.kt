package io.github.droidkit.feature.settings.about

/**
 * Local release-notes model rendered by the What's New screen.
 * Content is transcribed from the authoritative CHANGELOG.md — update this
 * file alongside the changelog at release time. [dateLabel] mirrors the
 * changelog heading ("Unreleased" until the release ships).
 */
data class ReleaseNote(
    val version: String,
    val dateLabel: String,
    val highlights: List<String>,
)

object ReleaseNotes {
    val releases: List<ReleaseNote> = listOf(
        ReleaseNote(
            version = "0.9.0-alpha.1",
            dateLabel = "Unreleased",
            highlights = listOf(
                "Insights: on-device analytics dashboard with verdict ring, pass-rate trends and activity counters",
                "Elevated access groundwork: Shizuku, wireless ADB and root providers behind a shared ElevatedOps layer",
                "AppOps viewer, package controls and the ADB Lab for pairing and structured actions",
                "DroidKit Intelligence: optional Groq-powered assistant with your own key, personas, chat and confirmed proposals",
                "Technician workflow: session intake labels, technician signature on reports and before/after session compare",
                "Storage snapshots, NFC scan history, NDEF write presets and sensor session recording",
                "USB class inspectors, network interface inventory with mDNS discovery and a sensor orientation helper",
                "One-time onboarding intro covering honest results, optional capabilities and local-first privacy",
            ),
        ),
        ReleaseNote(
            version = "0.8.0",
            dateLabel = "Unreleased",
            highlights = listOf(
                "NFC Lab: tag scanning, NDEF reading and honest tag verdicts",
                "NDEF Writer: text/URI record builder with confirmed writes",
                "Network Lab: connection inspector, ping probe, DNS lookup and per-layer diagnostics",
                "Bluetooth Lab: adapter state, feature detection and bonded-device list",
            ),
        ),
        ReleaseNote(
            version = "0.7.0",
            dateLabel = "Unreleased",
            highlights = listOf(
                "USB Lab: host-mode capability card and live attach/detach device list",
                "Per-device detail with VID/PID, USB version and class names",
                "Interface/endpoint tree with transfer types and power flags",
                "USB permission flow with honest handling when descriptors are withheld",
            ),
        ),
        ReleaseNote(
            version = "0.6.0",
            dateLabel = "Unreleased",
            highlights = listOf(
                "App Inspector: installed apps with search and user/system filters",
                "App detail: version, SDK levels, install times, installer and actions",
                "Permission Inspector grouped by granted and not granted",
                "Component Explorer for activities, services, receivers and providers",
            ),
        ),
        ReleaseNote(
            version = "0.5.0",
            dateLabel = "Unreleased",
            highlights = listOf(
                "Report detail screen with device snapshot and grouped results",
                "Canonical text report builder with honest empty/unsupported wording",
                "Report export as text or PDF through the share sheet",
                "Report history with filters and confirmed deletes",
            ),
        ),
        ReleaseNote(
            version = "0.4.0",
            dateLabel = "Unreleased",
            highlights = listOf(
                "Phone Doctor hardware set: speaker, microphone, cameras, torch and connectivity checks",
                "Runtime permission gates that report PERMISSION_REQUIRED instead of failing",
                "Session review with state-count summary and per-category grouping",
            ),
        ),
        ReleaseNote(
            version = "0.3.0",
            dateLabel = "Unreleased",
            highlights = listOf(
                "Phone Doctor: guided diagnostic sessions with retry and notes",
                "Display color-cycle, dead-pixel and touch coverage tests",
                "Sensor tests with live readings; absent hardware reports NOT_SUPPORTED",
            ),
        ),
        ReleaseNote(
            version = "0.2.0",
            dateLabel = "Unreleased",
            highlights = listOf(
                "Device Inspector rebuild: sectioned dashboard with per-field copy and share",
                "Storage Lab volumes, Battery Lab readings and full sensor inventory",
                "Live sensor monitor for safe sensor types",
            ),
        ),
        ReleaseNote(
            version = "0.1.0",
            dateLabel = "Unreleased",
            highlights = listOf(
                "Multi-module project skeleton and Material3 design system",
                "ToolRegistry, CapabilityManager and permission handling foundations",
                "Home, Tools, Diagnose and Settings shells with Device Inspector",
            ),
        ),
    )
}
