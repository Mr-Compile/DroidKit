# DroidKit App Skeleton — Design Spec

Date: 2026-10-18
Source: `DroidKit_Master_Planning_Document.docx` (Master Planning Document, AI handoff package)

## Purpose

Build the full multi-module skeleton of DroidKit, an Android-first toolkit for
phone technicians, power users, developers, and support personnel. Every module
and feature shell from the approved structure exists and compiles; only
foundation-level functionality is implemented for real. All other tools are
registered in the ToolRegistry with declared capability requirements and render
precise blocked/setup states per the document's capability flow.

## Decisions

- Application ID / package root: `io.github.droidkit`
- minSdk 28, compileSdk 36, targetSdk 36
- Gradle 9.1.0 (wrapper), AGP 9.0.1, Kotlin 2.2.20
- Jetpack Compose (BOM), Hilt, Navigation Compose, Room, DataStore,
  kotlinx.serialization, Coroutines/Flow, WorkManager (dependency only)
- Multi-module layout approved by user (replaces single-module proposal)
- Project root is `C:\wamp64\www\DroidKit` itself (the `droidkit/` level in
  the approved tree)

## Architecture

`UI -> ViewModel -> Use Cases -> Repositories -> Data Sources -> Capability
Providers` (planning doc section 4).

Features never check Shizuku/ADB/root directly. A tool declares required
capabilities and permissions; `CapabilityManager` resolves them through
hardware/API/permission checks and the provider chain, returning `Ready`,
`Blocked(reasons)`, or `NeedsSetup(provider)`. Provider order per doc:
Standard -> Shizuku -> Wireless ADB -> Root (future).

## Module Map

### build-logic (included build)

Convention plugins so each module build file stays minimal:

| Plugin                          | Applies |
|---------------------------------|---------|
| `droidkit.android.application`  | com.android.application, kotlin-android, sdk defaults, compose |
| `droidkit.android.library`      | com.android.library, kotlin-android, sdk defaults |
| `droidkit.android.library.compose` | library + compose plugin, BOM, `buildFeatures.compose` |
| `droidkit.hilt`                 | hilt plugin, ksp, hilt deps |

### core (com.android.library)

