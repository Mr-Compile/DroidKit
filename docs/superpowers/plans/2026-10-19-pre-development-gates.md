# Pre-Development Gates (J01–J04 tails) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close out the pre-development journeys — PRD approval record, J02 feasibility audit, J03 design foundation, J04 engineering ratification — ending with `V1_SCOPE_FROZEN = TRUE`.

**Architecture:** All deliverables are Markdown documents committed to `docs/`. Each task produces review-ready artifacts; owner sign-off happens at the freeze task. No Android code is touched.

**Tech Stack:** Markdown, git.

## Global Constraints

- Rules: `AGENTS.md` (root) + `docs/superpowers/specs/playbook/01_AI_OPERATING_CONTRACT.md`
- Queue refs: `docs/superpowers/plans/2026-10-18-droidkit-master-execution-plan.md` items Q3–Q6
- Conflicts → `docs/PLAN_MIGRATION_LOG.md` (template in `specs/feature_plans_v2/31`)
- Commit per task; push to `origin` immediately after every commit (AGENTS.md Git rule)
- Decided already — do not re-litigate: product name DroidKit, distribution GitHub Releases + direct APK, LICENSE Apache-2.0, `applicationId io.github.droidkit`, minSdk 28, targetSdk/compileSdk 36, multi-module layout
- Open decision D4 (report export format) stays open until J10/Q12 — record it, do not decide it here

---

### Task 1: PRD review + V1-OPTIONAL decisions → `docs/PRD_APPROVAL.md`

**Files:**
- Create: `docs/PRD_APPROVAL.md`
- Read: `docs/superpowers/specs/planning_package/01_DroidKit_PRD.md` (draft input), `docs/superpowers/specs/playbook/03_PRODUCT_SCOPE_AND_V1_FREEZE.md`, `docs/superpowers/specs/DROIDKIT_PRODUCT_BIBLE.md` §7, `docs/superpowers/specs/feature_plans_v2/30_FEATURE_SCOPE_MATRIX_V2.md`

**Interfaces:**
- Produces: approved scope basis consumed by Task 2 (feasibility records) and Task 5 (freeze record)

- [ ] **Step 1: Write `docs/PRD_APPROVAL.md`**

Sections:
1. `## Review outcome` — PRD reviewed line-by-line; list every line item changed/removed/kept with reason.
2. `## V1 scope decision` — final V1 list built from scope-matrix tiers; mark each tool `V1 CORE` / `V1 OPTIONAL` / `POST-V1` / `EXPERIMENTAL` / `REJECTED`.
3. `## Decisions needing owner sign-off` — exactly three calls:
   - Bluetooth Lab: keep `V1 OPTIONAL` basic capability (adapter state + bonded-device info) or move all to POST-V1? **Recommendation: keep V1 OPTIONAL basic.**
   - Developer Lab: which safe tools (Intent Launcher, Activity Launcher, Deep Link Tester, Display Metrics) count as `V1 OPTIONAL`? **Recommendation: Intent Launcher + Display Metrics only.**
   - APK export: in V1 scope as `V1 OPTIONAL` or POST-V1? **Recommendation: POST-V1** (legal/policy review needed).
4. `## Open decisions` — D4 export format deferred to J10 planning.

- [ ] **Step 2: Owner review**

Present the three decision calls to the owner. Record each answer inline (`Decision: … — approved by owner, date`).

- [ ] **Step 3: Commit**

```bash
git add docs/PRD_APPROVAL.md
git commit -m "docs: PRD approval record and V1 scope decisions"
git push origin main
```

---

### Task 2: J02 feasibility audit → `docs/feasibility/`

**Files:**
- Create: `docs/feasibility/J02_TOOL_RECORDS.md`
- Create: `docs/feasibility/J02_MATRICES.md`
- Read: `playbook/04_J02_FEASIBILITY_AND_ANDROID_COMPATIBILITY.md` (checklist + record format), all `feature_plans_v2/features/*.md` (sub-tool lists)

