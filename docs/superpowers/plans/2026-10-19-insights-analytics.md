# Insights & Analytics — Implementation Plan

> **For agentic workers:** Implement task-by-task. Steps use checkbox (`- [ ]`)
> syntax. Approved design: `docs/superpowers/specs/2026-10-19-insights-analytics-mockup-design.md`
> (mockup already built in `mockups/index.html` — this plan ports it to the app).

**Goal:** New `:feature:insights` module giving an on-device analytics screen
(health trends + activity stats) plus an Insights summary card on the dashboard.

**Architecture:** Room is the only data source — `diagnostic_sessions`,
`diagnostic_results`, `storage_snapshots`, `saved_reports`, `nfc_tag_scans`,
`elevated_action_log`. Aggregation lives in a pure `InsightsCalculator` (in
`core:database`, shared with the dashboard card). Charts are hand-rolled
`Canvas` composables — zero new dependencies.

**Tech Stack:** Kotlin, Jetpack Compose (Canvas), Room, DataStore, Hilt.

## Global Constraints

- minSdk 28, targetSdk 36; Kotlin + Compose, MVVM, Hilt (`droidkit.hilt`).
- Feature modules are leaves — **no `implementation(projects.feature.*)`**
  between features. Only `:app` wires features.
- No new external dependencies (no chart lib). All charts = `Canvas`.
- Local-first: every number derives from Room/DataStore. No fabricated data —
  empty states, not zeros. `TestResultState` enum drives result counting.
- Route literal `"insights"` is used by name in the dashboard graph (existing
  convention: `nav.navigate("tools")` etc. — no constant import needed).
- `JAVA_HOME` must point at the Android Studio JBR to build on this machine:
  `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`.
- No commits without explicit user approval (global git rule).

## File map

- Create `core/common/.../TestTitleLookup.kt` — SAM interface, `titleFor(testId)`.
- Move `feature/deviceinfo/.../StorageSnapshotCodec.kt` → `core/database/.../StorageSnapshotCodec.kt` (codec for `StorageSnapshotEntity`; `VolumeStat`, `VolumeDelta` ride along).
- Move `feature/deviceinfo/src/test/.../StorageSnapshotCodecTest.kt` → `core/database/src/test/.../StorageSnapshotCodecTest.kt`.
- Create `core/database/.../InsightsCalculator.kt` — pure aggregation.
- Create `feature/insights/` module (build file + 4 src files + test).
- Modify `core/datastore/.../SettingsRepository.kt` — `toolUseCounts` flow + counter inside `recordRecent`.
- Modify `core/database/.../Daos.kt` — 3 `COUNT(*)` flows.
- Modify `feature/dashboard/` — VM + screen card + `recordOpen`.
- Modify `feature/diagnostics/` — `DiagnosticsModule.kt` provides `TestTitleLookup`.
- Modify `app/` — `build.gradle.kts`, `AppModule.kt`, `DroidKitNavHost.kt`, `settings.gradle.kts`.
- Modify `CHANGELOG.md` (0.9.0-alpha.1 Added), `README.md` (features bullet).

---

### Task 1: Core plumbing — TestTitleLookup, codec move, DAO counts, tool-use store

