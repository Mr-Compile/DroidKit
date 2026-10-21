package io.github.droidkit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.droidkit.core.common.DLog
import io.github.droidkit.core.system.ProcessExitProbe

@HiltAndroidApp
class DroidKitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DLog.enabled = BuildConfig.DEBUG
        // Forensics first: capture how the PREVIOUS process ended (the system's
        // own ANR/crash record), then arm this run's uncaught-exception logger
        // and the main-looper watchdog before anything else can die quietly.
        ProcessExitProbe.snapshotPreviousExit(this)
        ProcessExitProbe.install(this)
        ProcessExitProbe.startWatchdog(this)
    }
}
