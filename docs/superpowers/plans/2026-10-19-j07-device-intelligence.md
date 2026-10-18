# J07 Device Intelligence (v0.2.0) Implementation Plan

> **STATUS: IMPLEMENTED** — all 6 tasks executed on branch `feature/j07-device-intelligence`
> (2026-10-19). `:app:assembleDebug` green; `:core:system` formatter tests pass (3).
> Open gate items: real-device verification and the `v0.2.0` tag at merge to `main`.
> Deviations: battery lab route signature simplified (`batteryGraph(nav)`), live
> sensor monitor restricted to the plan's safe list, `SensorManager.SENSOR_DELAY_UI`
> constant corrected, `LocalClipboard` used over deprecated `LocalClipboardManager`.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn Device Inspector into DroidKit's device-intelligence dashboard — sectioned inspection with copy/share — and ship real Battery Lab, Storage Lab, and Sensor Lab (inventory + live monitor). Tag `v0.2.0`.

**Architecture:** Extends the skeleton. `DeviceSystemProbe` gains the richer read-only fields the labs need; feature modules keep the skeleton convention (`FeatureContribution` object + `NavGraphBuilder` extension + Hilt VM). No sessions, no diagnostics engine — that is J08.

**Tech Stack:** Kotlin, Compose M3, Hilt, `core:system` probe, `core:ui` design system.

## Global Constraints

- Requires: skeleton plan `plans/2026-10-18-droidkit-app-skeleton.md` fully executed (Q8) — all signatures below come from it
- Rules: root `AGENTS.md`; specs: `specs/feature_plans_v2/features/10_DEVICE_INSPECTOR_V2.md`, `18_SENSOR_LAB_V2.md`, `20_BATTERY_STORAGE_LABS_V2.md`; gate: `playbook/09_J07_V0.2_DEVICE_INSPECTOR.md`
- Honesty: never invent battery-health %, NAND health, serials, calibration status. Missing value → show `Not exposed` / `NOT_SUPPORTED`, never `FAIL`
- Package root `io.github.droidkit`; minSdk 28 / target 36; commit per task, push to `origin` after every commit
- JDK for builds: `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`

---

### Task 1: Extend `DeviceSystemProbe` for the labs

**Files:**
- Modify: `core/system/src/main/java/io/github/droidkit/core/system/DeviceSystemProbe.kt`
- Test: `core/system/src/test/java/io/github/droidkit/core/system/ProbeFormattersTest.kt`

**Interfaces:**
- Consumes: existing probe (Task 4 of skeleton)
- Produces: `ProbeSensor` upgraded fields; new `BatteryStatus`, `CameraSummary`, `MemoryInfo` data classes; probe fields `memory`, `batteryStatus`, `cameraSummary`, `displayModeRatesHz`; formatter `formatBytes(Long): String`, `formatHz(Float): String`

- [x] **Step 1: Write the failing formatter test**

```kotlin
package io.github.droidkit.core.system

import org.junit.Assert.assertEquals
import org.junit.Test

class ProbeFormattersTest {
    @Test fun `formatBytes renders GB for large values`() {
        assertEquals("64.0 GB", formatBytes(64L * 1024 * 1024 * 1024))
    }
    @Test fun `formatBytes renders MB under 1 GB`() {
        assertEquals("512 MB", formatBytes(512L * 1024 * 1024))
    }
    @Test fun `formatHz trims trailing zero`() {
        assertEquals("120 Hz", formatHz(120f))
        assertEquals("59.94 Hz", formatHz(59.94f))
    }
}
```

- [x] **Step 2: Upgrade `ProbeSensor` + add data classes** (top of `DeviceSystemProbe.kt`)

```kotlin
data class ProbeSensor(
    val name: String,
    val vendor: String,
    val type: Int,
    val stringType: String,
    val version: Int,
    val maxRange: Float,
    val resolution: Float,
    val powerMa: Float,
    val isWakeUp: Boolean,
    val minDelayUs: Int,
)

data class BatteryStatus(
    val levelPct: Float,        // -1 when not exposed
    val statusText: String,     // Charging | Discharging | Full | Not charging | Unknown
    val pluggedText: String,    // AC | USB | Wireless | Not plugged
    val temperatureC: Float,    // -1f when not exposed
    val voltageMv: Int,         // -1 when not exposed
    val technology: String?,    // null when not exposed
    val healthText: String?,    // Good | Overheat | Dead | ... null when not exposed
    val currentNowUa: Int?,     // instantaneous current, null when unsupported
)

data class CameraSummary(val total: Int, val backFacing: Int, val frontFacing: Int, val external: Int)
data class MemoryInfo(val totalMb: Long, val availableMb: Long, val lowMemory: Boolean, val thresholdMb: Long)
```

