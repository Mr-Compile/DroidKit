package io.github.droidkit.feature.permissions

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

private data class PermissionRow(
    val name: String,
    val shortName: String,
    val granted: Boolean?,
    val isRuntime: Boolean,
)

private val runtimePermissions = setOf(
    android.Manifest.permission.CAMERA,
    android.Manifest.permission.RECORD_AUDIO,
)

/** DroidKit's own permission center — no elevation needed, reads own manifest. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCenterScreen() {
    val context = LocalContext.current

    val rows = remember {
        val declared = try {
            context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions?.toList() ?: emptyList()
        } catch (e: PackageManager.NameNotFoundException) {
            emptyList()
        }
        declared.map { name ->
            val isRuntime = name in runtimePermissions || isRuntimePermission(name)
            PermissionRow(
                name = name,
                shortName = name.substringAfterLast('.'),
                granted = if (isRuntime) {
                    ContextCompat.checkSelfPermission(context, name) ==
                        PackageManager.PERMISSION_GRANTED
                } else null,
                isRuntime = isRuntime,
            )
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Permission Center") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Permissions DroidKit declares. Runtime permissions are requested " +
                        "only when you start a test that needs them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(rows, key = { it.name }) { row ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(row.shortName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                row.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            when {
                                !row.isRuntime -> "Install-time"
                                row.granted == true -> "Granted"
                                else -> "Not granted"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = when {
                                !row.isRuntime -> MaterialTheme.colorScheme.onSurfaceVariant
                                row.granted == true -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.error
                            },
                        )
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null),
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text("Open system app settings")
                }
            }
        }
    }
}

/** Normal permissions auto-granted at install; dangerous ones are runtime. */
private fun isRuntimePermission(name: String): Boolean =
    name.startsWith("android.permission.") &&
        name !in setOf(
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.WAKE_LOCK,
            android.Manifest.permission.FOREGROUND_SERVICE,
        )
