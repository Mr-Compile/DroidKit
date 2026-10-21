package io.github.droidkit.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.ui.theme.DroidKitMark
import kotlinx.coroutines.launch

/**
 * One-time intro per docs/02_foundations/j03_design/J03_SCREEN_SPECS §1 — Welcome →
 * Capabilities → Privacy → Done. Sets the product's trust contract:
 * honest states, optional elevated providers, local-first data.
 */
private data class Page(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val tagline: String? = null,
)

private val pages = listOf(
    Page(
        DroidKitMark,
        "Welcome to DroidKit",
        "A technician's toolkit for Android: diagnostics, device " +
            "inspection, connectivity labs, and repair reports — all " +
            "running natively on this device.",
        tagline = "Tools for a healthier Android",
    ),
    Page(
        Icons.Filled.CheckCircle,
        "Honest results, always",
        "DroidKit never fabricates a reading. Missing hardware shows " +
            "NOT SUPPORTED, denied permissions show PERMISSION REQUIRED — " +
            "neither is a failure, both are facts about the device.",
    ),
    Page(
        Icons.Filled.Shield,
        "Optional capabilities",
        "Core tools work with standard Android access. Wireless ADB and " +
            "root are optional providers you connect later in " +
            "Settings → Advanced access — nothing requires them.",
    ),
    Page(
        Icons.Filled.Lock,
        "Your data stays here",
        "Reports, scan history, and diagnostics are stored locally. " +
            "AI assistance is off by default and opt-in only — you " +
            "choose what leaves the device.",
    ),
)

@Composable
fun OnboardingScreen(
    settings: SettingsRepository,
    onDone: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    fun finish() {
        scope.launch {
            settings.setOnboardingCompleted()
            onDone()
        }
    }

    // No topBar here — without safeDrawing the Skip row can slide under the
    // status bar (Scaffold's default insets leave the top to a topBar).
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = ::finish) { Text("Skip") }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(32.dp))
                    Text(
                        page.title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                    page.tagline?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            it.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        page.body,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Page dots
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(pages.size) { i ->
                    val active = i == pagerState.currentPage
                    Text(
                        if (active) "●" else "○",
                        color = if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        finish()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (pagerState.currentPage < pages.lastIndex) "Next" else "Get started")
            }
        }
    }
}
