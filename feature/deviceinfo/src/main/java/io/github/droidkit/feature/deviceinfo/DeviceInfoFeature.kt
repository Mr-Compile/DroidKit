package io.github.droidkit.feature.deviceinfo

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.common.ToolIconKeys

object DeviceInfoFeature : FeatureContribution {
    const val ROUTE = "deviceinfo"

    override val tools = listOf(
        Tool(
            id = "deviceinfo.overview",
            title = "Device Overview",
            description = "About-phone style device identity, status, SIM, OS, hardware and capabilities",
            category = ToolCategory.DEVICE,
            route = ROUTE,
            keywords = setOf(
                "phone info", "build", "hardware", "specs", "model",
                "about phone", "device name", "imei", "sim", "status", "serial",
            ),
            iconKey = ToolIconKeys.DEVICE,
        ),
        Tool(
            id = "deviceinfo.storage",
            title = "Storage Lab",
            description = "Volumes, capacity, free space, storage warnings",
            category = ToolCategory.DEVICE,
            route = "$ROUTE/storage",
            keywords = setOf("disk", "space", "memory", "volumes", "free"),
            iconKey = ToolIconKeys.STORAGE,
        ),
        Tool(
            id = "deviceinfo.codecs",
            title = "Codec Inventory",
            description = "All MediaCodecList decoders/encoders with MIME types and acceleration flags",
            category = ToolCategory.DEVICE,
            route = "$ROUTE/codecs",
            keywords = setOf(
                "codec", "decoder", "encoder", "h264", "h265", "av1", "aac",
                "media", "video decode", "mime",
            ),
            iconKey = ToolIconKeys.TUNE,
        ),
    )
}

fun NavGraphBuilder.deviceInfoGraph(nav: NavHostController) {
    composable(DeviceInfoFeature.ROUTE) { DeviceInfoScreen(nav) }
    composable("${DeviceInfoFeature.ROUTE}/storage") {
        StorageLabScreen(onBack = { nav.navigateUp() })
    }
    composable("${DeviceInfoFeature.ROUTE}/codecs") {
        CodecListScreen(onBack = { nav.navigateUp() })
    }
}
