package io.github.droidkit.feature.deviceinfo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.ServiceState
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Per-slot cellular snapshot — About-phone "SIM status" parity. Every read is
 * permission-checked or runCatching-guarded: OEM builds vary wildly in what
 * they expose, and a field that fails surfaces as "Not exposed", never a crash.
 */
data class SimSlotStatus(
    val slotIndex: Int,
    val subscriptionId: Int,
    val carrierName: String?,
    val displayName: String?,
    val mccMnc: String?,
    val carrierId: Int?,
    val isEmbedded: Boolean,
    /** Last-4 ICCID tail only — the full ICCID is a device identifier. */
    val iccIdTail: String?,
    val dataRoaming: Boolean?,
    val phoneNumber: String?,
    val serviceState: String?,
    val dataNetworkType: String?,
    val voiceNetworkType: String?,
    val networkRoaming: Boolean?,
    /** SignalStrength level 0–4; null when unexposed. */
    val signalLevel: Int?,
    val isDefaultData: Boolean,
    val isDefaultVoice: Boolean,
)

@Singleton
class SimStatusReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun phonePermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // gated by phonePermissionGranted()
    fun read(): List<SimSlotStatus> {
        if (!phonePermissionGranted()) return emptyList()
        val sm = context.getSystemService(SubscriptionManager::class.java) ?: return emptyList()
        val tm = context.getSystemService(TelephonyManager::class.java) ?: return emptyList()
        val subs = runCatching { sm.activeSubscriptionInfoList }.getOrNull().orEmpty()
        val defaultData = runCatching { SubscriptionManager.getDefaultDataSubscriptionId() }.getOrNull()
        val defaultVoice = runCatching { SubscriptionManager.getDefaultVoiceSubscriptionId() }.getOrNull()

        return subs.map { sub ->
            val subTm = runCatching { tm.createForSubscriptionId(sub.subscriptionId) }.getOrNull() ?: tm
            SimSlotStatus(
                slotIndex = sub.simSlotIndex,
                subscriptionId = sub.subscriptionId,
                carrierName = sub.carrierName?.toString()?.takeIf { it.isNotBlank() },
                displayName = sub.displayName?.toString()?.takeIf { it.isNotBlank() },
                mccMnc = mccMnc(sub),
                carrierId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    runCatching { sub.carrierId.takeIf { it >= 0 } }.getOrNull()
                } else {
                    null
                },
                isEmbedded = runCatching { sub.isEmbedded }.getOrDefault(false),
                iccIdTail = runCatching { sub.iccId?.takeLast(4) }
                    .getOrNull()?.takeIf { it.isNotBlank() }?.let { "…$it" },
                dataRoaming = runCatching {
                    sub.dataRoaming == SubscriptionManager.DATA_ROAMING_ENABLE
                }.getOrNull(),
                phoneNumber = phoneNumber(sub, subTm),
                serviceState = runCatching { subTm.serviceState?.state }
                    .getOrNull()?.let { serviceStateName(it) },
                dataNetworkType = runCatching {
                    @Suppress("DEPRECATION") subTm.dataNetworkType
                }.getOrNull()?.takeIf { it != TelephonyManager.NETWORK_TYPE_UNKNOWN }
                    ?.let { networkTypeName(it) },
                voiceNetworkType = runCatching {
                    @Suppress("DEPRECATION") subTm.voiceNetworkType
                }.getOrNull()?.takeIf { it != TelephonyManager.NETWORK_TYPE_UNKNOWN }
                    ?.let { networkTypeName(it) },
                networkRoaming = runCatching { subTm.isNetworkRoaming }.getOrNull(),
                signalLevel = runCatching { subTm.signalStrength?.level }.getOrNull()
                    ?.takeIf { it >= 0 },
                isDefaultData = sub.subscriptionId == defaultData,
                isDefaultVoice = sub.subscriptionId == defaultVoice,
            )
        }
    }

    private fun mccMnc(sub: SubscriptionInfo): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listOfNotNull(sub.mccString, sub.mncString)
                .joinToString("/").takeIf { it.isNotBlank() }
        } else {
            @Suppress("DEPRECATION") "${sub.mcc}/${sub.mnc}"
        }

    @SuppressLint("MissingPermission") // gated by phonePermissionGranted() in read()
    private fun phoneNumber(sub: SubscriptionInfo, tm: TelephonyManager): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching { sub.number }.getOrNull()?.takeIf { it.isNotBlank() }
        } else {
            runCatching {
                @Suppress("DEPRECATION") tm.line1Number
            }.getOrNull()?.takeIf { it.isNotBlank() }
        }
}

// ---------- pure label mappers (JVM-testable) ----------

fun networkTypeName(type: Int): String = when (type) {
    TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
    TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
    TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
    TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
    TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO-0"
    TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO-A"
    TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
    TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
    TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
    TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
    TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN"
    TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO-B"
    TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
    TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
    TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
    TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
    TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD-SCDMA"
    TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
    TelephonyManager.NETWORK_TYPE_NR -> "NR (5G)"
    TelephonyManager.NETWORK_TYPE_UNKNOWN -> "Unknown"
    else -> "Type $type"
}

fun serviceStateName(state: Int): String = when (state) {
    ServiceState.STATE_IN_SERVICE -> "In service"
    ServiceState.STATE_OUT_OF_SERVICE -> "Out of service"
    ServiceState.STATE_EMERGENCY_ONLY -> "Emergency calls only"
    ServiceState.STATE_POWER_OFF -> "Radio off"
    else -> "Unknown"
}

fun simStateName(state: Int): String = when (state) {
    TelephonyManager.SIM_STATE_READY -> "Ready"
    TelephonyManager.SIM_STATE_ABSENT -> "Absent"
    TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN required"
    TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK required"
    TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network locked"
    TelephonyManager.SIM_STATE_NOT_READY -> "Not ready"
    TelephonyManager.SIM_STATE_PERM_DISABLED -> "Permanently disabled"
    TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "Card I/O error"
    TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "Restricted"
    else -> "Unknown"
}
