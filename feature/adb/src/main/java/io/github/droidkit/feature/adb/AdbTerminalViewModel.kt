package io.github.droidkit.feature.adb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.privileges.ElevatedOps
import io.github.droidkit.core.privileges.ProviderKind
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AdbTerminalViewModel @Inject constructor(
    private val elevatedOps: ElevatedOps,
) : ViewModel() {

    /** One executed command and its result — newest last. */
    data class Entry(
        val command: String,
        val provider: ProviderKind,
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
    )

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun run(command: String) {
        val cmd = command.trim()
        if (cmd.isEmpty() || _busy.value) return
        viewModelScope.launch {
            _busy.value = true
            val r = elevatedOps.runRawCommand(cmd)
            _entries.value = _entries.value + Entry(
                command = cmd,
                provider = r.provider,
                exitCode = r.exitCode,
                stdout = r.stdout,
                stderr = r.stderr,
            )
            _busy.value = false
        }
    }

    fun clear() {
        _entries.value = emptyList()
    }

    /** Scrollback as plain text for copy/share. */
    fun scrollbackText(): String = _entries.value.joinToString("\n\n") { e ->
        buildString {
            append("$ ${e.command}\n")
            if (e.stdout.isNotBlank()) append(e.stdout.trimEnd()).append('\n')
            if (e.stderr.isNotBlank()) append("stderr: ${e.stderr.trimEnd()}\n")
            append("[${e.provider.name} · exit ${e.exitCode}]")
        }
    }
}
