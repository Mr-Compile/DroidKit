package io.github.droidkit.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ui.theme.DroidKitShapes
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Dark Instrument card (mockup `.card`): flat `panel` surface, 1dp neutral
 * hairline border, 12dp corners, no tonal elevation. Clickable cards compress
 * to 0.985 on press (mockup `.card.clickable:active`).
 *
 * Signatures mirror `androidx.compose.material3.Card` so call sites migrate
 * by rename + import only.
 */
object DroidKitCardDefaults {
    @Composable
    fun cardColors(): CardColors = CardDefaults.cardColors(
        containerColor = DroidKitTheme.colors.panel,
        contentColor = DroidKitTheme.colors.textPrimary,
        disabledContainerColor = DroidKitTheme.colors.panel,
        disabledContentColor = DroidKitTheme.colors.textDisabled,
    )

    @Composable
    fun cardElevation(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = 0.dp,
    )

    @Composable
    fun cardBorder(): BorderStroke = BorderStroke(1.dp, DroidKitTheme.colors.lineNeutral)

    @Composable
    fun cardShape(): Shape = DroidKitShapes.medium
}

/** Press-scale for clickable instrument surfaces (mockup `:active scale(.985)`). */
@Composable
fun Modifier.instrumentPressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, label = "pressScale")
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
fun DroidKitCard(
    modifier: Modifier = Modifier,
    shape: Shape = DroidKitCardDefaults.cardShape(),
    colors: CardColors = DroidKitCardDefaults.cardColors(),
    elevation: CardElevation = DroidKitCardDefaults.cardElevation(),
    border: BorderStroke? = DroidKitCardDefaults.cardBorder(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        content = content,
    )
}

@Composable
fun DroidKitCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DroidKitCardDefaults.cardShape(),
    colors: CardColors = DroidKitCardDefaults.cardColors(),
    elevation: CardElevation = DroidKitCardDefaults.cardElevation(),
    border: BorderStroke? = DroidKitCardDefaults.cardBorder(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        modifier = modifier.instrumentPressScale(source),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        interactionSource = source,
        content = content,
    )
}
