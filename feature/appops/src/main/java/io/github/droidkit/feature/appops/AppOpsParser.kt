package io.github.droidkit.feature.appops

/** One op line from `appops get <pkg>` — e.g. `RUN_IN_BACKGROUND: ignore; time=...`. */
data class AppOpEntry(val op: String, val mode: String)

/** Parses `pm list packages` output (`package:com.foo` lines) into package names. */
object PackageListParser {
    fun parse(stdout: String): List<String> =
        stdout.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("package:") }
            .map { it.removePrefix("package:").trim() }
            .filter { it.isNotEmpty() }
            .sorted()
            .toList()
}

object AppOpsParser {
    private val LINE = Regex("""^\s*([A-Z0-9_]+)\s*:\s*([a-z_]+)""")

    /** Extracts op->mode rows; ignores headers, attribution tags and noise. */
    fun parse(stdout: String): List<AppOpEntry> =
        stdout.lineSequence()
            .mapNotNull { line ->
                val m = LINE.find(line) ?: return@mapNotNull null
                if (m.groupValues[1] == "Uid" || m.groupValues[1] == "OP") null
                else AppOpEntry(op = m.groupValues[1], mode = m.groupValues[2])
            }
            .toList()
}
