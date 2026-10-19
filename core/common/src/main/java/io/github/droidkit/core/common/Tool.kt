package io.github.droidkit.core.common

enum class SafetyLevel { SAFE, MINOR, SYSTEM_CHANGE, HIGH_IMPACT, ADVANCED }

enum class ToolCategory { DEVICE, DIAGNOSTICS, APPS, CONNECTIVITY, HARDWARE, DEVELOPER, REPORTS }

enum class ToolPhase { V1, V2, FUTURE }

enum class Capability { STANDARD_API, RUNTIME_PERMISSION, ELEVATED_SHELL, SHIZUKU_SERVICE, ADB_SHELL, ROOT }

enum class TestResultState { PASS, FAIL, NOT_TESTED, NOT_SUPPORTED, PERMISSION_REQUIRED, UNKNOWN }

data class Tool(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val route: String,
    val safetyLevel: SafetyLevel = SafetyLevel.SAFE,
    val requiredCapabilities: Set<Capability> = setOf(Capability.STANDARD_API),
    val requiredPermissions: List<String> = emptyList(),
    val requiredFeatures: Set<String> = emptySet(),
    val minSdk: Int? = null,
    val phase: ToolPhase = ToolPhase.V1,
    val keywords: Set<String> = emptySet(),
)

interface FeatureContribution {
    val tools: List<Tool>
}
