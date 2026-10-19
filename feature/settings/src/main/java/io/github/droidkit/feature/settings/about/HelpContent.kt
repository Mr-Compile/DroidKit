package io.github.droidkit.feature.settings.about

/**
 * Local Help & FAQ content. Answers only describe functionality that exists
 * in the current build — extend entries as features ship.
 */
data class FaqEntry(val question: String, val answer: String)
data class FaqCategory(val title: String, val entries: List<FaqEntry>)

object HelpContent {
    val categories: List<FaqCategory> = listOf(
        FaqCategory(
            title = "Getting started",
            entries = listOf(
                FaqEntry(
                    "What is DroidKit?",
                    "DroidKit brings useful Android tools into one organized experience. " +
                        "It is designed to make common device utilities easier to discover " +
                        "and use without unnecessary complexity.",
                ),
                FaqEntry(
                    "Where do I start?",
                    "The Home tab summarizes your device and pinned tools. The Tools tab " +
                        "lists every lab, Diagnose runs guided Phone Doctor sessions, and " +
                        "Reports keeps your saved results.",
                ),
            ),
        ),
        FaqCategory(
            title = "Using DroidKit tools",
            entries = listOf(
                FaqEntry(
                    "How do diagnostics work?",
                    "Open Diagnose and run a guided session or a single test. You judge " +
                        "the result of human-observation tests (screen, speaker, vibration); " +
                        "automatic checks report their own readings. Missing hardware shows " +
                        "NOT_SUPPORTED rather than a failure.",
                ),
                FaqEntry(
                    "Where do saved results go?",
                    "Sessions and reports are stored on-device and listed under Reports. " +
                        "You can export a report as text or PDF, compare two sessions, or " +
                        "delete them individually.",
                ),
            ),
        ),
        FaqCategory(
            title = "Permissions",
            entries = listOf(
                FaqEntry(
                    "Why does a tool ask for a permission?",
                    "Some tools need Android runtime permissions — for example the camera " +
                        "test needs CAMERA and the microphone test needs RECORD_AUDIO. " +
                        "Denying a permission shows PERMISSION_REQUIRED; the rest of the " +
                        "app keeps working.",
                ),
                FaqEntry(
                    "What is Advanced access?",
                    "Settings lists optional elevated providers (Shizuku, wireless ADB, " +
                        "root) for technician actions like AppOps changes. They are " +
                        "opt-in and off by default — every standard tool works without them.",
                ),
            ),
        ),
        FaqCategory(
            title = "Troubleshooting",
            entries = listOf(
                FaqEntry(
                    "A field says \"Not exposed\" — is something broken?",
                    "No. Android hides some values on certain devices or versions. " +
                        "DroidKit reports what the system actually provides instead of " +
                        "guessing.",
                ),
                FaqEntry(
                    "An elevated action says a provider is needed.",
                    "Open Settings → Advanced access, check the provider status and run " +
                        "its grant flow, or pair a wireless ADB session from the ADB Lab.",
                ),
            ),
        ),
        FaqCategory(
            title = "Privacy",
            entries = listOf(
                FaqEntry(
                    "What leaves my device?",
                    "Nothing by default — sessions, reports and preferences stay in local " +
                        "storage. Optional DroidKit Intelligence features send a minimized " +
                        "context to Groq only when you enable AI and add your own key.",
                ),
            ),
        ),
        FaqCategory(
            title = "Updates",
            entries = listOf(
                FaqEntry(
                    "How are updates delivered?",
                    "DroidKit is distributed through GitHub Releases. Use Check for updates " +
                        "in About DroidKit to see whether a newer release exists.",
                ),
            ),
        ),
    )
}
