package io.github.droidkit.feature.diagnostics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.tests.DisplayColorScreen

/**
 * Single dispatch point for every Phone Doctor test screen.
 * Used by both the standalone quick-test route and the guided runner.
 */
@Composable
fun PhoneDoctorTestScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    when {
        test.id.startsWith("display.") -> DisplayColorScreen(test, onResult)
        test.sensorType != null -> UnsupportedTestPlaceholder(test)
        else -> UnsupportedTestPlaceholder(test)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnsupportedTestPlaceholder(test: PhoneDoctorTest) {
    Scaffold(topBar = { TopAppBar(title = { Text(test.title) }) }) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Test screen not yet wired: ${test.id}")
        }
    }
}
