# DroidKit - Master Product Plan

## 1. Product Vision
DroidKit is an Android-first technician, developer, and power-user toolkit. It combines hardware diagnostics, device inspection, application inspection, USB/NFC utilities, networking tools, and optional elevated capabilities through Shizuku, Wireless ADB, and eventually root.

The app must remain useful without elevated access. Privileged backends unlock additional tools rather than becoming a hard dependency.

## 2. Target Users
- Phone repair technicians
- Second-hand phone buyers/sellers
- Android power users
- Android developers
- IT/support personnel

## 3. Product Principles
1. Android-first and native.
2. Local-first; no account or cloud backend required for V1.
3. Capability-based instead of privilege-backend-specific.
4. Never claim unavailable data as a failure.
5. Explain risky actions before execution.
6. Modular architecture so tools can be added independently.
7. Use Android-supported APIs wherever possible.
8. Advanced operations must degrade gracefully when Shizuku/ADB/root is unavailable.

## 4. Recommended Technology Stack
- Language: Kotlin
- UI: Jetpack Compose
- Architecture: Clean Architecture + MVVM
- Dependency injection: Hilt
- Async/state: Kotlin Coroutines + Flow
- Database: Room
- Preferences/settings: DataStore
- Navigation: Navigation Compose
- Background work: WorkManager
- Serialization: Kotlin Serialization
- Networking: OkHttp; Retrofit only where an HTTP API is actually needed
- Camera: CameraX + CameraManager
- USB: Android USB Host API / UsbManager
- NFC: Android NFC APIs
- Bluetooth/BLE: Android Bluetooth APIs
- Sensors: SensorManager
- Location: Android Location APIs
- Privileged backend: Shizuku
- ADB backend: local Wireless ADB layer
- Root backend: optional future module
- Testing: JUnit plus focused Compose/instrumented tests

## 5. Android Support Direction
Initial planning target:
- minSdk: Android 8 / API 26
- compileSdk and targetSdk: current stable values at implementation time

Every tool must declare its minimum Android version, required hardware, permissions, and capability requirements.

## 6. Product Areas
### Device Inspector
Android version, build information, CPU/ABI, RAM, storage, display, battery, hardware features, cameras, sensors, connectivity capabilities.

### Phone Doctor
Interactive diagnostics for display, touch, multitouch, audio, microphone, camera, flash, vibration, sensors, connectivity, charging, and other observable hardware.

### App Inspector
Installed apps, system/user classification, package metadata, APK path and version information, permissions, activities, services, receivers, providers, and supported export/launch actions.

### Permission and AppOps Tools
Permission inspection in standard mode; elevated AppOps and supported permission operations through capability providers.

### USB Lab
OTG/USB detection, VID/PID, product/manufacturer information when exposed, interfaces, endpoints, and descriptors accessible through Android.

### NFC Lab
Tag detection, technology list, NDEF records, reading, supported writing, erase/format operations where the tag/API permits, and record export.

### Network Lab
Connection information, Wi-Fi/network status, IP information, ping/DNS utilities, and diagnostics that are allowed by Android.

### Developer Lab
Intent launcher, activity launcher, deep-link testing, package/component inspection, display metrics, developer shortcuts, and supported log tools.

### Diagnostic Reports
Store diagnostic sessions locally and export/share technician-friendly reports.

## 7. Privilege Model
DroidKit uses capability providers:

Standard Android -> Shizuku -> Wireless ADB -> Optional Root

A feature asks for a capability, not a specific backend. Example:
- NFC_READER may be provided by Standard Android.
- APP_OPS_CONTROL may be provided by Shizuku, ADB, or Root.
- ADVANCED_PACKAGE_CONTROL may be provided by Shizuku, ADB, or Root.

## 8. Risk Model
- Level 0 Safe: read-only device information and diagnostics.
- Level 1 Minor: launch settings, activities, intents.
- Level 2 System Change: supported setting/permission/AppOps changes.
- Level 3 High Impact: disable package, clear data, uninstall or similarly destructive operations.
- Level 4 Advanced: root/system-level operations.

High-impact operations require explicit confirmation and a plain-language explanation of consequences.

## 9. Development Roadmap
Phase 0 - Research and architecture
Phase 1 - DroidKit core/foundation
Phase 2 - Device Inspector
Phase 3 - Phone Doctor
Phase 4 - Diagnostic Reports
Phase 5 - App/APK Inspector
Phase 6 - USB Lab and NFC Lab
Phase 7 - Shizuku capability provider
Phase 8 - Permission/AppOps advanced tools
Phase 9 - Wireless ADB provider
Phase 10 - Developer tools
Phase 11 - Optional root provider
Phase 12 - Polish, compatibility, beta/release

## 10. Planning Exit Criteria
Planning is complete when the team has:
- Approved PRD
- Approved V1/V2 feature matrix
- Feasibility/API matrix
- Navigation/screen map
- Architecture/module map
- Data model
- Security/risk rules
- Implementation backlog
- Definition of MVP and release criteria
