package io.github.droidkit.feature.deviceinfo

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.database.StorageSnapshotDao
import io.github.droidkit.core.database.StorageSnapshotEntity
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class StorageLabViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snapshotDao: StorageSnapshotDao,
) : ViewModel() {

    val snapshots: StateFlow<List<StorageSnapshotEntity>> =
        snapshotDao.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Live volume readings — re-read each time [refresh] runs. */
    private val _live = MutableStateFlow<List<VolumeStat>>(emptyList())
    val live = _live.asStateFlow()

    private val _diff = MutableStateFlow<Pair<StorageSnapshotEntity, List<VolumeDelta>>?>(null)
    val diff = _diff.asStateFlow()

    init { refresh() }

    fun refresh() {
        _live.value = readVolumes()
    }

    fun saveSnapshot(label: String?) {
        viewModelScope.launch {
            snapshotDao.insert(
                StorageSnapshotEntity(
                    id = UUID.randomUUID().toString(),
                    capturedAtEpochMs = System.currentTimeMillis(),
                    label = label?.ifBlank { null },
                    volumesData = StorageSnapshotCodec.encode(readVolumes()),
                ),
            )
        }
    }

    /** Computes per-volume delta between [before] and the newest snapshot... or [after]. */
    fun compare(beforeId: String, afterId: String) {
        viewModelScope.launch {
            val b = snapshotDao.getById(beforeId) ?: return@launch
            val a = snapshotDao.getById(afterId) ?: return@launch
            _diff.value = a to StorageSnapshotCodec.diff(b, a)
        }
    }

    fun clearDiff() {
        _diff.value = null
    }

    fun deleteSnapshot(id: String) {
        viewModelScope.launch { snapshotDao.deleteById(id) }
    }

    private fun readVolumes(): List<VolumeStat> {
        val volumes = mutableListOf<Pair<String, String>>()
        volumes += "Internal (data)" to Environment.getDataDirectory().absolutePath
        context.getExternalFilesDirs(null).forEachIndexed { i, dir ->
            if (dir != null) volumes += "External #$i" to dir.absolutePath
        }
        return volumes.map { (name, path) ->
            val stat = runCatching { StatFs(path) }.getOrNull()
            VolumeStat(
                name = name,
                path = path,
                state = runCatching {
                    Environment.getExternalStorageState(java.io.File(path))
                }.getOrDefault("unknown"),
                totalBytes = stat?.totalBytes ?: 0,
                freeBytes = stat?.availableBytes ?: 0,
            )
        }
    }
}
