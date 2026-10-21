package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.BluetoothSearching
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Adb
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.AppSettingsAlt
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.BluetoothConnected
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.DisplaySettings
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Hardware
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.NetworkPing
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TapAndPlay
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Troubleshoot
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.SearchEntry
import io.github.droidkit.core.common.SearchResultType
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys
import io.github.droidkit.core.ui.theme.DroidKitCategoryColors

/**
 * UI v6 icon registry (plan files 06/07): the single mapping from semantic
 * [ToolIconKeys] to Material vectors. Every surface resolves icons through
 * here — never per screen. Outlined family only for one visual weight.
 */
object ToolIcons {

    private val byKey: Map<String, ImageVector> = mapOf(
        ToolIconKeys.DEVICE to Icons.Outlined.PhoneAndroid,
        ToolIconKeys.INSPECTOR to Icons.AutoMirrored.Outlined.ManageSearch,
        ToolIconKeys.STORAGE to Icons.Outlined.Storage,
        ToolIconKeys.PALETTE to Icons.Outlined.Palette,
        ToolIconKeys.BATTERY to Icons.Outlined.BatteryStd,
        ToolIconKeys.CHARGING to Icons.Outlined.BatteryChargingFull,
        ToolIconKeys.DEAD_PIXEL to Icons.Outlined.FiberManualRecord,
        ToolIconKeys.TOUCH to Icons.Outlined.TouchApp,
        ToolIconKeys.DISPLAY_MODES to Icons.Outlined.DisplaySettings,
        ToolIconKeys.MOTION to Icons.Outlined.Animation,
        ToolIconKeys.SPEAKER to Icons.AutoMirrored.Outlined.VolumeUp,
        ToolIconKeys.MIC to Icons.Outlined.Mic,
        ToolIconKeys.VIBRATION to Icons.Outlined.Vibration,
        ToolIconKeys.AUDIO_ROUTE to Icons.AutoMirrored.Outlined.AltRoute,
        ToolIconKeys.CAMERA to Icons.Outlined.PhotoCamera,
        ToolIconKeys.FLASH to Icons.Outlined.FlashOn,
        ToolIconKeys.CAMERA_DETAIL to Icons.Outlined.CameraAlt,
        ToolIconKeys.SENSORS to Icons.Outlined.Sensors,
        ToolIconKeys.LIVE to Icons.Outlined.Timeline,
        ToolIconKeys.COMPASS to Icons.Outlined.Explore,
        ToolIconKeys.NETWORK to Icons.Outlined.Hub,
        ToolIconKeys.PING to Icons.Outlined.NetworkPing,
        ToolIconKeys.DNS to Icons.Outlined.Dns,
        ToolIconKeys.TROUBLESHOOT to Icons.Outlined.Troubleshoot,
        ToolIconKeys.PORTS to Icons.Outlined.SettingsEthernet,
        ToolIconKeys.LAN to Icons.Outlined.Lan,
        ToolIconKeys.RADAR to Icons.Outlined.Radar,
        ToolIconKeys.WIFI to Icons.Outlined.Wifi,
        ToolIconKeys.BLUETOOTH to Icons.Outlined.Bluetooth,
        ToolIconKeys.BLE to Icons.AutoMirrored.Outlined.BluetoothSearching,
        ToolIconKeys.GATT to Icons.Outlined.BluetoothConnected,
        ToolIconKeys.GPS to Icons.Outlined.GpsFixed,
        ToolIconKeys.USB to Icons.Outlined.Usb,
        ToolIconKeys.USB_DETAIL to Icons.Outlined.Memory,
        ToolIconKeys.NFC to Icons.Outlined.Nfc,
        ToolIconKeys.CONTACTLESS to Icons.Outlined.TapAndPlay,
        ToolIconKeys.APPS to Icons.Outlined.Apps,
        ToolIconKeys.COMPONENTS to Icons.Outlined.AccountTree,
        ToolIconKeys.APP_PERMS to Icons.Outlined.Policy,
        ToolIconKeys.SECURITY to Icons.Outlined.Security,
        ToolIconKeys.KEY to Icons.Outlined.Key,
        ToolIconKeys.TUNE to Icons.Outlined.Tune,
        ToolIconKeys.APP_SETTINGS to Icons.Outlined.AppSettingsAlt,
        ToolIconKeys.BLOCK to Icons.Outlined.Block,
        ToolIconKeys.DELETE_SWEEP to Icons.Outlined.DeleteSweep,
        ToolIconKeys.DELETE to Icons.Outlined.Delete,
        ToolIconKeys.ADB to Icons.Outlined.Adb,
        ToolIconKeys.TERMINAL to Icons.Outlined.Terminal,
        ToolIconKeys.INTENT to Icons.AutoMirrored.Outlined.Send,
        ToolIconKeys.METRICS to Icons.Outlined.Straighten,
        ToolIconKeys.LINK to Icons.Outlined.Link,
        ToolIconKeys.ARTICLE to Icons.AutoMirrored.Outlined.Article,
        ToolIconKeys.REPORTS to Icons.Outlined.Assessment,
        ToolIconKeys.COMPARE to Icons.Outlined.Compare,
        ToolIconKeys.INSIGHTS to Icons.Outlined.Insights,
        ToolIconKeys.MONITOR_HEART to Icons.Outlined.MonitorHeart,
        ToolIconKeys.FACT_CHECK to Icons.AutoMirrored.Outlined.FactCheck,
        ToolIconKeys.SETTINGS to Icons.Outlined.Settings,
        ToolIconKeys.PRIVACY to Icons.Outlined.PrivacyTip,
        ToolIconKeys.INFO to Icons.Outlined.Info,
        ToolIconKeys.WHATS_NEW to Icons.Outlined.NewReleases,
        ToolIconKeys.HELP to Icons.AutoMirrored.Outlined.HelpOutline,
        ToolIconKeys.CHAT to Icons.Outlined.SmartToy,
        ToolIconKeys.HOME to Icons.Outlined.Home,
        ToolIconKeys.TOOLS to Icons.Outlined.Build,
    )