**Files:**
- Create: `core/common/src/main/java/io/github/droidkit/core/common/TestTitleLookup.kt`
- Move: `feature/deviceinfo/src/main/java/io/github/droidkit/feature/deviceinfo/StorageSnapshotCodec.kt` → `core/database/src/main/java/io/github/droidkit/core/database/StorageSnapshotCodec.kt` (change package to `io.github.droidkit.core.database`; content otherwise identical)
- Move: `feature/deviceinfo/src/test/java/io/github/droidkit/feature/deviceinfo/StorageSnapshotCodecTest.kt` → `core/database/src/test/java/io/github/droidkit/core/database/StorageSnapshotCodecTest.kt` (same package change)
- Modify: `feature/deviceinfo/src/main/java/io/github/droidkit/feature/deviceinfo/StorageLabViewModel.kt` — add imports `io.github.droidkit.core.database.StorageSnapshotCodec`, `.VolumeStat`, `.VolumeDelta`
- Modify: `core/database/build.gradle.kts` — add `testImplementation(libs.androidx.junit)`
- Modify: `core/database/src/main/java/io/github/droidkit/core/database/Daos.kt` — add `observeCount()` to `SavedReportDao`, `NfcTagScanDao`, `ElevatedActionLogDao`
- Modify: `core/datastore/.../SettingsRepository.kt` — `toolUseCounts` flow + bump in `recordRecent`
- Modify: `Entities.kt` doc comment — it already references `StorageSnapshotCodec` by name; now same package (no code change needed, verify comment still accurate)

**Interfaces produced:**
```kotlin
// core:common
fun interface TestTitleLookup { fun titleFor(testId: String): String? }
// DAOs
fun observeCount(): Flow<Int>          // on SavedReportDao / NfcTagScanDao / ElevatedActionLogDao
// SettingsRepository
val toolUseCounts: Flow<Map<String, Int>>
suspend fun recordRecent(toolId: String) // now also bumps tool_use_counts
```

- [x] **Step 1: TestTitleLookup**
```kotlin
package io.github.droidkit.core.common

/** Maps a diagnostic testId to a display title; app supplies the catalog impl. */
fun interface TestTitleLookup {
    fun titleFor(testId: String): String?
}
```

- [x] **Step 2: Move codec** — `git mv` the two files to `core/database` (same relative package path), change `package` line to `io.github.droidkit.core.database` in both, delete nothing else. In `StorageLabViewModel.kt` add:
```kotlin
import io.github.droidkit.core.database.StorageSnapshotCodec
import io.github.droidkit.core.database.VolumeStat
import io.github.droidkit.core.database.VolumeDelta
```
Add `testImplementation(libs.androidx.junit)` to `core/database/build.gradle.kts` dependencies.

- [x] **Step 3: DAO counts** — add to each DAO:
```kotlin
@Query("SELECT COUNT(*) FROM saved_reports")
fun observeCount(): Flow<Int>
```
(plus `nfc_tag_scans`, `elevated_action_log` variants)

- [x] **Step 4: toolUseCounts in SettingsRepository**
```kotlin
private val toolUseKey = stringPreferencesKey("tool_use_counts")

/** Times each tool was opened — feeds Insights "most used". */
val toolUseCounts: Flow<Map<String, Int>> = context.settingsStore.data.map { prefs ->
    prefs[toolUseKey].orEmpty().split(",")
        .mapNotNull { it.substringBeforeLast(':') to (it.substringAfterLast(':').toIntOrNull() ?: return@mapNotNull null) }
        .toMap()
}

suspend fun recordRecent(toolId: String) {
    context.settingsStore.edit { prefs ->
        val current = prefs[recentsKey]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        prefs[recentsKey] = (listOf(toolId) + current.filter { it != toolId }).take(8).joinToString(",")
        val counts = prefs[toolUseKey].orEmpty().split(",")
            .mapNotNull { it.substringBeforeLast(':') to (it.substringAfterLast(':').toIntOrNull() ?: return@mapNotNull null) }
            .toMap().toMutableMap()
        counts[toolId] = (counts[toolId] ?: 0) + 1
        prefs[toolUseKey] = counts.entries.joinToString(",") { "${it.key}:${it.value}" }
    }
}
```

- [x] **Step 5: Verify build + moved test**
```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :core:database:testDebugUnitTest :feature:deviceinfo:assembleDebug
```
Expected: BUILD SUCCESSFUL, `StorageSnapshotCodecTest` green.

---

### Task 2: InsightsCalculator (TDD)

