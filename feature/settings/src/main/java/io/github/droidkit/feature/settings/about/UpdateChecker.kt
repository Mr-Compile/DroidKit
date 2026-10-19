package io.github.droidkit.feature.settings.about

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.droidkit.core.common.DLog
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Typed result for a version check — never fabricates an "up to date" state. */
sealed interface UpdateCheckResult {
    /** The check succeeded and the installed version is the newest known. */
    data object UpToDate : UpdateCheckResult

    /** The check succeeded and a newer release exists. */
    data class UpdateAvailable(val latestTag: String, val releaseUrl: String) : UpdateCheckResult

    /** The endpoint answered but no release has been published yet. */
    data object NoPublishedReleases : UpdateCheckResult

    /** No usable network — the check never ran. */
    data object Offline : UpdateCheckResult

    /** The endpoint could not be reached or returned an unexpected answer. */
    data object Failed : UpdateCheckResult
}

interface UpdateChecker {
    suspend fun check(currentVersion: String): UpdateCheckResult
}

/** Semantic-version comparison: tolerates a leading "v" and pre-release/build suffixes. */
object VersionComparator {
    fun compare(a: String, b: String): Int {
        val pa = parse(a)
        val pb = parse(b)
        for (i in 0 until maxOf(pa.core.size, pb.core.size)) {
            val x = pa.core.getOrElse(i) { 0 }
            val y = pb.core.getOrElse(i) { 0 }
            if (x != y) return x.compareTo(y)
        }
        return comparePrerelease(pa.pre, pb.pre)
    }

    private class Parsed(val core: List<Int>, val pre: List<String>?)

    private fun parse(version: String): Parsed {
        val v = version.trim().removePrefix("v").removePrefix("V")
        val dash = v.indexOf('-').let { if (it < 0) v.length else it }
        val plus = v.indexOf('+').let { if (it < 0) v.length else it }
        val coreEnd = minOf(dash, plus)
        val core = v.substring(0, coreEnd).split('.').map { it.toIntOrNull() ?: 0 }
        val pre = if (dash < plus) v.substring(dash + 1, plus).split('.') else null
        return Parsed(core, pre)
    }

    private fun comparePrerelease(a: List<String>?, b: List<String>?): Int = when {
        a == null && b == null -> 0
        a == null -> 1 // a stable release outranks a prerelease of the same core
        b == null -> -1
        else -> {
            for (i in 0 until maxOf(a.size, b.size)) {
                val x = a.getOrNull(i) ?: return -1 // fewer identifiers rank lower
                val y = b.getOrNull(i) ?: return 1
                compareIdentifier(x, y).takeIf { it != 0 }?.let { return it }
            }
            0
        }
    }

    private fun compareIdentifier(x: String, y: String): Int {
        val xn = x.toIntOrNull()
        val yn = y.toIntOrNull()
        return when {
            xn != null && yn != null -> xn.compareTo(yn)
            xn != null -> -1 // numeric identifiers rank below alphanumeric
            yn != null -> 1
            else -> x.compareTo(y)
        }
    }
}

/**
 * Checks the project's configured distribution channel — GitHub Releases —
 * for a newer tag. Plain HttpURLConnection on Dispatchers.IO, same zero-dep
 * approach as core:ai's GroqProvider. Failures map to honest typed results;
 * nothing is retried automatically.
 */
@Singleton
class GitHubReleasesUpdateChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) : UpdateChecker {

    override suspend fun check(currentVersion: String): UpdateCheckResult =
        withContext(Dispatchers.IO) {
            if (!isOnline()) return@withContext UpdateCheckResult.Offline
            try {
                fetchLatestRelease(currentVersion)
            } catch (e: IOException) {
                DLog.w(TAG, "Update check failed: ${e.message}")
                UpdateCheckResult.Failed
            } catch (e: RuntimeException) {
                DLog.w(TAG, "Update check failed: ${e.message}")
                UpdateCheckResult.Failed
            }
        }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return true
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun fetchLatestRelease(currentVersion: String): UpdateCheckResult {
        val conn = (URL(AboutLinks.GITHUB_RELEASES_API).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "DroidKit/$currentVersion")
        }
        try {
            return when (conn.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val tag = TAG_REGEX.find(body)?.groupValues?.get(1)
                        ?: return UpdateCheckResult.Failed
                    val url = URL_REGEX.find(body)?.groupValues?.get(1)
                        ?: AboutLinks.GITHUB_RELEASES
                    if (VersionComparator.compare(tag, currentVersion) > 0) {
                        UpdateCheckResult.UpdateAvailable(tag, url)
                    } else {
                        UpdateCheckResult.UpToDate
                    }
                }
                // GitHub also 404s for repos with no releases — same honest outcome.
                HttpURLConnection.HTTP_NOT_FOUND -> UpdateCheckResult.NoPublishedReleases
                else -> UpdateCheckResult.Failed
            }
        } finally {
            conn.disconnect()
        }
    }

    private companion object {
        const val TAG = "UpdateChecker"
        const val TIMEOUT_MS = 8_000
        val TAG_REGEX = Regex("\"tag_name\"\\s*:\\s*\"([^\"]+)\"")
        val URL_REGEX = Regex("\"html_url\"\\s*:\\s*\"([^\"]+)\"")
    }
}
