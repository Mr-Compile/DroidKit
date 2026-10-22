package io.github.droidkit.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ui.theme.DroidKitMark
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Brand lockup for app chrome: the hex mark beside the two-tone
 * "Droid" / "Kit" wordmark. Usage rules: docs/03_design_system/branding/README.md.
 */
@Composable
fun DroidKitWordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 26.dp,
) {
    val c = DroidKitTheme.colors
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
            imageVector = DroidKitMark,
            contentDescription = null,
            modifier = Modifier.size(markSize),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = c.textPrimary)) {
                    append("Droid")
                }
                withStyle(SpanStyle(color = c.brandPrimary)) { append("Kit") }
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}
