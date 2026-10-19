# DroidKit V1 Completion — Execution Plan

> **For agentic workers:** execute phase-by-phase. The pack (`docs/01_plans/droidkit_v1_completion_plan/`) is the task spec; this file sequences it against audited HEAD. Each phase ends with the pack's 10-point report, tracker update, and a STOP for authorization. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Take DroidKit from v0.8.0 (tag `v0.8.0`, `versionCode 8`) to a verified, accessible, release-ready V1 without rebuilding working architecture.

**Architecture:** Kotlin + Compose, MVVM/Clean, 35 Gradle modules (`:app`, 8 `:core:*`, 4 `:privilege:*`, 22 `:feature:*`), Hilt, Room (DB v5), DataStore. Preserve: ToolRegistry/FeatureContribution, CapabilityManager, ElevatedAction/ElevatedOps, SessionEngine, ReportBuilder, InsightsCalculator, AI privacy/BYOK pipeline, feature-module boundaries.

**Tech Stack:** AGP 9.0.1, Kotlin 2.2.20, Compose BOM 2025.09.01, Navigation 2.9.4, Room 2.7.2, Hilt 2.59.2. build-logic convention plugins (`droidkit.android.application`, `droidkit.android.library`, `droidkit.android.library.compose`, `droidkit.hilt`).

**Spec files (authoritative task requirements):**
- `docs/01_plans/droidkit_v1_completion_plan/00_MASTER_PROMPT.md` — phases 0–8, operating rules
- `docs/01_plans/droidkit_v1_completion_plan/01_TRACKER.md` — DK-ID status board
- `docs/01_plans/droidkit_v1_completion_plan/02_DEFINITION_OF_DONE.md` — DoD
- `docs/01_plans/droidkit_v1_completion_plan/03_DEVICE_VERIFICATION_MATRIX.md` — device evidence grid
- `docs/01_plans/droidkit_v1_completion_plan/04_PHASE0_PROMPT.md` — Phase-0 brief

## Global Constraints

- Pack operating rules 1–15 apply verbatim (inspect before edit; no duplicate systems; smallest coherent change; no QUERY_ALL_PACKAGES without justification; STOP after each phase).
- Repo rules (`AGENTS.md`): commit per completed task, push to `origin` immediately after every commit; audit the diff for secrets before every commit/push.
- Report CANNOT VERIFY rather than PASS for checks that cannot run. Device-required behavior is never verified from JVM tests alone.
- Windows environment: use `gradlew.bat` (or `.\gradlew.bat`); a stale Gradle daemon/file lock is an environment issue — diagnose, never work around it in production code.

## Baseline drift vs pack audit (re-verified 2026-10-19 @ 7313076)

| Pack claim | Actual HEAD |
|---|---|
| 38 modules | 35 modules |
| v0.9 alpha | `versionName 0.8.0`, `versionCode 8`; tags `v0.1.0`–`v0.8.0` |
| `sensors.live` navigates literal placeholder | Worse: `Tool.init` now has `require('{' !in route)` — `SensorsFeature` still violates it, so registry construction throws (probable crash on first registry access, not a wrong navigation) |
| 107 JVM tests | 14 `test` source dirs, 1 `androidTest` dir (recount in Phase 0) |
| No CI/lint | Confirmed: no `.github/`, no lint config, no detekt/ktlint in catalog |
| Feature manifests empty | Confirmed: `feature:apps` + `feature:packages` are bare `<manifest />`; zero `<queries>` repo-wide |
| Unused WorkManager dep | Confirmed: `androidx-work-runtime` in `libs.versions.toml`, referenced by no module |
| FeatureStubScreen dead | `core/ui/.../FeatureStubScreen.kt` exists — verify zero call sites in Phase 5 |
| `core:ui` components | Only `DroidKitWordmark`, `Feedback`, `ToolCard` (+ stub screen) |

---

## PHASE 0 — Immediate correctness defects (gate: build + tests, report, STOP)

### Task 0.1 — DK-001: `sensors.live` launch route

