package io.github.droidkit.feature.nfc

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.common.DLog
import io.github.droidkit.core.database.NfcTagScanDao
import io.github.droidkit.core.database.NfcTagScanEntity
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NfcViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanDao: NfcTagScanDao,
) : ViewModel() {

    /** Reader callbacks arrive off the main executor — persists them safely. */
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val history: StateFlow<List<NfcTagScanEntity>> =
        scanDao.observeRecent()
            .stateIn(ioScope, SharingStarted.Eagerly, emptyList())

    enum class Verdict { PASS, WARNING, UNSUPPORTED }

    data class DoctorCheck(val name: String, val verdict: Verdict, val detail: String)

    /** Mutating op waiting for the next presented tag. */
    sealed interface ArmedAction {
        data class Write(val message: NdefMessage, val previewBytes: Int) : ArmedAction
        data object Erase : ArmedAction

        /** Format a non-NDEF tag to NDEF with empty content. */
        data object Format : ArmedAction
    }

    sealed interface ActionResult {
        data class Success(val text: String) : ActionResult
        data class Failed(val text: String) : ActionResult
    }

    private val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

    val nfcSupported: Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
    val adapterPresent: Boolean get() = nfcAdapter != null

    private val _adapterEnabled = MutableStateFlow(nfcAdapter?.isEnabled == true)
    val adapterEnabled: StateFlow<Boolean> = _adapterEnabled.asStateFlow()

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    private val _tag = MutableStateFlow<NfcTagInfo?>(null)
    val tag: StateFlow<NfcTagInfo?> = _tag.asStateFlow()

    private val _techSections = MutableStateFlow<List<TechSection>>(emptyList())
    val techSections: StateFlow<List<TechSection>> = _techSections.asStateFlow()

    /** Monotonic counter bumped per scanned tag — drives haptics/UI events. */
    private val _scanSeq = MutableStateFlow(0)
    val scanSeq: StateFlow<Int> = _scanSeq.asStateFlow()

    private val _armed = MutableStateFlow<ArmedAction?>(null)
    val armed: StateFlow<ArmedAction?> = _armed.asStateFlow()

    private val _actionResult = MutableStateFlow<ActionResult?>(null)
    val actionResult: StateFlow<ActionResult?> = _actionResult.asStateFlow()

    private val readerCallback = NfcAdapter.ReaderCallback { tag ->
        when (val action = _armed.value) {
            is ArmedAction.Write -> {
                _armed.value = null
                writeMessage(tag, action.message)
            }
            ArmedAction.Erase -> {
                _armed.value = null
                eraseTag(tag)
            }
            ArmedAction.Format -> {
                _armed.value = null
                formatTag(tag)
            }
            null -> {
                val parsed = runCatching { NfcTagParser.parse(tag) }
                    .onFailure { DLog.w(TAG, "tag parse failed", it) }
                    .getOrNull()
                _tag.value = parsed
                _techSections.value = runCatching { NfcTechProbe.probe(tag) }
                    .onFailure { DLog.w(TAG, "tech probe failed", it) }
                    .getOrDefault(emptyList())
                _scanSeq.value += 1
                parsed?.let { recordScan(it) }
            }
        }
    }

    fun refreshAdapterState() {
        _adapterEnabled.value = nfcAdapter?.isEnabled == true
    }

    fun startScan(activity: Activity): Boolean {
        val adapter = nfcAdapter ?: return false
        runCatching {
            adapter.enableReaderMode(activity, readerCallback, READER_FLAGS, null)
        }.onSuccess { _scanning.value = true; refreshAdapterState() }
        return _scanning.value
    }

    fun stopScan(activity: Activity) {
        runCatching { nfcAdapter?.disableReaderMode(activity) }
        _scanning.value = false
    }

    fun armWrite(message: NdefMessage, previewBytes: Int) {
        _actionResult.value = null
        _armed.value = ArmedAction.Write(message, previewBytes)
    }

    fun armErase() {
        _actionResult.value = null
        _armed.value = ArmedAction.Erase
    }

    fun armFormat() {
        _actionResult.value = null
        _armed.value = ArmedAction.Format
    }

    /**
     * Arms a write carrying the scanned tag's own NDEF payload. Copies record
     * content only — tag UID, locked regions and secure-element data are never
     * copied.
     */
    fun armDuplicate(): Boolean {
        val message = _tag.value?.rawMessage ?: return false
        _actionResult.value = null
        _armed.value = ArmedAction.Write(message, message.toByteArray().size)
        return true
    }

    fun disarm() {
        _armed.value = null
    }

    fun setScanning(enabled: Boolean) {
        _scanning.value = enabled
    }

    fun clearHistory() {
        ioScope.launch { scanDao.deleteAll() }
    }

    /** Logs a scan locally — only the last 4 bytes of the tag ID are kept. */
    private fun recordScan(info: NfcTagInfo) {
        ioScope.launch {
            runCatching {
                scanDao.insert(
                    NfcTagScanEntity(
                        id = UUID.randomUUID().toString(),
                        scannedAtEpochMs = System.currentTimeMillis(),
                        tagIdTail = info.idHex?.takeLast(8),
                        techList = info.techList.joinToString(","),
                        recordSummary = info.records
                            .mapNotNull { it.decoded ?: it.tnfName }
                            .joinToString(" · ")
                            .take(200)
                            .ifBlank { null },
                    ),
                )
            }
        }
    }

    private fun writeMessage(tag: Tag, message: NdefMessage) {
        val result = runCatching {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                try {
                    check(ndef.isWritable) { "Tag is read-only" }
                    val size = message.toByteArray().size
                    check(size <= ndef.maxSize) { "Message ${size}B exceeds tag capacity ${ndef.maxSize}B" }
                    ndef.writeNdefMessage(message)
                    "Wrote ${size}B to tag"
                } finally {
                    runCatching { ndef.close() }
                }
            } else {
                val formatable = NdefFormatable.get(tag)
                    ?: throw IllegalStateException("Tag is not NDEF-formatable")
                formatable.connect()
                try {
                    formatable.format(message)
                    "Formatted tag and wrote message"
                } finally {
                    runCatching { formatable.close() }
                }
            }
        }
        _actionResult.value = result.fold(
            onSuccess = { ActionResult.Success(it) },
            onFailure = { ActionResult.Failed(it.message ?: it.javaClass.simpleName) },
        )
    }

    private fun eraseTag(tag: Tag) {
        val empty = NfcTagParser.emptyMessage()
        val result = runCatching {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                try {
                    check(ndef.isWritable) { "Tag is read-only" }
                    ndef.writeNdefMessage(empty)
                    "Tag content erased (empty NDEF written)"
                } finally {
                    runCatching { ndef.close() }
                }
            } else {
                val formatable = NdefFormatable.get(tag)
                    ?: throw IllegalStateException("Tag is not NDEF-formatable")
                formatable.connect()
                try {
                    formatable.format(empty)
                    "Tag formatted to empty NDEF"
                } finally {
                    runCatching { formatable.close() }
                }
            }
        }
        _actionResult.value = result.fold(
            onSuccess = { ActionResult.Success(it) },
            onFailure = { ActionResult.Failed(it.message ?: it.javaClass.simpleName) },
        )
    }

    private fun formatTag(tag: Tag) {
        val result = runCatching {
            val formatable = NdefFormatable.get(tag)
                ?: throw IllegalStateException("Tag is not NDEF-formatable")
            formatable.connect()
            try {
                formatable.format(NfcTagParser.emptyMessage())
                "Tag formatted to NDEF"
            } finally {
                runCatching { formatable.close() }
            }
        }
        _actionResult.value = result.fold(
            onSuccess = { ActionResult.Success(it) },
            onFailure = { ActionResult.Failed(it.message ?: it.javaClass.simpleName) },
        )
    }

    /** Tag Doctor — per-check verdicts; never claims tags are clonable. */
    fun doctorChecks(info: NfcTagInfo): List<DoctorCheck> = buildList {
        add(DoctorCheck("Readable", Verdict.PASS, "Tag responded to a scan"))
        add(
            DoctorCheck(
                "NDEF capable",
                if (info.ndefCapable) Verdict.PASS else Verdict.WARNING,
                if (info.ndefCapable) "NDEF technology present" else "No NDEF support${if (info.formatable) " — formatable" else ""}",
            ),
        )
        add(
            DoctorCheck(
                "Writable",
                when (info.ndefWritable) {
                    true -> Verdict.PASS
                    false -> Verdict.WARNING
                    null -> Verdict.UNSUPPORTED
                },
                when (info.ndefWritable) {
                    true -> "Tag accepts NDEF writes"
                    false -> "Tag is read-only or locked"
                    null -> "Not an NDEF tag"
                },
            ),
        )
        add(
            DoctorCheck(
                "Capacity",
                if (info.ndefMaxSize != null) Verdict.PASS else Verdict.UNSUPPORTED,
                info.ndefMaxSize?.let { "$it bytes" } ?: "Not exposed",
            ),
        )
        add(
            DoctorCheck(
                "Records",
                if (info.parseError == null) Verdict.PASS else Verdict.WARNING,
                if (info.parseError == null) "${info.records.size} record(s) parsed" else "Parse error: ${info.parseError}",
            ),
        )
        add(
            DoctorCheck(
                "Copyable",
                if (info.rawMessage != null) Verdict.PASS else Verdict.UNSUPPORTED,
                if (info.rawMessage != null) {
                    "NDEF content can be duplicated to another tag"
                } else {
                    "No NDEF message to copy"
                },
            ),
        )
        if (info.techList.any { it == "MifareClassic" }) {
            add(
                DoctorCheck(
                    "Protected technology",
                    Verdict.WARNING,
                    "MifareClassic present — protected sectors may be unreadable; secure credentials are never clonable",
                ),
            )
        }
        if (info.techList.all { it == "IsoDep" || it == "NfcB" || it == "NfcA" }) {
            add(
                DoctorCheck(
                    "Secure-element hint",
                    Verdict.WARNING,
                    "Only low-level techs detected — this may be a bank/transit/access card; contents are intentionally unreachable",
                ),
            )
        }
    }

    fun overallVerdict(checks: List<DoctorCheck>): Verdict = when {
        checks.any { it.verdict == Verdict.UNSUPPORTED } -> Verdict.UNSUPPORTED
        checks.any { it.verdict == Verdict.WARNING } -> Verdict.WARNING
        else -> Verdict.PASS
    }

    companion object {
        private const val TAG = "NfcViewModel"
        private const val READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NFC_BARCODE
    }
}

