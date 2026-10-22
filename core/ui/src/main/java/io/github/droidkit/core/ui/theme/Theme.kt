package io.github.droidkit.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private fun darkInstrumentScheme(dk: DroidKitSemanticColors) = darkColorScheme(
    primary = dk.brandPrimary,
    onPrimary = dk.onBrand,
    primaryContainer = dk.raise,
    onPrimaryContainer = dk.textPrimary,
    inversePrimary = dk.brandPrimary,
    secondary = dk.brandSecondary,
    onSecondary = dk.onBrand,
    secondaryContainer = dk.panel2,
    onSecondaryContainer = dk.textPrimary,
    tertiary = dk.statusWarning,
    onTertiary = dk.onBrand,
    tertiaryContainer = dk.panel2,
    onTertiaryContainer = dk.textPrimary,
    background = dk.ink,
    onBackground = dk.textPrimary,
    surface = dk.panel,
    onSurface = dk.textPrimary,
    surfaceVariant = dk.panel2,
    onSurfaceVariant = dk.textSecondary,
    surfaceTint = dk.brandPrimary,
    inverseSurface = dk.raise,
    inverseOnSurface = dk.textPrimary,
    error = dk.statusError,
    onError = dk.onBrand,
    errorContainer = dk.panel2,
    onErrorContainer = dk.statusError,
    outline = dk.outline,
    outlineVariant = dk.lineNeutral,
    scrim = dk.scrim,
    surfaceBright = dk.raise,
    surfaceDim = dk.ink,
    surfaceContainer = dk.panel2,
    surfaceContainerHigh = dk.raise,
    surfaceContainerHighest = dk.raise,
    surfaceContainerLow = dk.panel,
    surfaceContainerLowest = dk.ink,
)

private fun lightInstrumentScheme(dk: DroidKitSemanticColors) = lightColorScheme(
    primary = dk.brandPrimary,
    onPrimary = dk.onBrand,
    primaryContainer = dk.raise,
    onPrimaryContainer = dk.textPrimary,
    inversePrimary = dk.brandPrimary,
    secondary = dk.brandSecondary,
    onSecondary = dk.onBrand,
    secondaryContainer = dk.panel2,
    onSecondaryContainer = dk.textPrimary,
    tertiary = dk.statusWarning,
    onTertiary = dk.onBrand,
    tertiaryContainer = dk.panel2,
    onTertiaryContainer = dk.textPrimary,
    background = dk.ink,
    onBackground = dk.textPrimary,
    surface = dk.panel,
    onSurface = dk.textPrimary,
    surfaceVariant = dk.panel2,
    onSurfaceVariant = dk.textSecondary,
    surfaceTint = dk.brandPrimary,
    inverseSurface = dk.raise,
    inverseOnSurface = dk.textPrimary,
    error = dk.statusError,
    onError = dk.onBrand,
    errorContainer = dk.panel2,
    onErrorContainer = dk.statusError,
    outline = dk.outline,
    outlineVariant = dk.lineNeutral,
    scrim = dk.scrim,
    surfaceBright = dk.panel,
    surfaceDim = dk.panel2,
    surfaceContainer = dk.panel2,
    surfaceContainerHigh = dk.raise,
    surfaceContainerHighest = dk.raise,
    surfaceContainerLow = dk.panel,
    surfaceContainerLowest = dk.panel,
)

/**
 * DroidKit "Dark Instrument" theme (v5 + `mockups/improved-index.html`).
 *
 * The brand palette is fixed — dark-first graphite-ink surfaces with the
 * green→teal identity — because the app's look is part of the product
 * identity, not a wallpaper derivative. [dynamicColor] stays available for
 * the Settings "Material You colors" opt-in, off by default.
 */
@Composable
fun DroidKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dk = if (darkTheme) DarkDroidKitColors else LightDroidKitColors
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkInstrumentScheme(dk)
        else -> lightInstrumentScheme(dk)
    }
    CompositionLocalProvider(
        LocalDroidKitColors provides dk,
        LocalDroidKitTypography provides DroidKitExtraTypography(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DroidKitTypography,
            shapes = DroidKitShapes,
            content = content,
        )
    }
}

/** Token accessors — `DroidKitTheme.colors` / `.extraTypography` inside composition. */
object DroidKitTheme {
    val colors: DroidKitSemanticColors
        @Composable get() = LocalDroidKitColors.current
    val extraTypography: DroidKitExtraTypography
        @Composable get() = LocalDroidKitTypography.current
    /** The identity gradient (mockup --grad, 135deg brand → teal). */
    val brandGradient
        @Composable get() = colors.brandGradient
    /** Icon-tile gradient wash (mockup .tic background). */
    val tileGradient
        @Composable get() = colors.tileGradient
}
