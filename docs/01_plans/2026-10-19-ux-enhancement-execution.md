# UX Enhancement Pack — Execution Plan (2026-10-19)

Status: **IMPLEMENTED** — all phases landed; validation results below.
Source pack: `docs/01_plans/droidkit_ux_plan/` (16 feature docs + master + QA).
Audited against: current HEAD + uncommitted UX-foundation work.

## Audit → classification

| Pack item | Status | Notes |
|---|---|---|
| 01 UX foundation | Implemented | `UiProblem`, `UiProblemCard`, `UiProblemMappers`, `RuntimePermissionGate`, `PermissionRequestEffect`, `openAppSettings`, `EmptyState`, `AiError.describe()` |
| 02 Permissions center | Partial | `PermissionCenterScreen` lists declared permissions only; no Settings entry, no purpose/affected-tools, no refresh-on-resume |
| 03 Global search | Partial | Tools search (title/desc/keywords/category); no Settings/Help results |
| 04 Favorites/recents | Implemented | DataStore-backed, deduped, bounded(8), dashboard sections |
| 05 Tool information | Missing | No per-tool info surface |
| 06 Privacy Center | Missing | — |
| 07 AI & online services | Implemented | Settings "DroidKit Intelligence" (BYOK, model, test, chat); `PrivacyFilter` in core:ai |
| 08 Help & support | Implemented | `AboutHelpScreen` categories; GitHub issue links (configured via `AboutLinks`) |
| 09 About | Implemented | `AboutScreen` + 5 subscreens, `AppInfo`, `AboutLinks` |
| 10 What's New | Implemented | `ReleaseNotes` transcribed from CHANGELOG |
| 11 Accessibility | Partial | Merged row semantics in About; favorite toggle + dialog semantics need a pass |
| 12 Tool availability | Implemented | `ToolResolution.Blocked` → `UiProblemCard` dialog, reactive `ProviderStatusBus` |
| 13 Service status | Implemented | Provider statuses in Settings/Dashboard/DeviceInfo; update check distinguishes offline/failed/no-releases |
| 14 Settings IA | Partial | Needs Permissions & Access / Privacy / Help entries |
| 15 Settings recovery/export | Missing | Reset + clear-history justified; export/import deferred (small config surface) |
| 16 Security/secrets | Implemented | BYOK AES-GCM keystore; diff audit before each commit |
| 17 Testing/QA | Pending | Scoped: changed modules only |

## Remaining work — phases

### Phase A — Permissions & Access (pack 02, 14)
- Settings → "Permissions & Access" row → existing `permissions` route.
- Enrich `PermissionCenterScreen`: purpose + affected tools per permission
  (from `ToolRegistry.requiredPermissions`), runtime vs install-time sections,
  refresh grant state on resume, "Open system app settings" kept.

### Phase B — Privacy Center (pack 06, 14)
- New `settings/privacy` route + screen under `about/` package.
- Sections: on-device data (sessions/reports/favorites/recents/preferences,
  elevated history), network activity (AI opt-in Groq, GitHub update check),
  AI data minimization (PrivacyFilter).
- Controls: clear recents, clear favorites, clear elevated history.
- Links: Permissions & Access, AI settings info. Privacy Policy row follows
  `AboutLinks.privacyPolicyUrl` (unconfigured → honest state).

### Phase C — Tool information (pack 05)
- Info action on `ToolCard` → `ToolInfoDialog` built only from real `Tool`
  metadata: description, category, safety level, required capabilities,
  permissions, hardware features, minSdk, availability resolution, phase.

### Phase D — Search extension (pack 03)
- Group results in Tools search: "Tools" (existing) + "Pages" — static index
  of real routes only (Settings, Permissions & Access, Privacy Center,
  About, What's New, Help & FAQ, DroidKit Chat). No hidden/dev routes.

### Phase E — Settings IA + recovery (pack 14, 15)
- "Support & privacy" section: Permissions & Access, Privacy Center,
  Help & FAQ, What's New rows.
- "Reset settings" with confirmation; clears DataStore settings
  (theme, favorites, recents, use-counts, technician name). AI key/provider
  credentials untouched — stated in dialog.
- `SettingsRepository`: `clearFavorites()`, `clearRecents()`, `resetAll()`.

### Phase F — Accessibility pass (pack 11)
- Favorite toggle: state-aware contentDescription (Add/Remove).
- Confirm dialog semantics, decorative icons unlabeled, full-row targets
  on all new rows.

### Phase G — QA (pack 17)
- Targeted: `:feature:settings`, `:feature:permissions`, `:feature:tools`,
  `:core:privileges`, `:core:ui` compile + unit tests; secrets scan of diff;
  update journey checklist; commit+push per task.

## Deferred
- Settings export/import (15): config surface too small to justify; documented.
- What's-New auto-prompt on upgrade (10): no reliable version-transition
  detection yet; manual screen only.
- Per-tool bespoke help pages (05): single metadata-driven dialog instead —
  no duplicated per-tool layouts.

## Validation performed (2026-10-19)
- `:feature:settings`, `:feature:permissions`, `:feature:tools`, `:core:ui`,
  `:core:privileges`, `:feature:dashboard` `compileDebugKotlin` — PASS.
- `:core:database:testDebugUnitTest` + `:feature:settings:testDebugUnitTest` —
  PASS (165 tasks, 3 executed). DB test SDK-pinned for Robolectric (max SDK 35
  vs targetSdk 36).
- `verifyModuleBoundaries` — PASS.
- Secrets scan of full diff — clean; only configured GitHub URLs appear.
- Note: commit `5ea2d1e` references `onInfo`/`ToolInfoDialog` whose definitions
  land in the follow-up commit (working tree is green; HEAD standalone is not).
