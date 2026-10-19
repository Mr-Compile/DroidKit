package io.github.droidkit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

/**
 * Full-bleed splash shown while the app initializes (DataStore read +
 * minimum branding time). The artwork is a single baked asset — crop-fill
 * keeps the centered logo correct on any aspect ratio.
 */
@Composable
fun SplashScreen() {
    Image(
        painter = painterResource(R.drawable.splash_loading),
        contentDescription = "DroidKit is initializing",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}
