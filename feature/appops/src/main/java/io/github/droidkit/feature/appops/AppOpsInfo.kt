package io.github.droidkit.feature.appops

/** Pure AppOps presentation helpers — JVM-testable. */
object AppOpsInfo {

    /**
     * Plain-language description for common ops. Unknown ops get an honest
     * fallback — Android adds ops every release and only AOSP documents all.
     */
    fun describe(op: String): String = when (op) {
        "CAMERA" -> "Access the camera"
        "RECORD_AUDIO" -> "Record microphone audio"
        "COARSE_LOCATION" -> "Approximate (network) location"
        "FINE_LOCATION" -> "Precise (GPS) location"
        "READ_CONTACTS" -> "Read the contacts database"
        "WRITE_CONTACTS" -> "Modify the contacts database"
        "READ_CALL_LOG" -> "Read call history"
        "WRITE_CALL_LOG" -> "Modify call history"
        "READ_SMS" -> "Read SMS messages"
        "SEND_SMS" -> "Send SMS messages"
        "RECEIVE_SMS" -> "Receive SMS messages"
        "CALL_PHONE" -> "Place phone calls"
        "ANSWER_PHONE_CALLS" -> "Answer incoming calls"
        "READ_EXTERNAL_STORAGE" -> "Read shared storage"
        "WRITE_EXTERNAL_STORAGE" -> "Write shared storage"
        "MANAGE_EXTERNAL_STORAGE" -> "Broad all-files storage access"
        "SYSTEM_ALERT_WINDOW" -> "Draw overlays on other apps"
        "WRITE_SETTINGS" -> "Modify system settings"
        "POST_NOTIFICATION" -> "Show notifications"
        "RUN_IN_BACKGROUND" -> "Run background work"
        "RUN_ANY_IN_BACKGROUND" -> "Run any background work"
        "WAKE_LOCK" -> "Keep the device awake"
        "VIBRATE" -> "Run the vibrator motor"
        "READ_CLIPBOARD" -> "Read clipboard contents"
        "WRITE_CLIPBOARD" -> "Write clipboard contents"
        "ACCESS_ACCESSIBILITY" -> "Use accessibility services"
        "PICTURE_IN_PICTURE" -> "Float a PiP window"
        "REQUEST_INSTALL_PACKAGES" -> "Install unknown apps"
        "GET_USAGE_STATS" -> "Read app-usage statistics"
        "SCHEDULE_EXACT_ALARM" -> "Schedule exact wake-up alarms"
        "BLUETOOTH_CONNECT" -> "Talk to paired Bluetooth devices"
        "BLUETOOTH_SCAN" -> "Scan for Bluetooth devices"
        "BLUETOOTH_ADVERTISE" -> "BLE advertising"
        "BODY_SENSORS" -> "Heart-rate and other body sensors"
        "ACTIVITY_RECOGNITION" -> "Detect walking, driving, steps"
        "NEARBY_WIFI_DEVICES" -> "Find nearby Wi-Fi devices"
        "UWB_RANGING" -> "Ultra-wideband ranging"
        "READ_MEDIA_IMAGES" -> "Read images from shared storage"
        "READ_MEDIA_VIDEO" -> "Read videos from shared storage"
        "READ_MEDIA_AUDIO" -> "Read audio from shared storage"
        "LEGACY_STORAGE" -> "Legacy (unscoped) storage behavior"
        else -> "Undocumented op — Android adds new ops every release; this is the raw name."
    }

    /** Canonical display order + label for a mode string from `appops get`. */
    fun modeLabel(mode: String): String = when (mode) {
        "allow" -> "Allowed"
        "foreground" -> "Foreground only"
        "default" -> "Default"
        "deny" -> "Denied"
        "ignore" -> "Ignored (silently fails)"
        else -> mode.replaceFirstChar { it.uppercase() }
    }

    /** Count per mode, ordered by the canonical mode order above. */
    fun modeStats(ops: List<AppOpEntry>): List<Pair<String, Int>> {
        val counts = ops.groupingBy { it.mode }.eachCount()
        val order = listOf("allow", "foreground", "default", "deny", "ignore")
        val known = order.mapNotNull { m -> counts[m]?.let { m to it } }
        val other = counts.filterKeys { it !in order }.entries.sortedBy { it.key }
            .map { it.key to it.value }
        return known + other
    }

    /** Group ops by mode in canonical order — the "ops-by-mode" view. */
    fun groupByMode(ops: List<AppOpEntry>): List<Pair<String, List<AppOpEntry>>> =
        modeStats(ops).map { (mode, _) -> mode to ops.filter { it.mode == mode } }
}
