package io.github.droidkit.feature.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

private fun facingName(facing: Int?): String = when (facing) {
    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
    CameraCharacteristics.LENS_FACING_BACK -> "Back"
    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
    else -> "Unknown"
}

private fun hardwareLevelName(level: Int?): String = when (level) {
    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3"
    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "External"
    else -> "Unknown"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetailsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    val cameraIds = runCatching { cm?.cameraIdList?.toList().orEmpty() }.getOrDefault(emptyList())

    Scaffold(topBar = { DroidKitTopAppBar("Camera Capabilities", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (cameraIds.isEmpty()) {
                item {
                    SectionCard(title = "Cameras") {
                        Text(
                            "No camera hardware reported (NOT_SUPPORTED).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            } else {
                items(cameraIds) { id ->
                    val chars = runCatching { cm?.getCameraCharacteristics(id) }.getOrNull()
                    if (chars == null) {
                        SectionCard(title = "Camera $id") {
                            Text(
                                "Characteristics unavailable (device may be a logical camera).",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    } else {
                        CameraCard(id, chars)
                    }
                }
                item {
                    AiSummaryCard(
                        screenTitle = "Camera capabilities",
                        fields = buildMap<String, String?> {
                            put("Cameras reported", cameraIds.size.toString())
                            cameraIds.forEach { id ->
                                val c = runCatching { cm?.getCameraCharacteristics(id) }.getOrNull()
                                    ?: return@forEach
                                put(
                                    "Camera $id facing",
                                    facingName(c.get(CameraCharacteristics.LENS_FACING)),
                                )
                                put(
                                    "Camera $id hardware level",
                                    hardwareLevelName(
                                        c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL),
                                    ),
                                )
                                put(
                                    "Camera $id sensor",
                                    c.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                                        ?.let { "${it.width}x${it.height}" },
                                )
                                put(
                                    "Camera $id flash",
                                    c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)?.toString(),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraCard(id: String, c: CameraCharacteristics) {
    val facing = c.get(CameraCharacteristics.LENS_FACING)
    val level = c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
    val flash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
    val focal = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
    val apertures = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
    val sensor = c.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
    val caps = c.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)

    SectionCard(title = "Camera $id — ${facingName(facing)}") {
        InfoRow("Hardware level", hardwareLevelName(level), copyable = false)
        InfoRow("Sensor", sensor?.let { "${it.width} x ${it.height}" })
        InfoRow("Flash", flash?.toString(), copyable = false)
        InfoRow(
            "Focal lengths",
            focal?.joinToString { "%.1f mm".format(it) },
        )
        InfoRow(
            "Apertures",
            apertures?.joinToString { "f/%.1f".format(it) },
        )
        val capNames = caps?.toList()?.mapNotNull(::capabilityName)
        InfoRow(
            "Capabilities",
            capNames?.takeIf { it.isNotEmpty() }?.joinToString(),
        )
        val logical = c.physicalCameraIds.takeIf { it.isNotEmpty() }?.joinToString()
        InfoRow("Physical cameras", logical)
    }
}

private fun capabilityName(cap: Int): String? = when (cap) {
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE -> "Backward compatible"
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR -> "Manual sensor"
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING -> "Manual post-processing"
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW -> "RAW"
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE -> "Burst"
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS -> "Read sensor settings"
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT -> "Depth"
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO -> "High-speed video"
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING -> "Motion tracking"
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA -> "Logical multi-camera"
    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_ULTRA_HIGH_RESOLUTION_SENSOR -> "Ultra-high-res"
    else -> null
}
