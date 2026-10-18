package io.github.droidkit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.droidkit.core.common.DLog

@HiltAndroidApp
class DroidKitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DLog.enabled = BuildConfig.DEBUG
    }
}
