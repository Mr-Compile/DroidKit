# Journey 04 - Engineering Foundation

## Stack
Kotlin, Jetpack Compose, Clean Architecture + MVVM, Hilt, Coroutines/Flow, Room, DataStore, Navigation Compose, WorkManager where justified.

## Dependency direction
UI -> ViewModel -> Use Case -> Repository -> Data Source/System API -> Capability Provider

Feature modules may depend on core contracts. Core must not depend on feature modules.

## Suggested modules
app
core:common
core:ui
core:database
core:datastore
core:permissions
core:system
core:tools
core:capabilities
privilege:standard
privilege:shizuku
privilege:adb
privilege:root (future)
feature:dashboard
feature:deviceinfo
feature:diagnostics
feature:reports
feature:apps
feature:usb
feature:nfc
feature:network
feature:settings
feature:developer (later)

## Required contracts
### ToolDefinition
ID, title, description, category, route, min API, hardware requirements, permissions, capabilities, risk, keywords.

### CapabilityProvider
Reports availability and executes only capabilities it owns.

### CapabilityManager
Resolves capability -> available provider -> setup/permission/unsupported state.

### Result model
Success / expected unavailable / permission required / unsupported / failure. Avoid exception-driven UI state.

### Diagnostic statuses
PASS, FAIL, NOT_TESTED, NOT_SUPPORTED, PERMISSION_REQUIRED, UNKNOWN.

## Persistence
Room:
DiagnosticSession
DiagnosticResult
DeviceSnapshot
SavedReport metadata

DataStore:
theme, onboarding, favorites/preferences, small setup flags.

## Engineering rules
- Android framework calls behind interfaces where testability benefits.
- Do not wrap trivial pure Kotlin unnecessarily.
- No arbitrary shell command strings from untrusted UI input.
- Database migrations are explicit after first released schema.
- Public contracts documented.
- New dependencies require justification.
- Feature modules should compile independently where practical.

## Gate
Architecture decisions, package/module boundaries, database schema v1, result model, capability contracts, and coding conventions are approved.
