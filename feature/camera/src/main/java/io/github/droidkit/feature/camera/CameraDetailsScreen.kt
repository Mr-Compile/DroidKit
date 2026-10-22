package io.github.droidkit.feature.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.view.SurfaceHolder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.CopyAllAction
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.formatSectionsForCopy
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

private fun facingName(facing: Int?) = CameraInfo.facingName(facing)

private fun hardwareLevelName(level: Int?) = CameraInfo.hardwareLevelName(level)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetailsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    val cameraIds = runCatching { cm?.cameraIdList?.toList().orEmpty() }.getOrDefault(emptyList())

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                "Camera Capabilities",
                onBack = onBack,
                actions = { CopyAllAction { cameraSectionsText(cm, cameraIds) } },
            )
        },
    ) { padding ->
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
                                    "Camera $id ISO range",
                                    c.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                                        ?.let { "${it.lower}–${it.upper}" },
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

private fun cameraSectionsText(cm: CameraManager?, cameraIds: List<String>): String? {
    val sections = cameraIds.mapNotNull { id ->
        val c = runCatching { cm?.getCameraCharacteristics(id) }.getOrNull()
            ?: return@mapNotNull null
        val rows = buildList<Pair<String, String?>> {
            add(
                "Hardware level" to hardwareLevelName(
                    c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL),
                ),
            )
            add(
                "Sensor" to c.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                    ?.let { "${it.width}×${it.height} (${CameraInfo.megapixels(it)})" },
            )
            add("Flash" to c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)?.toString())
            add(
                "Focal lengths" to c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    ?.joinToString { "%.1f mm".format(it) },
            )
            add(
                "Apertures" to c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                    ?.joinToString { "f/%.1f".format(it) },
            )
            c.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)?.let {
                add("Sensor physical size" to "%.2f × %.2f mm".format(it.width, it.height))
            }
            c.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)?.let {
                add("ISO range" to "${it.lower}–${it.upper}")
            }
            c.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)?.let {
                add("Exposure range" to "${it.lower}–${it.upper} ns")
            }
            c.get(CameraCharacteristics.SENSOR_ORIENTATION)?.let {
                add("Sensor orientation" to "$it°")
            }
            c.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
                ?.takeIf { it.isNotEmpty() }?.let { ranges ->
                    add("FPS ranges" to ranges.joinToString { CameraInfo.fpsRangeLabel(it) })
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                c.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)?.let {
                    add("Zoom ratio" to "${it.lower}×–${it.upper}×")
                }
            }
            add(
                "Capabilities" to c.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                    ?.toList()?.mapNotNull(CameraInfo::capabilityName)
                    ?.takeIf { it.isNotEmpty() }?.joinToString(),
            )
            add(
                "Physical cameras" to c.physicalCameraIds
                    .takeIf { it.isNotEmpty() }?.joinToString(),
            )
        }
        "Camera $id — ${facingName(c.get(CameraCharacteristics.LENS_FACING))}" to rows
    }
    return sections.takeIf { it.isNotEmpty() }?.let { formatSectionsForCopy(it) }
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
    var showSizes by remember { mutableStateOf(false) }
    var showModes by remember { mutableStateOf(false) }

    SectionCard(title = "Camera $id — ${facingName(facing)}") {
        InfoRow("Hardware level", hardwareLevelName(level), copyable = false)
        InfoRow(
            "Sensor",
            sensor?.let { "${it.width}×${it.height} (${CameraInfo.megapixels(it)})" },
        )
        InfoRow("Flash", flash?.toString(), copyable = false)
        InfoRow(
            "Focal lengths",
            focal?.joinToString { "%.1f mm".format(it) },
        )
        InfoRow(
            "Apertures",
            apertures?.joinToString { "f/%.1f".format(it) },
        )
        c.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)?.let {
            InfoRow("Sensor physical size", "%.2f × %.2f mm".format(it.width, it.height))
        }
        c.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)?.let {
            InfoRow("ISO range", "${it.lower}–${it.upper}", copyable = false)
        }
        c.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)?.let {
            InfoRow(
                "Exposure range",
                "${it.lower}–${it.upper} ns",
                copyable = false,
            )
        }
        c.get(CameraCharacteristics.SENSOR_ORIENTATION)?.let {
            InfoRow("Sensor orientation", "$it°", copyable = false)
        }
        c.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
            ?.takeIf { it.isNotEmpty() }?.let { ranges ->
                InfoRow(
                    "FPS ranges",
                    ranges.joinToString { CameraInfo.fpsRangeLabel(it) },
                    copyable = false,
                )
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            c.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)?.let {
                InfoRow("Zoom ratio", "${it.lower}×–${it.upper}×", copyable = false)
            }
        }
        val capNames = caps?.toList()?.mapNotNull(CameraInfo::capabilityName)
        InfoRow(
            "Capabilities",
            capNames?.takeIf { it.isNotEmpty() }?.joinToString(),
        )
        val logical = c.physicalCameraIds.takeIf { it.isNotEmpty() }?.joinToString()
        InfoRow("Physical cameras", logical)

        // StreamConfigurationMap — output sizes per format, high-speed sizes.
        val map = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        if (map != null) {
            DroidKitOutlinedButton(
                onClick = { showSizes = !showSizes },
                modifier = Modifier.padding(top = 8.dp),
            ) { Text(if (showSizes) "Hide output sizes" else "Output sizes") }
            if (showSizes) {
                listOf(
                    ImageFormat.JPEG,
                    ImageFormat.YUV_420_888,
                    ImageFormat.RAW_SENSOR,
                    ImageFormat.PRIVATE,
                ).forEach { fmt ->
                    val sizes = runCatching {
                        map.getOutputSizes(fmt)?.toList()
                    }.getOrNull()
                        ?: runCatching { map.getOutputSizes(SurfaceHolder::class.java)?.toList() }
                            .getOrNull()
                    CameraInfo.sizesSummary(sizes?.toTypedArray())?.let { summary ->
                        Text(
                            "${CameraInfo.imageFormatName(fmt)}: $summary",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                val hsSizes = runCatching { map.highSpeedVideoSizes?.toList() }.getOrNull()
                if (!hsSizes.isNullOrEmpty()) {
                    Text(
                        "High-speed video: " + hsSizes.sortedByDescending {
                            it.width.toLong() * it.height
                        }.take(4).joinToString { s ->
                            val fps = runCatching {
                                map.getHighSpeedVideoFpsRangesFor(s)?.toList()
                            }.getOrNull()
                            CameraInfo.sizeLabel(s) +
                                (fps?.joinToString(prefix = " @ ", separator = ", ") {
                                    CameraInfo.fpsRangeLabel(it)
                                } ?: "")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }

        // Available mode lists — AE/AWB/AF, stabilization, scenes, effects.
        DroidKitOutlinedButton(
            onClick = { showModes = !showModes },
            modifier = Modifier.padding(top = 8.dp),
        ) { Text(if (showModes) "Hide modes" else "Modes & tuning") }
        if (showModes) {
            @Composable
            fun modes(label: String, ints: IntArray?, name: (Int) -> String?) {
                val names = ints?.toList()?.mapNotNull(name).orEmpty()
                if (names.isNotEmpty()) {
                    InfoRow(
                        label,
                        names.joinToString(),
                        copyable = false,
                    )
                }
            }
            modes("AE modes", c.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES), CameraInfo::aeModeName)
            modes("AWB modes", c.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES), CameraInfo::awbModeName)
            modes("AF modes", c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), CameraInfo::afModeName)
            modes("Video stabilization", c.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES), CameraInfo::videoStabilizationName)
            modes("Optical stabilization", c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION), CameraInfo::oisName)
            modes("Scene modes", c.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES), CameraInfo::sceneModeName)
            modes("Effects", c.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS), CameraInfo::effectName)
        }
    }
}