**Files:**
- Modify: `feature/sensors/src/main/java/io/github/droidkit/feature/sensors/SensorsFeature.kt:29`
- Test: `core/common/src/test/java/io/github/droidkit/core/common/ToolRegistryTest.kt` (extend)

**Audit state:** CONFIRMED at HEAD, evolved since pack audit — `Tool.kt:30` already enforces the invariant, so `SensorsFeature` now breaks registry construction. Generic call sites all navigate `tool.route` verbatim: `MainActivity.kt:110`, `DashboardScreen.kt:124/137/147`, `InsightsScreen.kt:198`, `ToolsScreen.kt:84`. Only one `{` hazard repo-wide (line 47 is the NavGraph destination — legitimate).

- [ ] **Step 1: Failing regression test** — add a test that builds every shipped `FeatureContribution`'s tools (or at minimum constructs `SensorsFeature.tools`) and asserts no route contains `{`/`}`. `SensorsFeature.tools` currently throws on construction; assert fixed behavior once decided below.
- [ ] **Step 2: Fix** — set `sensors.live` route to `ROUTE` (the sensor list is the honest entry point; `LiveSensorScreen` requires a concrete `type` the list supplies via `SensorsScreen.kt:65`). Update description to "Pick a sensor to stream live readings" so the card doesn't promise a screen it can't deep-link.
- [ ] **Step 3: Verify** — `gradlew.bat :feature:sensors:assembleDebug :core:common:testDebugUnitTest`; confirm all five `tool.route` call sites now receive navigable routes.
- [ ] **Step 4: Report + commit** — CONFIRMED/evolved finding, files changed, commands+results. `git commit` per repo rules, push.

### Task 0.2 — DK-002: App Inspector package visibility

**Files:**
- Modify: `feature/apps/src/main/AndroidManifest.xml` (currently `<manifest />`)
- Modify: `feature/packages/src/main/AndroidManifest.xml` (currently `<manifest />`)
- Check honesty copy: `feature/apps/src/main/java/io/github/droidkit/feature/apps/AppsViewModel.kt:63`, `feature/packages/.../PackagesViewModel.kt:79`
- Spec basis: `docs/02_foundations/j02_feasibility/J02_TOOL_RECORDS.md:298-301`, `docs/01_plans/2026-10-19-j11-app-inspector.md` ("Android 11+ limits package visibility — N apps visible" header already specced), `docs/05_release/PRIVACY_POLICY_DRAFT.md` (already promises no QUERY_ALL_PACKAGES)

**Audit state:** CONFIRMED — `getInstalledPackages` called in both modules; no `<queries>` anywhere; on API 30+ both screens see near-empty sets. Prior docs already decided: scoped `<queries>`, no `QUERY_ALL_PACKAGES`, honest "visible to DroidKit" framing (GitHub distribution, not Play).

- [ ] **Step 1: Decide `<queries>` scope** — minimal set: `QUERY_ALL_PACKAGES` rejected. Candidate intents per J02: launcher apps (`MAIN`+`LAUNCHER`), plus `<intent>` actions App Inspector legitimately needs (e.g. `VIEW`/`MAIN` categories). Keep the smallest block that makes the catalog useful; document rationale in the manifest comment.
- [ ] **Step 2: Add `<queries>`** to `feature/apps` and `feature/packages` manifests (duplicate the same block; manifests merge into the app).
- [ ] **Step 3: Honest UI state** — verify the list header/empty state states "Android 11+ limits package visibility — N apps visible" (J11 spec); wire the visible-count if missing.
- [ ] **Step 4: Verify** — `gradlew.bat :app:processDebugMainManifest` then inspect merged manifest `app/build/intermediates/merged_manifest/**/AndroidManifest.xml` for the `<queries>` block. API-30+ device check deferred to Phase 6 matrix — report CANNOT VERIFY on hardware.
- [ ] **Step 5: Report + commit + push.** STOP — Phase 0 gate.

---

## PHASE 1 — Automated quality foundation (gate: PR workflow green, STOP)

### Task 1.1 — DK-010: Minimal CI