`ProbeSensor` gains `stringType` (`sensor.stringType ?: "custom"`), `version`, `maxRange`, `resolution`, `powerMa` (`sensor.power`), `isWakeUp`, `minDelayUs` — all straight `android.hardware.Sensor` getters. Update the `sensors` lazy to fill them.

- [x] **Step 3: Add `memory`, `batteryStatus`, `cameraSummary`, `displayModeRatesHz`**

```kotlin
val memory: MemoryInfo by lazy {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val info = ActivityManager.MemoryInfo()
    am.getMemoryInfo(info)
    MemoryInfo(info.totalMem / (1024 * 1024), info.availMem / (1024 * 1024),
        info.lowMemory, info.threshold / (1024 * 1024))
}

val batteryStatus: BatteryStatus
    get() {
        val i: android.content.Intent? = context.registerReceiver(
            null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val raw = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val level = if (raw in 0..100) raw.toFloat() else -1f
        val cur = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (i == null) {
            return BatteryStatus(level, "Unknown", "Not plugged", -1f, -1, null, null,
                cur?.takeIf { it != Int.MIN_VALUE && it != 0 })
        }
        val status = when (i.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)) {
            android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            android.os.BatteryManager.BATTERY_STATUS_FULL -> "Full"
            android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }
        val plugged = when (i.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, 0)) {
            android.os.BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            android.os.BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not plugged"
        }
        val tempTenths = i.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, -1000)
        val volts = i.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, -1)
        val health = when (i.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, -1)) {
            android.os.BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            android.os.BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            android.os.BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified failure"
            else -> null
        }
        return BatteryStatus(level, status, plugged,
            if (tempTenths > -1000) tempTenths / 10f else -1f, volts,
            i.getStringExtra(android.os.BatteryManager.EXTRA_TECHNOLOGY), health,
            cur?.takeIf { it != Int.MIN_VALUE && it != 0 })
    }

val cameraSummary: CameraSummary by lazy {
    val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    var back = 0; var front = 0; var ext = 0
    try {
        cm?.cameraIdList?.forEach { id ->
            when (cm.getCameraCharacteristics(id)
                .get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)) {
                android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK -> back++
                android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT -> front++
                else -> ext++
            }
        }
    } catch (e: Exception) { /* leave zeros — absence is honest */ }
    CameraSummary(back + front + ext, back, front, ext)
}

val displayModeRatesHz: List<Float>
    get() = context.display?.supportedModes?.map { it.refreshRate }?.distinct()?.sorted()
        ?: emptyList()
```

Keep existing `batteryCapacityPct` (delegates to `batteryStatus.levelPct`) so Task 15 code still compiles.

- [x] **Step 4: Add formatters** (bottom of file, outside class)

```kotlin
fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024 * 1024)
    return if (gb >= 1) "%.1f GB".format(gb) else "${bytes / (1024 * 1024)} MB"
}
fun formatHz(hz: Float): String =
    if (hz == hz.toInt().toFloat()) "${hz.toInt()} Hz" else "%.2f Hz".format(hz)
```

- [x] **Step 5: Run test + compile**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :core:system:testDebugUnitTest :core:system:compileDebugKotlin
```

Expected: 3 tests PASS, BUILD SUCCESSFUL.

- [x] **Step 6: Commit**

```bash
git add core/system
git commit -m "feat(core): probe fields for device intelligence labs"
git push origin main
```

---

### Task 2: `:core:ui` — info row + section card primitives

**Files:**
- Audit first: list exported composables in `core/ui/…` (Task 9) — reuse `SectionHeader`, `ToolCard`, `StatusBadge`, `BlockedStateView`, `EmptyState`; do not recreate
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/InfoRow.kt`
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/SectionCard.kt`

**Interfaces:**
- Produces: `InfoRow(label: String, value: String?, copyable: Boolean = true)` (renders `Not exposed` when value null); `SectionCard(title: String, content: @Composable ColumnScope.() -> Unit)`

- [x] **Step 1: `InfoRow.kt`**

```kotlin
package io.github.droidkit.core.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun InfoRow(label: String, value: String?, copyable: Boolean = true) {
    val clipboard = LocalClipboardManager.current
    val shown = value ?: "Not exposed"
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(shown, style = MaterialTheme.typography.bodyMedium)
        if (copyable && value != null) {
            TextButton(onClick = { clipboard.setText(AnnotatedString("$label: $value")) }) {
                Text("Copy")
            }
        }
    }
}
```

- [x] **Step 2: `SectionCard.kt`** — `Card(fillMaxWidth)` wrapping `Column(16.dp padding)` that emits `SectionHeader(title)` then `content()`.

- [x] **Step 3: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :core:ui:compileDebugKotlin
git add core/ui
git commit -m "feat(core-ui): InfoRow and SectionCard primitives"
git push origin main
```

