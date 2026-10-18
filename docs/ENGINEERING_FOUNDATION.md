# DroidKit Engineering Foundation (J04 — Ratified)

Date: 2026-10-19
Status: RATIFIED. A fresh implementer needs **zero** new architecture decisions for J05–J06.
Sources: `playbook/06_J04_ENGINEERING_FOUNDATION.md`, `specs/2026-10-18-droidkit-app-skeleton-design.md`, skeleton plan Global Constraints.

## Stack (ratified)

Kotlin, Jetpack Compose (Material3), Hilt, Coroutines/Flow, Room, DataStore, Navigation Compose, kotlinx.serialization, WorkManager (dependency only, no workers yet). Gradle 9.1.0 / AGP 9.0.1 / Kotlin 2.3.20 (fallback 2.2.20). Build JDK: Android Studio bundled JBR.

## Dependency direction (ratified)

`UI → ViewModel → UseCase → Repository → DataSource/System API → CapabilityProvider`

- `feature/* → core/*` (contracts only); `privilege/* → core:privileges`; `:app` depends on everything and performs DI assembly. **Nothing depends on `feature/*`.**
- Features declare required capabilities — never check Shizuku/ADB/root directly.

## Module map (ratified — 31 modules)

- `build-logic` (included build): convention plugins `droidkit.android.application`, `droidkit.android.library`, `droidkit.android.library.compose`, `droidkit.hilt`
- `core`: `common`, `privileges`, `permissions`, `system`, `database`, `datastore`, `ui`
- `privilege`: `standard` (real), `shizuku`, `adb`, `root` (stubs → `Unavailable`)
- `feature` (20): `dashboard`, `tools`, `diagnostics`, `reports`, `settings`, `deviceinfo`, `battery`, `display`, `audio`, `camera`, `sensors`, `network`, `bluetooth`, `usb`, `nfc`, `apps`, `permissions`, `appops`, `packages`, `adb`, `developer`
- `app`

## Contracts (ratified signatures — implemented in skeleton)

- `Tool(id, title, description, category, route, safetyLevel, requiredCapabilities, requiredPermissions, requiredFeatures, minSdk, phase, keywords)`
- `FeatureContribution { val tools: List<Tool> }` — each feature module exposes `object XFeature : FeatureContribution` + `fun NavGraphBuilder.xGraph(nav)`
- `ToolRegistry(contributions)` → `tools`, `search(q)`, `byCategory(c)`, `byId(id)`
- `CapabilityProvider { kind, status, capabilities }` — `ProviderKind { STANDARD, SHIZUKU, ADB, ROOT }`, `ProviderStatus { Ready | NeedsSetup | Unavailable(msg) }`
- `CapabilityManager(providers, sdkInt, hasFeature, permissionChecker)` → `resolve(Tool): ToolResolution`, `providerStatuses()`
- `ToolResolution = Ready | Blocked(reasons)`; `BlockedReason = MissingPermission | MissingHardware | NeedsProvider | RequiresApi`
- `TestResultState = PASS | FAIL | NOT_TESTED | NOT_SUPPORTED | PERMISSION_REQUIRED | UNKNOWN`
- `SafetyLevel = SAFE | MINOR | SYSTEM_CHANGE | HIGH_IMPACT | ADVANCED` (risk levels 0–4 map in order)
- `PermissionChecker` (functional interface) — faked in tests

## Naming & SDK pins (ratified)

- `applicationId` = `io.github.droidkit`; module namespaces `io.github.droidkit.<module path dots>`
- minSdk **28**, compileSdk **36**, targetSdk **36**
- Project root = repo root `C:\wamp64\www\DroidKit`

## Persistence (ratified schema v1)

Room `DroidKitDatabase`: `DeviceSnapshotEntity`, `DiagnosticSessionEntity`, `DiagnosticResultEntity`, `SavedReportEntity` + DAOs (suspend inserts, `Flow` observers).
DataStore: theme mode, onboarding flag, favorite + recent tool ids.
Rule: **explicit migrations after the first shipped schema** — no destructive fallbacks post-release.

## Engineering rules (ratified)

- Android framework calls behind interfaces where testability benefits (`PermissionChecker`, probe seams as lambdas); no pointless wrapping of pure Kotlin.
- No arbitrary shell command strings from untrusted UI input — allow-listed structured actions only.
- Public contracts documented; new dependencies require written justification.
- Feature modules compile independently where practical.
- UI state is explicit (loading/content/empty/permission-required/unsupported/error); never exception-driven UI state.

## Git & commit convention (ratified)

- `main` trunk; work branches `feature/<name>`, `fix/<name>`, `refactor/<name>`, `docs/<name>` for code journeys.
- Commit per task, message `type(scope): summary` (`feat|fix|docs|refactor|test|release`).
- **Push to `origin` immediately after every commit** (root `AGENTS.md` Git rule).
- Tags `vX.Y.Z` at release gates only, pushed with `--tags`.

## Testing rules (ratified)

- Identify changed files/features first; validate only the changed scope.
- Broaden to full suite only when shared contracts/DI/navigation/DB schema change or at release gates.
- Unit tests where plans mark TDD (`core:common` registry, `core:privileges` resolution, pure formatters).
- Real-device validation required for hardware-dependent features before `[x]`.

## Definition of Done (ratified)

`playbook/27_DEFINITION_OF_DONE.md` adopted verbatim — Feature / Journey / Release tiers.

## Report format

**OPEN (D4)** — decided at J10 (v0.5.0 Reports) planning; v2 wording "PDF or approved structured format". `PdfDocument` confirmed feasible (J02).

## Privacy & data behavior (ratified)

Local-first V1; no backend; data minimization per `playbook/24`; reports preview exported fields; ADB keys → secure storage post-V1; delete-history control in Settings.

## Gate confirmation

- [x] Architecture decisions approved (module map, contracts, dependency direction)
- [x] Package/module boundaries approved
- [x] Database schema v1 approved
- [x] Result model approved
- [x] Capability contracts approved
- [x] Coding conventions approved
