package io.github.droidkit.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Primary gradient action (mockup `.btn`): green→teal fill, brand glow,
 * top inner highlight, ink text. Signature mirrors `material3.Button`.
 */
@Composable
fun DroidKitButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(10.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 11.dp),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val c = DroidKitTheme.colors
    val src = interactionSource ?: remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.then(
            if (enabled) {
                Modifier.shadow(
                    elevation = 8.dp,
                    shape = shape,
                    ambientColor = c.brandGlow,
                    spotColor = c.brandGlow,
                )
            } else {
                Modifier
            },
        ),
        enabled = enabled,
        shape = shape,
        color = Color.Transparent,
        contentColor = if (enabled) c.onBrand else c.textSecondary,
        border = if (enabled) {
            // mockup .btn inset 0 1px 0 rgba(255,255,255,.22)
            BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                ),
            )
        } else {
            null
        },
        interactionSource = src,
    ) {
        ProvideTextStyle(MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (enabled) c.brandGradient else SolidColor(c.raise), shape)
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides if (enabled) c.onBrand else c.textSecondary,
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * Secondary brand-bordered action (mockup `.btn-outline`): transparent fill,
 * brand hairline + text. Signature mirrors `material3.OutlinedButton`.
 */
@Composable
fun DroidKitOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(10.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val c = DroidKitTheme.colors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = c.brandPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = c.textSecondary,
        ),
        border = BorderStroke(1.dp, if (enabled) c.lineBrand else c.lineNeutral),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

/**
 * Filter chip (mockup `.fchip`): transparent with a neutral hairline;
 * selected gains the brand border, tint and weight.
 */
@Composable
fun DroidKitFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val c = DroidKitTheme.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(9.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = c.textSecondary,
            iconColor = c.textSecondary,
            disabledContainerColor = Color.Transparent,
            disabledLabelColor = c.textDisabled,
            disabledLeadingIconColor = c.textDisabled,
            disabledTrailingIconColor = c.textDisabled,
            selectedContainerColor = c.brandTintSelected,
            selectedLabelColor = c.brandPrimary,
            selectedLeadingIconColor = c.brandPrimary,
            selectedTrailingIconColor = c.brandPrimary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = c.lineNeutral,
            selectedBorderColor = c.brandPrimary.copy(alpha = 0.55f),
            disabledBorderColor = c.lineNeutral,
            disabledSelectedBorderColor = c.lineNeutral,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
        ),
    )
}

/**
 * Segmented control (mockup `.segcard`/`.segopt`): field-track pill row —
 * selected option raises to `panel` with a brand border.
 */
@Composable
fun <T> DroidKitSegmentedRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    val c = DroidKitTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = c.field,
        border = BorderStroke(1.dp, c.lineNeutral),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            options.forEach { option ->
                val isSel = option == selected
                Surface(
                    onClick = { onSelect(option) },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { role = Role.Tab },
                    shape = RoundedCornerShape(9.dp),
                    color = if (isSel) c.panel else Color.Transparent,
                    contentColor = if (isSel) c.brandPrimary else c.textSecondary,
                    border = BorderStroke(
                        1.dp,
                        if (isSel) c.brandPrimary.copy(alpha = 0.5f) else Color.Transparent,
                    ),
                    shadowElevation = if (isSel) 2.dp else 0.dp,
                ) {
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
