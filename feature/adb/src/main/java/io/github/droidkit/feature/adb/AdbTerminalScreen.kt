package io.github.droidkit.feature.adb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.droidkit.core.ui.CopyAllAction
import io.github.droidkit.core.ui.components.DroidKitButton
import io.github.droidkit.core.ui.components.DroidKitTextField
import io.github.droidkit.core.ui.components.DroidKitTopAppBar
import io.github.droidkit.core.ui.components.RiskBadge
import io.github.droidkit.core.ui.components.RiskLevel
import io.github.droidkit.core.ui.theme.DroidKitTheme

/**
 * Opt-in raw shell over the elevated session — the one place a free-form
 * command line exists. Every command goes through
 * `ElevatedOps.runRawCommand` and lands in the elevated-action audit log.
 */
@Composable
fun AdbTerminalScreen(
    onBack: () -> Unit,
    viewModel: AdbTerminalViewModel = hiltViewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val c = DroidKitTheme.colors

    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) listState.animateScrollToItem(entries.size - 1)
    }

    fun submit() {
        if (input.isNotBlank() && !busy) {
            viewModel.run(input)
            input = ""
        }
    }

    Scaffold(
        topBar = {
            DroidKitTopAppBar(
                "ADB terminal",
                onBack = onBack,
                actions = {
                    CopyAllAction {
                        viewModel.scrollbackText().takeIf { it.isNotBlank() }
                    }
                    TextButton(
                        onClick = { viewModel.clear() },
                        enabled = entries.isNotEmpty(),
                    ) { Text("Clear") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        RiskBadge(RiskLevel.Advanced)
                        Text(
                            "Commands run as the ADB shell user on this device. " +
                                "DroidKit does not inspect what you type — every " +
                                "command is recorded in the audit log.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary,
                        )
                    }
                }
                items(entries) { e ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "$ ${e.command}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                            color = c.brandPrimary,
                        )
                        if (e.stdout.isNotBlank()) {
                            Text(
                                e.stdout.trimEnd(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                ),
                                color = c.textPrimary,
                            )
                        }
                        if (e.stderr.isNotBlank()) {
                            Text(
                                e.stderr.trimEnd(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                ),
                                color = c.statusError,
                            )
                        }
                        Text(
                            "${e.provider.name.lowercase()} · exit ${e.exitCode}",
                            style = DroidKitTheme.extraTypography.microLabel,
                            color = c.faint,
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                DroidKitTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("command…") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                    ),
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() }),
                )
                DroidKitButton(
                    onClick = { submit() },
                    enabled = input.isNotBlank() && !busy,
                ) { Text(if (busy) "…" else "Run") }
            }
        }
    }
}
