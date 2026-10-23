# Target-User Coverage Megaplan — 2026-10-23

## Context

Audit question: does every committed feature for the five Product Bible §2 target
users exist? **Yes at code level** — 73 registered tools across 23 feature
modules, no stub screens. What remains is (a) spec'd-but-unbuilt surfaces,
(b) the gated/deferred items approved on 2026-10-23, and (c) the
technician-relevant V2 backlog.

Approved scope: spec'd gaps + V2 user features + ADB terminal + settings
export/import. Excluded: hardware-bound QA, J14–J16 release ceremonies,
non-technician V2 items (sync, team features, desktop companion, extension
system, extra labs).

## Gap list

| # | Gap | Source | Status |
|---|---|---|---|
| 1 | `RiskBadge`/`ConsequencePanel`/`ConfirmationSheet`/`AIInsightCard` | V5 Task 5 (partial) | Pending |
| 2 | `FriendlyError` + `strings_errors.xml` catalog | V5 Task 6 | Pending |
| 3 | Capability Center screen | V5 Task 10 / spec 23 | Pending |
| 4 | Candidate-9 launcher icons + splash + J03 v5 token map | V5 Tasks 1–2 | Pending |
| 5 | Settings export/import | UX-5 (deferred) | Pending |
| 6 | ADB terminal | J19 gated item — approved 2026-10-23 | Done |
| 7 | Custom diagnostic presets ("technician presets/checklists") | Product Bible future list | Done |
| 8 | Multi-device history filters | Product Bible "multi-device history" | Done |

## Execution tasks

Commit + push after each task. Update `mockups/index.html` and the checklist in
the same commit as any UI change.

### Task 1 — This document
Link this megaplan from `10_DroidKit_Master_Journey_Checklist.md`.

### Task 2 — Risk/AI shared components
`core/ui/.../components/Risk.kt`: `RiskBadge(level)`, `ConsequencePanel`,
`ConfirmationSheet` (wraps `DroidKitAlertDialog`, action-named confirm),
`AIInsightCard` (wraps `AiSurfaceCard`). Refactor `feature/packages`
disable/uninstall confirm to `ConfirmationSheet` as the proving consumer.

### Task 3 — Error catalog + FriendlyError
`core/ui/res/values/strings_errors.xml` keyed per `UiProblem`/`BlockedReason`/
`AiError` variant; `FriendlyErrorMapper`; `UiProblemCard` prefers catalog
strings, keeps sanitized technical-details expansion. Exhaustive-`when` unit test.

### Task 4 — Capability Center
`feature/settings` → `CapabilityCenterScreen` + VM, route `settings/capabilities`
in `settingsGraph`, entry row in Settings → "Advanced access". Per-`ProviderKind`
card: live `ProviderStatus`, served `Capability`s, limitations, setup action
(ADB → ADB Lab; ROOT → honest not-configured). Renders only registered providers
— the Shizuku row from spec 23 is superseded by native wireless ADB; log the
deviation in `02_foundations/PLAN_MIGRATION_LOG.md`.

### Task 5 — Icons, splash, token map
Convert `branding/candidates/candidate-09-{logo,splash}.svg` to vector
drawables (`ic_launcher_foreground`, `ic_launcher_monochrome`, splash). Set
`windowSplashScreenAnimatedIcon`. `androidx.core:core-splashscreen` for API 28–30
backport requires catalog approval — flagged decision point; fallback keeps the
existing static `windowBackground` splash. Append v5-name → `:core:ui`-token map
to `03_design_system/uiux_v5/implementation/J03_DESIGN_TOKENS.md`.

### Task 6 — Settings export/import
`SettingsRepository.exportJson()/importJson()` — schema `{v:1, exportedAt,
settings:{...}}` covering theme, dynamicColor, technician, favorites, recents,
useCounts, `wireless_adb_auto_connect`. Never exports `wireless_adb_paired`,
`onboarding_completed`, or the AI key. Settings "Backup" row: SAF
`CreateDocument`/`OpenDocument`, import behind a `ConfirmationSheet`. Unit tests:
round-trip, unknown-key tolerance, bad JSON, protected-key exclusion.

### Task 7 — ADB terminal
Amend `AGENTS.md` + `01_AI_OPERATING_CONTRACT.md` + `21_J19_WIRELESS_ADB.md`
first: free-form commands may reach a command line **only** through
`ElevatedOps.runRawCommand`, only from the Terminal screen — the `ElevatedAction`
allow-list invariant stays untouched.
- `core:privileges`: `ElevatedOps.runRawCommand(command)` — rejects blank/>512
  chars, requires a live elevated session, logs every invocation to
  `elevated_action_log`. Not an `ElevatedAction`.
- `SettingsRepository`: `terminal_enabled` (default off, risk dialog on enable).
- `feature/adb`: `adb/terminal` route; entry visible only when session live and
  toggle on; per-install `ConfirmationSheet` ack; mono scrollback (~1000 lines),
  input, clear/export. No AI integration.

### Task 8 — Custom diagnostic presets
`core:database`: `technician_presets(id, name, testIdsCsv, createdAtEpochMs)` +
DAO + DB v5→v6 migration + migration test. `feature/diagnostics`:
`PresetRepository` (resolves ids via `PhoneDoctorTests.findById`, drops
unknowns), preset-builder screen (name + checklist grouped by category), manage
(rename/delete). `armSession` takes resolved tests + label; session `preset`
stores `CUSTOM:<name>`. Preset picker shows custom presets + "New preset…".

> As-built deviation (2026-10-23): instead of a `PresetRepository` and a
> `CUSTOM:<name>` tag, sessions persist their resolved test list in a new
> `diagnostic_sessions.testIds` column (Room v6) — resume is exact even after
> the preset is edited or deleted. `preset` stays the human label; the
> `TechnicianPresetDao` is injected straight into `DiagnosticsViewModel`
> (feature→core:database is the established pattern).

### Task 9 — Multi-device history
Reports list: filter chips from distinct `deviceLabel`/`customerLabel` in
`diagnostic_sessions`; optional group-by-device sections. No schema change.

### Task 10 — Final sweep
Checklist boxes (V5 Tasks 1/2/5/6/10, UX-5 export/import, J19 terminal),
CHANGELOG unreleased entries, `PLAN_MIGRATION_LOG.md`, mockup parity.
No version bump/tag — release-gate action only.

## Verification per task

- `./gradlew :<changed>:assembleDebug` + `testDebugUnitTest` on touched modules;
  verify XML test reports (quiet mode hides task output).
- Reachability: every new screen has route + entry point in the same commit.
- Self-review diff; secrets audit; mockup + checklist in the same commit.
- Device-dependent behavior documented as pending-hardware, never claimed done.