    private val byCategory: Map<ToolCategory, ImageVector> = mapOf(
        ToolCategory.DEVICE to Icons.Outlined.PhoneAndroid,
        ToolCategory.DIAGNOSTICS to Icons.Outlined.MonitorHeart,
        ToolCategory.APPS to Icons.Outlined.Apps,
        ToolCategory.CONNECTIVITY to Icons.Outlined.Hub,
        ToolCategory.HARDWARE to Icons.Outlined.Hardware,
        ToolCategory.DEVELOPER to Icons.Outlined.Terminal,
        ToolCategory.REPORTS to Icons.Outlined.Assessment,
    )

    /** Resolve a [ToolIconKeys] value; unknown keys are a programming error. */
    fun of(key: String): ImageVector =
        byKey[key] ?: error("Icon key '$key' has no vector mapping — add it to ToolIcons")

    fun forCategory(category: ToolCategory): ImageVector = byCategory.getValue(category)

    /** Tool iconKey wins; blank falls back to the category icon. */
    fun forTool(tool: Tool): ImageVector =
        if (tool.iconKey.isNotEmpty()) of(tool.iconKey) else forCategory(tool.category)

    /** Entry iconKey wins; blank falls back to the result-type icon. */
    fun forEntry(entry: SearchEntry): ImageVector =
        if (entry.iconKey.isNotEmpty()) of(entry.iconKey) else forType(entry.type)

    fun forType(type: SearchResultType): ImageVector = when (type) {
        SearchResultType.PAGE -> Icons.AutoMirrored.Outlined.Article
        SearchResultType.SETTING -> Icons.Outlined.Settings
        SearchResultType.DIAGNOSTIC -> Icons.Outlined.MonitorHeart
        SearchResultType.HELP -> Icons.AutoMirrored.Outlined.HelpOutline
        SearchResultType.REPORT -> Icons.Outlined.Assessment
    }
}

/** Relative luminance of the scheme background — tracks the resolved theme, not the OS. */
private fun ColorScheme.isDark(): Boolean =
    background.red * 0.2126f + background.green * 0.7152f + background.blue * 0.0722f < 0.5f

/**
 * Canonical icon container: tonal rounded square, fixed 24dp glyph, decorative
 * by default so the labeled row/card announces once (plan file 19).
 * [category] tints the glyph with the per-category accent — grouping only,
 * never status; null keeps the default onSecondaryContainer.
 */
@Composable
fun ToolIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    category: ToolCategory? = null,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = category?.let {
                DroidKitCategoryColors.accent(it, darkTheme = MaterialTheme.colorScheme.isDark())
            } ?: MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(8.dp).size(24.dp),
        )
    }
}
