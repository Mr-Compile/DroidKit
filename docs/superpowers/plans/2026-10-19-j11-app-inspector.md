# J11 App Inspector (v0.6.0) Implementation Plan

> **STATUS: IMPLEMENTED** — all 3 tasks executed on branch `feature/j11-app-inspector`
> (2026-10-19). `:app:compileDebugKotlin` green. Open gate item: device run of the
> inspector (visibility count, launch, components), `v0.6.0` tag at merge.
> Deviations: component lists capped at 30 rows each with an overflow count;
> app icon rendered via `toBitmap()`; APK export deferred POST-V1 per freeze.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the apps stub with a real App Inspector: visible-package list with search + user/system filters, and a per-app detail screen (version/SDK/installer/times, components, permissions, launch/settings actions). Tag `v0.6.0`.

**Architecture:** `feature:apps`. `AppsViewModel` reads `PackageManager` on `Dispatchers.IO` — `getInstalledPackages(GET_PERMISSIONS or COMPONENT flags)` returns only *visible* packages on Android 11+ (no QUERY_ALL_PACKAGES; the spec requires visible apps only, and the header states the visibility caveat honestly). Detail screen takes `packageName` and reads the full `PackageInfo` lazily.

**Tech Stack:** Kotlin, Compose M3, Hilt, framework `PackageManager` only — no new deps.

## Global Constraints

- Requires: skeleton + J10 (InfoRow/SectionCard primitives, hiltViewModel pattern)
- Spec: `specs/feature_plans_v2/features/13_APP_APK_INSPECTOR_V2.md`
- Honesty: package list header states "Android 11+ limits package visibility — N apps visible" instead of claiming completeness. `minSdk` shown only when `ApplicationInfo.minSdkVersion` reports a value; absent → `Not exposed`. No QUERY_ALL_PACKAGES (Play-restricted; spec asks for visible apps)
- Frozen decision: **APK export is POST-V1** — not built; the checklist row gets a deferred note
- Safety: "Launch app" uses `getLaunchIntentForPackage` (null → honest state); "App settings" opens `ACTION_APPLICATION_DETAILS_SETTINGS`. No destructive package actions in V1
- Commit per task, push after every commit; `JAVA_HOME` export for builds

---

### Task 1: Model + VM + list screen

**Files:**
- Create: `feature/apps/.../InstalledApp.kt` — data class (packageName, label, versionName?, versionCode, isSystem, targetSdk, minSdk?, installer?, firstInstallTime, lastUpdateTime, sourceDir)
- Modify: `feature/apps/.../AppsViewModel.kt` (create) — `packages: StateFlow<List<InstalledApp>>`, `query`, `filter` (ALL/USER/SYSTEM); loads on init
- Modify: `feature/apps/.../AppsScreen.kt` (create) — search field, filter chips, visibility-caveat header, list rows → detail

- [x] **Step 1: Implement model + VM (Dispatchers.IO load, loading state)**
- [x] **Step 2: List screen + route to `apps/detail/{packageName}`**
- [x] **Step 3: Compile + commit** `feat(apps): installed-app list with search and filters`

---

### Task 2: Detail screen

**Files:**
- Create: `feature/apps/.../AppDetailScreen.kt`
- Modify: `feature/apps/.../AppsViewModel.kt` — `packageInfo(pkg): PackageInfo?` suspend getter with COMPONENT+PERMISSION flags
- Modify: `feature/apps/.../AppsFeature.kt` — detail route, drop `FeatureStubScreen`, update tools (App Inspector + Component Explorer + Permission Inspector sub-tools)

- [x] **Step 1: Detail sections** — identity (label, package, icon via `PackageManager.getApplicationIcon`→`Drawable`→`Image` bitmap), version name/code, target/min SDK (`minSdkVersion` null-safe), first install/update times, installer, APK source path; actions row: Launch / App settings / Copy package name
- [x] **Step 2: Components** — expandable lists: activities, services, receivers, providers with exported/enabled; empty groups say "None declared" or "Not exposed by this package" honestly (PM may return null arrays)
- [x] **Step 3: Permissions** — requested permissions grouped granted/denied via `requestedPermissionsFlags`; "Not requested" when null
- [x] **Step 4: Compile + commit** `feat(apps): package detail, components, permissions`

---

### Task 3: Release gate — 0.6.0, changelog, checklist, plan, queue

- [x] **Step 1: `0.6.0`/`6` + CHANGELOG**
- [x] **Step 2: J11 ticks + banner + Q13 DONE; APK export row → deferred note**
- [x] **Step 3: `testDebugUnitTest :app:assembleDebug` green**
- [x] **Step 4: Commit + push**

## Execution notes

- Fill `- [ ]` → `- [x]` as each step lands
- `v0.6.0` tag at merge to `main`
