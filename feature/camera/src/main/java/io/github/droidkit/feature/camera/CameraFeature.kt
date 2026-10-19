package io.github.droidkit.feature.camera

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.droidkit.core.common.FeatureContribution
import io.github.droidkit.core.common.Tool
import io.github.droidkit.core.common.ToolCategory
import io.github.droidkit.core.ui.LabEntry
import io.github.droidkit.core.ui.LabIndexScreen

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
        Tool(
            id = "camera.details",
            title = "Camera Capabilities",
            description = "Camera2 inventory: sensors, lenses, levels, capabilities",
            category = ToolCategory.DIAGNOSTICS,
            route = "$ROUTE/details",
            requiredFeatures = setOf("android.hardware.camera.any"),
        ),
    )
}

fun NavGraphBuilder.cameraGraph(nav: NavHostController) {
    composable(CameraFeature.ROUTE) {
        LabIndexScreen(
            title = "Camera & Flash",
            subtitle = "Standalone camera checks from Phone Doctor. " +
                "Results here are not saved — run Full Diagnostic to persist them.",
            entries = listOf(
                LabEntry(
                    testId = "camera.front",
                    title = "Front camera",
                    blurb = "Confirm the front preview renders a live image. " +
                        "Needs camera permission.",
                ),
                LabEntry(
                    testId = "camera.rear",
                    title = "Rear camera",
                    blurb = "Confirm the rear preview renders a live image. " +
                        "Needs camera permission.",
                ),
                LabEntry(
                    testId = "camera.flash",
                    title = "Flash",
                    blurb = "Toggle the torch and confirm the LED lights. " +
                        "Needs camera permission.",
                ),
            ),
            onOpenTest = { testId -> nav.navigate("diagnose/test/$testId") },
        )
    }
    composable("${CameraFeature.ROUTE}/details") {
        CameraDetailsScreen()
    }
}
