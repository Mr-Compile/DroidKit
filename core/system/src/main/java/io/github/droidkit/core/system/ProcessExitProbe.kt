package io.github.droidkit.core.system

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Forensic capture for how this process dies or freezes. Three sources:
 *
 *  - [install] — an uncaught-exception handler that persists the trace before
 *    delegating to the platform handler, so the system crash report still
 *    fires and the logcat/dropbox entry is untouched.
 *  - [snapshotPreviousExit] — API 30+: reads the system's own exit record
 *    (`ApplicationExitInfo`), which carries a frozen-thread trace for ANRs.
 *  - [startWatchdog] — a sleeping daemon thread that pings the main looper;
 *    a main thread that stays unresponsive across the interval gets its full
 *    thread dump persisted. This is the only ANR evidence on API < 30 and on
 *    freezes the OS never reports (app recovers, user force-quits).
 *
 * Everything lands in filesDir — the About screen's "Last run report" row is
 * the reader. Nothing here phones home; files stay on-device until the user
 * copies them out.
 */
object ProcessExitProbe {

    /** What the About row shows: headline for the row, full text for the dialog. */
    data class ExitReport(val headline: String, val detail: String)

    private const val CRASH_FILE = "last_crash.txt"
    private const val EXIT_FILE = "last_exit.txt"
    private const val FREEZE_FILE = "last_freeze.txt"
    private const val MAX_TRACE_CHARS = 128 * 1024
    private const val WATCHDOG_INTERVAL_MS = 5_000L
    private const val WATCHDOG_CONFIRM_MS = 2_000L