**Files:**
- Create: `.github/workflows/ci.yml`
- Modify: `docs/README.md` or `AGENTS.md` (document local equivalents: `gradlew.bat assembleDebug testDebugUnitTest lint`)

- [ ] **Step 1: Write workflow** — triggers: `push` to `main` + `pull_request`. Steps: checkout v4, setup-java (temurin, version matching `build-logic` toolchain — read it first), `gradle/actions/setup-gradle@v4` for cache, `gradlew.bat assembleDebug`, `gradlew.bat testDebugUnitTest`, `gradlew.bat lint` (once Task 1.2 lands), upload test reports on `failure()`. Runner: `windows-latest` (matches dev env) unless `gradlew` line endings force `ubuntu-latest` — decide in implementation, document choice.
- [ ] **Step 2: Push to a branch, open a PR, watch it go green.** No signing material anywhere in the file.
- [ ] **Step 3: Commit + push.** Report.

### Task 1.2 — DK-011: Lint baseline

**Files:**
- Modify: `build-logic/src/main/kotlin/droidkit/AndroidApplicationConventionPlugin.kt` and `AndroidLibraryConventionPlugin.kt` (add `lint { }` config)
- Create: `lint-baseline.xml` (generated, per-module or root) only if first-run violations are numerous
- Modify: `gradle/libs.versions.toml` only if detekt/ktlint justified — default answer is Android lint only (pack: "add ktlint/detekt only if justified")

- [ ] **Step 1:** `lint { abortOnError = true; warningsAsErrors = false; baseline = file("lint-baseline.xml") }` — generate baseline via `gradlew.bat lint` once, commit it, so only NEW violations fail.
- [ ] **Step 2: Run lint, review baseline entries for real P0/P1s** (e.g. `UnsafeIntentLaunch`, `ExportedService`, missing `lint.xml` ignores). File genuine findings as new DK defects rather than baseline-hiding them.
- [ ] **Step 3: Wire into CI** (Task 1.1 workflow gains `lint` step).
- [ ] **Step 4: Commit + push.** Report.

### Task 1.3 — DK-012: Regression/architecture gates

**Files:**
- Create: `core/common/src/test/java/io/github/droidkit/core/common/ArchitectureInvariantsTest.kt` (or `app/src/test/...` if it needs the full contribution set — Hilt multibinding needs a JVM-friendly way to enumerate `FeatureContribution`s; simplest: a JVM test listing the `object` contributions explicitly)
- Possibly create: `app/src/test/java/io/github/droidkit/ModuleDependencyTest.kt`

