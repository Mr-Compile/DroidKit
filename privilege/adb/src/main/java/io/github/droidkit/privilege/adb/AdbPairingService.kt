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
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import io.github.droidkit.core.privileges.AdbPairingState
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
                val code = RemoteInput.getResultsFromIntent(intent)
                    ?.getCharSequence(KEY_PAIRING_CODE)?.toString().orEmpty()
                scope.launch {
                    provider?.pairWithDiscovered(code)
                        ?.onFailure { showResult(success = false, it.message) }
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
                "Then reply here with the 6-digit code.",
        ))
        .addAction(
            Notification.Action.Builder(null, "Open Developer options", devOptionsIntent()).build(),
        )
        .addAction(
            Notification.Action.Builder(null, "Stop", stopIntent()).build(),
        )
        .setOngoing(true)
        .setContentIntent(openAppIntent())
        .build()

    private fun inputNotification(): Notification {
        val remoteInput = RemoteInput.Builder(KEY_PAIRING_CODE)
            .setLabel("Pairing code (6 digits)")
            .build()
        val replyIntent = PendingIntent.getService(
            this, REQUEST_REPLY,
            Intent(this, AdbPairingService::class.java).setAction(ACTION_REPLY),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle("Enter pairing code")
            .setContentText("Reply with the 6-digit code shown in the pairing dialog.")
            .addAction(
                Notification.Action.Builder(null, "Pair", replyIntent)
                    .addRemoteInput(remoteInput)
                    .build(),
            )
            .addAction(
                Notification.Action.Builder(null, "Stop", stopIntent()).build(),
            )
            .setOngoing(true)
            .setContentIntent(openAppIntent())
            .build()
    }

    private fun workingNotification(): Notification = Notification.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
        .setContentTitle("Wireless ADB pairing")
        .setContentText("Working…")
        .setOngoing(true)
        .setContentIntent(openAppIntent())
        .build()

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