**Interfaces:**
- Consumes: Task 1 scope decisions (which tools need records)
- Produces: per-tool Verified / Needs-device-test / Experimental classification consumed by Task 5 freeze

- [ ] **Step 1: `docs/feasibility/J02_TOOL_RECORDS.md`**

One record per tool, covering the 22 V1 audit items from playbook `04` **plus** the v2 sub-tools: Tag Doctor, NDEF Writer, Component Explorer, Sensor Live Monitor, Network layer-by-layer diagnostics, Component Launcher, APK Inspector, Permission Center, Capability Center.

Record format per tool (from playbook `04`):

```markdown
### <Tool name>
- User value:
- Android API/classes:
- Minimum API:
- Runtime/manifest/special permissions:
- Hardware feature:
- Package visibility / OS restrictions:
- Standard access level:
- Elevated access opportunities:
- OEM differences:
- Offline behavior:
- Unsupported behavior:
- Privacy/security concerns:
- Test devices required:
- Decision: V1 / Post-V1 / Reject
- Confidence: Verified / Needs device test / Experimental
```

- [ ] **Step 2: `docs/feasibility/J02_MATRICES.md`**

Six tables required by the J02 gate: API-level matrix, OEM risk matrix, permission matrix, hardware matrix, feature decision matrix, known-limitations list. One row per tool from Step 1.

- [ ] **Step 3: Self-check** — every V1 tool has Decision + Confidence filled; anything `Experimental` gets a Post-V1 recommendation note.

- [ ] **Step 4: Commit**

```bash
git add docs/feasibility/
git commit -m "docs: J02 feasibility records and compatibility matrices"
git push origin main
```

---

### Task 3: J03 design foundation → `docs/design/`

**Files:**
- Create: `docs/design/J03_DESIGN_TOKENS.md`
- Create: `docs/design/J03_COMPONENTS.md`
- Create: `docs/design/J03_SCREEN_SPECS.md`
- Read: `playbook/05_J03_DESIGN_FOUNDATION.md` (token/component/screen checklists); skeleton plan Task 9 (`:core:ui`) for what already exists as primitives

**Interfaces:**
- Produces: per-screen state models required before any feature journey implements a screen (J03 gate)

- [ ] **Step 1: `J03_DESIGN_TOKENS.md`** — typography scale, spacing ramp, shape/radius, elevation/surfaces, semantic color tokens, icon rules, motion rules, light theme values, dark theme values. Map each token to the Material3 name the skeleton's `:core:ui` theme uses.

- [ ] **Step 2: `J03_COMPONENTS.md`** — spec for each of the 16 core components (app scaffold, screen header, tool card, status badge, info row, section card, permission prompt, unsupported state, empty state, error state, loading state, risk confirmation dialog, diagnostic instruction card, Pass/Fail/Skip controls, search field, filter chips): purpose, states, props, a11y notes. Mark which already exist in `:core:ui` vs. net-new.

- [ ] **Step 3: `J03_SCREEN_SPECS.md`** — low-fi spec + explicit state model (loading/content/empty/permission-required/unsupported/error) for each wireframe target: onboarding, home, tools/search/category, device inspector, diagnostic start/test/summary/history, report detail/export, apps list/detail/components/permissions, USB list/detail, NFC scan/result/write confirmation, network tools, settings/advanced-access/about.

- [ ] **Step 4: Commit**

```bash
git add docs/design/
git commit -m "docs: J03 design tokens, components, and screen specs"
git push origin main
```

---

### Task 4: J04 engineering ratification → `docs/ENGINEERING_FOUNDATION.md`

**Files:**
- Create: `docs/ENGINEERING_FOUNDATION.md`
- Read: `playbook/06_J04_ENGINEERING_FOUNDATION.md`, `specs/2026-10-18-droidkit-app-skeleton-design.md` (decided values), skeleton plan Global Constraints

**Interfaces:**
- Produces: the single doc a fresh implementer reads to make zero new architecture decisions during J05–J06

