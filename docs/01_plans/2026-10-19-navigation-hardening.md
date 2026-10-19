# DroidKit Navigation Hardening — Execution Plan

> **For agentic workers:** execute phase-by-phase. The pack (`docs/01_plans/droidkit_navigation_plan/`) is the task spec; this file sequences it against audited HEAD + the in-flight working tree. Each phase ends with the pack's evidence report, tracker update, and a STOP for authorization. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Harden DroidKit navigation end-to-end — routes, back stack, return state, external-screen returns, hardware lifecycle, process death — per pack phases NAV-0…NAV-10, without redesigning working architecture.

**Architecture:** Kotlin + Compose, MVVM/Clean, 35 Gradle modules, single `NavHostController` owned by `MainActivity`, single `NavHost` in `DroidKitNavHost`, 21 per-feature `NavGraphBuilder` graph extensions. Bottom bar: Home/Tools/Diagnose/Settings.

**Tech Stack:** AGP 9.0.1, Kotlin 2.2.20, Compose BOM 2025.09.01, Navigation Compose 2.9.4, Hilt 2.59.2, Room 2.7.2 (DB v5), DataStore.

**Spec files (authoritative task requirements):**
- `docs/01_plans/droidkit_navigation_plan/00_MASTER_NAVIGATION_PROMPT.md` — NAV-0…NAV-10 phases, 13 non-negotiable rules
- `docs/01_plans/droidkit_navigation_plan/01_NAVIGATION_QA_MATRIX.md` — device/emulator evidence grid
- `docs/01_plans/droidkit_navigation_plan/02_ROUTE_CONTRACT.md` — route invariants
- `docs/01_plans/droidkit_navigation_plan/03_AI_FIRST_PROMPT.md` — NAV-0/NAV-1 kickoff brief
- Cross-refs: `docs/01_plans/droidkit_v1_completion_plan/` (parent roadmap — NAV-1 == DK-001, NAV-5 overlaps DK-022, NAV-8 overlaps DK-040/041/042 + Q16b V5); playbook `27` (DoD)

## Global Constraints

- Pack non-negotiables 1–13 apply verbatim: audit before editing; test Back AND Up; no whole-stack clears as shortcuts; no duplicate NavControllers; parameterized patterns never navigated verbatim; preserve user context on return; external returns re-check state; hardware screens release/reacquire; regression test per fix; STOP after each phase.
- Repo rules (`AGENTS.md`): commit per completed task, push to `origin` immediately; audit diff for secrets before every commit; smallest coherent change; no unrelated edits.
- Route contract invariants (`02_ROUTE_CONTRACT.md`): no `Tool.route` with unresolved placeholders (now enforced in `Tool.init`); required args validated pre-navigation; invalid/missing args → safe UI state, never uncaught exception; one source of truth per route constant/builder.
- Windows: use `gradlew.bat`. Report CANNOT VERIFY for device-bound checks — never claim PASS from compilation alone.

## Verified baseline (2026-10-19, HEAD f297198 + working tree)

**App shell** (`app/src/main/java/io/github/droidkit/MainActivity.kt`):
- `rememberNavController()` L84; splash gate = DataStore `onboardingCompleted` + 1.5 s min (`SPLASH_MIN_MS`); bottom `NavigationBar` hidden only on `onboarding`.
- Top-level click already uses the standard contract: `popUpTo(nav.graph.findStartDestination().id) { saveState = true }` + `launchSingleTop` + `restoreState` (L110–114).
- Gap: `selected = currentRoute == dest.route` (L108) fails to highlight the tab when inside a nested route (`diagnose/guided`, `reports/detail/*`, any lab).

**NavHost** (`app/src/main/java/io/github/droidkit/navigation/DroidKitNavHost.kt`): `startDestination` = `home`|`onboarding`; onboarding finish → `navigate("home"){ popUpTo("onboarding"){inclusive} }`; 21 feature graphs; `resolve` lambda passed to 8 capability-gated graphs.

