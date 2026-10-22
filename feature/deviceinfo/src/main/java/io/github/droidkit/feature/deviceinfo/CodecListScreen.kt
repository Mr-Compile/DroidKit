package io.github.droidkit.feature.deviceinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import io.github.droidkit.core.ui.components.DroidKitCard
import androidx.compose.material3.ExperimentalMaterial3Api
import io.github.droidkit.core.ui.components.DroidKitFilterChip
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.droidkit.core.ui.adaptive.contentWidthCap
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

private val ROLE_FILTERS = listOf(
    "all" to "All",
    "decoders" to "Decoders",
    "encoders" to "Encoders",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CodecListScreen(
    onBack: () -> Unit,
    viewModel: DeviceInfoViewModel = hiltViewModel(),
) {
    val codecs = viewModel.probe.codecs
    var role by rememberSaveable { mutableStateOf("all") }
    var query by rememberSaveable { mutableStateOf("") }
    val shown = DeviceInfoExtras.filterCodecs(codecs, role, query)

    Scaffold(topBar = { DroidKitTopAppBar("Codec inventory", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().contentWidthCap(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "Every decoder/encoder registered with MediaCodecList, with its MIME " +
                        "types and acceleration flags. Software codecs work on any device; " +
                        "hardware codecs are vendor-supplied.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            item {
                DroidKitTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Filter name or MIME type") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ROLE_FILTERS.forEach { (key, label) ->
                        DroidKitFilterChip(
                            selected = role == key,
                            onClick = { role = key },
                            label = { Text(label) },
                        )
                    }
                }
            }
            item {
                Text(
                    "${shown.size} of ${codecs.size} codecs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(shown, key = { it.name }) { codec ->
                DroidKitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(codec.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            listOf(
                                DeviceInfoExtras.codecRole(codec.isEncoder),
                                DeviceInfoExtras.codecSourceName(codec.name),
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        codec.types.forEach { type ->
                            Text(
                                type,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        DeviceInfoExtras.codecFlags(codec).takeIf { it.isNotEmpty() }?.let { flags ->
                            Text(
                                flags.joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            if (codecs.isEmpty()) {
                item {
                    Text(
                        "No codecs reported — MediaCodecList failed on this device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
