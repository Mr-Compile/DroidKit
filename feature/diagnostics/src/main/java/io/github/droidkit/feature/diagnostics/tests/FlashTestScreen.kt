package io.github.droidkit.feature.diagnostics.tests

import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
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

@Composable
private fun FlashContent(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
    autoResult: Boolean,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var provider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var hasFlash by remember { mutableStateOf<Boolean?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var toggleCount by remember { mutableIntStateOf(0) }
    var initError by remember { mutableStateOf<String?>(null) }
    var boundCamera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    LaunchedEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                try {
                    val p = future.get()
                    provider = p
                    val selector = CameraSelector.DEFAULT_BACK_CAMERA
                    if (p.hasCamera(selector)) {
                        val camera = p.bindToLifecycle(lifecycleOwner, selector)
                        boundCamera = camera
                        hasFlash = camera.cameraInfo.hasFlashUnit()
                        if (hasFlash == false && autoResult) {
                            onResult(TestResultState.NOT_SUPPORTED, "Rear camera reports no flash unit")
                        }
                    } else {
                        hasFlash = false
                        if (autoResult) {
                            onResult(TestResultState.NOT_SUPPORTED, "No rear camera for flash test")
                        }
                    }
                } catch (e: Exception) {
                    initError = e.message ?: e.javaClass.simpleName
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            boundCamera?.cameraControl?.enableTorch(false)
            provider?.unbindAll()
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
            initError?.let {
                Text(
                    "Camera init failed: $it",
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
            Button(
                onClick = {
                    torchOn = !torchOn
                    boundCamera?.cameraControl?.enableTorch(torchOn)
                    toggleCount++
                },
                enabled = hasFlash == true,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (torchOn) "Turn torch OFF" else "Turn torch ON")
            }
            if (hasFlash == false) {
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
                    initError?.let { "camera init error: $it" },
                    if (toggleCount == 0) "torch never toggled"
                    else "torch toggled $toggleCount time(s)",
                ).joinToString("; ").ifBlank { null }
                onResult(state, detail)
            },
        )
    }
}