**Parameterized destinations (all verified):**
| Pattern | Arg | Builders / entry |
|---|---|---|
| `sensors/live/{type}` | Int | `SensorsScreen.kt:65`, `LiveSensorPickerScreen.kt:51` — concrete `s.type` |
| `reports/detail/{sessionId}` | String (implicit) | `ReportsFeature.kt:42` |
| `reports/compare` | — | `ReportsFeature.kt:43` |
| `apps/detail/{packageName}` | String | `AppsFeature.detailRoute` |
| `usb/device/{deviceName}` | String (Uri.encode) | `UsbFeature.detailRoute` |
| `bluetooth/gatt/{address}` | String | `BluetoothFeature.gattRoute` |
| `diagnose/test/{testId}` | String | `DisplayFeature:75`, `CameraFeature:70`, `AudioFeature:73` via `testRoute` |

**Confirmed/suspected defects beyond the pack baseline:**
1. `diagnostics.full` tool route = `diagnose/guided`, but `GuidedSessionScreen` + `SessionReviewScreen` do `nav.getBackStackEntry(DiagnosticsFeature.ROUTE)` (`DiagnosticsFeature.kt:51,62`). Direct entry from Home/Search/Tools puts `[home, diagnose/guided]` on the stack — `diagnose` absent → `IllegalArgumentException` crash. Same latent fragility in `reportsGraph` (`getBackStackEntry(ROUTE)` at L51, safe only because detail is reachable solely from the list today). Nested-graph scoping is the idiomatic fix.
2. `diagnostics` `onRetry` (L64) pushes `diagnose/guided` on top of review — stacks `…→review→guided`; intentional or bug — decide in NAV-4.
3. `diagnose/test/{testId}` unknown-id path renders `Text("Unknown test: $testId")` (L77) — safe, no crash, but raw route arg leaks into UI copy (NAV-8 wants no internal strings surfaced).

**External screens / return surface (seed for NAV-5):** `PermissionCenterScreen.kt:118` (app-details settings), `NfcScreen.kt:91` (NFC settings), `AppDetailScreen.kt:121,130` (launch app + app-details settings), `DeveloperLabScreen.kt:44,77` (settings-intent presets), share sheets `ReportDetailScreen:347`, `UsbDetailScreen:224`, `LiveSensorScreen:160`, `DeviceInfoScreen:64`; activity-result launchers `diagnostics/tests/PermissionGate.kt:44`, `BluetoothScreen.kt:46,49,52`.

**Hardware/resource surface (seed for NAV-6):** NFC `enableReaderMode` (`NfcViewModel.kt:105`), `LifecycleEventObserver` (`BatteryScreen.kt:36`), USB attach/detach receiver, CameraX screens, mic/speaker tests, sensor listeners, vibration — cleanup-on-leave to be verified per screen.

**Tool entry call sites:** `MainActivity` (bottom bar), `DashboardScreen` (ready-now/CTA), `ToolsScreen:84`, `InsightsScreen:198`, `DiagnosticsScreen` (`onOpenTool`), lab `onOpenTest` callbacks — all navigate `tool.route` verbatim.

**In-flight (uncommitted at planning time):** NAV-1/DK-001 fix complete in tree — `Tool.init` placeholder guard, `SensorsFeature` route → `sensors/live`, new `LiveSensorPickerScreen`, 2 regression tests in `ToolRegistryTest`. Also DK-002 `<queries>` manifests (V1-pack item, separate commit).

**Test infrastructure:** `:app` has `androidTest` runner + `AppSmokeTest` (J5) — instrumented nav tests are feasible; no existing nav tests.

---

## PHASE NAV-1 — Immediate route safety (land in-flight fix)

### Task 1 — Validate and commit sensors.live fix (NAV-101 + NAV-102 / DK-001)

**Files (already changed, uncommitted):**
- `core/common/src/main/java/io/github/droidkit/core/common/Tool.kt` — `init` guard rejects `{`/`}` in `Tool.route`
- `feature/sensors/.../SensorsFeature.kt` — tool route `sensors/live`; new `composable("$ROUTE/live")` → `LiveSensorPickerScreen`
- `feature/sensors/.../LiveSensorPickerScreen.kt` — picker lists `LIVE_TYPES`-capable sensors → navigates `sensors/live/${s.type}` with concrete arg
- `core/common/src/test/.../ToolRegistryTest.kt` — 2 regression tests

