package io.github.droidkit.feature.battery

/**
 * Fuel-gauge values read from `/sys/class/power_supply/` through an elevated
 * provider. `null` where the kernel does not expose the node — never
 * fabricated.
 */
data class FuelGaugeReading(
    val node: String?,
    val chargeFullUa: Long?,
    val chargeFullDesignUa: Long?,
    val cycleCount: Int?,
    val stateOfHealthPct: Float?, // OEM soh/asoc node when present
) {
    /** Measured health % — direct SoH node wins, else full/design ratio. */
    val healthPct: Float?
        get() = when {
            stateOfHealthPct != null -> stateOfHealthPct
            chargeFullUa != null && chargeFullDesignUa != null && chargeFullDesignUa > 0L ->
                chargeFullUa * 100f / chargeFullDesignUa
            else -> null
        }
}

/** Accumulated AccuBattery-style capacity estimate inputs. */
data class CapacityEstimate(
    val samples: Int,
    val estimatedFullUa: Long?,   // null until enough charge was tracked
    val designMah: Int?,          // user-entered design capacity
) {
    /** Estimated health % — only when both an estimate and design capacity exist. */
    val healthPct: Float?
        get() = if (estimatedFullUa != null && designMah != null && designMah > 0) {
            (estimatedFullUa / 1000f) / designMah * 100f
        } else {
            null
        }
}

/**
 * Parses `grep -H .` output over the power_supply capacity nodes into
 * per-node attr maps (`battery` -> {charge_full: 4750000, …}). Returns null
 * when nothing parseable came back — callers surface that as "fuel gauge
 * not exposed".
 */
fun parsePowerSupplyDump(stdout: String): FuelGaugeReading? {
    val byNode = linkedMapOf<String, MutableMap<String, Long>>()
    stdout.lineSequence()
        .map { it.trim() }
        .filter { it.startsWith("/sys/class/power_supply/") && ':' in it }
        .forEach { line ->
            val path = line.substringBefore(':')
            val value = line.substringAfter(':').trim().toLongOrNull() ?: return@forEach
            val segments = path.split('/')
            val attr = segments.last()
            val node = segments.getOrNull(segments.lastIndex - 1) ?: return@forEach
            byNode.getOrPut(node) { mutableMapOf() }[attr] = value
        }
    if (byNode.isEmpty()) return null

    // Prefer a node carrying both capacity values; otherwise merge the best
    // value seen per attribute across nodes.
    val preferred = byNode.entries.firstOrNull { (_, attrs) ->
        attrs["charge_full"] != null && attrs["charge_full_design"] != null
    }?.key ?: byNode.keys.first()
    fun attr(name: String): Long? =
        byNode[preferred]?.get(name)
            ?: byNode.values.firstNotNullOfOrNull { it[name] }

    fun attrMatching(predicate: (String) -> Boolean): Long? =
        byNode[preferred]?.entries?.firstOrNull { predicate(it.key) }?.value
            ?: byNode.values.firstNotNullOfOrNull { m -> m.entries.firstOrNull { predicate(it.key) }?.value }

    val soh = attr("soh") ?: attrMatching { it.contains("asoc") }
    return FuelGaugeReading(
        node = preferred,
        chargeFullUa = attr("charge_full"),
        chargeFullDesignUa = attr("charge_full_design"),
        cycleCount = attr("cycle_count")?.toInt(),
        stateOfHealthPct = soh?.let { if (it > 100) it / 10f else it.toFloat() },
    )
}
