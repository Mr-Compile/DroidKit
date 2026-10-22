package io.github.droidkit.feature.diagnostics.tests

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import io.github.droidkit.core.ui.components.DroidKitButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest

@Composable
fun FlashTestScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
    autoResult: Boolean = true,
) {
    PermissionGate(permission = test.permission!!, onResult = onResult, autoResult = autoResult) {
        FlashContent(test, onResult, autoResult)
    }
}

/**
 * Drives the LED via [CameraManager.setTorchMode] — the same API the system
 * quick-settings tile uses — instead of CameraX `enableTorch`. CameraX torch
 * control needs an active capture session; binding a camera with no use case
 * leaves the torch request applied to nothing on many devices, so the flash
 * never lit even though the call succeeded.
 */
@Composable
private fun FlashContent(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
    autoResult: Boolean,
) {
    val context = LocalContext.current
    val cameraManager = remember {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    var flashCameraId by remember { mutableStateOf<String?>(null) }
    var hasFlash by remember { mutableStateOf<Boolean?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var toggleCount by remember { mutableIntStateOf(0) }
    var torchError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            flashCameraId = findFlashCameraId(cameraManager)
            hasFlash = flashCameraId != null
            if (hasFlash == false && autoResult) {
                onResult(TestResultState.NOT_SUPPORTED, "No camera reports a flash unit")
            }
        } catch (e: Exception) {
            torchError = e.message ?: e.javaClass.simpleName
        }
    }

    DisposableEffect(Unit) {
        val callback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                if (cameraId == flashCameraId) torchOn = enabled
            }

            override fun onTorchModeUnavailable(cameraId: String) {
                if (cameraId == flashCameraId) {
                    torchError = "Torch unavailable — another app holds the camera"
                }
            }
        }
        cameraManager.registerTorchCallback(callback, Handler(Looper.getMainLooper()))
        onDispose {
            cameraManager.unregisterTorchCallback(callback)
            flashCameraId?.let { id ->
                runCatching { cameraManager.setTorchMode(id, false) }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(test.title, style = MaterialTheme.typography.titleMedium)
            Text(
                test.instructions,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            torchError?.let {
                Text(
                    "Torch error: $it",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            DroidKitButton(
                onClick = {
                    val id = flashCameraId ?: return@DroidKitButton
                    try {
                        cameraManager.setTorchMode(id, !torchOn)
                        torchOn = !torchOn
                        toggleCount++
                        torchError = null
                    } catch (e: Exception) {
                        torchError = e.message ?: e.javaClass.simpleName
                    }
                },
                enabled = hasFlash == true,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (torchOn) "Turn torch OFF" else "Turn torch ON")
            }
            if (hasFlash == false && torchError == null) {
                Text(
                    "No flash unit reported — " +
                        if (autoResult) "recorded NOT SUPPORTED." else "NOT SUPPORTED.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val detail = listOfNotNull(
                    note,
                    torchError?.let { "torch error: $it" },
                    if (toggleCount == 0) "torch never toggled"
                    else "torch toggled $toggleCount time(s)",
                ).joinToString("; ").ifBlank { null }
                onResult(state, detail)
            },
        )
    }
}

/**
 * Picks the camera whose LED the torch mode controls. Prefers a back-facing
 * unit; falls back to any camera reporting [CameraCharacteristics.FLASH_INFO_AVAILABLE].
 */
private fun findFlashCameraId(manager: CameraManager): String? {
    data class FlashCamera(val id: String, val lensFacing: Int?)

    val withFlash = manager.cameraIdList.mapNotNull { id ->
        val chars = runCatching { manager.getCameraCharacteristics(id) }.getOrNull()
            ?: return@mapNotNull null
        if (chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) {
            FlashCamera(id, chars.get(CameraCharacteristics.LENS_FACING))
        } else {
            null
        }
    }
    return withFlash.firstOrNull { it.lensFacing == CameraCharacteristics.LENS_FACING_BACK }?.id
        ?: withFlash.firstOrNull()?.id
}
