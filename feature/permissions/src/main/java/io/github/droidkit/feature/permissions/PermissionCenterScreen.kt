package io.github.droidkit.feature.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import io.github.droidkit.core.ui.components.DroidKitCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ai.AiSummaryCard
import io.github.droidkit.core.permissions.RuntimePermissionState
import io.github.droidkit.core.privileges.ProviderStatus
import io.github.droidkit.core.ui.CopyAllAction
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.formatSectionsForCopy
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.SectionHeader
import io.github.droidkit.core.ui.components.RuntimePermissionRequest
import io.github.droidkit.core.ui.components.openAppSettings
import io.github.droidkit.core.ui.components.rememberRuntimePermission

/** What each declared permission is for — verified against real feature usage. */
private fun permissionPurpose(name: String): String = when (name) {
    android.Manifest.permission.CAMERA ->
        "Camera tests and flashlight control in Phone Doctor; the Camera Lab inspector."
    android.Manifest.permission.RECORD_AUDIO ->
        "Microphone test in Phone Doctor and audio routing details."
    android.Manifest.permission.BLUETOOTH_CONNECT ->
        "Read adapter state and bonded devices on Android 12+."
    android.Manifest.permission.BLUETOOTH_SCAN ->
        "Discover nearby Bluetooth devices on Android 12+ (never used for location)."
    android.Manifest.permission.ACCESS_FINE_LOCATION ->
        "Bluetooth discovery on Android 11 and older requires this runtime grant."
    android.Manifest.permission.NFC -> "Read and write NFC tags."
    android.Manifest.permission.INTERNET ->
        "Optional online features only: DroidKit Intelligence requests and update checks."
    android.Manifest.permission.ACCESS_NETWORK_STATE ->
        "Show connectivity status and distinguish offline from service failures."
    android.Manifest.permission.ACCESS_WIFI_STATE -> "Wi-Fi link details in Network Lab."
    android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE ->
        "mDNS device discovery on the local network."
    android.Manifest.permission.VIBRATE -> "Haptic feedback for the vibration test."
    android.Manifest.permission.BLUETOOTH,
    android.Manifest.permission.BLUETOOTH_ADMIN,
    -> "Bluetooth access on Android 11 and older."
    else -> "Declared in the app manifest."
}

/** True for dangerous permissions — resolved from PackageManager, not a guess list. */
private fun isRuntimePermission(context: Context, name: String): Boolean = runCatching {
    // getPermissionInfo's flags parameter is a plain Int — PackageInfoFlags
    // only applies to getPackageInfo/getInstalledPackages.
    val info = context.packageManager.getPermissionInfo(name, 0)
    info.protection == PermissionInfo.PROTECTION_DANGEROUS
}.getOrDefault(false)

private fun declaredPermissions(context: Context): List<String> = runCatching {
    context.packageManager
        .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        .requestedPermissions?.toList()
}.getOrNull().orEmpty()

