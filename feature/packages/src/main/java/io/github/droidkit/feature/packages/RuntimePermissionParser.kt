package io.github.droidkit.feature.packages

/**
 * Extracts the `runtime permissions:` block from `dumpsys package <pkg>`.
 * Lines look like `  android.permission.CAMERA: granted=true, flags=[...]`.
 */
object RuntimePermissionParser {

    private val LINE = Regex("""^\s*([A-Za-z0-9_.]+)\s*:\s*granted=(true|false)""")

    /** Returns permission name → granted, in declared order. */
    fun parse(dumpsys: String): List<Pair<String, Boolean>> {
        val out = mutableListOf<Pair<String, Boolean>>()
        var inBlock = false
        for (line in dumpsys.lineSequence()) {
            if (!inBlock) {
                if (line.trimStart().startsWith("runtime permissions:")) inBlock = true
                continue
            }
            if (line.isBlank() || (!line.startsWith(" ") && !line.startsWith("\t"))) break
            val m = LINE.find(line) ?: continue
            out += m.groupValues[1] to (m.groupValues[2] == "true")
        }
        return out
    }
}
