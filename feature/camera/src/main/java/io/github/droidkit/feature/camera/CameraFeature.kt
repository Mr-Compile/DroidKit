package io.github.droidkit.feature.camera

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.privileges.ToolResolution
import io.github.droidkit.core.ui.FeatureStubScreen

object CameraFeature : FeatureContribution {
    const val ROUTE = "camera"

    override val tools = listOf(
        Tool(
            id = "camera.capture",
            title = "Camera Test",
            description = "Preview each camera and capture a test frame",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredPermissions = listOf("android.permission.CAMERA"),
            requiredFeatures = setOf("android.hardware.camera.any"),
        ),
        Tool(
            id = "camera.flash",
            title = "Flash Test",
            description = "Toggle torch on each camera unit",
            category = ToolCategory.DIAGNOSTICS,
            route = ROUTE,
            requiredFeatures = setOf("android.hardware.camera.flash"),
        ),
    )
}

fun NavGraphBuilder.cameraGraph(nav: NavHostController, resolve: (Tool) -> ToolResolution) {
    composable(CameraFeature.ROUTE) {
        FeatureStubScreen(
            title = "Camera & Flash",
            subtitle = "Camera and flash tests ship in Phase 3 (Phone Doctor).",
            tools = CameraFeature.tools,
            resolve = resolve,
        )
    }
}
