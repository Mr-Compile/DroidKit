# DroidKit - Architecture and Technology Specification

## Architecture Direction
Native Android with Kotlin and Jetpack Compose.

Recommended high-level flow:

UI -> ViewModel -> Use Cases -> Repositories -> Android/System Data Sources -> Capability Providers

## Why Native Kotlin
DroidKit depends heavily on Android framework APIs: PackageManager, SensorManager, UsbManager, NfcAdapter, BluetoothManager, BatteryManager, CameraManager, ConnectivityManager, UsageStatsManager, Settings, and eventually privileged Binder/system interactions. Native Kotlin minimizes bridging complexity.

## Core Modules
Suggested Gradle structure:

app/
core/common/
core/ui/
core/database/
core/datastore/
core/permissions/
core/system/
core/tools/
core/capabilities/

privilege/standard/
privilege/shizuku/
privilege/adb/
privilege/root/        # future

feature/dashboard/
feature/deviceinfo/
feature/diagnostics/
feature/reports/
feature/apps/
feature/permissions/
feature/appops/
feature/usb/
feature/nfc/
feature/network/
feature/developer/
feature/settings/

## Tool Registry
A central ToolRegistry provides metadata for every tool:
- id
- name
- description
- category
- icon key
- route
- min API
- hardware requirements
- permission requirements
- capability requirements
- risk level
- searchable keywords

The UI derives category screens, search, favorites, recent tools, and availability badges from this registry.

## Capability Engine
Core concepts:
- Capability
- CapabilityProvider
- CapabilityStatus
- CapabilityRequirement
- CapabilityResolution

Providers:
1. StandardAndroidProvider
2. ShizukuProvider
3. AdbProvider
4. RootProvider (future)

Resolution flow:
Tool -> required capabilities -> CapabilityManager -> available providers -> permission/setup state -> executable or blocked with reason.

Do not hard-code Shizuku checks inside feature screens.

## Permission Manager
Responsibilities:
- Manifest/runtime permission mapping
- API-level differences
- Explain why a permission is needed
- Request flow
- Permanently-denied state
- Settings redirection when necessary
- No repeated permission harassment

## State and Async
- ViewModel for presentation state
- StateFlow for screen state
- Coroutines for asynchronous work
- Repository interfaces to isolate Android framework dependencies

## Persistence
Room:
- DiagnosticSession
- DiagnosticResult
- DeviceSnapshot
- SavedReport metadata
- RecentTool if persistence is desired

DataStore:
- theme
- onboarding state
- user preferences
- safety confirmations/preferences
- feature setup state that is small/key-value
- favorites if simple enough

## Background Work
Use WorkManager only for legitimate deferrable work such as report cleanup/export preparation or scheduled local maintenance. Do not introduce background services without a feature requirement.

## Networking
No backend is required for V1. OkHttp can support explicit network diagnostic tools. Retrofit should only be added if DroidKit later consumes a structured HTTP service.

## Security Boundaries
- Privileged commands must live behind provider interfaces.
- Validate command arguments; avoid constructing arbitrary shell strings from untrusted input.
- Separate read-only and mutating operations.
- Log operations locally without leaking sensitive data.
- Never silently elevate or execute destructive actions.

## Testing Strategy
- Unit tests for capability resolution, tool registry, parsers, use cases, report models.
- Instrumented tests for Android APIs that require a device/emulator.
- Compose tests for critical navigation and confirmation UX.
- Real-device matrix for hardware diagnostics.
- Validation should be scoped to files/features changed by the current task unless broader integration validation is specifically required.