    private val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    /** Install early in Application.onCreate — before Hilt work can throw. */
    fun install(context: Context) {
        val filesDir = context.applicationContext.filesDir
        val deviceHeader = deviceHeader(context)
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                File(filesDir, CRASH_FILE).writeText(buildString {
                    append("=== DroidKit uncaught exception ===\n")
                    append(deviceHeader)
                    append("time=${ts.format(Date())} thread=${thread.name}\n\n")
                    append("${error.javaClass.name}: ${error.message}\n")
                    append(android.util.Log.getStackTraceString(error))
                })
            }
            if (previous != null) {
                previous.uncaughtException(thread, error)
            } else {
                Process.killProcess(Process.myPid())
                kotlin.system.exitProcess(10)
            }
        }
    }

    /**
     * Persists the most recent exit the OS recorded for this package — reason,
     * timestamp, description, and the frozen-thread trace when one exists
     * (ANR always, some crashes). No-op below API 30 where the record doesn't
     * exist.
     */
    fun snapshotPreviousExit(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        runCatching {
            val am = context.getSystemService(ActivityManager::class.java) ?: return
            val last = am.getHistoricalProcessExitReasons(context.packageName, 0, 1)
                ?.firstOrNull() ?: return
            val fatal = last.reason == ApplicationExitInfo.REASON_ANR ||
                last.reason == ApplicationExitInfo.REASON_CRASH ||
                last.reason == ApplicationExitInfo.REASON_CRASH_NATIVE
            val trace = runCatching {
                last.traceInputStream?.bufferedReader()?.use { it.readText() }
            }.getOrNull()?.take(MAX_TRACE_CHARS)
            File(context.filesDir, EXIT_FILE).writeText(buildString {
                append("=== Previous process exit ===\n")
                append(deviceHeader(context))
                append("reason=${reasonName(last.reason)} (${last.reason})\n")
                append("time=${ts.format(Date(last.timestamp))}\n")
                append("importance=${last.importance} pid=${last.pid} status=${last.status}\n")
                if (!last.description.isNullOrBlank()) {
                    append("description=${last.description}\n")
                }
                if (fatal) {
                    append("\n--- system trace ---\n")
                    append(trace ?: "(system captured no trace for this exit)\n")
                }
            })
        }
    }

    /**
     * Main-looper watchdog: a daemon thread posts a beat every interval; if
     * the main thread hasn't run it by the next cycle — and still hasn't after
     * a short confirm window — every thread's stack is dumped to
     * [FREEZE_FILE]. Re-dumps each interval while frozen so the file that
     * survives an ANR kill describes the state at kill time.
     */
    fun startWatchdog(context: Context) {
        val app = context.applicationContext
        val mainHandler = Handler(Looper.getMainLooper())
        val beat = AtomicBoolean(true)
        thread(name = "dk-main-watchdog", isDaemon = true) {
            while (true) {
                beat.set(false)
                runCatching { mainHandler.post { beat.set(true) } }
                try {
                    Thread.sleep(WATCHDOG_INTERVAL_MS)
                } catch (e: InterruptedException) {
                    return@thread
                }
                // Confirm window: wake-from-doze can leave the beat queued even
                // though the looper is healthy — give it a moment before dumping.
                if (!beat.get()) {
                    try {
                        Thread.sleep(WATCHDOG_CONFIRM_MS)
                    } catch (e: InterruptedException) {
                        return@thread
                    }
                }
                if (!beat.get()) {
                    runCatching {
                        File(app.filesDir, FREEZE_FILE).writeText(buildString {
                            append("=== DroidKit main-thread freeze ===\n")
                            append(deviceHeader(app))
                            append("time=${ts.format(Date())} ")
                            append("main unresponsive ≥ ${WATCHDOG_INTERVAL_MS + WATCHDOG_CONFIRM_MS}ms\n\n")
                            append(dumpAllStacks())
                        })
                    }
                }
            }
        }
    }

    /**
     * Newest-capture-first combined report; null when nothing was recorded.
     * The headline describes the newest capture — for the exit file, the
     * recorded reason (a benign "closed by user" stays calm on purpose).
     */
    fun report(context: Context): ExitReport? {
        val dir = context.filesDir
        val files = listOf(
            File(dir, CRASH_FILE) to "Crashed (uncaught exception)",
            File(dir, EXIT_FILE) to null,
            File(dir, FREEZE_FILE) to "Main thread froze",
        ).filter { it.first.exists() }
            .sortedByDescending { it.first.lastModified() }
        if (files.isEmpty()) return null
        val detail = files.joinToString("\n\n") { (f, _) ->
            runCatching { f.readText() }.getOrNull()?.take(MAX_TRACE_CHARS).orEmpty()
        }
        val (newestFile, label) = files.first()
        val headline = label ?: runCatching {
            newestFile.readText().lineSequence()
                .firstOrNull { it.startsWith("reason=") }
                ?.let { "Last exit: ${it.removePrefix("reason=").substringBeforeLast(" (")}" }
        }.getOrNull() ?: "Exit recorded"
        return ExitReport(headline, detail)
    }

    /** Short row subtitle — honest "nothing recorded" when clean. */
    fun lastEventSummary(context: Context): String =
        report(context)?.headline?.let { "$it — tap to view" }
            ?: "No crashes or freezes recorded"

    private fun deviceHeader(context: Context): String {
        val version = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
        return "app=$version device=${Build.MANUFACTURER} ${Build.MODEL} " +
            "sdk=${Build.VERSION.SDK_INT}\n"
    }

    private fun reasonName(reason: Int): String = when (reason) {
        ApplicationExitInfo.REASON_ANR -> "not responding (ANR)"
        ApplicationExitInfo.REASON_CRASH -> "crash (exception)"
        ApplicationExitInfo.REASON_CRASH_NATIVE -> "crash (native)"
        ApplicationExitInfo.REASON_EXIT_SELF -> "closed itself"
        ApplicationExitInfo.REASON_USER_REQUESTED -> "closed by user"
        ApplicationExitInfo.REASON_USER_STOPPED -> "force-stopped"
        ApplicationExitInfo.REASON_LOW_MEMORY -> "killed — low memory"
        ApplicationExitInfo.REASON_SIGNALED -> "killed by signal"
        ApplicationExitInfo.REASON_OTHER -> "ended (other)"
        else -> "unknown reason"
    }

    private fun dumpAllStacks(): String = buildString {
        val mainId = Looper.getMainLooper().thread.id
        Thread.getAllStackTraces()
            .toList()
            .sortedByDescending { (t, _) -> t.id == mainId }
            .forEach { (thread, stack) ->
                append("\n----- ${thread.name} (id=${thread.id}) -----\n")
                stack.forEach { frame -> append("    at $frame\n") }
            }
    }.take(MAX_TRACE_CHARS)
}
