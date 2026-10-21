package io.github.droidkit.feature.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.privileges.ElevatedAction
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ElevatedResult
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Shared-storage browser backed by the elevated shell identity — the same
 * mechanism file-manager apps use through Shizuku to reach `Android/data`
 * on Android 11+. Runs through whichever provider is active (root → ADB).
 */
@HiltViewModel
class FilesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    data class Entry(
        val name: String,
        val path: String,
        val isDir: Boolean,
        val size: Long,
        val modifiedEpochSec: Long,
    )

    /** Text preview of a file head — [binary] marks content that is not text. */
    data class Preview(
        val name: String,
        val text: String,
        val binary: Boolean,
        val truncated: Boolean,
    )

    /** One node of the SAF tree listing. */
    data class SafEntry(
        val name: String,
        val uri: Uri,
        val isDir: Boolean,
        val size: Long,
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

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()

    private val _preview = MutableStateFlow<Preview?>(null)
    val preview: StateFlow<Preview?> = _preview.asStateFlow()

    /** Non-null while a name search is active — results replace the dir list. */
    private val _search = MutableStateFlow<List<Entry>?>(null)
    val search: StateFlow<List<Entry>?> = _search.asStateFlow()

    /** SAF tree browsing state — no elevated provider needed. */
    private val _safRoot = MutableStateFlow<Uri?>(null)
    val safRoot: StateFlow<Uri?> = _safRoot.asStateFlow()

    private val _safEntries = MutableStateFlow<List<SafEntry>>(emptyList())
    val safEntries: StateFlow<List<SafEntry>> = _safEntries.asStateFlow()

    private val _safCanUp = MutableStateFlow(false)
    val safCanUp: StateFlow<Boolean> = _safCanUp.asStateFlow()

    private val safStack = ArrayDeque<Uri>()

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
                _search.value = null
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

    fun toggleSelect(entry: Entry) {
        _selected.value = _selected.value.let {
            if (entry.path in it) it - entry.path else it + entry.path
        }
    }

    fun clearSelection() {
        _selected.value = emptySet()
    }

    /** Copy every selected entry out to Downloads — sequential, one summary. */
    fun copySelected() {
        val targets = _selected.value.toList()
        if (targets.isEmpty() || _copying.value != null) return
        viewModelScope.launch {
            _copying.value = "batch"
            val results = targets.map { path ->
                val ok = runCatching { ElevatedAction.CopyToDownload(path) }
                    .map { elevatedOps.execute(it, timeoutMs = COPY_TIMEOUT_MS).ok }
                    .getOrDefault(false)
                path.substringAfterLast('/') to ok
            }
            emit(FilesInfo.copySummary(results))
            _selected.value = emptySet()
            _copying.value = null
        }
    }

    /** Read the head of a file for a text preview — bounded, binary-aware. */
    fun previewFile(entry: Entry) {
        if (entry.isDir) return
        viewModelScope.launch {
            _preview.value = Preview(entry.name, "Loading…", binary = false, truncated = false)
            val r = elevatedOps.execute(ElevatedAction.ReadFileHead(entry.path, PREVIEW_BYTES))
            _preview.value = when {
                !r.ok -> Preview(entry.name, "Can't read — ${detail(r)}", binary = false, truncated = false)
                FilesInfo.isBinaryText(r.stdout) ->
                    Preview(entry.name, "", binary = true, truncated = false)
                else -> Preview(
                    entry.name,
                    r.stdout.take(PREVIEW_CHARS),
                    binary = false,
                    truncated = r.stdout.length >= PREVIEW_CHARS || entry.size > PREVIEW_BYTES,
                )
            }
        }
    }

    fun clearPreview() {
        _preview.value = null
    }

    /** Search entry names under the current directory (`find -iname`). */
    fun runSearch(query: String) {
        val pattern = query.trim().removePrefix("*").removeSuffix("*")
        if (pattern.isBlank()) {
            _search.value = null
            return
        }
        viewModelScope.launch {
            _loading.value = true
            val r = runCatching {
                elevatedOps.execute(
                    ElevatedAction.SearchStorage(_path.value, pattern),
                    timeoutMs = 60_000,
                )
            }.getOrNull()
            _search.value = when {
                r == null || !r.ok -> {
                    emit("Search failed — ${r?.let { detail(it) } ?: "invalid pattern"}")
                    null
                }
                else -> r.stdout.lineSequence()
                    .mapNotNull { FilesInfo.parseStatLine(it) }
                    .map { (full, size, dir) ->
                        Entry(full.substringAfterLast('/'), full, dir, size, 0L)
                    }
                    .toList()
            }
            _loading.value = false
        }
    }

    fun clearSearch() {
        _search.value = null
    }

    // --- SAF tree browsing (standard API — no elevated provider needed) ---

    fun safGrant(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        safStack.clear()
        _safCanUp.value = false
        _safRoot.value = uri
        safList(uri)
    }

    fun safOpen(entry: SafEntry) {
        if (!entry.isDir) return
        _safRoot.value?.let { safStack.addLast(it) }
        _safCanUp.value = true
        _safRoot.value = entry.uri
        safList(entry.uri)
    }

    fun safUp() {
        val parent = safStack.removeLastOrNull() ?: return
        _safCanUp.value = safStack.isNotEmpty()
        _safRoot.value = parent
        safList(parent)
    }

    fun safClose() {
        safStack.clear()
        _safCanUp.value = false
        _safRoot.value = null
        _safEntries.value = emptyList()
    }

    private fun safList(uri: Uri) {
        viewModelScope.launch {
            _safEntries.value = withContext(Dispatchers.IO) {
                runCatching {
                    DocumentFile.fromTreeUri(context, uri)
                        ?.listFiles()
                        ?.map {
                            SafEntry(
                                name = it.name ?: it.uri.lastPathSegment ?: "?",
                                uri = it.uri,
                                isDir = it.isDirectory,
                                size = it.length().coerceAtLeast(0),
                            )
                        }
                        ?.sortedWith(
                            compareByDescending<SafEntry> { it.isDir }
                                .thenBy { it.name.lowercase() },
                        )
                        ?: emptyList()
                }.getOrDefault(emptyList())
            }
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
        private const val PREVIEW_BYTES = 8192
        private const val PREVIEW_CHARS = 4000
    }
}
