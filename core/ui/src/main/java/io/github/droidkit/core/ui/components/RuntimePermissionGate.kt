package io.github.droidkit.core.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import io.github.droidkit.core.permissions.RuntimePermissionState

/** Snapshot + actions for one runtime permission inside Compose. */
class RuntimePermissionRequest(
    val state: RuntimePermissionState,
    /** True once the system dialog has been shown this composition. */
    val asked: Boolean,
    /** Launch the system permission dialog (no-op when granted/undeclared). */
    val request: () -> Unit,
    /** Open this app's system settings page — the recovery for permanent denial. */
    val openSettings: () -> Unit,
)

/**
 * Shared runtime-permission state machine (DK-021). Distinguishes
 * requestable denial from permanent denial via `shouldShowRequestPermissionRationale`
 * after a completed request round-trip.
 */
@Composable
fun rememberRuntimePermission(
    permission: String,
    onDenied: () -> Unit = {},
): RuntimePermissionRequest {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var asked by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }

    // Grant state changes in system Settings while the app is backgrounded —
    // re-read on resume so "Open Settings" recoveries actually unblock.
    LifecycleResumeEffect(permission) {
        granted = ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) permanentlyDenied = false
        onPauseOrDispose { }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        granted = isGranted
        asked = true
        if (!isGranted) {
            permanentlyDenied =
                activity?.shouldShowRequestPermissionRationale(permission) == false
            onDenied()
        }
    }

    val declared = remember {
        context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions
            ?.contains(permission) == true
    }

    val state = when {
        !declared -> RuntimePermissionState.NOT_DECLARED
        granted -> RuntimePermissionState.GRANTED
        permanentlyDenied -> RuntimePermissionState.DENIED_PERMANENT
        else -> RuntimePermissionState.DENIED_REQUESTABLE
    }

    return RuntimePermissionRequest(
        state = state,
        asked = asked,
        request = {
            if (state == RuntimePermissionState.DENIED_REQUESTABLE || !asked) {
                launcher.launch(permission)
            } else {
                // Nothing to gain re-asking; send the user to Settings.
                context.openAppSettings()
            }
        },
        openSettings = { context.openAppSettings() },
    )
}

/**
 * Requests [permission] once when composed, then invokes [onFinished] after the
 * system flow resolves (granted, denied, or permanent). Used by recovery
 * actions that originate outside a screen's own permission gate.
 */
@Composable
fun PermissionRequestEffect(permission: String, onFinished: () -> Unit) {
    val req = rememberRuntimePermission(permission)
    LaunchedEffect(req.state, req.asked) {
        when (req.state) {
            RuntimePermissionState.DENIED_REQUESTABLE ->
                if (!req.asked) req.request() else onFinished()
            else -> onFinished()
        }
    }
}

internal fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/** Open this app's system details screen — the only fix for permanent denial. */
fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        ),
    )
}
