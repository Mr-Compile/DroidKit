# Changelog

## [0.4.0] - Unreleased

### Added
- Phone Doctor hardware set (full sequence now 21 tests): speaker 440 Hz
  tone, microphone record/playback with RECORD_AUDIO permission gate,
  front/rear camera preview (CameraX), torch flash test, five auto-recorded
  connectivity capability checks (Wi-Fi, Bluetooth, GNSS, NFC, USB host/
  accessory), charging/battery check with live readings
- `PermissionGate`: runtime-permission wrapper reporting PERMISSION_REQUIRED
  on denial; CAMERA and RECORD_AUDIO declared with optional uses-features
- Session review: state-count summary header and per-category grouping;
  quick tests grouped by category on the Diagnose tab

## [0.3.0] - Unreleased

### Added
- Phone Doctor: guided diagnostic session engine (ordered 10-test CORE
  sequence, Pass/Fail/Skip judgment, retry of failed and skipped tests,
  technician notes, Room-persisted results)
- Display tests: fullscreen R/G/B/white/black color cycle and dead-pixel check
- Touch tests: 8x12 grid coverage tracker (Pass gated at 90% coverage) and
  multitouch point counter (per-pointer markers, max-count capture)
- Sensor tests: accelerometer, gyroscope, proximity, light and magnetometer
  screens with live readings; absent hardware reports NOT_SUPPORTED
- Vibration test via VibratorManager (VIBRATE permission declared)
- Session review screen with per-test result states and save flow

## [0.2.0] - Unreleased

### Added
- Device Inspector rebuild: sectioned dashboard (Identity, OS, Hardware,
  Memory/Storage, Display, Battery, Hardware features, Access providers) with
  per-field copy, copy-section, and share-summary
- Storage Lab: volume list with capacity/free-space reads and low-space warning
- Battery Lab: charge/status/source, temperature/voltage/technology/health,
  instantaneous current and battery-saver state, warm-battery notice
- Sensor Lab: full inventory (vendor, type, range, resolution, power, wake-up)
  plus live monitor for safe sensor types
- `DeviceSystemProbe` extensions: `memory`, `batteryStatus`, `cameraSummary`,
  `displayModeRatesHz`, richer `ProbeSensor` fields, `formatBytes`/`formatHz`
- `core:ui` `InfoRow` and `SectionCard` primitives

## [0.1.0] - Unreleased

### Added
- Multi-module project skeleton (app, 7 core, 4 privilege, 21 feature modules)
- Design system (Material3, dynamic color, shared components)
- ToolRegistry with search/category/favorites foundations
- CapabilityManager + provider chain (Standard ready; Shizuku/ADB/Root stubs)
- PermissionManager, DeviceSystemProbe (hardware/API detection)
- Room schema v1 (diagnostic sessions, results, technician notes)
- DataStore settings (theme, favorites, recents)
- Home / Tools / Diagnose / Settings navigation shells
- Device Inspector screen with live system probes
