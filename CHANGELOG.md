# Changelog

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
