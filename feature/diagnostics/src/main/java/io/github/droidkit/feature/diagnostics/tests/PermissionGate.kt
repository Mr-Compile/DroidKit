package io.github.droidkit.feature.diagnostics.tests

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.droidkit.core.common.TestResultState

/**
 * Wraps a test that needs a runtime permission. Denied → emits
 * PERMISSION_REQUIRED once via [onResult]; the user may also Skip.
 */
@Composable
fun PermissionGate(
    permission: String,
    onResult: (TestResultState, note: String?) -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var requested by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        granted = isGranted
        if (!isGranted) {
            onResult(TestResultState.PERMISSION_REQUIRED, "$permission denied")
        }
    }

    if (granted) {
        content()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Permission required", style = MaterialTheme.typography.titleMedium)
        Text(
            "This test needs $permission to run. DroidKit never requests " +
                "permissions in the background — it asks only when you start the test.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                requested = true
                launcher.launch(permission)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (requested) "Request again" else "Grant permission")
        }
        TestResultBar(onResult = onResult)
    }
}
