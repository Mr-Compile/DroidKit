package io.github.droidkit.feature.deviceinfo

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.droidkit.core.system.formatBytes
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageLabScreen() {
    val context = LocalContext.current

    data class Volume(val name: String, val path: String, val state: String)

    val volumes = buildList {
        add(Volume("Internal (data)", Environment.getDataDirectory().absolutePath, "mounted"))
        context.getExternalFilesDirs(null).forEachIndexed { i, dir ->
            if (dir != null) {
                add(Volume("External #$i", dir.absolutePath, Environment.getExternalStorageState(dir)))
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Storage Lab") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Free space is filesystem-visible space — not NAND health.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            items(volumes.size) { i ->
                val v = volumes[i]
                val stat = runCatching { StatFs(v.path) }.getOrNull()
                val free = stat?.availableBytes
                val total = stat?.totalBytes
                SectionCard(title = v.name) {
                    InfoRow("Path", v.path)
                    InfoRow("State", v.state)
                    InfoRow("Total", total?.let { formatBytes(it) })
                    InfoRow("Free", free?.let { formatBytes(it) })
                    InfoRow("Used", if (total != null && free != null) formatBytes(total - free) else null)
                    if (total != null && free != null && total > 0) {
                        val pctFree = free * 100 / total
                        if (pctFree < 10) {
                            Text(
                                "Low free space — under 10%",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
            item {
                SectionCard(title = "DroidKit directories") {
                    InfoRow("App files", context.filesDir.absolutePath)
                    InfoRow("Cache", context.cacheDir.absolutePath)
                }
            }
        }
    }
}