- [ ] **Step 1: Verify** — `gradlew.bat :core:common:testDebugUnitTest :feature:sensors:assembleDebug :app:assembleDebug`. Expected: all pass (registry construction no longer throws).
- [ ] **Step 2: Sweep all registered tools** — grep every `FeatureContribution.tools` for residual `{`/`}` in `route`; expected zero (the `init` guard makes violations unconstructable, but confirm no feature throws at registration).
- [ ] **Step 3: Self-review vs pack rules** — picker approach matches pack-preferred UX; no fake sensor ID; pattern destination `sensors/live/{type}` retained only as graph destination. Confirm `LiveSensorScreen` handles a bad/missing `type` gracefully.
- [ ] **Step 4: Commit + push** — `fix(sensors): route live monitor through sensor picker` (navigation files only). Separately commit `fix(apps): launcher-scoped package visibility queries` for the manifest changes (V1-pack DK-002; update `01_TRACKER.md` there).
- [ ] **Step 5: STOP and report** — audit findings, files, test results, remaining risks.

## PHASE NAV-0 — Route inventory audit

### Task 2 — Full route inventory + graph

**Files:**
- Create: `docs/04_audits/2026-10-19_nav_route_inventory.md`
- Read: all 21 `*Feature.kt` graph files + `MainActivity.kt` + `DroidKitNavHost.kt`

- [ ] **Step 1:** Produce the pack's Route Inventory table (ID | pattern | builder | args | entry points | back target | up target | state to preserve | external return? | risk) covering every `composable(...)` destination — seed from the verified baseline above.
- [ ] **Step 2:** Draw App Start → top-level → features → nested flows graph (mermaid in the audit doc).
- [ ] **Step 3:** Log every `navigate/popBackStack/navigateUp/popUpTo/launchSingleTop/restoreState/saveState` call site and every external `startActivity`/result-launcher; confirm/refute the suspected `getBackStackEntry` defect (baseline defect 1).
- [ ] **Step 4:** Commit `docs: NAV-0 route inventory audit` + push. STOP — compare against pack baseline; report material differences before edits.

## PHASE NAV-2 — Top-level back-stack contract

### Task 3 — Bottom-bar contract verification + selected-state fix

**Files:**
- Modify: `app/.../MainActivity.kt` (selected-state logic)
- Test: `app/src/androidTest/.../` (new `TopLevelNavigationTest`)
- Doc: append contract section to the NAV-0 audit doc or `docs/02_foundations/` nav decision note

- [ ] **Step 1:** Document the contract: Home/Tools/Diagnose/Settings; select = `popUpTo(start)+saveState+singleTop+restoreState` (already implemented — keep); reselect = no-op singleTop (confirm); Back from secondary tab → Home → exit.
- [ ] **Step 2:** Fix selected-state: use `backStackEntry?.destination?.hierarchy` to match nested routes to their tab (e.g. `diagnose/guided` → Diagnose selected). Decide nested→tab mapping: top-level route prefix.
- [ ] **Step 3:** Instrumented test: repeated tab switching creates no duplicate entries; Back order correct; nested screen keeps its tab highlighted.
- [ ] **Step 4:** `gradlew.bat :app:assembleDebug` + connected/emulator run where available (else CANNOT VERIFY). Commit + push. STOP.

## PHASE NAV-3 — Tool entry & return-state contract

### Task 4 — Entry-surface audit + state preservation

**Files:**
- Read: `DashboardScreen.kt`, `ToolsScreen.kt`, `InsightsScreen.kt`, `DiagnosticsScreen.kt`, favorites/recents sources
- Modify: screens missing saveable state (search query, filter chips, scroll)

- [ ] **Step 1:** For each entry surface (Home ready-now, Favorites, Recents, Tools list, Search, Category filter, dashboard CTAs, insights): verify Entry → Tool → Back/Up returns to source without duplicates.
- [ ] **Step 2:** Preserve search query / category selection / scroll — `rememberSaveable`/Saver or ViewModel per existing patterns; no Room/DataStore for ephemeral UI state.
- [ ] **Step 3:** Guard double-tap duplicate pushes where cheap (singleTop on entry routes where safe).
- [ ] **Step 4:** Targeted tests + `assembleDebug`. Commit + push. STOP.

## PHASE NAV-4 — Nested diagnostic/report flows

