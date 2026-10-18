# J08 Phone Doctor Core (v0.3.0) Implementation Plan

> **STATUS: IMPLEMENTED** — all 7 tasks executed on branch `feature/j08-phone-doctor`
> (2026-10-19). `:app:compileDebugKotlin` green; `SessionEngineTest` passes (5 tests).
> Open gate items: end-to-end device run and the `v0.3.0` tag at merge to `main`.
> Deviations: shared `TestResultBar` extracted (Pass/Fail/Skip/note) used by all test
> screens; `sessionDao.complete` extended with `notes` param; standalone quick tests
> capture results without persisting (spec: save only inside a session).

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the diagnostics shell into a guided Phone Doctor: a session engine that walks the technician through the V1 CORE test set, captures honest per-test results (`TestResultState`), and persists them to Room. Tag `v0.3.0`.

**Architecture:** Extends the skeleton + J07 probe. `feature:diagnostics` gains a test registry, a guided-session runner (ordered tests with Pass/Fail/Skip), and the individual test screens. Room schema v1 already has `diagnostic_sessions` / `diagnostic_results` / `device_snapshots` — no migration needed. Sensor tests reuse the `SensorManager` listener pattern from `feature:sensors` (`LiveSensorScreen`).

**Tech Stack:** Kotlin, Compose M3, Hilt, Room, `core:system` probe, `core:ui` primitives.

## Global Constraints

- Requires: skeleton plan + J07 executed. `TestResultState` enum already exists in `core:common` (`PASS, FAIL, NOT_TESTED, NOT_SUPPORTED, PERMISSION_REQUIRED, UNKNOWN`)
- Rules: root `AGENTS.md`; specs: `specs/feature_plans_v2/features/11_PHONE_DOCTOR_V2.md`, `18_SENSOR_LAB_V2.md`; gate: `playbook/` J08 doc if present, else the J08 checklist section
- Honesty: manual tests never auto-pass because an API call succeeded — the user presses Pass/Fail. Missing hardware → `NOT_SUPPORTED`, never `FAIL`. Aborted test → `NOT_TESTED`
- Scope: V1 CORE only — display (color + dead-pixel), touch (grid + multitouch), vibration, accelerometer, gyroscope, proximity, light, magnetometer. Audio/camera/connectivity tests are J09. Used-Phone-Check and Repair-Verification modes are POST-V1 — do not build
- Package root `io.github.droidkit`; minSdk 28 / target 36; commit per task, push to `origin` after every commit
- JDK for builds: `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`

---

### Task 1: Test registry, result model, session engine

**Files:**
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/PhoneDoctorTest.kt`
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/SessionEngine.kt`
- Test: `feature/diagnostics/src/test/java/io/github/droidkit/feature/diagnostics/SessionEngineTest.kt`

**Interfaces:**
- Consumes: `TestResultState` (core:common), `DiagnosticResultDao`/`DiagnosticSessionDao` (core:database)
- Produces: `data class PhoneDoctorTest(id, title, category, instructions, sensorType: Int?)`; `object PhoneDoctorTests { val CORE: List<PhoneDoctorTest> }`; `class SessionEngine { state: StateFlow<EngineState>; start(sessionId); record(testId, state, note?); skip(testId); advance(); finish() }` where `EngineState(current: PhoneDoctorTest?, index, total, results: Map<String, TestResultState>, done: Boolean)`

- [x] **Step 1: Write failing engine test** — `SessionEngineTest` in `feature/diagnostics/src/test/` (needs `testImplementation libs.junit` + `kotlinx-coroutines-test` in `feature/diagnostics/build.gradle.kts`): ordering preserved, `record` stores result and advances, `skip` stores `NOT_TESTED`, `finish` sets `done`, missing-sensor tests stay in list (screen decides NOT_SUPPORTED)

- [x] **Step 2: Define the CORE test list** — `PhoneDoctorTests.CORE` in display→touch→haptics→sensors order: `display.color`, `display.dead_pixel`, `touch.grid`, `touch.multitouch`, `haptics.vibration`, `sensor.accelerometer` (TYPE_ACCELEROMETER), `sensor.gyroscope` (TYPE_GYROSCOPE), `sensor.proximity` (TYPE_PROXIMITY), `sensor.light` (TYPE_LIGHT), `sensor.magnetometer` (TYPE_MAGNETIC_FIELD). Each test carries technician instructions text (from spec test groups)