---

### Task 3: `:feature:deviceinfo` — dashboard rebuild + Storage Lab

**Files:**
- Modify: `feature/deviceinfo/…/DeviceInfoFeature.kt` (tools list + routes)
- Modify: `feature/deviceinfo/…/DeviceInfoScreen.kt` (replace with dashboard)
- Create: `feature/deviceinfo/…/StorageLabScreen.kt`
- Modify: `feature/deviceinfo/build.gradle.kts` (add `androidx.lifecycle.viewmodel.compose` is already implied by hilt nav; add `projects.core.database` only if snapshot save is included — it is NOT in J07; keep deps unchanged)

**Interfaces:**
- Consumes: `DeviceSystemProbe` (all fields), `InfoRow`, `SectionCard`, `formatBytes`, `formatHz`
- Produces: routes `deviceinfo` (dashboard) and `deviceinfo/storage` (Storage Lab); tools `deviceinfo.overview`, `deviceinfo.storage`

- [x] **Step 1: `DeviceInfoFeature.kt`** — register both tools:

```kotlin
override val tools = listOf(
    Tool(id = "deviceinfo.overview", title = "Device Inspector",
        description = "Identity, OS, hardware, memory, storage, display, battery, capabilities",
        category = ToolCategory.DEVICE, route = ROUTE,
        keywords = setOf("phone info", "build", "hardware")),
    Tool(id = "deviceinfo.storage", title = "Storage Lab",
        description = "Volumes, capacity, free space, storage warnings",
        category = ToolCategory.DEVICE, route = "$ROUTE/storage",
        keywords = setOf("disk", "space", "memory")),
)

fun NavGraphBuilder.deviceInfoGraph(nav: NavHostController) {
    composable(DeviceInfoFeature.ROUTE) { DeviceInfoScreen(nav) }
    composable("${DeviceInfoFeature.ROUTE}/storage") { StorageLabScreen() }
}
```

- [x] **Step 2: `DeviceInfoScreen.kt`** — sectioned dashboard. Sections in order: **Identity** (manufacturer/model/device/product), **OS** (release/API/securityPatch), **Hardware** (ABIs, cameraSummary, sensors count, `capabilitySummary()` rows: NFC/BT/BLE/USB-host/telephony/location via `hasFeature`), **Memory/Storage** (`memory` fields, `storage` via `formatBytes`, row linking to `deviceinfo/storage`), **Display** (`displayInfo` + `displayModeRatesHz`), **Battery** (`batteryStatus` level/status/plugged/temp — each `InfoRow` shows `Not exposed` when null/−1), **Capabilities** (provider statuses via `CapabilityManager.providerStatuses()` — inject into VM).

Actions per spec: `InfoRow(copyable)` for every field; `Copy Section` TextButton per SectionCard copying `label: value` lines; `Share Summary` button in the top bar → `Intent.ACTION_SEND` `text/plain` with `probe.summary() + probe.capabilitySummary()`; `Launch Related Lab` — rows that navigate to `deviceinfo/storage`, `battery`, `sensors`.

State model (gate requirement): probe reads are synchronous, so "loading" is a brief first-composition frame; "partial" is the normal case — every `InfoRow` with a null/−1 source renders `Not exposed`; a section whose every field is absent renders `BlockedStateView`-style `NOT_SUPPORTED` body instead of an empty card; wrap section reads in runCatching → error state card on throw.

- [x] **Step 3: `StorageLabScreen.kt`** — `LazyColumn` of `SectionCard`s: Internal volume (`formatBytes` total/free/used + free-space warning when <10%), app-accessible volumes (iterate `context.getExternalFilesDirs(null)` — path + `StatFs` totals + `Environment.getExternalStorageState`), DroidKit directories note. Header text: "Free space is filesystem-visible space — not NAND health."

