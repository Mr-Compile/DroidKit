package io.github.droidkit.feature.diagnostics.tests

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest

@Composable
fun CameraTestScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    PermissionGate(permission = test.permission!!, onResult = onResult) {
        CameraContent(test, onResult)
    }
}

@Composable
private fun CameraContent(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var provider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var initError by remember { mutableStateOf<String?>(null) }
    var cameraAvailable by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                try {
                    val p = future.get()
                    provider = p
                    val selector = CameraSelector.Builder()
                        .requireLensFacing(test.lensFacing!!)
                        .build()
                    cameraAvailable = p.hasCamera(selector)
                    if (!cameraAvailable!!) {
                        onResult(
                            TestResultState.NOT_SUPPORTED,
                            "No camera with lensFacing=${test.lensFacing} reported",
                        )
                    }
                } catch (e: Exception) {
                    initError = e.message ?: e.javaClass.simpleName
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    DisposableEffect(Unit) {
        onDispose { provider?.unbindAll() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
        if (cameraAvailable == true && provider != null) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        val preview = Preview.Builder().build()
                        preview.surfaceProvider = surfaceProvider
                        provider?.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.Builder()
                                .requireLensFacing(test.lensFacing!!)
                                .build(),
                            preview,
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            )
        } else {
            Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                Text(
                    when {
                        initError != null -> "Could not start camera — judge based on the error shown."
                        cameraAvailable == false -> "No ${test.title.lowercase()} hardware — recorded NOT SUPPORTED."
                        else -> "Initializing camera…"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val detail = listOfNotNull(
                    note,
                    initError?.let { "camera init error: $it" },
                    when (cameraAvailable) {
                        true -> "preview rendered"
                        false -> "preview not rendered"
                        null -> null
                    },
                ).joinToString("; ").ifBlank { null }
                onResult(state, detail)
            },
        )
    }
}
