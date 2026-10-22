package io.github.droidkit.feature.diagnostics.tests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.core.permissions.RuntimePermissionState
import io.github.droidkit.core.ui.components.DroidKitButton
import io.github.droidkit.core.ui.components.GateCard
import io.github.droidkit.core.ui.components.rememberRuntimePermission
import io.github.droidkit.core.ui.theme.DroidKitTheme

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
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val (message, actionLabel, onAction) = when (perm.state) {
            RuntimePermissionState.NOT_DECLARED ->
                Triple(
                    "This build does not declare $permission — the test cannot run.",
                    null,
                    null,
                )
            RuntimePermissionState.DENIED_PERMANENT ->
                Triple(
                    "Permission $permission is permanently denied. Enable it in " +
                        "system Settings to run this test.",
                    "Open Settings",
                    perm.openSettings,
                )
            else ->
                Triple(
                    "This test needs $permission to run. DroidKit never requests " +
                        "permissions in the background — it asks only when you start the test.",
                    if (perm.asked) "Request again" else "Grant permission",
                    perm.request,
                )
        }
        // mockup .gatecard — dashed brand gate with lock glyph.
        GateCard(
            title = "Permission required",
            message = message,
            icon = Icons.Outlined.Lock,
            accent = DroidKitTheme.colors.statusSetupRequired,
            action = if (actionLabel != null && onAction != null) {
                {
                    DroidKitButton(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
                        Text(actionLabel)
                    }
                }
            } else {
                null
            },
        )
        TestResultBar(onResult = onResult)
    }
}
