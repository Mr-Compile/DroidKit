package io.github.droidkit.core.common

import android.util.Log

/** Thin log facade — swap internals for Timber later without touching call sites. */
object DLog {
    var enabled: Boolean = true

    fun d(tag: String, message: String) {
        if (enabled) Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        if (enabled) Log.i(tag, message)
    }

    fun w(tag: String, message: String, t: Throwable? = null) {
        if (enabled) Log.w(tag, message, t)
    }

    fun e(tag: String, message: String, t: Throwable? = null) {
        if (enabled) Log.e(tag, message, t)
    }
}
