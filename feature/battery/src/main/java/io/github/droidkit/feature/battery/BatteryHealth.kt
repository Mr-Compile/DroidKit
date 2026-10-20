package io.github.droidkit.feature.battery

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists charge-session samples for the capacity estimator plus the
 * optional user-entered design capacity. The estimate converges over real
 * charge sessions — it is always presented as an estimate, never as a
 * measured health value.
 */
@Singleton
class BatteryHealthStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val store = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("battery_health") },
    )
    private val lastLevelKey = floatPreferencesKey("last_level_pct")
    private val lastCounterKey = longPreferencesKey("last_counter_ua")
    private val sumChargeKey = longPreferencesKey("sum_charge_ua")
    private val sumPctKey = floatPreferencesKey("sum_pct_delta")
    private val samplesKey = intPreferencesKey("sample_count")
    private val designKey = intPreferencesKey("design_mah")

    val estimate: Flow<CapacityEstimate> = store.data.map { p ->
        val sumCharge = p[sumChargeKey] ?: 0L
        val sumPct = p[sumPctKey] ?: 0f
        CapacityEstimate(
            samples = p[samplesKey] ?: 0,
            estimatedFullUa = if (sumPct >= 20f) (sumCharge / (sumPct / 100f)).toLong() else null,
            designMah = p[designKey]?.takeIf { it > 0 },
        )
    }

    /**
     * Record one (level, charge-counter) observation. Deltas are counted only
     * when both rise — i.e. actual charge went into the pack — which filters
     * idle reads automatically. `|Δcounter|` is used because some fuel gauges
     * count down while charging.
     */
    suspend fun recordSample(levelPct: Float, counterUa: Int?) {
        if (levelPct < 0f || counterUa == null) return
        store.edit { p ->
            val lastLevel = p[lastLevelKey]
            val lastCounter = p[lastCounterKey]
            if (lastLevel != null && lastCounter != null) {
                val dLevel = levelPct - lastLevel
                val dCharge = kotlin.math.abs(counterUa - lastCounter)
                if (dLevel >= 1f && dCharge > 0L) {
                    p[sumChargeKey] = (p[sumChargeKey] ?: 0L) + dCharge
                    p[sumPctKey] = (p[sumPctKey] ?: 0f) + dLevel
                    p[samplesKey] = (p[samplesKey] ?: 0) + 1
                }
            }
            p[lastLevelKey] = levelPct
            p[lastCounterKey] = counterUa.toLong()
        }
    }

    suspend fun setDesignCapacity(mah: Int?) {
        store.edit { p ->
            if (mah != null && mah > 0) p[designKey] = mah else p.remove(designKey)
        }
    }

    suspend fun reset() {
        store.edit { p ->
            p.remove(lastLevelKey)
            p.remove(lastCounterKey)
            p.remove(sumChargeKey)
            p.remove(sumPctKey)
            p.remove(samplesKey)
        }
    }
}
