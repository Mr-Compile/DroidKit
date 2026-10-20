package io.github.droidkit.feature.diagnostics.tests

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.common.TestResultState
import io.github.droidkit.feature.diagnostics.PhoneDoctorTest

@Composable
fun VibrationScreen(
    test: PhoneDoctorTest,
    onResult: (TestResultState, note: String?) -> Unit,
) {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    val hasVibrator = remember { vibrator?.hasVibrator() == true }
    var pulseCount by remember { mutableIntStateOf(0) }

    if (!hasVibrator) {
        LaunchedEffect(test.id) {
            onResult(TestResultState.NOT_SUPPORTED, "Device reports no vibrator")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(test.title, style = MaterialTheme.typography.titleMedium)
            Text(
                test.instructions,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE),
                    )
                    pulseCount++
                },
                enabled = hasVibrator,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Vibrate (400 ms)")
            }
            if (!hasVibrator) {
                Text(
                    "No vibrator reported — recording NOT SUPPORTED.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        TestResultBar(
            onResult = { state, note ->
                val buzz = if (pulseCount == 0) {
                    "buzz never triggered"
                } else {
                    "buzz triggered $pulseCount time(s)"
                }
                onResult(state, listOfNotNull(note, buzz).joinToString("; "))
            },
        )
    }
}
