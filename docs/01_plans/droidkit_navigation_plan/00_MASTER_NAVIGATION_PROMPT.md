# DroidKit Navigation, Back Stack & Return-State Hardening — Master AI Prompt

## Mission
Audit and harden DroidKit navigation from app launch/onboarding through every top-level destination, tool, nested flow, external Android screen, capability setup, and return path. Work against the CURRENT repository as source of truth. Do not redesign working architecture merely to match this document.

## Known repository baseline from prior QA
- Kotlin + Jetpack Compose + Navigation Compose 2.9.4.
- Central DroidKitNavHost with per-feature NavGraphBuilder graph extensions.
- Top-level navigation: Home, Tools, Diagnostics, Settings; Reports/Insights reachable within flows.
- ToolRegistry + FeatureContribution provide tool metadata/routes.
- Current routes are strings, not type-safe.
- Back stack is standard Navigation Compose; nested diagnostic/session flows exist.
- Known defect: ToolRegistry entry `sensors.live` uses parameterized `sensors/live/{type}` and direct tool navigation can navigate the literal placeholder, causing a crash.
- Capability/provider status is currently pull-based/non-reactive, so returning from Shizuku/ADB/root/settings may show stale state.
- There are currently no meaningful navigation instrumentation tests.

## Preserve unless repository evidence proves a defect
- ToolRegistry + FeatureContribution multibinding
- CapabilityManager + ToolResolution
- ElevatedAction/ElevatedOps provider architecture
- Room/DataStore models and migrations
- SessionEngine, ReportBuilder, InsightsCalculator
- Existing feature module boundaries
- Existing app shell and per-feature graph organization

## Non-negotiable rules
1. Audit before editing.
2. Never assume a route works because it compiles.
3. Never assume Android Back and app-bar Up are equivalent; test both.
4. Do not clear the entire back stack as a shortcut unless the intended UX explicitly requires it.
5. Do not create duplicate NavControllers.
6. Do not move business logic into composables to fix navigation.
7. Do not duplicate ToolRegistry data in navigation.
8. Parameterized route patterns must never be used as executable destination strings without concrete arguments.
9. Preserve user context when returning where reasonable: search/filter state, selected tab/category, scroll state, form/session state, and diagnostic progress.
10. External activity/settings returns must re-check permissions/capabilities rather than rely on stale pre-launch state.
11. Hardware screens must release resources when leaving and safely reacquire them when returning.
12. Every fix needs a regression test where technically feasible.
13. After each phase: compile, test, report evidence, then STOP unless explicitly told to continue.

## Target navigation model
APP START
→ first launch? Onboarding → Home
→ returning user → Home

TOP LEVEL
Home ↔ Tools ↔ Diagnostics ↔ Settings
- switching top-level tabs must not create endless duplicate copies
- reselect behavior must be intentional
- Back behavior from top level must be consistent

TOOL FLOW
Home/Tools/Search/Favorites/Recents → Tool → nested detail/action → Back/Up → previous context

DIAGNOSTIC FLOW
Diagnostics → preset/intake → session → individual test → review/result/report
- Back/Up must not accidentally destroy an active session
- destructive exit requires explicit confirmation if data/progress would be lost
- process recreation must recover persisted session data where architecture supports it

EXTERNAL FLOW
DroidKit → Android Settings/permission/provider setup → DroidKit
- re-check state on resume/return
- show updated Ready/Blocked state
- avoid duplicate destination creation

HARDWARE FLOW
DroidKit → Camera/BLE/NFC/USB/Sensor screen → Back → Return
- stop listeners/scans/readers/camera safely
- reacquire only when screen becomes active again
- no leaked receivers/jobs/resources

## Phase NAV-0 — Repository Navigation Audit
Before modifications, map:
- MainActivity/app start/onboarding decision
- NavController ownership
- DroidKitNavHost
- every top-level route
- every feature graph
- every Tool.route
- every parameterized route and argument type/default
- all `navigate`, `popBackStack`, `navigateUp`, `popUpTo`, `launchSingleTop`, `restoreState`, `saveState` usage
- external Intent launches and ActivityResult launchers
- lifecycle/resume handling after external screens
- session flows
- places where selected/search/filter/scroll state is stored

Produce a Route Inventory table:
ID | Source | Route Pattern | Executable Route Builder | Args | Entry Points | Back Target | Up Target | State to Preserve | External Return? | Risk

Also produce a graph showing App Start → top-level → features → nested flows.

STOP after audit and report findings before edits if the repository differs materially from the baseline.

## Phase NAV-1 — Immediate Route Safety
### NAV-101 Fix sensors.live
- Inspect actual SensorsFeature registration and graph.
- Separate route PATTERN from route DESTINATION/build function if necessary.
- A ToolRegistry route must be directly executable or provide enough metadata to build a valid destination.
- Do not insert fake sensor IDs just to avoid the crash unless product UX clearly defines a default sensor.
- Preferred UX: registry card may route to sensor inventory/list, then user selects a concrete sensor; or define an explicit safe entry destination.
- Add regression test ensuring no registered direct tool route contains unresolved `{argument}` placeholders.

### NAV-102 Registry-route validation
Add a lightweight invariant/test that checks all registered tool entry routes are navigable entry destinations and do not contain unresolved required placeholders. If the architecture intentionally permits route templates, model template vs executable entry separately.

