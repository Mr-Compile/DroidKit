package io.github.droidkit.core.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ui.components.StatusBadge
import io.github.droidkit.core.ui.components.StatusKind
import io.github.droidkit.core.ui.components.color
import io.github.droidkit.core.ui.theme.DroidKitSpacing

/**
 * Renders a model reply as humanized rich text: verdict pills, tinted
 * status icons per judged line, real bold/italic/mono spans and true
 * bullet/number rows — markdown symbols never reach the screen.
 */
@Composable
fun AiRichText(text: String, modifier: Modifier = Modifier) {
    val blocks = remember(text) { AiTextFormat.parse(text) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DroidKitSpacing.Xs),
    ) {
        blocks.forEach { block ->
            when (block) {
                is AiBlock.Paragraph -> BodyRow(block.spans, block.cue)
                is AiBlock.Heading -> Text(
                    block.spans.toAnnotatedString(),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(top = DroidKitSpacing.Xs)
                        .semantics { heading() },
                )
                is AiBlock.Bullet -> MarkerRow(
                    marker = { CueMarker(block.cue) { Text("•") } },
                    spans = block.spans,
                )
                is AiBlock.Numbered -> MarkerRow(
                    marker = {
                        Text(
                            "${block.n}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    spans = block.spans,
                )
                is AiBlock.Verdict -> VerdictRow(block)
                is AiBlock.Mono -> MonoBlock(block.text)
                AiBlock.Divider -> HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = DroidKitSpacing.Xs),
                )
            }
        }
    }
}

/** A text row that may carry a leading status icon (paragraph lines). */
@Composable
private fun BodyRow(spans: List<AiSpan>, cue: StatusCue) {
    if (cue == StatusCue.NONE) {
        Text(spans.toAnnotatedString(), style = MaterialTheme.typography.bodyMedium)
    } else {
        MarkerRow(marker = { CueMarker(cue) {} }, spans = spans)
    }
}

/** Bullet/numbered/icon rows share one indent: 20dp marker slot + text. */
@Composable
private fun MarkerRow(
    marker: @Composable () -> Unit,
    spans: List<AiSpan>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp).padding(top = 1.dp),
        ) {
            marker()
        }
        Text(
            spans.toAnnotatedString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 2.dp).weight(1f),
        )
    }
}

/** 16dp status glyph for a line's cue; [fallback] when cue is NONE. */
@Composable
private fun CueMarker(cue: StatusCue, fallback: @Composable () -> Unit) {
    val icon = cue.icon()
    if (icon == null) {
        fallback()
    } else {
        Icon(
            imageVector = icon,
            // Decorative — the line's own words carry the verdict.
            contentDescription = null,
            tint = cue.kind().color(),
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun VerdictRow(block: AiBlock.Verdict) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        StatusBadge(
            kind = block.cue.kind(),
            label = block.label,
            modifier = Modifier.padding(top = 1.dp),
        )
        if (block.rest.isNotEmpty()) {
            Text(
                block.rest.toAnnotatedString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp).weight(1f),
            )
        }
    }
}

@Composable
private fun MonoBlock(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(8.dp),
        )
    }
}

private fun StatusCue.kind(): StatusKind = when (this) {
    StatusCue.POSITIVE -> StatusKind.Success
    StatusCue.WARNING -> StatusKind.Warning
    StatusCue.NEGATIVE -> StatusKind.Error
    StatusCue.INFO -> StatusKind.Info
    StatusCue.NONE -> StatusKind.Neutral
}

private fun StatusCue.icon(): ImageVector? = when (this) {
    StatusCue.POSITIVE -> Icons.Outlined.CheckCircle
    StatusCue.WARNING -> Icons.Outlined.Warning
    StatusCue.NEGATIVE -> Icons.Outlined.Cancel
    StatusCue.INFO -> Icons.Outlined.Info
    StatusCue.NONE -> null
}

@Composable
private fun List<AiSpan>.toAnnotatedString(): AnnotatedString {
    val codeBg = MaterialTheme.colorScheme.surfaceContainerHighest
    return buildAnnotatedString {
        for (span in this@toAnnotatedString) {
            when (span) {
                is AiSpan.Text -> append(span.text)
                is AiSpan.Strong -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(span.text)
                }
                is AiSpan.Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(span.text)
                }
                is AiSpan.Code -> withStyle(
                    SpanStyle(fontFamily = FontFamily.Monospace, background = codeBg),
                ) {
                    append(span.text)
                }
            }
        }
    }
}
