package io.github.droidkit

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.droidkit.core.ui.theme.DroidKitBrand
import io.github.droidkit.core.ui.theme.DroidKitMark

private val Wash = Color(0x243CE88A)

/**
 * Splash shown while the app initializes (DataStore read + minimum branding
 * time). Candidate-9 direction: near-black ink, corner washes, the glass
 * Android head over a pulsing green halo, two-tone wordmark, and a live
 * loading ring — drawn in Compose so it stays crisp on any aspect ratio.
 * Mood reference: docs/03_design_system/branding/candidates/candidate-09-splash.svg.
 */
@Composable
fun SplashScreen() {
    val glowPulse by rememberInfiniteTransition(label = "splashGlow").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DroidKitBrand.SplashInk)
            .semantics(mergeDescendants = true) {
                contentDescription = "DroidKit is initializing"
            },
    ) {
        // Corner washes bleeding in from off-screen.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-230).dp, y = (-250).dp)
                .size(430.dp)
                .background(Brush.radialGradient(listOf(Wash, Color.Transparent))),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 250.dp, y = 270.dp)
                .size(460.dp)
                .background(Brush.radialGradient(listOf(Wash, Color.Transparent))),
        )
        // Faint watermark head, bottom-right.
        Image(
            imageVector = DroidKitMark,
            contentDescription = null,
            alpha = 0.05f,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 70.dp, y = 90.dp)
                .size(300.dp),
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .alpha(glowPulse)
                        .background(
                            Brush.radialGradient(
                                listOf(DroidKitBrand.Glow, Color.Transparent),
                            ),
                        ),
                )
                Image(
                    imageVector = DroidKitMark,
                    contentDescription = null,
                    modifier = Modifier.size(140.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFFF2F6F5))) { append("Droid") }
                    withStyle(SpanStyle(color = DroidKitBrand.Green)) { append("Kit") }
                },
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "DEEPER INSIGHTS. BETTER DEVICES.",
                color = Color(0xFF8FA5A7),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.5.sp,
            )
            Spacer(Modifier.height(52.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = DroidKitBrand.Green,
                trackColor = Color(0xFF1C2A2C),
                strokeWidth = 4.dp,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Initializing DroidKit…",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8FA5A7),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(52.dp)
                    .background(DroidKitBrand.gradient),
            )
            Spacer(Modifier.width(14.dp))
            Column {
                listOf("YOUR ANDROID", "TOOLKIT", "COMPANION").forEach { line ->
                    Text(
                        text = line,
                        color = Color(0xFF7E9497),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 3.sp,
                    )
                }
            }
        }
    }
}
