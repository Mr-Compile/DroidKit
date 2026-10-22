package io.github.droidkit.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Input field (mockup `.field`): `field` fill, neutral hairline, 10dp corners,
 * brand focus ring. Signature mirrors `material3.OutlinedTextField`.
 */
@Composable
fun DroidKitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
) {
    val c = DroidKitTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = c.textPrimary,
            unfocusedTextColor = c.textPrimary,
            disabledTextColor = c.textDisabled,
            errorTextColor = c.textPrimary,
            focusedContainerColor = c.field,
            unfocusedContainerColor = c.field,
            disabledContainerColor = c.field,
            errorContainerColor = c.field,
            cursorColor = c.brandPrimary,
            errorCursorColor = c.statusError,
            focusedBorderColor = c.brandPrimary.copy(alpha = 0.55f),
            unfocusedBorderColor = c.lineNeutral,
            disabledBorderColor = c.lineNeutral,
            errorBorderColor = c.statusError,
            focusedLeadingIconColor = c.brandPrimary,
            unfocusedLeadingIconColor = c.faint,
            disabledLeadingIconColor = c.textDisabled,
            errorLeadingIconColor = c.faint,
            focusedTrailingIconColor = c.faint,
            unfocusedTrailingIconColor = c.faint,
            disabledTrailingIconColor = c.textDisabled,
            errorTrailingIconColor = c.statusError,
            focusedLabelColor = c.brandPrimary,
            unfocusedLabelColor = c.textSecondary,
            disabledLabelColor = c.textDisabled,
            errorLabelColor = c.statusError,
            focusedPlaceholderColor = c.faint,
            unfocusedPlaceholderColor = c.faint,
            disabledPlaceholderColor = c.textDisabled,
            errorPlaceholderColor = c.faint,
            focusedSupportingTextColor = c.textSecondary,
            unfocusedSupportingTextColor = c.textSecondary,
            disabledSupportingTextColor = c.textDisabled,
            errorSupportingTextColor = c.statusError,
            focusedPrefixColor = c.textSecondary,
            unfocusedPrefixColor = c.textSecondary,
            disabledPrefixColor = c.textDisabled,
            errorPrefixColor = c.textSecondary,
            focusedSuffixColor = c.textSecondary,
            unfocusedSuffixColor = c.textSecondary,
            disabledSuffixColor = c.textDisabled,
            errorSuffixColor = c.textSecondary,
        ),
    )
}

/** Field label (mockup `.flabel`) — letterspaced uppercase faint micro-label. */
@Composable
fun DroidKitFieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = DroidKitTheme.extraTypography.fieldLabel,
        color = DroidKitTheme.colors.faint,
        modifier = modifier.padding(start = 2.dp, bottom = 4.dp),
    )
}

/**
 * Thin raised tile wrapping arbitrary content — used for `.prop`-style
 * rows (AI proposals, suggestion rows).
 */
@Composable
fun DroidKitRaisedRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val c = DroidKitTheme.colors
    val shape = RoundedCornerShape(10.dp)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = c.raise,
            border = BorderStroke(1.dp, c.lineNeutral),
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) { content() }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = c.raise,
            border = BorderStroke(1.dp, c.lineNeutral),
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) { content() }
        }
    }
}
