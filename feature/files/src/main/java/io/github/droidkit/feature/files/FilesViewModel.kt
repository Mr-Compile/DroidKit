package io.github.droidkit.feature.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.privileges.ElevatedAction
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ElevatedResult
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shared-storage browser backed by the elevated shell identity — the same
 * mechanism file-manager apps use through Shizuku to reach `Android/data`
 * on Android 11+. Runs through whichever provider is active (root → ADB).
 */
@HiltViewModel
class FilesViewModel @Inject constructor(
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    data class Entry(
        val name: String,
        val path: String,
        val isDir: Boolean,
        val size: Long,
        val modifiedEpochSec: Long,
    )

    sealed interface Event {
        data class Message(val text: String) : Event
    }

    private val _path = MutableStateFlow(START_PATH)
    val path: StateFlow<String> = _path.asStateFlow()

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    private val _providerReady = MutableStateFlow<Boolean?>(null)
    val providerReady: StateFlow<Boolean?> = _providerReady.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _copying = MutableStateFlow<String?>(null)
    val copying: StateFlow<String?> = _copying.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    init {
        refreshProvider()
    }

    fun refreshProvider() {
        viewModelScope.launch {
            val ready = elevatedOps.activeProvider() != null
            _providerReady.value = ready
            if (ready) open(_path.value)
        }
    }

    /** Open a directory — only validated shared-storage paths reach the action. */
    fun open(path: String) {
        val target = runCatching { ElevatedAction.requireStoragePath(path) }
            .getOrElse {
                emit("That location is outside shared storage.")
                return
            }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val r = elevatedOps.execute(ElevatedAction.ListStorageDir(target))
            if (r.ok) {
                _path.value = target
                _entries.value = parseEntries(target, r.stdout)
            } else {
                _error.value = "Can't open $target — ${detail(r)}"
            }
            _loading.value = false
        }
    }

    fun up() {
        val p = _path.value
        if (p == "/sdcard" || p == "/storage/emulated/0") {
            open(p) // already at the storage floor — refresh
            return
        }
        open(p.substringBeforeLast('/').ifEmpty { "/sdcard" })
    }

    /** Copy [entry] out to `/sdcard/Download/DroidKit/` — the visible destination. */
    fun copyOut(entry: Entry) {
        if (_copying.value != null) return
        val action = runCatching { ElevatedAction.CopyToDownload(entry.path) }
            .getOrElse {
                emit("Can't copy — the name contains unsupported characters.")
                return
            }
        viewModelScope.launch {
            _copying.value = entry.path
            val r = elevatedOps.execute(action, timeoutMs = COPY_TIMEOUT_MS)
            if (r.ok) {
                emit("Copied to ${ElevatedAction.CopyToDownload.COPY_OUT_DIR}/${entry.name}")
            } else {
                emit("Copy failed — ${detail(r)}")
            }
            _copying.value = null
        }
    }

    fun consumeEvent(event: Event) {
        _events.value = _events.value - event
    }

    private fun detail(r: ElevatedResult): String =
        (r.stderr.ifEmpty { r.stdout }).lineSequence()
            .firstOrNull { it.isNotBlank() }?.trim()?.take(120)
            ?: "exit ${r.exitCode}"

    private fun emit(text: String) {
        _events.value = _events.value + Event.Message(text)
    }

    private fun parseEntries(dir: String, stdout: String): List<Entry> =
        stdout.lineSequence()
            .mapNotNull { line ->
                val p = line.split('|', limit = 4)
                if (p.size != 4) return@mapNotNull null
                val name = p[3].removePrefix("./")
                if (name.isEmpty()) return@mapNotNull null
                Entry(
                    name = name,
                    path = "$dir/$name",
                    isDir = p[0].contains("directory"),
                    size = p[1].toLongOrNull() ?: 0L,
                    modifiedEpochSec = p[2].toLongOrNull() ?: 0L,
                )
            }
            .sortedWith(compareByDescending<Entry> { it.isDir }.thenBy { it.name.lowercase() })
            .toList()

    companion object {
        const val START_PATH = "/sdcard/Android/data"
        private const val COPY_TIMEOUT_MS = 120_000L
    }
}