**Files:**
- Create: `core/database/src/main/java/io/github/droidkit/core/database/InsightsCalculator.kt`
- Test: `core/database/src/test/java/io/github/droidkit/core/database/InsightsCalculatorTest.kt`

**Produces:**
```kotlin
enum class InsightRange { DAYS_30, ALL;
    fun cutoff(now: Long): Long = when (this) { DAYS_30 -> now - 30L*86_400_000; ALL -> Long.MIN_VALUE }
}
data class SessionHealth(sessionId, startedAtEpochMs, preset, pass, fail, skip, total) { val passRate: Float }
data class TestAttention(testId, title, category, fails, skips)
data class StoragePoint(atEpochMs, freeBytes)
object InsightsCalculator {
    fun health(sessionId: String, startedAt: Long, preset: String?, results: List<DiagnosticResultEntity>): SessionHealth
    fun inRange(sessions: List<DiagnosticSessionEntity>, range: InsightRange, nowMs: Long): List<DiagnosticSessionEntity> // COMPLETED only, ascending
    fun attention(results: List<DiagnosticResultEntity>, titleFor: (String) -> String?, limit: Int = 4): List<TestAttention>
    fun storageTrend(snapshots: List<StorageSnapshotEntity>): List<StoragePoint> // first decoded volume's freeBytes, ascending
}
```

- [x] **Step 1: failing test** — cover: PASS/FAIL/other counting; `NOT_SUPPORTED`+`NOT_TESTED`+`PERMISSION_REQUIRED`+`UNKNOWN` all fold into skip; passRate = pass/total; `inRange` drops non-COMPLETED and out-of-cutoff; `attention` ranks `fails*2+skips` and uses `titleFor` fallback `testId`; `storageTrend` decodes first volume only.
- [x] **Step 2: run** `./gradlew :core:database:testDebugUnitTest` → FAIL (class missing).
- [x] **Step 3: implement** `InsightsCalculator` — `health` counts `TestResultState.valueOf(r.state)`; `skip` = total - pass - fail. `attention` aggregates all results rows across the given list, top `limit`. `storageTrend` sorts snapshots by `capturedAtEpochMs`, maps to `StoragePoint(at, StorageSnapshotCodec.decode(volumesData).firstOrNull()?.freeBytes ?: skip)`.
- [x] **Step 4: run** — same command → PASS.

---

### Task 3: `:feature:insights` module

**Files:**
- Create: `feature/insights/build.gradle.kts`
- Modify: `settings.gradle.kts` — `include(":feature:insights")` after `:feature:reports`
- Create: `feature/insights/src/main/java/io/github/droidkit/feature/insights/InsightsFeature.kt`
- Create: `feature/insights/src/main/java/io/github/droidkit/feature/insights/InsightsViewModel.kt`
- Create: `feature/insights/src/main/java/io/github/droidkit/feature/insights/InsightsScreen.kt`
- Create: `feature/insights/src/main/java/io/github/droidkit/feature/insights/InsightsCharts.kt`

**build.gradle.kts:**
```kotlin
plugins {
    id("droidkit.android.library.compose")
    id("droidkit.hilt")
}
android { namespace = "io.github.droidkit.feature.insights" }
dependencies {
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.ui)
    testImplementation(libs.androidx.junit)
}
```

**InsightsFeature.kt** — `object InsightsFeature : FeatureContribution { const val ROUTE = "insights" }`, tool:
```kotlin
Tool(
    id = "analytics.insights",
    title = "Device Insights",
    description = "Health trends, diagnostic history and on-device usage stats",
    category = ToolCategory.REPORTS,
    route = ROUTE,
)
```
plus `fun NavGraphBuilder.insightsGraph(nav: NavHostController)` → `composable(InsightsFeature.ROUTE) { InsightsScreen(onOpenTool = { nav.navigate(it) }, onStartDiagnostic = { nav.navigate("diagnose") }) }`.

