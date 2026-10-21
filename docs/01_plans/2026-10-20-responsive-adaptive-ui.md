# Responsive & Adaptive UI Plan

**Goal:** Fix the "top bar sits too low" bug on newer/taller phones, then make every screen responsive across compact/medium/expanded widths — adaptive nav shell, responsive content rules, list-detail where spec'd, landscape-correct, synced mockup.

**Architecture:** `enableEdgeToEdge` + `windowLayoutInDisplayCutoutMode=shortEdges` keeps content full-bleed; M3 `NavigationSuiteScaffold` (in `core:ui`) swaps NavigationBar ↔ NavigationRail from `WindowSizeClass`; per-screen fixes stay targeted. List-detail (`ListDetailPaneScaffold`) lands only for Apps/USB/Reports at expanded, routes preserved.

**Tech Stack:** Kotlin + Compose, `material3` 1.4.0 (BOM 2025.09.01), `material3-adaptive-navigation-suite` (BOM), `androidx.compose.material3.adaptive:adaptive` + `adaptive-layout` 1.3.0.

**Locked decisions (owner-approved 2026-10-20):**
- Bug is in the real app, not the mockup.
- Absorbs v5 plan Task 7's **shell portion only** — Home bento + per-feature v5 migration stay in `2026-10-19-ui-ux-v5-design-system.md` (logged in `PLAN_MIGRATION_LOG.md`).
- Form factors: phones + small tablets/foldables. Landscape: works correctly, no bespoke layouts.
- List-detail only where `20_ADAPTIVE_COMPOSE.md` specs it: Apps, USB, Reports.
- Diagnose first, then fix.

## Global constraints

- Kotlin `2.2.20` (2.3.20 fallback per build-config plan), Compose BOM `2025.09.01`, minSdk 28.
- `material3-adaptive-navigation-suite` — BOM-managed, no explicit version. `adaptive`/`adaptive-layout` — pin `1.3.0`; fall back to `1.2.x` only on resolution conflict.
- Nav chrome only on top-level routes (`home/tools/diagnose/settings`), same as today; secondary screens keep the Up affordance.
- Full-bleed diagnostic test screens stay full-bleed — only verify affordances clear the status bar/cutout.
- Every catalog/new-screen change ships its destination + dispatch in the same commit; mockup updates ride the same commit as UI changes.
- Commit per task, push after every commit; targeted validation only (changed modules).
- No secrets; AI surfaces unchanged by this plan.

---

## Phase A — Top-bar diagnosis & edge-to-edge correctness

### Task A1: Diagnose the gap

**Files:** `app/src/main/java/io/github/droidkit/MainActivity.kt`, `app/src/main/AndroidManifest.xml`, `app/src/main/res/values/themes.xml`.

Root-cause candidates (ranked):
1. `windowLayoutInDisplayCutoutMode` unset → letterboxing below the cutout on punch-hole phones (matches "bigger on newer phones").
2. Status-bar strip color discontinuity (`splash_background` `#040808` window background vs light surface).
3. Double top inset (unlikely — M3 `TopAppBar` + `Scaffold` each apply once).
4. `OnboardingScreen` missing top inset (confirmed — Skip row can slide under status bar).

- [x] Static audit: theme attrs, cutout mode, inset application per screen. (No AVDs on dev machine — device verification left to owner.)
- [ ] DEBUG-only inset probe in `MainActivity` — log `statusBars`/`displayCutout`/`layoutInDisplayCutoutMode` at startup for device-side verification.

### Task A2: Cutout + theme fix

**Files:** `app/src/main/res/values/themes.xml`, `app/src/main/java/io/github/droidkit/MainActivity.kt`.

- [ ] `android:windowLayoutInDisplayCutoutMode="shortEdges"` in `Theme.DroidKit`.
- [ ] `window.attributes.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` in `onCreate` (OEM quirks).
- [ ] Verify status/nav icon contrast light + dark; transparent system bar colors.

### Task A3: Inset normalization

**Files:** `app/src/main/java/io/github/droidkit/onboarding/OnboardingScreen.kt`; offenders found in A1.

- [ ] Onboarding: `WindowInsets.safeDrawing` on the Scaffold (or `statusBarsPadding` on top row).
- [ ] Full-bleed test screens: verify result/exit controls clear status bar + cutout.
- [ ] Remove any double-applied top inset found.

