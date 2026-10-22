package io.github.droidkit.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Standard app bar (UI v6 plan file 04). Pass [onBack] on secondary/detail
 * surfaces to render the Up affordance; top-level destinations omit it.
 * Toolbar Up and the system back gesture stay equivalent because callers
 * wire [onBack] to `navigateUp`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DroidKitTopAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    actions: @Composable RowScope.() -> Unit = {},
) {
    DroidKitTopAppBar(
        title = { Text(title) },
        onBack = onBack,
        windowInsets = windowInsets,
        actions = actions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DroidKitTopAppBar(
    title: @Composable () -> Unit,
    onBack: (() -> Unit)? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val c = DroidKitTheme.colors
    TopAppBar(
        title = title,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = c.ink,
            scrolledContainerColor = c.panel2,
            titleContentColor = c.textPrimary,
            navigationIconContentColor = c.textSecondary,
            actionIconContentColor = c.textSecondary,
        ),
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
        actions = actions,
        windowInsets = windowInsets,
    )
}