**InsightsViewModel** — two `stateIn` flows:
```kotlin
val health: StateFlow<HealthState> = combine(
    sessionDao.observeAll(), resultDao.observeAll(), storageDao.observeAll(), range,
) { sessions, results, snapshots, r ->
    val inRange = InsightsCalculator.inRange(sessions, r, System.currentTimeMillis())
    val bySession = results.groupBy { it.sessionId }
    val rows = inRange.map { s -> InsightsCalculator.health(s.id, s.startedAtEpochMs, s.preset, bySession[s.id] ?: emptyList()) }
    val rangeResults = inRange.flatMap { bySession[it.id] ?: emptyList() }
    HealthState(
        range = r,
        sessions = rows,
        attention = InsightsCalculator.attention(rangeResults, testTitles::titleFor),
        storage = InsightsCalculator.storageTrend(snapshots),
        hasCompletedAnywhere = sessions.any { it.overallStatus == "COMPLETED" },
    )
}.stateIn(viewModelScope, WhileSubscribed(5000), HealthState())

val activity: StateFlow<ActivityState> = combine(
    reportDao.observeCount(), nfcDao.observeCount(), elevatedDao.observeCount(),
    resultDao.observeAll(), settings.toolUseCounts,
) { reports, nfc, elevated, results, toolUse ->
    ActivityState(
        diagnostics = ... // completed sessions count — see note
        testsRecorded = results.size,
        reportsSaved = reports, nfcScans = nfc, elevatedActions = elevated,
        toolsUsed = toolUse.size,
        topTools = toolUse.entries.sortedByDescending { it.value }.take(3)
            .mapNotNull { (id, n) -> registry.byId(id)?.let { it to n } },
    )
}...
```
(Note: `diagnostics` tile = completed session count — derive inside health combine and surface on HealthState as `completedCount`, or add sessionDao.observeAll() to the activity combine — it exceeds 5 params → keep `completedCount` on HealthState and let the screen merge the two states.)

**InsightsCharts.kt** — `Canvas`-based:
- `VerdictDonut(pass, fail, skip, total)` — `drawArc` segments (primary/error/outline) on a `size = 104.dp` box; center `Column` shows percent + "passed".
- `PassRateBars(points: List<SessionHealth>)` — Canvas `drawRoundRect` bars (error color if `fail>0` else primary), baseline stroke, and a `Row` of `Text` date labels under the canvas (one per point, `fmtDay`).
- `TrendSparkline(points: List<StoragePoint>)` — `drawPath` polyline + last-point circle.
- `StatTile(label, value)` + `UsageBar(fraction)` — `Box`/`LinearProgressIndicator`-style `Box` with `surfaceVariant` track.

**InsightsScreen.kt** — `Scaffold` + `TopAppBar("Insights")`, `LazyColumn` mirroring mockup order: range `FilterChip`s (30 days / All time) → `SectionHeader("Device health")` → donut card (or empty state + "Start Full Diagnostic" button) → bars card → attention section → `SectionHeader("Storage")` → sparkline card (or "Capture two or more snapshots in Storage Lab") → `SectionHeader("Activity")` → 2-col `statgrid` via `LazyVerticalGrid`-free `Column` of `Row`s → `SectionHeader("Most used tools")` → tool rows clickable `onOpenTool(tool.route)` → footer note "All stats are computed on-device… Nothing leaves this phone." Date label helper `SimpleDateFormat("MMM d")`.

- [x] **Step 1: create module + all files**
- [x] **Step 2: build** `./gradlew :feature:insights:assembleDebug` → BUILD SUCCESSFUL.

---

### Task 4: App wiring

