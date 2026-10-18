# J09 Phone Doctor Hardware (v0.4.0) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend Phone Doctor with the J09 hardware set — speaker, microphone, front/rear camera, flash, five connectivity capability checks, and a charging/battery check — folded into the existing guided engine. Tag `v0.4.0`.

**Architecture:** All test screens live in `feature:diagnostics` (same as J08 — Phone Doctor tests are diagnostics, not labs). `PhoneDoctorTests` gains a `HARDWARE` list; the guided engine runs `ALL = CORE + HARDWARE` (19 tests). CameraX provides camera preview/torch; `AudioTrack` plays a generated PCM sine for the speaker test; `MediaRecorder`/`MediaPlayer` handle mic record/playback. Runtime permissions use `rememberLauncherForActivityResult` + `PermissionChecker` for pre-checks.

**Tech Stack:** Kotlin, Compose M3, CameraX 1.4.2, Hilt, Room.

## Global Constraints

- Requires: J08 executed — `SessionEngine`, `TestResultBar`, `PhoneDoctorTestScreen` dispatch, `diagnose/test/{testId}` route all exist
- Specs: `specs/feature_plans_v2/features/11_PHONE_DOCTOR_V2.md`, `19_CAMERA_DISPLAY_AUDIO_LABS_V2.md`
- Honesty: absent hardware → `NOT_SUPPORTED` (auto-emit, never FAIL). Denied permission → `PERMISSION_REQUIRED`. Capability checks auto-PASS only when the capability is objectively present (enabled state recorded in note). Sensory tests (speaker, mic, camera, flash, charging) stay manual-judgment
- No fabricated values: camera specs not exposed by CameraX are not invented
- New deps: `androidx.camera:camera-core/camera2/lifecycle/view:1.4.2` (stable, >7d old) added to catalog + `feature:diagnostics`. No other new deps (AudioTrack/MediaRecorder/MediaPlayer are framework)
- Manifest additions to `feature/diagnostics`: CAMERA + RECORD_AUDIO permissions, `<uses-feature>` for camera/microphone with `required="false"`
- Package root `io.github.droidkit`; commit per task, push after every commit; `JAVA_HOME` export for builds

---

### Task 1: Test registry extension + CameraX deps + manifest

**Files:**
- Modify: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/PhoneDoctorTest.kt`
- Modify: `feature/diagnostics/build.gradle.kts`
- Modify: `gradle/libs.versions.toml` — `cameraX = "1.4.2"` + 4 library entries
- Modify: `feature/diagnostics/src/main/AndroidManifest.xml`

- [ ] **Step 1: Add `HARDWARE` list** — `audio.speaker`, `audio.microphone` (permission RECORD_AUDIO), `camera.front`, `camera.rear`, `camera.flash`, `connectivity.wifi`, `connectivity.bluetooth`, `connectivity.location`, `connectivity.nfc`, `connectivity.usb`, `power.charging`. `PhoneDoctorTest` gains optional fields `permission: String?`, `lensFacing: Int?`, `capabilityKey: String?`. `ALL = CORE + HARDWARE`; `findById` searches `ALL`. Engine + screens use `ALL`

- [ ] **Step 2: Catalog + module deps + manifest** — CAMERA/RECORD_AUDIO permissions; uses-feature camera.any/microphone `required=false`; feature uses-features for wifi/bluetooth/nfc/location also `required=false` (diagnostics must run on tablets lacking radios)

- [ ] **Step 3: Update `SessionEngineTest`** to construct `SessionEngine(PhoneDoctorTests.ALL)` and assert 19 total; keep ordering (CORE first)

- [ ] **Step 4: Compile + test** `./gradlew :feature:diagnostics:testDebugUnitTest` — green; commit `feat(diagnostics): j09 hardware test registry, camerax deps, permissions`

---

### Task 2: Speaker + microphone tests

**Files:**
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/SpeakerTestScreen.kt`
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/MicrophoneTestScreen.kt`
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/PermissionGate.kt`

- [ ] **Step 1: `PermissionGate` composable** — wraps a test needing a runtime permission: shows "requires {permission} to run" + Request button; granted → renders content; denied → emits `PERMISSION_REQUIRED`. Uses `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`