### Task 5 — Fix parent-entry scoping crash + session-flow contract

**Files:**
- Modify: `feature/diagnostics/.../DiagnosticsFeature.kt` (nested graph or VM scoping)
- Modify: `feature/reports/.../ReportsFeature.kt` (same pattern at L51)
- Modify: `feature/diagnostics/.../GuidedSessionScreen.kt`, `SessionReviewScreen.kt` (Back confirm)
- Test: `app/src/androidTest/` diagnostics flow test + JVM test for retry/back semantics if extractable

- [ ] **Step 1: Failing test** — instrumented: Home → `diagnostics.full` card → guided screen renders (today: crash at `getBackStackEntry`).
- [ ] **Step 2: Fix** — wrap each feature's destinations in a nested `navigation(route=..., startDestination=...)` graph and scope the session ViewModel to the graph's entry (always present inside the graph), or scope to activity if product semantics require. Keep shared-VM behavior identical.
- [ ] **Step 3: Session-exit contract** — Back/Up during an active `diagnose/guided` session with recorded progress → confirmation dialog (data-loss risk); no progress → pop directly. `onRetry`: pop review first (`popBackStack` then navigate, or `popUpTo(REVIEW){inclusive}`) so retry doesn't stack review copies.
- [ ] **Step 4: Reports detail** — `reports/detail/{sessionId}`: keep parent-scoped VM valid under direct entry (same nested-graph fix) or make detail self-scoped; empty/invalid `sessionId` must render an honest empty state, not crash or show raw arg.
- [ ] **Step 5:** `gradlew.bat assembleDebug testDebugUnitTest` + instrumentation. Commit + push. STOP.

## PHASE NAV-5 — External screens & return refresh

### Task 6 — Lifecycle-aware return-state refresh

**Files:**
- Modify: screens with external exits — `PermissionCenterScreen`, `NfcScreen`, `AppDetailScreen`, `DeveloperLabScreen`, `BluetoothScreen`, `AdbScreen`/`AppOpsScreen`, `SettingsScreen` (provider setup)
- Read: `core/privileges/.../CapabilityManager` (current pull-based `resolve`)

- [ ] **Step 1:** Inventory every external Intent (baseline seed above) + result launchers; record pre-launch → return → refresh expectation per screen.
- [ ] **Step 2:** Introduce the smallest resume-refresh contract: re-resolve capabilities/permissions on `ON_RESUME` (LifecycleEventObserver pattern already in `BatteryScreen`) for affected screens — designed to migrate cleanly onto DK-022 reactive status later. No screen-specific hacks duplicating that work.
- [ ] **Step 3:** Verify grant / deny / permanent-deny / cancel / repeated-return paths; correct Ready/Blocked rendering.
- [ ] **Step 4:** Tests where feasible + `assembleDebug`. Commit + push. STOP.

## PHASE NAV-6 — Hardware lifecycle navigation

### Task 7 — Per-screen resource audit + cleanup

**Files:**
- Audit: `NfcViewModel` (readerMode start/stop), `CameraFeature`/`CameraScreen` (CameraX bind/unbind), flash torch, `AudioFeature` mic/speaker jobs, `SensorsViewModel`/`LiveSensorScreen` (listeners), `UsbViewModel` (receiver), `BluetoothViewModel`, vibration, `diagnostics/tests/*` hardware tests

- [ ] **Step 1:** Per screen: enter → start → Back → verify cleanup → return → verify reacquire → background/foreground → rotate. Record in QA matrix format.
- [ ] **Step 2:** Fix leaks — `DisposableEffect`/`onStop`/`viewModelScope` discipline per existing patterns; no resource stays active because a composable left.
- [ ] **Step 3:** `assembleDebug` + leak verification notes (device-bound items → CANNOT VERIFY + manual script). Commit + push. STOP.

## PHASE NAV-7 — State restoration & process death

### Task 8 — State classification + SavedStateHandle audit

**Files:**
- Audit: every screen ViewModel for `SavedStateHandle`; `rememberSaveable` coverage; Room `diagnostic_sessions`/`device_snapshots` recovery path
- Create: classification table in the NAV audit doc (EPHEMERAL | SAVEABLE_UI | VIEWMODEL | PERSISTED_DOMAIN)

