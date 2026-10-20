package io.github.droidkit.feature.display

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private data class ModeInfo(
    val id: Int,
    val width: Int,
    val height: Int,
    val refreshRate: Float,
    val isCurrent: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayModesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val display = activity?.windowManager?.defaultDisplay
        ?: (context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
    var listenerTick by remember { mutableStateOf(0) }

    DisposableEffect(display) {
        if (display == null) return@DisposableEffect onDispose {}
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(id: Int) {}
            override fun onDisplayRemoved(id: Int) {}
            override fun onDisplayChanged(id: Int) {
                if (id == display.displayId) listenerTick++
            }
        }
        dm.registerDisplayListener(listener, null)
        onDispose { dm.unregisterDisplayListener(listener) }
    }

    Scaffold(topBar = { DroidKitTopAppBar("Display Modes", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                listenerTick // recompose on mode change
                if (display == null) {
                    SectionCard(title = "Display") {
                        Text(
                            "No display available (NOT_SUPPORTED).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                } else {
                    val current = display.mode
                    val modes = display.supportedModes.map {
                        ModeInfo(it.modeId, it.physicalWidth, it.physicalHeight, it.refreshRate, it.modeId == current.modeId)
                    }
                    SectionCard(title = "Current mode") {
                        InfoRow("Mode id", current.modeId.toString(), copyable = false)
                        InfoRow("Resolution", "${current.physicalWidth} x ${current.physicalHeight}", copyable = false)
                        InfoRow("Refresh rate", "%.2f Hz".format(current.refreshRate), copyable = false)
                        InfoRow("Rotation", display.rotation.toString(), copyable = false)
                        InfoRow("HDR capable", display.isHdr.toString(), copyable = false)
                        InfoRow("Wide color", display.isWideColorGamut.toString(), copyable = false)
                    }
                }
            }
            if (display != null) {
                item {
                    val current = display.mode
                    SectionCard(title = "Supported modes (${display.supportedModes.size})") {
                        display.supportedModes.forEach { mode ->
                            InfoRow(
                                "${mode.physicalWidth} x ${mode.physicalHeight}",
                                "%.2f Hz".format(mode.refreshRate) +
                                    if (mode.modeId == current.modeId) " · current" else "",
                                copyable = false,
                            )
                        }
                        Text(
                            "Mode switching is restricted to the system — this is a read-only inventory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                item {
                    val current = display.mode
                    AiSummaryCard(
                        screenTitle = "Display modes",
                        fields = mapOf(
                            "Resolution" to "${current.physicalWidth} x ${current.physicalHeight}",
                            "Refresh rate" to "%.2f Hz".format(current.refreshRate),
                            "HDR capable" to display.isHdr.toString(),
                            "Wide color" to display.isWideColorGamut.toString(),
                            "Supported modes" to display.supportedModes.joinToString("; ") {
                                "${it.physicalWidth}x${it.physicalHeight} @ %.0f Hz".format(it.refreshRate)
                            },
                        ),
                    )
                }
            }
        }
    }
}