- [ ] **Step 1: Write `docs/ENGINEERING_FOUNDATION.md`**

Ratify (quote decided value + source):
- Module map: 31 modules exactly as skeleton design spec (`core/*`, `privilege/*`, `feature/*`, `app`, `build-logic`)
- Contracts: `Tool`, `FeatureContribution`, `ToolRegistry`, `CapabilityProvider`, `CapabilityManager`, `ToolResolution`, `BlockedReason`, `TestResultState` (PASS/FAIL/NOT_TESTED/NOT_SUPPORTED/PERMISSION_REQUIRED/UNKNOWN)
- Package naming: `io.github.droidkit.<module-path-dots>`; applicationId `io.github.droidkit`
- SDK pins: minSdk 28 / compileSdk 36 / targetSdk 36
- Persistence: Room entities `DeviceSnapshot`/`DiagnosticSession`/`DiagnosticResult`/`SavedReport` (schema v1); DataStore for prefs; explicit migrations after first shipped schema
- Git convention: `main` + `feature|fix|refactor|docs/<name>` branches; commit per task; push immediately; tags `v*` at release gates
- Testing rules: targeted validation only (contract Step 4); unit tests where the skeleton plan marks TDD
- DoD adoption: `playbook/27` verbatim
- Report format: record D4 as OPEN (decided at J10)
- Privacy/data behavior: local-first, `playbook/24` summary

- [ ] **Step 2: Commit**

```bash
git add docs/ENGINEERING_FOUNDATION.md
git commit -m "docs: J04 engineering foundation ratification"
git push origin main
```

---

### Task 5: Freeze V1 scope

**Files:**
- Modify: `docs/superpowers/specs/playbook/03_PRODUCT_SCOPE_AND_V1_FREEZE.md` (freeze checklist → all `[x]` + `V1_SCOPE_FROZEN = TRUE` line)
- Modify: `docs/superpowers/specs/10_DroidKit_Master_Journey_Checklist.md` (tick J01 tail items: PRD approved, V1 scope frozen, feasibility research, SDKs confirmed, package ID, distribution, module boundaries, Room schema, DoD/conventions — only the ones actually done)
- Modify: `docs/superpowers/plans/2026-10-18-droidkit-master-execution-plan.md` (mark Q3–Q6 status)

- [ ] **Step 1: Verify gate** — every checkbox in `03`'s "Freeze procedure" is satisfied by Tasks 1–4 outputs. Owner signs off on `docs/PRD_APPROVAL.md` decisions.

- [ ] **Step 2: Edit `03_PRODUCT_SCOPE_AND_V1_FREEZE.md`** — tick all seven freeze-procedure boxes; append:

```markdown
## Freeze record
`V1_SCOPE_FROZEN = TRUE` — <date>, owner-approved via `docs/PRD_APPROVAL.md`.
```

- [ ] **Step 3: Tick tracker + queue** — checklist items actually completed; queue items Q3–Q6 marked done.

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/specs/playbook/03_PRODUCT_SCOPE_AND_V1_FREEZE.md docs/superpowers/specs/10_DroidKit_Master_Journey_Checklist.md docs/superpowers/plans/2026-10-18-droidkit-master-execution-plan.md
git commit -m "docs: freeze V1 scope (J01-J04 tails complete)"
git push origin main
```

---

## Gate (Phase A exit)

- [ ] `docs/PRD_APPROVAL.md` exists, three V1-OPTIONAL calls answered by owner
- [ ] `docs/feasibility/` — every V1 tool classified
- [ ] `docs/design/` — tokens + 16 components + screen state models
- [ ] `docs/ENGINEERING_FOUNDATION.md` — ratified
- [ ] `V1_SCOPE_FROZEN = TRUE` recorded
- [ ] Next queue item ready: Q7/Q8 = execute `plans/2026-10-18-droidkit-app-skeleton.md` (21 tasks, already written — do not re-plan)
