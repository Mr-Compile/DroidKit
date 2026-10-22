package io.github.droidkit.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import io.github.droidkit.core.ui.components.DroidKitButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.droidkit.core.datastore.SettingsRepository
import io.github.droidkit.core.ui.theme.DroidKitMark
import io.github.droidkit.core.ui.theme.DroidKitTheme
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
    val c = DroidKitTheme.colors
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // mockup .obw.a/.b — radial brand washes bleeding off the corners.
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(c.brandPrimary.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(-10f, -10f),
                        radius = size.minDimension * 0.9f,
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(c.brandPrimary.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width + 10f, size.height + 10f),
                        radius = size.minDimension * 0.9f,
                    ),
                )
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
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
                        // mockup .obic — 96dp gradient tile with brand hairline.
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(c.tileGradient)
                                .border(1.dp, c.lineBrand, RoundedCornerShape(26.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                page.icon,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = c.brandPrimary,
                            )
                        }
                        page.tagline?.let {
                            Text(
                                it.uppercase(),
                                style = DroidKitTheme.extraTypography.microLabel.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 3.sp,
                                ),
                                textAlign = TextAlign.Center,
                                color = c.brandPrimary,
                                modifier = Modifier.padding(top = 18.dp),
                            )
                        }
                        Text(
                            page.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp,
                            ),
                            textAlign = TextAlign.Center,
                            color = c.textPrimary,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                        Text(
                            page.body,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = c.textSecondary,
                            modifier = Modifier.padding(top = 14.dp).widthIn(max = 300.dp),
                        )
                    }
                }

                // mockup .obdot — raise dots; active is gradient + glow.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    repeat(pages.size) { i ->
                        val active = i == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 5.dp)
                                .size(6.dp)
                                .then(
                                    if (active) {
                                        Modifier.shadow(
                                            elevation = 8.dp,
                                            shape = CircleShape,
                                            clip = false,
                                            ambientColor = c.brandGlow,
                                            spotColor = c.brandGlow,
                                        )
                                    } else {
                                        Modifier
                                    },
                                )
                                .clip(CircleShape)
                                .background(
                                    if (active) c.brandGradient
                                    else SolidColor(c.raise),
                                ),
                        )
                    }
                }

                DroidKitButton(
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
}
