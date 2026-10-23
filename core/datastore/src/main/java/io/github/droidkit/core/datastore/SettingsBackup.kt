package io.github.droidkit.core.datastore

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Flat snapshot of every exportable preference. Device-bound state
 * (`wireless_adb_paired`), onboarding and the AI key are structurally absent —
 * they can never be written into a backup.
 */
data class SettingsSnapshot(
    val themeMode: String = "SYSTEM",
    val dynamicColor: Boolean = false,
    val technicianName: String = "",
    val favoriteTools: Set<String> = emptySet(),
    val recentTools: List<String> = emptyList(),
    val toolUseCounts: Map<String, Int> = emptyMap(),
    val wirelessAdbAutoConnect: Boolean = true,
)

/**
 * Versioned JSON codec for settings export/import. Unknown keys are ignored
 * (forward compatibility); a wrong schema version or malformed known field is
 * a hard failure — silent partial restores are worse than an honest error.
 */
object SettingsBackupCodec {
    const val SCHEMA_VERSION = 1

    fun encode(snapshot: SettingsSnapshot, exportedAtEpochMs: Long): String =
        buildJsonObject {
            put("v", SCHEMA_VERSION)
            put("exportedAt", exportedAtEpochMs)
            putJsonObject("settings") {
                put("themeMode", snapshot.themeMode)
                put("dynamicColor", snapshot.dynamicColor)
                put("technicianName", snapshot.technicianName)
                putJsonArray("favoriteTools") {
                    snapshot.favoriteTools.forEach { add(JsonPrimitive(it)) }
                }
                putJsonArray("recentTools") {
                    snapshot.recentTools.forEach { add(JsonPrimitive(it)) }
                }
                putJsonObject("toolUseCounts") {
                    snapshot.toolUseCounts.forEach { (k, v) -> put(k, v) }
                }
                put("wirelessAdbAutoConnect", snapshot.wirelessAdbAutoConnect)
            }
        }.toString()

    fun decode(json: String): Result<SettingsSnapshot> = runCatching {
        val root = Json.parseToJsonElement(json).jsonObject
        val v = root["v"]?.jsonPrimitive?.int
            ?: error("missing schema version")
        require(v == SCHEMA_VERSION) { "unsupported backup version $v" }
        val s = root["settings"]?.jsonObject ?: error("missing settings object")

        SettingsSnapshot(
            themeMode = s.str("themeMode") ?: "SYSTEM",
            dynamicColor = s.bool("dynamicColor") ?: false,
            technicianName = s.str("technicianName").orEmpty(),
            favoriteTools = s.strList("favoriteTools").toSet(),
            recentTools = s.strList("recentTools"),
            toolUseCounts = s.intMap("toolUseCounts"),
            wirelessAdbAutoConnect = s.bool("wirelessAdbAutoConnect") ?: true,
        )
    }

    private fun JsonObject.str(key: String): String? =
        this[key]?.jsonPrimitive?.content

    private fun JsonObject.bool(key: String): Boolean? =
        this[key]?.jsonPrimitive?.boolean

    private fun JsonObject.strList(key: String): List<String> =
        this[key]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

    private fun JsonObject.intMap(key: String): Map<String, Int> =
        this[key]?.jsonObject?.mapValues { it.value.jsonPrimitive.int } ?: emptyMap()
}