- [x] **Step 4: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:deviceinfo:compileDebugKotlin
git add feature/deviceinfo
git commit -m "feat(deviceinfo): device intelligence dashboard + storage lab"
git push origin main
```

---

### Task 4: `:feature:battery` — real Battery Lab

**Files:**
- Modify: `feature/battery/build.gradle.kts` — add `id("droidkit.hilt")`, deps `core.system`, `androidx.hilt:hilt-navigation-compose` via catalog as used by deviceinfo
- Modify: `feature/battery/…/BatteryFeature.kt` (tool registration), `feature/battery/…/BatteryScreen.kt` or equivalent stub → real screen
- Create: `feature/battery/…/BatteryViewModel.kt`

**Interfaces:**
- Consumes: `probe.batteryStatus`
- Produces: route `battery`, tool `battery.lab`

- [x] **Step 1: `BatteryViewModel`** — `@HiltViewModel` holding `probe`; `refresh()` re-reads `batteryStatus` (battery is live — refresh on `ON_RESUME` via `LifecycleEventObserver`).

- [x] **Step 2: `BatteryScreen`** — `SectionCard("Charge")` rows: level %, status, source; `SectionCard("Conditions")`: temperature °C (`Not exposed` if −1), voltage mV, technology, health text; `SectionCard("Power")`: current-now µA when non-null else `Not exposed`, battery-saver state via `PowerManager.isPowerSaveMode`. Conservative warning line only when `temperatureC >= 45` → "Battery unusually warm — let device cool" (documented threshold, no health %).

- [x] **Step 3: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:battery:compileDebugKotlin
git add feature/battery
git commit -m "feat(battery): real battery lab"
git push origin main
```

---

### Task 5: `:feature:sensors` — inventory + live monitor

**Files:**
- Modify: `feature/sensors/build.gradle.kts` — same hilt/system additions as Task 4
- Modify: `feature/sensors/…/SensorsFeature.kt` — tools `sensors.lab` (route `sensors`), `sensors.live` (route `sensors/live`)
- Modify: `feature/sensors/…/SensorsScreen.kt` — inventory
- Create: `feature/sensors/…/LiveSensorScreen.kt`

**Interfaces:**
- Consumes: `probe.sensors` (upgraded `ProbeSensor`), `SensorManager` via injected `Context`
- Produces: routes `sensors`, `sensors/live`; live monitor arg `sensorType: Int`

- [x] **Step 1: `SensorsScreen`** — inventory `LazyColumn`: one `SectionCard` per sensor — name, vendor, `stringType`, version, maxRange + unit, resolution, power mA, wakeup flag. Tap → `nav.navigate("sensors/live/${sensor.type}")`.

- [x] **Step 2: `LiveSensorScreen`** — restrict to safe list `ACCELEROMETER, GRAVITY, LINEAR_ACCELERATION, GYROSCOPE, MAGNETIC_FIELD, LIGHT, PROXIMITY, PRESSURE, AMBIENT_TEMPERATURE, RELATIVE_HUMIDITY, ROTATION_VECTOR`; absent type → `BlockedStateView`-style "Sensor not present" (`NOT_SUPPORTED`, never FAIL). `DisposableEffect` registers `SensorEventListener` on `SensorManager.getDefaultSensor(type)`, unregisters on dispose. Show x/y/z values + units + update rate; simple numeric display (no charting lib — bar via `LinearProgressIndicator` normalized to maxRange is enough).

- [x] **Step 3: Compile + commit**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :feature:sensors:compileDebugKotlin
git add feature/sensors
git commit -m "feat(sensors): sensor inventory and live monitor"
git push origin main
```

---

### Task 6: Registration, device pass, release gate v0.2.0

**Files:**
- Modify: `app/build.gradle.kts` or version source — `versionName = "0.2.0"`, `versionCode` incremented
- Modify: `CHANGELOG.md` — `[0.2.0]` entry (Added: dashboard, battery/storage/sensor labs)
- Modify: `docs/superpowers/specs/10_DroidKit_Master_Journey_Checklist.md` — tick J07 items actually verified
- Modify: `docs/superpowers/plans/2026-10-18-droidkit-master-execution-plan.md` — mark Q9 done

- [x] **Step 1: Full assemble + smoke**

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :app:assembleDebug
```

Expected: BUILD SUCCESSFUL. Install on a real device/emulator: dashboard sections populate, storage/battery/sensor screens open from Tools search and dashboard links, unavailable fields read `Not exposed`.

- [x] **Step 2: Verify honesty rules** — grep screens for hardcoded claims; every absent value path shows `Not exposed`; no battery-health %, no NAND claims.

- [x] **Step 3: Version, CHANGELOG, checklist, tag**

```bash
git add -A
git commit -m "release: v0.2.0 device intelligence"
git tag v0.2.0
git push origin main --tags
```

---

## Release gate (J07 exit)

- [ ] Supported fields verified on device; unsupported fields represented honestly
- [ ] Sub-tools independently launchable and registered (`deviceinfo.overview`, `deviceinfo.storage`, `battery.lab`, `sensors.lab`, `sensors.live`)
- [ ] Targeted tests pass (`:core:system` unit tests + module compiles)
- [ ] CHANGELOG, `versionName 0.2.0`, bumped `versionCode`, tag `v0.2.0`
- [ ] Tracker + execution plan updated
