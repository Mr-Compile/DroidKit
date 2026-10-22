package io.github.droidkit.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.ui.components.DroidKitCard
import io.github.droidkit.core.ui.components.SectionHeader

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    DroidKitCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(title)
            content()
        }
    }
}
