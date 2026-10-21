package io.github.droidkit.core.ui.adaptive

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Max readable content width on wide windows (inspector rows, settings, reports). */
val DefaultContentMaxWidth: Dp = 840.dp

/**
 * Zeroed window insets — pass to a `Scaffold`/`DroidKitTopAppBar` embedded in a
 * pane whose parent already consumed the system bars, so it doesn't re-apply
 * status/nav padding inside the pane.
 */
val EmptyWindowInsets: WindowInsets = WindowInsets(0, 0, 0, 0)

/**
 * Caps an outer container at [maxWidth] and centers it horizontally. Apply to a
 * screen's root content container — children keep `fillMaxWidth` and stay
 * readable on expanded windows instead of stretching edge to edge.
 */
fun Modifier.contentWidthCap(maxWidth: Dp = DefaultContentMaxWidth): Modifier =
    fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .widthIn(max = maxWidth)
