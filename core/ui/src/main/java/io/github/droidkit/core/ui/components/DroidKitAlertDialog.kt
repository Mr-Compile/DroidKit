package io.github.droidkit.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import io.github.droidkit.core.ui.theme.DroidKitShapes
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Modal dialog (mockup `.dialog`): `raise` surface, hairline border, 16dp
 * corners. [danger] tints the border with the error accent
 * (mockup `.dialog.danger`). Signature mirrors the common `material3.AlertDialog`
 * slots so call sites migrate by rename + import only.
 */
@Composable
fun DroidKitAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    danger: Boolean = false,
    properties: DialogProperties = DialogProperties(),
) {
    val c = DroidKitTheme.colors
    val shape = DroidKitShapes.large
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier.border(
            1.dp,
            if (danger) c.statusError.copy(alpha = 0.35f) else c.lineNeutral,
            shape,
        ),
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = c.raise,
        iconContentColor = c.brandPrimary,
        titleContentColor = c.textPrimary,
        textContentColor = c.textPrimary,
        tonalElevation = 0.dp,
        properties = properties,
    )
}

/** Convenience title slot matching `.dialog h3` (17px w650 ls -.2). */
@Composable
fun DroidKitDialogTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 17.sp,
            letterSpacing = (-0.2).sp,
        ),
    )
}
