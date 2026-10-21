package io.github.droidkit.feature.permissions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolRegistry
import io.github.droidkit.core.privileges.CapabilityManager
import io.github.droidkit.core.privileges.ProviderKind
import io.github.droidkit.core.privileges.ProviderStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class PermissionCenterViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    toolRegistry: ToolRegistry,
    capabilityManager: CapabilityManager,
) : ViewModel() {

    /** permission name → tools that declare it in their requirements. */
    val permissionToTools: Map<String, List<Tool>> = toolRegistry.tools
        .flatMap { tool -> tool.requiredPermissions.map { it to tool } }
        .groupBy({ it.first }, { it.second })

    /** Live elevated-provider statuses for the "Advanced access" section. */
    val providerStatuses: StateFlow<Map<ProviderKind, ProviderStatus>> = capabilityManager.statuses

    private val pm get() = context.packageManager

    private fun isDangerous(permission: String): Boolean = runCatching {
        pm.getPermissionInfo(permission, 0).protection ==
            android.content.pm.PermissionInfo.PROTECTION_DANGEROUS
    }.getOrDefault(false)

    /** permission → apps that declare it, limited to dangerous permissions. */
    // Launcher-scope <queries> declared in the manifest; screens disclose the limit.
    @android.annotation.SuppressLint("QueryPermissionsNeeded")
    suspend fun loadAppsByPermission(): List<PermGroup> = kotlinx.coroutines.withContext(
        kotlinx.coroutines.Dispatchers.IO,
    ) {
        val apps = runCatching {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(android.content.pm.PackageManager.GET_PERMISSIONS)
        }.getOrNull().orEmpty().mapNotNull { pi ->
            val label = pi.applicationInfo?.loadLabel(pm)?.toString() ?: return@mapNotNull null
            val perms = pi.requestedPermissions?.mapIndexed { i, p ->
                p to (pi.requestedPermissionsFlags?.getOrNull(i)
                    ?.let { it and android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED != 0 }
                    ?: false)
            } ?: emptyList()
            PermAppSource(label, pi.packageName, perms)
        }
        PermissionInfo.buildGroups(apps, ::isDangerous)
    }

    /** For each special-access op, the apps whose AppOps mode is allowed. */
    @android.annotation.SuppressLint("QueryPermissionsNeeded")
    suspend fun loadSpecialAccess(): Map<SpecialOp, List<PermAppEntry>> = kotlinx.coroutines.withContext(
        kotlinx.coroutines.Dispatchers.IO,
    ) {
        val aom = context.getSystemService(android.content.Context.APP_OPS_SERVICE)
            as? android.app.AppOpsManager ?: return@withContext emptyMap()
        val apps = runCatching {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }.getOrNull().orEmpty().mapNotNull { pi ->
            val app = pi.applicationInfo ?: return@mapNotNull null
            Triple(app.loadLabel(pm).toString(), pi.packageName, app.uid)
        }
        PermissionInfo.specialOps.associateWith { op ->
            apps.mapNotNull { (label, pkg, uid) ->
                val allowed = runCatching {
                    if (android.os.Build.VERSION.SDK_INT >= 29) {
                        @Suppress("NewApi")
                        aom.unsafeCheckOpNoThrow(op.op, uid, pkg) ==
                            android.app.AppOpsManager.MODE_ALLOWED
                    } else {
                        @Suppress("DEPRECATION")
                        aom.checkOpNoThrow(op.op, uid, pkg) ==
                            android.app.AppOpsManager.MODE_ALLOWED
                    }
                }.getOrDefault(false)
                if (allowed) PermAppEntry(label, pkg, granted = true) else null
            }.sortedBy { it.label.lowercase() }
        }
    }
}