- [x] **Step 3: Implement `SessionEngine`** — pure-Kotlin class, no Android deps: holds ordered list, current index, results map; `record`/`skip` mutate state and advance; `finish()` emits done. Inject DAO writes stay in the ViewModel layer (engine stays testable without Room)

- [x] **Step 4: Run** `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" && ./gradlew :feature:diagnostics:testDebugUnitTest` — expect green

- [x] **Step 5: Commit** `feat(diagnostics): phone-doctor test registry and session engine`

---

### Task 2: Display tests — color cycle + dead pixel

**Files:**
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/DisplayColorScreen.kt`

**Interfaces:**
- Produces: `@Composable fun DisplayColorScreen(onResult: (TestResultState, note: String?) -> Unit)` — immersive fullscreen; cycles solid R, G, B, white, black on tap; instructions overlay at start; for `display.dead_pixel` the copy asks the user to look for stuck/dead pixels on each color. Bottom bar (auto-hides after first tap; tap top edge or back-gesture to reveal): `Pass` / `Fail` / `Skip` buttons
- Uses `LocalActivity`/`WindowInsetsControllerCompat`? — simpler: `Modifier.fillMaxSize().background(color).pointerInput` only; system bars may remain (acceptable, documented). No new deps

- [x] **Step 1: Implement screen** — one composable handles both `display.color` and `display.dead_pixel` via a `variant` param (copy differs: "color uniformity & banding" vs "tap through each color, look for stuck/dead pixels"). Result callbacks: `onResult(PASS, null)` / `onResult(FAIL, note)` / `onResult(NOT_TESTED, null)`

- [x] **Step 2: Wire temp route** — add `diagnose/test/{testId}` composable in `diagnosticsGraph` (route constants in `DiagnosticsFeature`); screen resolved from `PhoneDoctorTests.findById(testId)`

- [x] **Step 3: Compile** `./gradlew :feature:diagnostics:compileDebugKotlin`

- [x] **Step 4: Commit** `feat(diagnostics): display color and dead-pixel test screens`

---

### Task 3: Touch tests — grid coverage + multitouch

**Files:**
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/TouchGridScreen.kt`
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/MultitouchScreen.kt`

**Interfaces:**
- `TouchGridScreen(onResult)` — Compose `Canvas` + `pointerInput` drag: screen divided into ~8x12 grid cells; dragging marks cells covered; live "X/96 covered" counter; `Pass` enabled only when coverage ≥90% (honesty: coverage is measured, judgment is still user's), `Fail`/`Skip` always available
- `MultitouchScreen(onResult)` — `pointerInput` tracking `event.changes.size` active pointers, draws one circle per pointer id; reports max simultaneous count; Pass/Fail/Skip; measured count included in result note

- [x] **Step 1: `TouchGridScreen`** — `remember { mutableStateOf(setOf<Int>()) }` for covered cells; `detectDragGestures` + `detectTapGestures` map offsets → cell index; draw covered cells semi-transparent accent over grid lines

- [x] **Step 2: `MultitouchScreen`** — track `PointerId → Offset` map inside `awaitPointerEventScope`; cap at device limit (10); show counter text overlay

- [x] **Step 3: Add both to `diagnose/test/{testId}` dispatch**

- [x] **Step 4: Compile** `./gradlew :feature:diagnostics:compileDebugKotlin`

- [x] **Step 5: Commit** `feat(diagnostics): touch-grid and multitouch test screens`

---

### Task 4: Sensor-based tests — accel, gyro, proximity, light, magnetometer

**Files:**
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/SensorTestScreen.kt`
- Modify: `feature/diagnostics/build.gradle.kts` (no new deps expected — `SensorManager` is framework)

**Interfaces:**
- `SensorTestScreen(test: PhoneDoctorTest, onResult)` — resolves `sensorType` → `SensorManager.getDefaultSensor`; `null` → immediately emits `NOT_SUPPORTED` (with note "No {name} sensor reported") — never shown as FAIL. Live reading panel reuses the listener pattern from `LiveSensorScreen` (`DisposableEffect` register/unregister, `SENSOR_DELAY_UI`). Per-sensor verification hint in `test.instructions` ("tilt the phone and watch X/Y/Z change", "cover the top bezel", "hold under a lamp"). Pass/Fail/Skip bar

- [x] **Step 1: Implement `SensorTestScreen`** — one composable covers all five tests via `test.sensorType`

- [x] **Step 2: Add to `diagnose/test/{testId}` dispatch**

- [x] **Step 3: Compile** `./gradlew :feature:diagnostics:compileDebugKotlin`

