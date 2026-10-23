package io.github.droidkit.privilege.adb

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import io.github.droidkit.core.privileges.AdbPairingState
import java.net.Inet4Address
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps mDNS discovery alive while the user is in the
 * developer-options pairing dialog, and collects the 6-digit pairing code via
 * a notification [RemoteInput] — no need to leave the dialog. Required on OEMs that
 * stop advertising the pairing service when the dialog is backgrounded.
 *
 * All pairing work happens in [WirelessAdbProvider]; this service only renders
 * notifications and forwards the reply.
 */
class AdbPairingService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Wireless ADB pairing", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
                setShowBadge(false)
            },
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val provider = WirelessAdbProvider.instance
        when (intent?.action) {
            ACTION_REPLY -> {
                val raw = RemoteInput.getResultsFromIntent(intent)
                    ?.getCharSequence(KEY_PAIRING_CODE)?.toString().orEmpty().trim()
                scope.launch {
                    val codeAndPort = parseCodeAndPort(raw)
                    val result = when {
                        codeAndPort != null ->
                            // Dialog shows host+port — pair straight to it, no
                            // waiting on mDNS which can't always self-resolve.
                            provider?.pairWithEndpoint(selfHost(), codeAndPort.port, codeAndPort.code)
                        raw.matches(Regex("\\d{6}")) ->
                            provider?.pairWithDiscovered(raw)
                        else ->
                            Result.failure(
                                IllegalArgumentException(
                                    "Reply with the 6-digit code — or CODE PORT " +
                                        "from the dialog, e.g. 123456 38745",
                                ),
                            )
                    }
                    result?.onFailure { showResult(success = false, it.message) }
                        ?: showResult(
                            success = false,
                            "DroidKit restarted — open the app and start pairing again",
                        )
                }
                showNotification(workingNotification())
            }
            ACTION_STOP -> {
                provider?.stopPairingSearch()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // ACTION_START
                if (provider == null) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                startForegroundWith(searchingNotification())
                observeProvider(provider)
            }
        }
        return START_REDELIVER_INTENT
    }

    private fun startForegroundWith(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (t: Throwable) {
            // e.g. ForegroundServiceStartNotAllowedException — still post it
            getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, notification)
        }
    }

    private fun observeProvider(provider: WirelessAdbProvider) {
        scope.launch {
            provider.pairingState.collect { state ->
                when (state) {
                    is AdbPairingState.EndpointFound ->
                        showNotification(inputNotification())
                    is AdbPairingState.Pairing,
                    is AdbPairingState.SearchingConnect,
                    is AdbPairingState.Connecting,
                    -> showNotification(workingNotification())
                    is AdbPairingState.Connected -> finish(success = true, state.summary)
                    is AdbPairingState.Failed -> finish(success = false, state.message)
                    else -> {}
                }
            }
        }
    }

    private fun finish(success: Boolean, message: String) {
        showResult(success, message)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun showResult(success: Boolean, message: String?) {
        val title = if (success) "Wireless ADB connected" else "Wireless ADB pairing failed"
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle(title)
            .setContentText(message.orEmpty())
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun showNotification(notification: Notification) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun searchingNotification(): Notification = Notification.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
        .setContentTitle("Wireless ADB pairing")
        .setContentText("Open Developer options → Wireless debugging → “Pair device with pairing code”")
        .setStyle(Notification.BigTextStyle().bigText(
            "Searching for the pairing service… In Developer options, open " +
                "Wireless debugging and tap “Pair device with pairing code”. " +
                "Reply here with the 6-digit code. If it keeps searching, " +
                "reply as CODE PORT from the dialog — e.g. 123456 38745.",
        ))
        .addAction(
            Notification.Action.Builder(null, "Pair", replyIntent())
                .addRemoteInput(codeInput())
                .build(),
        )
        .addAction(
            Notification.Action.Builder(null, "Developer options", devOptionsIntent()).build(),
        )
        .addAction(
            Notification.Action.Builder(null, "Stop", stopIntent()).build(),
        )
        .setOngoing(true)
        .setContentIntent(openAppIntent())
        .build()

    private fun inputNotification(): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle("Enter pairing code")
            .setContentText("Reply with the 6-digit code shown in the pairing dialog.")
            .addAction(
                Notification.Action.Builder(null, "Pair", replyIntent())
                    .addRemoteInput(codeInput())
                    .build(),
            )
            .addAction(
                Notification.Action.Builder(null, "Stop", stopIntent()).build(),
            )
            .setOngoing(true)
            .setContentIntent(openAppIntent())
            .build()

    private fun workingNotification(): Notification = Notification.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
        .setContentTitle("Wireless ADB pairing")
        .setContentText("Working…")
        .setOngoing(true)
        .setContentIntent(openAppIntent())
        .build()

    private fun codeInput(): RemoteInput = RemoteInput.Builder(KEY_PAIRING_CODE)
        .setLabel("Pairing code — or CODE PORT")
        .build()

    private fun replyIntent(): PendingIntent = PendingIntent.getService(
        this, REQUEST_REPLY,
        Intent(this, AdbPairingService::class.java).setAction(ACTION_REPLY),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
    )

    private data class CodeAndPort(val code: String, val port: Int)

    /**
     * Parses "123456 38745" (or "38745 123456", ":"/"/" separators) from a
     * reply that carries both the pairing code and the port shown in the
     * dialog. Null unless exactly two numeric tokens with one 6-digit code.
     */
    private fun parseCodeAndPort(raw: String): CodeAndPort? {
        val tokens = raw.split(Regex("\\D+")).filter { it.isNotEmpty() }
        if (tokens.size != 2) return null
        val (a, b) = tokens
        val (code, portStr) = when {
            a.length == 6 && b.length != 6 -> a to b
            b.length == 6 && a.length != 6 -> b to a
            else -> return null
        }
        val port = portStr.toIntOrNull() ?: return null
        return if (port in 1..65535) CodeAndPort(code, port) else null
    }

    /**
     * This device's own address for self-pairing — the pairing server binds
     * all interfaces, so WiFi IP works and [WirelessAdbProvider] transparently
     * prefers loopback when reachable.
     */
    private fun selfHost(): String {
        val cm = getSystemService(ConnectivityManager::class.java)
        val nets = cm?.allNetworks.orEmpty().toList()
        val wifi = nets.firstOrNull { n ->
            cm?.getNetworkCapabilities(n)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }
        return listOfNotNull(wifi, cm?.activeNetwork)
            .asSequence()
            .mapNotNull { n ->
                cm?.getLinkProperties(n)?.linkAddresses
                    ?.mapNotNull { it.address as? Inet4Address }
                    ?.firstOrNull { !it.isLoopbackAddress }
                    ?.hostAddress
            }
            .firstOrNull() ?: "127.0.0.1"
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        this, REQUEST_OPEN_APP,
        packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun devOptionsIntent(): PendingIntent = PendingIntent.getActivity(
        this, REQUEST_DEV_OPTIONS,
        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            .putExtra(":settings:fragment_args_key", "toggle_adb_wireless")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun stopIntent(): PendingIntent = PendingIntent.getService(
        this, REQUEST_STOP,
        Intent(this, AdbPairingService::class.java).setAction(ACTION_STOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    override fun onDestroy() {
        scope.cancel()
        // stopService() (provider-initiated) skips stopForeground — don't
        // leave a stale pairing notification behind either way.
        getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "adb_pairing"
        private const val NOTIFICATION_ID = 42
        private const val REQUEST_REPLY = 1
        private const val REQUEST_STOP = 2
        private const val REQUEST_OPEN_APP = 3
        private const val REQUEST_DEV_OPTIONS = 4
        private const val KEY_PAIRING_CODE = "pairing_code"

        const val ACTION_START = "io.github.droidkit.privilege.adb.action.START_PAIRING"
        private const val ACTION_REPLY = "io.github.droidkit.privilege.adb.action.REPLY"
        private const val ACTION_STOP = "io.github.droidkit.privilege.adb.action.STOP"

        fun startIntent(context: Context): Intent =
            Intent(context, AdbPairingService::class.java).setAction(ACTION_START)

        fun stopServiceIntent(context: Context): Intent =
            Intent(context, AdbPairingService::class.java).setAction(ACTION_STOP)
    }
}