/** Plain-text tag report for copy/share — no secrets, no key material. */
internal fun buildTagReport(
    info: NfcTagInfo,
    checks: List<NfcViewModel.DoctorCheck>,
    sections: List<TechSection>,
): String = buildString {
    append("DroidKit NFC tag report\n")
    append("Tag ID: ").append(info.idHex ?: "—").append('\n')
    append("Technologies: ").append(info.techList.joinToString()).append('\n')
    append("NDEF capable: ").append(info.ndefCapable).append('\n')
    append("Writable: ").append(info.ndefWritable?.toString() ?: "unknown").append('\n')
    info.ndefMaxSize?.let { append("Capacity: ").append(it).append(" bytes\n") }
    info.ndefType?.let { append("NDEF type: ").append(it).append('\n') }
    if (sections.isNotEmpty()) {
        append("Technology details:\n")
        sections.forEach { s ->
            append("  ").append(s.tech).append('\n')
            s.fields.forEach { (k, v) -> append("    ").append(k).append(": ").append(v).append('\n') }
        }
    }
    if (info.records.isNotEmpty()) {
        append("NDEF records:\n")
        info.records.forEachIndexed { i, r ->
            append("  #").append(i + 1).append(" ").append(r.tnfName)
                .append(" ").append(r.typeHex)
            r.decoded?.let { append(" — ").append(it) }
            append('\n')
        }
    }
    append("Tag Doctor: ")
    checks.forEach { append(it.name).append('=').append(it.verdict.name).append("  ") }
    append('\n')
}