- [x] **Step 4: Commit** `feat(diagnostics): sensor diagnostic test screens`

---

### Task 5: Vibration test

**Files:**
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/VibrationScreen.kt`
- Modify: `feature/diagnostics/src/main/AndroidManifest.xml` — add `<uses-permission android:name="android.permission.VIBRATE"/>`

**Interfaces:**
- `VibrationScreen(onResult)` — "Vibrate" button fires `Vibrator.vibrate(VibrationEffect.createOneShot(400, DEFAULT_AMPLITUDE))` (API 26+, minSdk 28 fine); `Vibrator.hasVibrator() == false` → `NOT_SUPPORTED`. Repeat button + Pass/Fail/Skip

- [x] **Step 1: Manifest permission + screen**

- [x] **Step 2: Add to dispatch; compile; commit** `feat(diagnostics): vibration test screen`

---

### Task 6: Guided session runner + result persistence

**Files:**
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/GuidedSessionScreen.kt`
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/SessionReviewScreen.kt`
- Modify: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/DiagnosticsViewModel.kt`
- Modify: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/DiagnosticsFeature.kt`
- Modify: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/DiagnosticsScreen.kt`

**Interfaces:**
- `DiagnosticsViewModel` gains: `engine = SessionEngine()`; `startGuided()` → creates session+snapshot (existing `startSession` logic), seeds engine, returns sessionId; `recordResult(testId, state, note)` → engine + `resultDao.insert(DiagnosticResultEntity(...))`; `finishSession(notes)` → sets `completedAtEpochMs`, `overallStatus="COMPLETED"`, `notes`, persists
- `GuidedSessionScreen(sessionId, onDone)` — reads engine state; renders the current test's screen component inline (same dispatch map as `diagnose/test/{testId}`), progress `Test i of N` header; auto-advances on result; end → `SessionReviewScreen` (list of results with state chips, retry-failed button re-queues `NOT_TESTED`/`FAIL` tests, notes field, Save button → `finishSession`)
- `DiagnosticsScreen` — "Start Full Diagnostic" now navigates to `diagnose/guided` instead of silently creating a session; quick-test cards become the CORE list → `diagnose/test/{testId}` (standalone results are captured but only persisted if launched inside a session — standalone quick test results show a transient "not saved — run inside a diagnostic to save" note)

- [x] **Step 1: ViewModel engine integration + persistence**

- [x] **Step 2: `GuidedSessionScreen` + shared test-dispatch composable** (`@Composable fun PhoneDoctorTestScreen(test, onResult)` — single dispatch used by both quick-test route and guided runner)

- [x] **Step 3: `SessionReviewScreen`** — results list, retry button (engine.restart(ids)), notes `OutlinedTextField`, Save → pop to `diagnose`

- [x] **Step 4: Update `DiagnosticsScreen` buttons + nav graph** (`diagnose/guided`, `diagnose/review/{sessionId}` routes)

- [x] **Step 5: Build** `./gradlew :app:assembleDebug` — expect green; fix any wiring gaps

- [x] **Step 6: Commit** `feat(diagnostics): guided session engine with persisted results`

---

### Task 7: Release gate — version, changelog, checklist

**Files:**
- Modify: `build-logic/src/main/kotlin/droidkit/AndroidApplicationConventionPlugin.kt` (`0.3.0`/`3`)
- Modify: `CHANGELOG.md`
- Modify: `docs/superpowers/specs/10_DroidKit_Master_Journey_Checklist.md` — tick all J08 items + gate entries verified; device-dependent rows get "requires device run" notes
- Modify: `docs/superpowers/plans/2026-10-19-j08-phone-doctor-core.md` — IMPLEMENTED banner
- Modify: `docs/superpowers/plans/2026-10-18-droidkit-master-execution-plan.md` — mark Q10 DONE

- [x] **Step 1: Version + CHANGELOG** (0.3.0 entry: guided session engine, 10 core tests, honest result states)

- [x] **Step 2: Checklist + plan banners + queue**

- [x] **Step 3: Full build + unit tests** `./gradlew testDebugUnitTest :app:assembleDebug` — green

- [x] **Step 4: Commit** `docs(j08): mark plan implemented, bump to 0.3.0` and push

---

## Execution notes

- Fill `- [ ]` → `- [x]` as each step lands (this file is the live tracker)
- Add "Discovered Issues & Fixes" section at the end if implementation deviates
- `v0.3.0` tag is created at merge to `main`, same convention as v0.1.0/v0.2.0