Definition of Done:
- no ToolRegistry card can emit an unresolved route
- sensors.live no longer crashes from Home/Tools/Search
- unit/navigation test catches recurrence
- assembleDebug + unit tests pass

STOP and report.

## Phase NAV-2 — Top-Level Back Stack Contract
Define and implement explicit contract for Home/Tools/Diagnostics/Settings.

Expected behavior unless current product docs specify otherwise:
- tapping a different bottom destination navigates once
- tapping the current destination does not stack duplicates
- switching tabs preserves each tab's reasonable state when supported
- Back from a nested screen returns within that feature first
- Back from a top-level secondary tab follows the chosen product contract consistently (prefer Android-standard behavior; document exact choice)
- Home is the stable root
- no loops like Home→Tools→Home→Tools accumulating indefinitely

Inspect/use Navigation Compose options such as launchSingleTop/saveState/restoreState/popUpTo only where they match the contract.

Add tests for repeated tab switching and Back.

STOP and report.

## Phase NAV-3 — Tool Entry & Return-State Contract
Test every entry surface:
- Home ready-now
- Favorites
- Recents
- Tools list
- Search results
- Category filter
- Dashboard CTAs

For each:
Entry → Tool → Back/Up must return to the logical source without duplicating screens.

Preserve where reasonable:
- search query
- category/filter selection
- list scroll position
- favorite/recent state

Use ViewModel/SavedStateHandle/saveable state according to existing architecture. Do not globally persist ephemeral UI state in Room/DataStore unless justified.

Add automated tests for representative paths.

STOP and report.

## Phase NAV-4 — Nested Diagnostic/Report Flows
Audit Diagnostics, Phone Doctor, reports, insights, compare/review flows.

Verify:
- preset → intake → session → test → next test → review
- retry/skip
- report detail/export/share preview
- Back during active diagnostic session
- Up vs system Back
- accidental exit prevention where progress could be lost
- completion removes/replaces obsolete transient screens so Back does not reopen completed steps incorrectly
- persisted session can be reconstructed after activity/process recreation where supported

Never fake session recovery. Use existing Room/SessionEngine as source of truth.

STOP and report.

## Phase NAV-5 — External Android Screens & Return Refresh
Inventory every external Intent/activity-result flow:
- app settings
- runtime permission flows
- Shizuku grant/setup
- wireless ADB setup/pairing if external screens are involved
- developer settings
- NFC/USB permission flows
- any package/app settings pages

For each flow verify:
DroidKit state before launch → external screen → return → refresh → correct Ready/Blocked/Denied state.

Integrate with the planned reactive capability work rather than adding screen-specific hacks. If full reactive provider status is not yet implemented, introduce the smallest lifecycle-aware refresh contract that can later migrate cleanly.

Test denial, grant, cancellation, repeated return, and process recreation where feasible.

STOP and report.

## Phase NAV-6 — Hardware Lifecycle Navigation
For Camera, BLE, Bluetooth, NFC, USB, Sensors, audio/mic and other active-resource screens:
- enter
- start resource
- Back
- confirm cleanup
- return
- confirm safe restart
- background/foreground
- rotate/recreate where supported

Check receivers, callbacks, scans, reader modes, camera sessions, coroutines/jobs and flows.

No resource should remain active solely because a composable left the screen.

STOP and report.

## Phase NAV-7 — State Restoration & Process Death
Audit SavedStateHandle, rememberSaveable, Room/DataStore and ViewModel ownership.

Test/reason about:
- rotation
- activity recreation
- process death/relaunch
- active diagnostic session
- search/filter state
- report detail identifiers
- tool arguments

Classify state as:
EPHEMERAL | SAVEABLE_UI | VIEWMODEL | PERSISTED_DOMAIN
and store it at the correct layer.

Do not persist everything merely to make tests pass.

STOP and report.

## Phase NAV-8 — Navigation Accessibility & UX
Ensure:
- app-bar Back/Up has correct contentDescription
- screen titles/headings update appropriately
- focus lands logically after navigation
- important navigation-driven status changes can be announced where needed
- Back confirmation dialogs are accessible
- route errors never expose internal route strings to users

Coordinate with DroidKit V5 semantic UI/a11y work.

STOP and report.

## Phase NAV-9 — Automated Navigation Test Suite
Build a maintainable suite covering at minimum:
1. app start/onboarding → Home
2. top-level switching without duplicates
3. Home → Tool → Back
4. Tools → search/filter → Tool → Back with state retained
5. Favorites/Recents → Tool → Back
6. Sensors direct entry regression
7. Diagnostics nested flow Back behavior
8. Reports detail Back
9. Settings → external settings → return/refresh
10. provider grant return state
11. permission deny/permanent deny return state
12. hardware screen leave/return lifecycle (where instrumentation allows)

Prefer testing behavior over implementation details.

## Phase NAV-10 — Final Acceptance
Run available checks. At minimum:
- assembleDebug
- testDebugUnitTest
- configured lint/static analysis if now available
- navigation/UI instrumentation suite on emulator/device

Then provide:
- changed files
- route inventory final version
- fixed defects
- remaining unverified device-only cases
- tests added and results
- known limitations
- whether navigation is V1-ready: YES / YES WITH CONDITIONS / NO

## Required final evidence format
For every completed NAV task:
Status | Files | What changed | Why | Tests | Command/result | Device/emulator evidence | Remaining risk

Do not mark a task complete from compilation alone.
