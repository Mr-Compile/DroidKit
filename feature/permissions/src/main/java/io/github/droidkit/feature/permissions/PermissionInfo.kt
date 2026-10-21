package io.github.droidkit.feature.permissions

/** Raw per-app permission grant for grouping — kept framework-free. */
data class PermAppSource(
    val label: String,
    val packageName: String,
    /** permission name → currently granted */
    val permissions: List<Pair<String, Boolean>>,
)

data class PermAppEntry(val label: String, val packageName: String, val granted: Boolean)

data class PermGroup(
    val permission: String,
    val apps: List<PermAppEntry>,
) {
    val grantedCount: Int get() = apps.count { it.granted }
}

/** One row of the special-access matrix. */
data class SpecialOp(
    val op: String,
    val label: String,
    val blurb: String,
)

/** Pure grouping/format helpers — JVM-testable. */
object PermissionInfo {

    fun shortName(permission: String): String = permission.substringAfterLast('.')

    /**
     * Flip the app→permissions mapping into permission→apps groups. Only
     * [dangerous] permissions are grouped (install-time perms would drown the
     * list); groups sort by granted count, descending.
     */
    fun buildGroups(
        apps: List<PermAppSource>,
        dangerous: (String) -> Boolean,
    ): List<PermGroup> {
        val map = LinkedHashMap<String, MutableList<PermAppEntry>>()
        apps.forEach { app ->
            app.permissions.forEach { (perm, granted) ->
                if (!dangerous(perm)) return@forEach
                map.getOrPut(perm) { mutableListOf() }
                    .add(PermAppEntry(app.label, app.packageName, granted))
            }
        }
        return map.entries
            .map { (perm, entries) -> PermGroup(perm, entries.sortedBy { it.label.lowercase() }) }
            .sortedByDescending { it.grantedCount }
    }

    /** Special-access ops worth auditing — read via AppOpsManager on-device. */
    val specialOps: List<SpecialOp> = listOf(
        SpecialOp(
            "android:system_alert_window",
            "Display over other apps",
            "Can draw overlays on top of other apps.",
        ),
        SpecialOp(
            "android:write_settings",
            "Modify system settings",
            "Can change system-level settings like brightness and timeout.",
        ),
        SpecialOp(
            "android:request_install_packages",
            "Install unknown apps",
            "Can sideload packages without Play.",
        ),
        SpecialOp(
            "android:picture_in_picture",
            "Picture-in-picture",
            "Can float a video window over other apps.",
        ),
        SpecialOp(
            "android:manage_external_storage",
            "All files access",
            "Broad storage access beyond scoped storage.",
        ),
        SpecialOp(
            "android:get_usage_stats",
            "Usage access",
            "Can read which apps you use and when.",
        ),
        SpecialOp(
            "android:schedule_exact_alarm",
            "Alarms & reminders",
            "Can schedule exact alarms that wake the device.",
        ),
    )
}