| Module               | Contents |
|----------------------|----------|
| `:core:common`       | `Tool` model (id, title, category, safetyLevel, requiredCapabilities, requiredPermissions, phase), `Capability` enum, `ToolCategory`, `SafetyLevel` (SAFE/MINOR/SYSTEM_CHANGE/HIGH_IMPACT/ADVANCED), `ToolRegistry`, `FeatureContribution` interface, `TestResultState`, shared result/sealed types |
| `:core:privileges`   | `CapabilityProvider` SPI, `ProviderKind`, `ProviderStatus` (Ready/NeedsSetup/Unavailable), `BlockedReason` (MissingPermission, MissingHardware, NeedsProvider, RequiresApi), `ToolResolution`, `CapabilityManager` (resolves a tool's requirements against the injected provider set + hardware/permission checks) |
| `:core:permissions`  | `PermissionManager` — runtime permission state + request flow helpers |
| `:core:system`       | `DeviceSystemProbe` — manufacturer/model, SDK, ABI, RAM/storage summary, sensor list, camera count, package-feature flags |
| `:core:database`     | Room `DroidKitDatabase`; entities per `06_DroidKit_Data_Model`: `DeviceSnapshot`, `DiagnosticSession`, `DiagnosticResult` (PASS/FAIL/NOT_TESTED/NOT_SUPPORTED/PERMISSION_REQUIRED/UNKNOWN), `SavedReport`; DAOs |
| `:core:datastore`    | `SettingsRepository` — theme mode, favorite tool ids, recent tool ids |
| `:core:ui`           | Compose design system: Material3 theme (dynamic color opt-in), color, typography; shared `ToolCard`, `StatusBadge`, `BlockedStateView`, `EmptyState`, `SectionHeader` |

### privilege (provider implementations)

| Module               | Contents |
|----------------------|----------|
| `:privilege:standard` | `StandardAndroidProvider` — the only functional provider; satisfies capabilities resolvable through normal SDK APIs/permissions |
| `:privilege:shizuku` | `ShizukuProvider` stub — always `Unavailable("Coming in Phase 7")` |
| `:privilege:adb`     | `WirelessAdbProvider` stub — always `Unavailable("Coming in Phase 9")` |
| `:privilege:root`    | `RootProvider` stub — always `Unavailable("Postponed — future provider")` |

Each privilege module exposes a plain provider class; `:app` assembles
`Set<CapabilityProvider>` in its DI module (keeps privilege modules free of
Hilt/KSP).

### feature (library + compose + hilt)

Each feature module exposes:

- a `NavGraphBuilder` extension registering its Compose screens
- an `object XFeature : FeatureContribution` exposing its `Tool` descriptors;
  `:app` assembles `Set<FeatureContribution>` manually in its DI module (no
  annotation processing needed in leaf feature modules)

| Module                 | Role |
|------------------------|------|
| `:feature:dashboard`   | Home tab — device summary card (core:system), capability status, quick/recent tools, diagnostic shortcut |
| `:feature:tools`       | Tools tab — searchable registry browser, categories, favorites, availability badges (added: required by doc nav, absent from user's tree) |
| `:feature:diagnostics` | Diagnose tab hub — full diagnostic entry point, quick tests grid, session history from Room. Orchestrates the Phone Doctor category modules |
| `:feature:battery`     | Phone Doctor battery/charging tests (stub) |
| `:feature:display`     | display/touch/multitouch tests (stub) |
| `:feature:audio`       | speaker/microphone tests (stub) |
| `:feature:camera`      | camera/flash tests (stub) |
| `:feature:sensors`     | sensor tests (stub) |
| `:feature:deviceinfo`  | Device Inspector — build, CPU/ABI, memory, storage, display, battery, sensors, cameras, connectivity sections (stub screens with real probes where standard API allows) |
| `:feature:network`     | network/Wi-Fi tools (stub) |
| `:feature:bluetooth`   | Bluetooth tools (stub) |
| `:feature:usb`         | USB Lab — host detection, VID/PID, interfaces/endpoints (stub) |
| `:feature:nfc`         | NFC Lab — tag techs, NDEF (stub) |
| `:feature:apps`        | App Inspector — installed apps, package metadata (stub) |
| `:feature:permissions` | per-app permission inspector (stub) |
| `:feature:appops`      | AppOps controls (stub; gated on Shizuku) |
| `:feature:packages`    | advanced package controls (stub; gated on Shizuku) |
| `:feature:adb`         | Wireless ADB tools (stub; gated on provider) |
| `:feature:developer`   | Developer Lab (stub) |
| `:feature:reports`     | saved reports list + detail (Room-backed, mostly empty at skeleton) |
| `:feature:settings`    | appearance, advanced-access setup (provider status), safety info, about |

### app

`io.github.droidkit`: `DroidKitApp` (`@HiltAndroidApp`), `MainActivity`
(`@AndroidEntryPoint`, Compose `NavHost` calling every feature's graph
extension, bottom bar Home | Tools | Diagnose | Settings), Hilt aggregation of
`Set<FeatureContribution>` into `ToolRegistry` and `Set<CapabilityProvider>`
into `CapabilityManager`.

## Key Wiring

- **Dependency direction:** `feature/*` -> `core/*`; `privilege/*` ->
  `core:privileges`; `app` -> everything. Nothing depends on `feature/*`.
- **ToolRegistry:** in-memory registry built from `Set<FeatureContribution>`;
  supports search, category filter, favorites lookup.
- **CapabilityManager(tool):** checks `requiredPermissions` via
  `PermissionManager`, hardware flags via `DeviceSystemProbe`, then resolves
  each `Capability` against the provider set; returns aggregate
  `ToolResolution` consumed by UI badges/blocked views.
- **Navigation:** string routes constants per feature
  (`DroidKitDestination`), typed data kept minimal at skeleton stage.
- **Manifest:** only `app` has a real manifest (INTERNET not needed; USB/NFC
  feature declarations added when those features land). Library manifests are
  empty stubs.

## Error Handling

- Capability resolution never throws; failures map to `BlockedReason` values
  rendered by `BlockedStateView`.
- High-impact tools (Level 3+) always render a consequence explainer +
  confirmation stub, even before implementation (doc section 11).

## Testing

- `:core:common` — ToolRegistry search/category/favorites unit tests (JUnit4)
- `:core:privileges` — CapabilityManager resolution unit tests with fake
  providers
- Build verification: `gradlew :app:assembleDebug` must succeed

## Out of Scope (per doc)

- Shizuku/ADB/root implementations (stubs only)
- Real diagnostic test executors (display/touch/audio/camera loops)
- USB host / NFC hardware calls
- Report export/share implementation
- Security/credential bypass features (never, per doc)

## Doc Checklist Coverage

| Doc item | Skeleton coverage |
|----------|-------------------|
| Module boundaries | Finalized here (approved tree) |
| Navigation | Bottom nav + per-feature graphs |
| Room schema | v1: sessions, results, notes |
| Feasibility notes | Each stub tool declares real required capabilities/permissions |
| V1 scope freeze | Skeleton registers all V1+V2 tools with phase labels |