- [ ] **Step 1: Route invariant** — iterate all `FeatureContribution` singletons, assert every `Tool.route` has no `{`/`}` (extends Task 0.1's guard from sensors to all features).
- [ ] **Step 2: Boundary invariant** — scan feature-module source sets for imports of `io.github.droidkit.privilege.*` (implementation modules). If not practically testable in JVM, document a `check` Gradle task or a `grep` gate in CI instead — pick the cheapest honest enforcement.
- [ ] **Step 3: ElevatedAction invariant** — test that `ElevatedAction` construction rejects unvalidated/free-form parameters (audit `core/privileges` first for the existing validation seam).
- [ ] **Step 4: Commit + push.** Report. STOP — Phase 1 gate.

---

## PHASE 2 — Core UX/system hardening (gate: full JVM tests + build + lint, STOP)

### Task 2.1 — DK-020: Unified `UiProblem` model

**Files:**
- Audit first: `core/common` (`ToolResolution`, `ElevatedResult`, `BlockedReasonList`), `core/ai` (`AiError`), `core/permissions` (`PermissionGate`), feature-local error types
- Create: `core/ui/src/main/java/io/github/droidkit/core/ui/problem/UiProblem.kt` (or `core/common` if non-UI layers must consume it)

- [ ] **Step 1:** Audit + map existing error types; design smallest shared model: `title`, `message`, `severity`, `technicalDetails?`, `recoveryActions: List<RecoveryAction>` where `RecoveryAction` = sealed type (Retry, GrantPermission(perm), OpenSettings, SetupProvider(kind), Dismiss).
- [ ] **Step 2:** Implement model + boundary mappers (exception→UiProblem at ViewModel edge, never raw stderr as primary copy).
- [ ] **Step 3:** Migrate 2–3 representative screens (recommend: a permission-blocked tool, an elevated-op failure, an AI failure).
- [ ] **Step 4:** Unit tests for mappers; commit + push.

### Task 2.2 — DK-021: Permission flow hardening

**Files:**
- Audit: `core/permissions` module (existing permission architecture), current request call sites in features (camera, bluetooth, nfc, usb, microphone/audio)
- Likely modify: `core/permissions` state model + a shared `PermissionGate`/rationale composable

- [ ] **Step 1:** Extend state model to distinguish: granted / requestable denial / permanently denied→Settings / missing hardware / unsupported API. Keep Shizuku/ADB/root needs as provider concerns (capability layer), never as Android runtime permissions.
- [ ] **Step 2:** Contextual requests only (no startup dump); reusable rationale + Settings-recovery UX.
- [ ] **Step 3:** State-transition unit tests.
- [ ] **Step 4:** Commit + push.

### Task 2.3 — DK-022: Reactive provider/capability state

**Files:**
- Audit: `core/privileges` (`ElevatedOps`, provider status reads), `privilege/shizuku`, `privilege/adb`, `privilege/root` — find where status is currently read once vs observed
- Modify: whichever seam exposes status (e.g. add `Flow<ProviderStatus>`)

- [ ] **Step 1:** Add minimal `Flow`/`StateFlow` over provider status (Shizuku grant, ADB connect/disconnect, root availability).
- [ ] **Step 2:** Collect in ViewModels/UI so status changes recompose without restart.
- [ ] **Step 3:** Tests where feasible (fake provider); commit + push.

### Task 2.4 — DK-023: Room DAO/migration tests

**Files:**
- Audit: `core/database` (`DroidKitDatabase` v5, DAOs, existing `androidTest`/`test` coverage — 1 androidTest dir exists repo-wide, locate it)
- Create: `core/database/src/test/...` Robolectric-free JVM tests won't cover Room — use `androidTest` or `Room.inMemoryDatabaseBuilder` under `test` if Robolectric gets added (justify dependency) — else instrumented tests documented for Phase 6

- [ ] **Step 1:** Enumerate DAOs + schema versions 1→5 migrations actually shipped in tags `v0.1.0`–`v0.8.0`.
- [ ] **Step 2:** Add migration tests for each shipped upgrade path + focused DAO tests. Never rewrite working schema for test convenience.
- [ ] **Step 3:** Commit + push. Report. STOP — Phase 2 gate.

---

## PHASE 3 — V5 design system (gate: build + tests + lint + UI checks, STOP)

Spec: `docs/03_design_system/uiux_v5/` (canonical); superseded reference `uiux_v4_superseded/`. Current `core:ui` = 3 components + stub.

### Task 3.1 — DK-030: Design tokens
- [ ] Audit `uiux_v5` token spec vs `core/ui` theme files. Centralize semantic colors, spacing, shapes, typography, elevation, motion in `core/ui` theme — shared/systemic values only, no mechanical dp sweep. Dark theme first-class, dynamic color optional per Settings decision (DK-051).

### Task 3.2 — DK-031: Shared components
- [ ] Inventory repeated UI patterns across `feature/*` screens (cards, status badges, section headers, tool rows, dialogs). Consolidate into `core/ui/components/` alongside `ToolCard`/`Feedback`. No wrappers that add nothing over Material 3.

### Task 3.3 — DK-032: Semantic state components
- [ ] Create `core/ui/state/` components: `LoadingState`, `EmptyState`, `ProblemState` (renders `UiProblem` from Phase 2 — reuse, never duplicate), `UnsupportedState`, `PermissionRequiredState`, `ProviderSetupState`, `OfflineState` where relevant.

### Task 3.4 — DK-033: Apply V5 to primary surfaces
- [ ] Order: app shell → `feature:dashboard` (Home) → `feature:tools` → `feature:diagnostics` → `feature:reports` → `feature:settings` → remaining features. Presentation only; verify no navigation/data/capability regression.
- [ ] Commit per surface batch; final phase commit + push. STOP — Phase 3 gate.

---

## PHASE 4 — Accessibility (gate: automated checks run; TalkBack manual reported separately, STOP)

### Task 4.1 — DK-040: Semantics pass
- [ ] Audit shared components + major surfaces for `contentDescription`, decorative-element exclusion, `stateDescription`, roles, headings, live regions on dynamic state, merged semantics on card-controls.

### Task 4.2 — DK-041: Interaction/layout
- [ ] Verify touch targets ≥48dp, focus order, font scaling to 200%, contrast vs V5 tokens, no color-only status, dialog/sheet announcements.

### Task 4.3 — DK-042: Verification
- [ ] Compose accessibility checks where supported (audit `androidTest` infra; add `compose-ui-test` deps if justified); write the manual TalkBack checklist into `docs/04_audits/` as a living checklist.
- [ ] Commit + push. Report; manual TalkBack rows feed Phase 6 matrix. STOP — Phase 4 gate.

---

## PHASE 5 — V1 feature closure (gate: build/test/lint + final tool matrix, STOP)

### Task 5.1 — DK-050: Phone Doctor catalog audit
- [ ] Enumerate every `Tool`/`FeatureContribution` catalog entry vs actual screens; classify each: implemented / intentionally-unsupported / post-V1 (`ToolPhase.V2|FUTURE` exists — use it) / accidentally-unwired. Wire real V1 gaps; demote or remove pretenders.

### Task 5.2 — DK-051: Settings completion
- [ ] Reconcile `feature:settings` against V1 needs: dynamic-color toggle (decide), privacy info, reset preferences, OSS licenses, onboarding/help revisit. Each entry needs a real behavior — no decorative settings.

### Task 5.3 — DK-052: Dead code/dependency cleanup (confirmed candidates)
- [ ] `core/ui/.../FeatureStubScreen.kt` — grep call sites first; remove only if zero references (update any remaining stub users to a real semantic state).
- [ ] Remove `androidx-work-runtime` + `work = "2.10.5"` from `gradle/libs.versions.toml` — confirmed unreferenced by any module at HEAD.
- [ ] Centralize stray dep versions (e.g. Shizuku coordinates) into catalog per convention.
- [ ] Full `assembleDebug` + `testDebugUnitTest` + `lint` after removals.

### Task 5.4 — DK-053: strings.xml / i18n readiness
- [ ] Move production UI copy to `strings.xml` per module, preserving wording/tests. Readiness only — no translations in V1.
- [ ] Commit + push. Produce final V1 tool matrix into `docs/05_release/` (or `04_audits/` — decide by content type: evidence→audits). STOP — Phase 5 gate.

---

## PHASE 6 — Real-device verification (gate: evidence matrix + defect list, STOP)

**Requires hardware. Without devices this phase is BLOCKED — report as such, never simulate PASS.**

### Task 6.1 — Matrix setup
- [ ] Fill `03_DEVICE_VERIFICATION_MATRIX.md` device rows: D1 = emulator or Pixel/AOSP baseline (min-SDK + latest API AVDs), D2 = major-OEM physical device if available.

### Task 6.2 — Core journeys + permissions + hardware rows
- [ ] Walk every matrix row (journeys, permission grant/deny/permanent-deny, missing hardware, CameraX, mic, sensors, BLE, NFC r/w, USB, network tools, export/share, reinstall persistence, rotation). Record evidence per cell.

### Task 6.3 — DK-060/061/062 elevated gates
- [ ] Shizuku: unavailable→installed→grant/deny→allow-listed read→confirmed mutation→reconnect UI→fallback.
- [ ] Wireless ADB: pair→connect→key persistence/security→allowed command→failure→logcat if V1.
- [ ] Root (rooted env only): detection→denial/grant→allow-listed exec→audit log→mutation confirmation→fallback.
- [ ] Every new defect → new row in `01_TRACKER.md` with priority. STOP — Phase 6 gate.

---

## PHASE 7 — Compatibility & hardening (gate: P0/P1 device defects closed/accepted, STOP)

### Task 7.1 — DK-070: API-level matrix
- [ ] Verify permission/platform behavior across supported API levels; document per-level differences.

### Task 7.2 — DK-071: OEM matrix
- [ ] Harden only reproduced OEM findings from Phase 6 — no speculative vendor hacks.

### Task 7.3 — DK-072: Performance/lifecycle
- [ ] Profile high-traffic screens + diagnostics: main-thread work, leaks, repeated probes, recomposition, receiver/collection cleanup, cancellation. Fix measured issues only.

### Task 7.4 — DK-073: Security/privacy review
- [ ] Checklist: exported components, FileProvider paths (`app/.../file_paths` exists — audit scope), log hygiene, report redaction, AI `PrivacyFilter` coverage, secret handling, `ElevatedAction` validation, elevated confirmation/audit, permission minimization. Fixes → commit; findings log → `docs/04_audits/`.
- [ ] STOP — Phase 7 gate.

---

## PHASE 8 — Release engineering (gate: release-readiness report, STOP — no publish)

### Task 8.1 — DK-080: Release build/R8
- [ ] Add `release` build type in `AndroidApplicationConventionPlugin.kt`: minify/shrink decisions, keep rules from actual reflection needs (Hilt mostly handled; check serialization/Room), verify release runtime.

### Task 8.2 — DK-081: Signing strategy
- [ ] Signing via env/CI secrets only — keystore + passwords never in repo. Document `.gitignore` coverage; verify no tracked secrets (`git ls-files | grep -i key`).

### Task 8.3 — DK-082: APK/AAB + upgrade verification
- [ ] Clean install + upgrade from `v0.8.0` APK (build from tag if no artifact retained): Room v5 + DataStore preservation, `versionCode`/`versionName` SemVer bump discipline (V1 → `1.0.0`/code 9).

### Task 8.4 — DK-083: Release CI
- [ ] Extend workflow: RC artifact job (tag/manual trigger), checksums, retained reports.

### Task 8.5 — DK-084: Final V1 acceptance
- [ ] Full green board: debug+release builds, JVM tests, lint, required instrumented tests, a11y checklist, device matrix (or documented exceptions), elevated-provider claims match evidence, security review passed, zero open P0/P1, accurate `CHANGELOG.md`/release notes (`docs/05_release/RELEASE_NOTES_v1.0.0_DRAFT.md` already exists — reconcile). Output release-readiness report to `docs/05_release/`. Do NOT publish.

---

## Cross-phase bookkeeping

- [ ] After every task: update `01_TRACKER.md` status + evidence column.
- [ ] After every phase: 10-point completion report (pack §Completion report format), checklist/docs touched, STOP for authorization.
- [ ] Newly discovered defects: append rows to `01_TRACKER.md` (continue DK-### numbering past 084).
- [ ] `docs/README.md` `01_plans` section gets updated if new folders/packs land (Docs layout rule).

## Spec coverage check

All pack tasks mapped: DK-001→0.1, DK-002→0.2, DK-010→1.1, DK-011→1.2, DK-012→1.3, DK-020→2.1, DK-021→2.2, DK-022→2.3, DK-023→2.4, DK-030..033→3.1–3.4, DK-040..042→4.1–4.3, DK-050..053→5.1–5.4, DK-060..062→6.3, DK-070..073→7.1–7.4, DK-080..084→8.1–8.5. No gaps.

## Known risks

- Phase 6 needs real hardware — without devices it's BLOCKED, not skippable.
- DK-023 may need an `androidTest`/Robolectric decision — JVM-only Room testing isn't free.
- Windows CI on `windows-latest` is slow for Gradle Android builds; ubuntu runner + `gradlew` line-ending normalization may be needed — decide at Task 1.1.
- `sensors.live` defect now likely crashes at startup — Phase 0 is genuinely P0.