- [ ] **Step 1:** Classify each stored state; move mis-layered state (e.g. domain state in `rememberSaveable`, ephemeral UI in Room).
- [ ] **Step 2:** Args safety — every parameterized destination tolerates missing/invalid args (defaults or safe UI); `reports/detail` empty-id path explicitly handled.
- [ ] **Step 3:** Rotation/activity-recreation verified for: active diagnostic session, report detail id, tool args, search/filter. Process-death session recovery only via existing Room session data — never fake it.
- [ ] **Step 4:** `assembleDebug testDebugUnitTest`. Commit + push. STOP.

## PHASE NAV-8 — Navigation accessibility & UX

### Task 9 — A11y pass on navigation surfaces

**Files:**
- Modify: `MainActivity` nav items (done: `contentDescription = dest.label` — verify), top app bars (Up affordance + description), error/empty states

- [ ] **Step 1:** App-bar Up exists and is described on non-top-level screens where product UX expects it; verify Back==Up outcomes (NAV-4 dialog consistent for both).
- [ ] **Step 2:** Screen titles/headings update per destination; focus order logical after navigation; navigation-driven status changes announced where needed.
- [ ] **Step 3:** User-facing copy never exposes raw route strings/args (fix `diagnose/test` unknown-id copy).
- [ ] **Step 4:** Coordinate with Q16b V5 + DK-040/041/042; record which items belong to that pass vs this one. `assembleDebug`. Commit + push. STOP.

## PHASE NAV-9 — Automated navigation test suite

### Task 10 — Instrumented nav suite in `:app`

**Files:**
- Create: `app/src/androidTest/java/io/github/droidkit/navigation/` test classes
- Read: existing `AppSmokeTest` for runner/Hilt-test patterns

- [ ] **Step 1:** Suite covering pack's 12-case minimum: start/onboarding→Home; tab switching no-dup; Home→Tool→Back; search/filter→Tool→Back state retained; Favorites/Recents→Tool→Back; sensors entry regression; diagnostics nested Back; reports detail Back; settings→external→return (grantable subset); provider grant return (mocked); deny/permanent-deny return; hardware leave/return (where instrumentation allows).
- [ ] **Step 2:** Prefer behavior assertions over implementation details; mark device-only cases explicitly.
- [ ] **Step 3:** Run on emulator/device where attached; else `assembleDebugAndroidTest` + CANNOT VERIFY. Commit + push.

## PHASE NAV-10 — Final acceptance

### Task 11 — Full check + evidence report

**Files:**
- Create: `docs/04_audits/2026-10-19_nav_acceptance.md`
- Modify: `docs/01_plans/10_DroidKit_Master_Journey_Checklist.md` (J14 "Navigation review"), V1 tracker DK-001 row, `docs/01_plans/2026-10-19-v1-completion-execution.md` status note

- [ ] **Step 1:** `gradlew.bat assembleDebug testDebugUnitTest` (+ `assembleDebugAndroidTest` / instrumented run if device attached).
- [ ] **Step 2:** Fill `01_NAVIGATION_QA_MATRIX.md` results into the acceptance doc with PASS/FAIL/BLOCKED/NOT TESTED + evidence; final route inventory; fixed defects; tests added; device-only remainder.
- [ ] **Step 3:** Verdict: navigation V1-ready — YES / YES WITH CONDITIONS / NO. Update checklist/tracker entries only for verified items.
- [ ] **Step 4:** Commit `docs: NAV-10 acceptance report` + push. STOP — final gate.

---

## Notes on scope boundaries

- NAV-1 fix and DK-002 manifest fix are already written in the working tree; Task 1 lands them as two separate commits (nav scope vs V1-pack scope). Do not expand Task 1 into Phase-0-of-the-V1-pack work beyond committing what's already there.
- DK-022 (reactive provider status) stays in the V1 pack; NAV-5 introduces only the minimal resume-refresh contract designed to migrate onto it.
- Q16b V5 design system owns visual/a11y restyling; NAV-8 covers only navigation-driven a11y (Up affordance, focus, announcements, copy hygiene).
- Every phase reports in the pack's evidence format: Status | Files | What changed | Why | Tests | Command/result | Device/emulator evidence | Remaining risk.
