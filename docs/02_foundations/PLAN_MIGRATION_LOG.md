# PLAN_MIGRATION_LOG

Use one entry per resolved conflict. Template source: `docs/00_specs/feature_plans_v2/31_PLAN_MIGRATION_LOG_TEMPLATE.md`.

## Entry
Date: 2026-10-18
Feature: Build config
Old document/decision: Master Plan — minSdk 26
Improved v2 decision: Skeleton design spec — minSdk 28
Repository reality: no code yet; spec approved by owner
Conflict classification: breaking plan change (raises minimum supported Android)
Decision: minSdk 28 wins (skeleton design spec, Level: current sprint)
Owner approval required?: already approved via design spec
Files/docs updated: `docs/00_specs/2026-10-18-droidkit-app-skeleton-design.md`, `01_plans/2026-10-18-droidkit-app-skeleton.md`
Migration completed? [x]
Notes: recorded in master execution plan §1.

## Entry
Date: 2026-10-18
Feature: Build config
Old document/decision: Skeleton design spec — Kotlin 2.2.20
Improved v2 decision: Skeleton implementation plan — Kotlin 2.3.20 with 2.2.20 fallback
Repository reality: plan controls toolchain versions + cached Gradle
Conflict classification: wording refinement / toolchain pin
Decision: plan wins — Kotlin 2.3.20, fall back to 2.2.20 only if resolution fails (then KSP `2.2.20-2.0.3`)
Owner approval required?: no
Files/docs updated: `docs/01_plans/2026-10-18-droidkit-app-skeleton.md` (Global Constraints)
Migration completed? [x]
Notes: recorded in master execution plan §1.

## Entry
Date: 2026-10-18
Feature: Milestone naming
Old document/decision: Planning package v2 — M0–M10 / DK-xxx IDs
Improved v2 decision: Playbook — J01–J20 journeys + SemVer map
Repository reality: playbook is canonical for milestones
Conflict classification: wording refinement (naming scheme)
Decision: J-numbering is canonical; M/DK IDs map onto journeys, no second backlog
Owner approval required?: no
Files/docs updated: `docs/01_plans/2026-10-18-droidkit-master-execution-plan.md` §1
Migration completed? [x]
Notes: playbook `25_VERSIONING_RELEASE_MANAGEMENT.md` holds the J→SemVer map.

## Entry
Date: 2026-10-20
Feature: Error/problem UX + design tokens
Old document/decision: Per-feature ad-hoc error text + `BlockedReasonList` dialog; duplicated `AiError`→message maps in 4 files
Improved v2 decision: uiux_v5 semantic state model (`semantics/03`) — unified problem/recovery
Repository reality: v5 spec is canonical for UX
Conflict classification: approach refinement / supersession
Decision: v5 wins — `UiProblem`/`RecoveryAction` + `UiProblemCard` + `AiError.describe()` are canonical; `BlockedReasonList` removed as dead code; semantic status tokens in `theme/Tokens.kt`
Owner approval required?: no
Files/docs updated: `core/common/UiProblem.kt`, `core/ui/components/*`, `core/ui/theme/Tokens.kt`, tracker DK-020/030/031/032
Migration completed? [x]
Notes: Tools/Settings surface adoption deferred to parallel UX workstream (droidkit_ux_plan).
