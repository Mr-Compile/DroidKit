package io.github.droidkit.core.permissions

fun interface PermissionChecker {
    fun isGranted(permission: String): Boolean
}