/** DroidKit's own permission center — no elevation needed, reads own manifest. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCenterScreen(
    onOpenSettings: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: PermissionCenterViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val providerStatuses by viewModel.providerStatuses.collectAsState()

    // Grant state self-refreshes on resume inside rememberRuntimePermission.
    val (runtime, installTime) = remember {
        declaredPermissions(context).partition { isRuntimePermission(context, it) }
    }
    // Hoisted once per permission — the row and the AI summary share the same
    // request so a grant updates both.
    val runtimeRequests = runtime.associateWith { rememberRuntimePermission(it) }

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                "Permissions & Access",
                onBack = onBack,
                actions = {
                    CopyAllAction {
                        formatSectionsForCopy(
                            buildList {
                                if (runtimeRequests.isNotEmpty()) {
                                    add(
                                        "Runtime permissions" to runtimeRequests.map { (p, r) ->
                                            p.substringAfterLast('.') to
                                                "${r.state.name} — ${permissionPurpose(p)}"
                                        },
                                    )
                                }
                                if (providerStatuses.isNotEmpty()) {
                                    add(
                                        "Advanced access" to providerStatuses.map { (kind, status) ->
                                            kind.name to when (status) {
                                                ProviderStatus.Ready -> "Ready"
                                                is ProviderStatus.NeedsSetup -> "Setup required"
                                                is ProviderStatus.Unavailable -> status.reason
                                            }
                                        },
                                    )
                                }
                                if (installTime.isNotEmpty()) {
                                    add(
                                        "Install-time permissions" to installTime.map { p ->
                                            p.substringAfterLast('.') to permissionPurpose(p)
                                        },
                                    )
                                }
                            },
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().contentWidthCap(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Runtime permissions are requested only when you start a tool " +
                        "that needs them — never on first launch or in the background.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item { SectionHeader("Runtime permissions") }
            items(runtime, key = { "rt_$it" }) { permission ->
                runtimeRequests[permission]?.let { req ->
                    RuntimePermissionRow(
                        permission = permission,
                        request = req,
                        affectedTools = viewModel.permissionToTools[permission].orEmpty()
                            .map { it.title },
                    )
                }
            }

            item {
                AiSummaryCard(
                    screenTitle = "Permissions & access",
                    fields = buildMap {
                        runtimeRequests.forEach { (p, r) ->
                            put(p.substringAfterLast('.'), r.state.name)
                        }
                        providerStatuses.forEach { (kind, status) ->
                            put(
                                "Elevated ${kind.name}",
                                when (status) {
                                    ProviderStatus.Ready -> "ready"
                                    is ProviderStatus.NeedsSetup -> "setup required"
                                    is ProviderStatus.Unavailable -> status.reason
                                },
                            )
                        }
                    },
                )
            }

            item { SectionHeader("Advanced access") }
            item {
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Optional elevated providers for technician actions. " +
                                "Every standard tool works without them.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        providerStatuses.forEach { (kind, status) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            ) {
                                Text(
                                    kind.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    when (status) {
                                        ProviderStatus.Ready -> "Ready"
                                        is ProviderStatus.NeedsSetup -> "Setup required"
                                        is ProviderStatus.Unavailable -> status.reason
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (status == ProviderStatus.Ready) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                        TextButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("Manage in Settings")
                        }
                    }
                }
            }

            item { SectionHeader("Install-time permissions") }
            item {
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        installTime.forEach { permission ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Text(
                                    permission.substringAfterLast('.'),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    permissionPurpose(permission),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            "Granted automatically at install; Android does not offer " +
                                "a per-permission toggle for these.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            item {
                DroidKitOutlinedButton(
                    onClick = { context.openAppSettings() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text("Open system app settings")
                }
            }
        }
    }
}

@Composable
private fun RuntimePermissionRow(
    permission: String,
    request: RuntimePermissionRequest,
    affectedTools: List<String>,
) {
    val perm = request
    val granted = perm.state == RuntimePermissionState.GRANTED

    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        permission.substringAfterLast('.'),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        permission,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    if (granted) "Granted" else "Not granted",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (granted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Text(
                permissionPurpose(permission),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (affectedTools.isNotEmpty()) {
                Text(
                    "Used by: ${affectedTools.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            when (perm.state) {
                RuntimePermissionState.DENIED_PERMANENT -> {
                    Text(
                        "Denied permanently — enable it in system Settings to use " +
                            "tools that need it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    TextButton(
                        onClick = perm.openSettings,
                        modifier = Modifier.align(Alignment.End),
                    ) { Text("Open Settings") }
                }
                RuntimePermissionState.DENIED_REQUESTABLE ->
                    TextButton(
                        onClick = perm.request,
                        modifier = Modifier.align(Alignment.End),
                    ) { Text("Grant") }
                RuntimePermissionState.NOT_DECLARED ->
                    Text(
                        "Not declared in this build.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                RuntimePermissionState.GRANTED -> Unit
            }
        }
    }
}