**Files:** `settings.gradle.kts` (done in Task 3), `app/build.gradle.kts` (`implementation(projects.feature.insights)` after `feature.dashboard`), `app/.../di/AppModule.kt` (add `InsightsFeature` to `provideContributions()` set), `app/.../navigation/DroidKitNavHost.kt` (import `insightsGraph`, call after `reportsGraph(nav)`), `feature/diagnostics/.../DiagnosticsModule.kt` (new):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DiagnosticsModule {
    @Provides @Singleton
    fun provideTestTitleLookup(): TestTitleLookup =
        TestTitleLookup { id -> PhoneDoctorTests.findById(id)?.title }
}
```

- [x] **Step 1: apply edits** — `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL.

---

### Task 5: Dashboard card + open-recording

**Files:** `feature/dashboard/build.gradle.kts` (+`core.database`), `DashboardViewModel.kt`, `DashboardScreen.kt`.

- `DashboardViewModel`: inject `sessionDao`, `resultDao`; add:
```kotlin
data class InsightHomeSummary(val pass: Int, val fail: Int, val total: Int, val startedAt: Long)
val latestInsight: StateFlow<InsightHomeSummary?> =
    combine(sessionDao.observeAll(), resultDao.observeAll()) { s, r ->
        val latest = s.filter { it.overallStatus == "COMPLETED" }.maxByOrNull { it.startedAtEpochMs } ?: return@combine null
        val rs = r.filter { it.sessionId == latest.id }
        InsightHomeSummary(rs.count { it.state == "PASS" }, rs.count { it.state == "FAIL" }, rs.size, latest.startedAtEpochMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

fun recordOpen(tool: Tool) { viewModelScope.launch { settings.recordRecent(tool.id) } }
```
- `DashboardScreen`: add `onOpenTool("insights")` card (`Modifier.clickable`) after "Elevated access", shown when `latestInsight != null` — `Insights — Latest diagnostic: {pass}/{total} passed{· N failed} · {date} →`. Also call `viewModel.recordOpen(tool)` inside each `ToolCard` `onClick` (currently unrecorded — fixes under-counted recents AND usage).
- [x] **Step 1: edits** — `./gradlew :feature:dashboard:assembleDebug` → SUCCESS.

---

### Task 6: Docs + verification

- `README.md` — add bullet under Features (e.g. after Reports bullet): `- **Insights** — on-device analytics: latest-diagnostic verdict donut, per-session pass-rate trend, tests needing attention, storage free-space sparkline, activity stats and most-used tools. Home shows a summary card; all numbers come from Room.`
- `CHANGELOG.md` — add under `0.9.0-alpha.1` Added: `- Insights (on-device analytics): :feature:insights screen — diagnostic health trends, per-test failure ranking, storage free-space sparkline, activity stats and most-used tools — plus a dashboard summary card; storage snapshot codec moved to :core:database`
- Full validation:
```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew testDebugUnitTest
./gradlew :app:assembleDebug
```

---

## Self-review notes

- Spec coverage: donut ✓, trend bars ✓, attention list ✓, storage sparkline ✓, activity tiles ✓ (Diagnostics/Tests recorded/Tools used/NFC scans/Reports saved/Elevated actions), most-used tools ✓, range chips ✓, home card ✓, empty states ✓, on-device footer ✓.
- `deviceinfo` codec move keeps `StorageLabViewModel` working via added imports; test travels with the codec.
- `TestTitleLookup` impl lives in `:feature:diagnostics` (owns `PhoneDoctorTests`); insights depends only on the `core:common` interface — dependency direction preserved.
- "Most used tools" counting piggybacks `recordRecent` — also fixes dashboard opens not being recorded (pre-existing gap).
- Implementation deviations: dashboard card reuses `SessionHealth`/`InsightsCalculator.health` instead of a separate `InsightHomeSummary` (same data, less code); `HealthState` gained `snapshotCount` for the Snapshots tile; activity tiles include "Reports saved" (7 tiles). All tasks complete — `:feature:insights:assembleDebug`, `:app:assembleDebug`, and `testDebugUnitTest` on changed modules (core:common, core:database, feature:diagnostics) all green.
