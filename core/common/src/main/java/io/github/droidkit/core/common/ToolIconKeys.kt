package io.github.droidkit.core.common

/**
 * Canonical icon vocabulary for tools and searchable destinations (UI v6).
 * Keys are semantic, stable identifiers — `core:ui` maps each key to a
 * Material vector in `ToolIcons`. A key added here must have a mapping
 * there or validation fails.
 */
object ToolIconKeys {
    const val DEVICE = "device"
    const val INSPECTOR = "inspector"
    const val STORAGE = "storage"
    const val PALETTE = "palette"
    const val BATTERY = "battery"
    const val CHARGING = "charging"
    const val DEAD_PIXEL = "deadPixel"
    const val TOUCH = "touch"
    const val DISPLAY_MODES = "displayModes"
    const val MOTION = "motion"
    const val SPEAKER = "speaker"
    const val MIC = "mic"
    const val VIBRATION = "vibration"
    const val AUDIO_ROUTE = "audioRoute"
    const val CAMERA = "camera"
    const val FLASH = "flash"
    const val CAMERA_DETAIL = "cameraDetail"
    const val SENSORS = "sensors"
    const val LIVE = "live"
    const val COMPASS = "compass"
    const val NETWORK = "network"
    const val PING = "ping"
    const val DNS = "dns"
    const val TROUBLESHOOT = "troubleshoot"
    const val PORTS = "ports"
    const val LAN = "lan"
    const val RADAR = "radar"
    const val WIFI = "wifi"
    const val BLUETOOTH = "bluetooth"
    const val BLE = "ble"
    const val GATT = "gatt"
    const val GPS = "gps"
    const val USB = "usb"
    const val USB_DETAIL = "usbDetail"
    const val NFC = "nfc"
    const val CONTACTLESS = "contactless"
    const val APPS = "apps"
    const val COMPONENTS = "components"
    const val APP_PERMS = "appPerms"
    const val SECURITY = "security"
    const val KEY = "key"
    const val TUNE = "tune"
    const val APP_SETTINGS = "appSettings"
    const val BLOCK = "block"
    const val DELETE_SWEEP = "deleteSweep"
    const val DELETE = "delete"
    const val ADB = "adb"
    const val TERMINAL = "terminal"
    const val INTENT = "intent"
    const val METRICS = "metrics"
    const val LINK = "link"
    const val ARTICLE = "article"
    const val REPORTS = "reports"
    const val COMPARE = "compare"
    const val INSIGHTS = "insights"
    const val MONITOR_HEART = "monitorHeart"
    const val FACT_CHECK = "factCheck"
    const val SETTINGS = "settings"
    const val PRIVACY = "privacy"
    const val INFO = "info"
    const val WHATS_NEW = "whatsNew"
    const val HELP = "help"
    const val CHAT = "chat"
    const val HOME = "home"
    const val TOOLS = "tools"

    val ALL: Set<String> = setOf(
        DEVICE, INSPECTOR, STORAGE, PALETTE, BATTERY, CHARGING,
        DEAD_PIXEL, TOUCH, DISPLAY_MODES, MOTION,
        SPEAKER, MIC, VIBRATION, AUDIO_ROUTE,
        CAMERA, FLASH, CAMERA_DETAIL,
        SENSORS, LIVE, COMPASS,
        NETWORK, PING, DNS, TROUBLESHOOT, PORTS, LAN, RADAR,
        WIFI, BLUETOOTH, BLE, GATT, GPS,
        USB, USB_DETAIL, NFC, CONTACTLESS,
        APPS, COMPONENTS, APP_PERMS, SECURITY, KEY, TUNE, APP_SETTINGS,
        BLOCK, DELETE_SWEEP, DELETE,
        ADB, TERMINAL, INTENT, METRICS, LINK, ARTICLE,
        REPORTS, COMPARE, INSIGHTS, MONITOR_HEART, FACT_CHECK,
        SETTINGS, PRIVACY, INFO, WHATS_NEW, HELP, CHAT, HOME, TOOLS,
    )
}