### Task A4: Validate + commit

- [ ] `./gradlew :app:assembleDebug`; visual check matrix (device owner: tall punch-hole phone, small phone, tablet; light/dark; gesture/3-button).
- [ ] Commit `fix(ui): correct top-bar positioning on cutout/tall-status-bar devices` + push.

## Phase B — Adaptive navigation shell (v5 Task 7 shell portion)

### Task B1: Dependencies

**Files:** `gradle/libs.versions.toml`, `build-logic/src/main/kotlin/droidkit/AndroidLibraryComposeConventionPlugin.kt`, `feature/{apps,usb,reports}/build.gradle.kts`.

- [ ] Catalog: `material3-adaptive-navigation-suite` (BOM), `adaptive` + `adaptive-layout` @ `composeAdaptive = "1.3.0"`.
- [ ] Convention plugin: nav-suite + adaptive to all Compose modules; `adaptive-layout` per-module (apps/usb/reports).
- [ ] Sync check + commit.

### Task B2: Shared destinations + suite wrapper

**Files:** create `core/ui/.../navigation/TopLevelDestinations.kt`, `core/ui/.../navigation/DroidKitNavigationSuite.kt`.

- [ ] Move destination list (route/label/icon) out of `MainActivity` into `core:ui`; suite uses it + `navigateTopLevel`/`openRoute`.
- [ ] `DroidKitNavigationSuite(currentRoute, onNavigate) { content }` — default `calculateFromAdaptiveInfo` (bar at compact width or compact height/tabletop → bottom bar persists in phone landscape; rail otherwise); chrome only when `currentRoute in TOP_LEVEL_ROUTES`.
- [ ] Unit test: nav-type mapping compact→bar, medium/expanded→rail, tabletop→bar.

### Task B3: MainActivity integration

- [ ] Replace `Scaffold{bottomBar}` with `DroidKitNavigationSuite`; splash/onboarding outside chrome.
- [ ] Nested inner Scaffolds keep correct insets (no double bottom pad).
- [ ] `assembleDebug` + rail check ≥600dp; commit.

## Phase C — Responsive content

### Task C1: Helpers

**Files:** create `core/ui/.../adaptive/DroidKitWindowInfo.kt`, `core/ui/.../adaptive/Responsive.kt`.

- [ ] `rememberDroidKitWindowInfo()` → width class via `isWidthAtLeastBreakpoint(600/840)`, `isLandscape`, `showsRail`.
- [ ] `Modifier.contentWidthCap(840.dp)` — centered readable width on expanded.
- [ ] Unit tests for pure mapping.

### Task C2: Per-screen audit + fixes

- [ ] Audit table (all screens): top inset · bottom inset · expanded cap · landscape scroll · large-font stacking.
- [ ] Targeted fixes only (AGENTS scope); no unrelated rewrites.

### Task C3: List-detail at expanded — Apps, USB, Reports

- [ ] `ListDetailPaneScaffold` at expanded only; compact/medium keep push nav; routes preserved for deep links.
- [ ] Screen + dispatch same commit per feature.

## Phase D — Mockup, docs, validation

### Task D1: `mockups/index.html`

- [ ] Responsive frame: `height: min(800px, 92vh)`, `width: min(392px, calc(92vh*0.49), 96vw)`, top-anchored.
- [ ] Size-class switcher (compact/medium/expanded); rail render at medium+; two-pane mock for Apps/USB/Reports at expanded.

### Task D2: Docs

- [ ] This plan file; `PLAN_MIGRATION_LOG.md` entry (partial Task-7 supersession); checklist V5-3 "Adaptive navigation" tick when B3 lands.

### Task D3: Validation

- [ ] `assembleDebug` + `testDebugUnitTest` on changed modules only.
- [ ] Preview matrix (compact/medium/expanded); emulator/device matrix per `playbook/23` where hardware allows; mark device-bound items `BLOCKED: hardware` in audit notes.

## Audit trail

| Date | Finding |
|---|---|
| 2026-10-20 | `windowLayoutInDisplayCutoutMode` unset — letterboxing suspected on punch-hole phones. `OnboardingScreen` missing top inset. No `WindowSizeClass`/adaptive usage anywhere. `NavExtensions.kt` (top-level routes) landed in `5d78df8`. |
