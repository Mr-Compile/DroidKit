package io.github.droidkit.feature.diagnostics.tests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.core.permissions.RuntimePermissionState
import io.github.droidkit.core.ui.components.rememberRuntimePermission

/**
 * Wraps a test that needs a runtime permission. Denied → emits
 * PERMISSION_REQUIRED once via [onResult]; the user may also Skip.
 * Permanent denial offers an "Open Settings" recovery instead of a dead-end.
 */
@Composable
fun PermissionGate(
    permission: String,
    onResult: (TestResultState, note: String?) -> Unit,
    autoResult: Boolean = true,
    content: @Composable () -> Unit,
) {
    val perm = rememberRuntimePermission(
        permission = permission,
        onDenied = {
            if (autoResult) onResult(TestResultState.PERMISSION_REQUIRED, "$permission denied")
        },
    )

    if (perm.state == RuntimePermissionState.GRANTED) {
        content()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Permission required", style = MaterialTheme.typography.titleMedium)
        when (perm.state) {
            RuntimePermissionState.NOT_DECLARED -> Text(
                "This build does not declare $permission — the test cannot run.",
                style = MaterialTheme.typography.bodyMedium,
            )
            RuntimePermissionState.DENIED_PERMANENT -> {
                Text(
                    "Permission $permission is permanently denied. Enable it in " +
                        "system Settings to run this test.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = perm.openSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Settings")
                }
            }
            else -> {
                Text(
                    "This test needs $permission to run. DroidKit never requests " +
                        "permissions in the background — it asks only when you start the test.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = perm.request, modifier = Modifier.fillMaxWidth()) {
                    Text(if (perm.asked) "Request again" else "Grant permission")
                }
            }
        }
        TestResultBar(onResult = onResult)
    }
}