- [ ] **Step 2: `SpeakerTestScreen`** — `AudioTrack` (MODE_STATIC, 22050Hz mono PCM16) plays a generated 440 Hz sine (1 s); three buttons: left-speaker tone (pan via channel config note — keep simple: single tone), repeat, Pass/Fail/Skip bar. AudioManager absent → NOT_SUPPORTED

- [ ] **Step 3: `MicrophoneTestScreen`** — wrapped in `PermissionGate(RECORD_AUDIO)`: `MediaRecorder` records 3 s to `cacheDir/mic_test.m4a` (AAC), `MediaPlayer` plays back; Record/Play buttons + Pass/Fail/Skip. Recorder start failure → show error state + allow user judgment, never crash

- [ ] **Step 4: Dispatch + compile + commit** `feat(diagnostics): speaker and microphone tests`

---

### Task 3: Camera + flash tests

**Files:**
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/CameraTestScreen.kt`
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/FlashTestScreen.kt`

- [ ] **Step 1: `CameraTestScreen(lensFacing)`** — inside `PermissionGate(CAMERA)`: `ProcessCameraProvider.getInstance(context)` + addListener main executor → `hasCamera(cameraSelector)` check → absent → NOT_SUPPORTED; preview via `AndroidView(PreviewView)` + `Preview` use case bound to `LocalLifecycleOwner`. Provider init failure → UNKNOWN state with message

- [ ] **Step 2: `FlashTestScreen`** — same provider; `cameraInfo.hasFlashUnit()` false → NOT_SUPPORTED; else toggle button `cameraControl.enableTorch(bool)` + Pass/Fail/Skip; torch off on dispose

- [ ] **Step 3: Dispatch + compile + commit** `feat(diagnostics): camera and flash tests`

---

### Task 4: Connectivity + charging checks

**Files:**
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/ConnectivityCheckScreen.kt`
- Create: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/tests/ChargingCheckScreen.kt`

- [ ] **Step 1: `ConnectivityCheckScreen(test)`** — `capabilityKey` drives check: `wifi`→`PackageManager.hasSystemFeature(FEATURE_WIFI)` + `WifiManager.isWifiEnabled`; `bluetooth`→FEATURE_BLUETOOTH + `BluetoothAdapter.isEnabled`; `location`→FEATURE_LOCATION_GPS + `LocationManager.isLocationEnabled`; `nfc`→FEATURE_NFC + `NfcAdapter.isEnabled`; `usb`→FEATURE_USB_HOST or USB accessory. Absent feature → auto `NOT_SUPPORTED`; present → auto `PASS` with note recording enabled/disabled state (capability check, not a sensory judgment). Status panel shows what was measured

- [ ] **Step 2: `ChargingCheckScreen`** — `probe.batteryStatus` null → NOT_SUPPORTED; else panel: charging state, plug source, level, temperature + instructions ("plug in a charger and watch the state change") — manual Pass/Fail/Skip

- [ ] **Step 3: Dispatch + compile + commit** `feat(diagnostics): connectivity and charging checks`

---

### Task 5: Full-sequence wiring + summary

**Files:**
- Modify: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/DiagnosticsViewModel.kt` — engine uses `ALL`
- Modify: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/PhoneDoctorTestScreen.kt` — full dispatch
- Modify: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/DiagnosticsScreen.kt` — quick tests grouped by category
- Modify: `feature/diagnostics/src/main/java/io/github/droidkit/feature/diagnostics/SessionReviewScreen.kt` — category grouping + state counts header

- [ ] **Step 1: Wire `ALL` into engine + screens**

- [ ] **Step 2: Review screen summary** — "PASS 12 · FAIL 1 · SKIPPED 3 · NOT SUPPORTED 2" header + `SectionHeader` per category

- [ ] **Step 3: Build** `./gradlew :app:assembleDebug` green; commit `feat(diagnostics): full 19-test sequence and summary review`

---

### Task 6: Release gate — 0.4.0, changelog, checklist, plan, queue

- [ ] **Step 1: `0.4.0`/`4` + CHANGELOG entry**
- [ ] **Step 2: J09 checklist ticks + IMPLEMENTED banner + Q11 DONE in queue**
- [ ] **Step 3: `./gradlew testDebugUnitTest :app:assembleDebug` green**
- [ ] **Step 4: Commit + push**

## Execution notes

- Fill `- [ ]` → `- [x]` as each step lands
- `v0.4.0` tag at merge to `main`
