package io.github.droidkit.feature.diagnostics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import io.github.droidkit.core.ui.components.DiagnosticProgress
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedSessionScreen(
    onFinished: () -> Unit,
    viewModel: DiagnosticsViewModel,
) {
    val sessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val engineState by viewModel.engine.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (sessionId == null) viewModel.startGuided()
    }
    LaunchedEffect(engineState.done) {
        if (engineState.done) onFinished()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        engineState.current?.let { "Test ${engineState.index + 1} of ${engineState.total}: ${it.title}" }
                            ?: "Full Diagnostic",
                    )
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            DiagnosticProgress(
                progress = if (engineState.total == 0) 0f
                    else engineState.index.toFloat() / engineState.total,
            )
            val current = engineState.current
            if (current != null) {
                PhoneDoctorTestScreen(test = current) { state, note ->
                    viewModel.recordResult(current.id, state, note)
                }
            } else {
                Text(
                    "Preparing session…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(padding.calculateTopPadding()),
                )
            }
        }
    }
}
