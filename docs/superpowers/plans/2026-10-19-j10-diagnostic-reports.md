# J10 Diagnostic Reports (v0.5.0) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reports become first-class: history with filters, a reopenable detail screen, structured text + PDF export through FileProvider share, sensitive-field preview, and delete flows. Tag `v0.5.0`.

**Architecture:** `feature:reports` owns report generation (`ReportBuilder` — pure Kotlin, unit-testable), PDF rendering (framework `PdfDocument`, no dep), and screens. Room gains delete DAOs + snapshot lookup; no schema change. `FileProvider` lives in the app manifest (single authority `io.github.droidkit.fileprovider`), cache dir `reports/`.

**Tech Stack:** Kotlin, Compose M3, Room, framework `PdfDocument`, `FileProvider`.

## Global Constraints

- Requires: J08+J09 — sessions/results/snapshots persist already (persistence items tick as verified, not rebuilt)
- Spec: `specs/feature_plans_v2/features/12_REPORTS_TECHNICIAN_WORKFLOW_V2.md`
- Report structure (spec order): Report ID/date/app version → device summary → overall status → grouped tests → failures/warnings → unsupported/skipped → notes → limitations/disclaimer
- Honesty/privacy: export preview lists every field class included (device model/build/patch, app version, results, notes) — user confirms before share. No identifiers beyond what Room already stores. Text is the canonical format; PDF renders the same text
- POST-V1 stays out: repair intake, before/after compare, signatures, batch workflow
- Commit per task, push after every commit; `JAVA_HOME` export for builds

---

### Task 1: DAOs + ReportBuilder (TDD)

**Files:**
- Modify: `core/database/.../Daos.kt` — `sessionDao.deleteById(id)`, `sessionDao.deleteAll()`, `resultDao.deleteBySession(sessionId)`, `resultDao.deleteAll()`, `snapshotDao.getById(id)`, `reportDao.insert` already exists
- Create: `feature/reports/.../ReportBuilder.kt` — `fun buildReport(session, snapshot?, results, appVersion): String` (pure Kotlin)
- Test: `feature/reports/src/test/.../ReportBuilderTest.kt` — sections present in order, states grouped, notes included, disclaimer tail, empty-results honest ("No test results recorded")

- [ ] **Step 1: Write failing test + DAO additions**
- [ ] **Step 2: Implement `ReportBuilder`** + run `./gradlew :feature:reports:testDebugUnitTest` green
- [ ] **Step 3: Commit** `feat(reports): report builder and delete daos`

---

### Task 2: Report detail screen

**Files:**
- Create: `feature/reports/.../ReportDetailScreen.kt`
- Modify: `feature/reports/.../ReportsViewModel.kt` — `session(id)`, `resultsFor(id)`, `snapshotFor(id)` suspend getters; `deleteSession(id)`; `deleteAll()`
- Modify: `feature/reports/.../ReportsFeature.kt` — `reports/detail/{sessionId}` route
- Modify: `feature/reports/.../ReportsScreen.kt` — rows navigate to detail

- [ ] **Step 1: VM getters + delete flows (delete session → its results too)**
- [ ] **Step 2: Detail screen** — session header, snapshot card (device summary), grouped results with notes, technician notes; action bar: Copy summary / Export (opens preview) / Delete (confirm dialog)
- [ ] **Step 3: Compile + commit** `feat(reports): report detail screen`

---

### Task 3: Export — text + PDF + share + preview

**Files:**
- Create: `feature/reports/.../ReportExporter.kt` — `exportText(report): File`, `exportPdf(report): File` into `cacheDir/reports/`; PDF = `PdfDocument` pages of fixed-height lines
- Modify: `app/src/main/AndroidManifest.xml` — FileProvider entry
- Create: `app/src/main/res/xml/file_paths.xml` — `cache-path reports/`
- Modify: `feature/reports/.../ReportDetailScreen.kt` — export preview sheet: field list + format choice (Text/PDF) → writes file, records `SavedReportEntity`, fires `ACTION_SEND` with FileProvider URI + `FLAG_GRANT_READ_URI_PERMISSION`
- Modify: `feature/reports/.../ReportsViewModel.kt` — `recordSavedReport(sessionId, format, uri)`

- [ ] **Step 1: `ReportExporter`** — text→file trivial; PDF: paginate lines (11pt paint, ~40 lines/page, margins)
- [ ] **Step 2: FileProvider + preview sheet + share intent**
- [ ] **Step 3: Build** `:app:assembleDebug` + commit `feat(reports): text/pdf export with privacy preview and share`

---

### Task 4: History filters + delete-all

**Files:**
- Modify: `feature/reports/.../ReportsScreen.kt` — status `FilterChip` row (All / In progress / Completed / Abandoned) via `rememberSaveable` state; overflow menu → "Delete all reports" with confirm dialog

- [ ] **Step 1: Filters + delete-all UI**
- [ ] **Step 2: Compile + commit** `feat(reports): history filters and delete-all`

---

### Task 5: Release gate — 0.5.0, changelog, checklist, plan, queue

- [ ] **Step 1: `0.5.0`/`5` + CHANGELOG**
- [ ] **Step 2: J10 ticks + banner + Q12 DONE**
- [ ] **Step 3: `testDebugUnitTest :app:assembleDebug` green**
- [ ] **Step 4: Commit + push**

## Execution notes

- Fill `- [ ]` → `- [x]` as each step lands
- `v0.5.0` tag at merge to `main`
