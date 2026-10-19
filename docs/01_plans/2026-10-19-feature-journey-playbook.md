# Feature Journey Playbook — How to Plan and Ship J08+

> **For agentic workers:** This is a meta-plan: the procedure for producing each journey's implementation plan. Do not write a journey's detailed plan until the prior milestone ships — earlier code defines the interfaces the plan must consume.

## The standing rule (from `00_specs/playbook/00_README` + queue §6.5)

> From Q9 onward: write a per-journey plan doc before each milestone, execute, then run the journey's release gate (CHANGELOG → version → tag → checklist).

## Journey → spec → version map

| Journey | Version | Playbook gate doc | Primary v2 spec(s) | Feature modules touched |
|---|---|---|---|---|
| J07 | v0.2.0 | `00_specs/playbook/09` | `10`, `20`, `18` | `deviceinfo`, `battery`, `sensors` — PLAN WRITTEN (`2026-10-19-j07-device-intelligence.md`) |
| J08 | v0.3.0 | `00_specs/playbook/10` | `11`, `19` | `diagnostics`, `display`, `sensors` |
| J09 | v0.4.0 | `00_specs/playbook/11` | `11`, `19`, `18` | `audio`, `camera`, `display`, `sensors` + diag sequencing |
| J10 | v0.5.0 | `00_specs/playbook/12` | `12` | `reports`, `core:database` (schema review) — decide D4 export format first |
| J11 | v0.6.0 | `00_specs/playbook/13` | `13`, `22` (V1 CORE only) | `apps`, `permissions` |
| J12 | v0.7.0 | `00_specs/playbook/14` | `14` | `usb` |
| J13 | v0.8.0 | `00_specs/playbook/15` | `15`, `16`, `17` (basic only) | `nfc`, `network`, `bluetooth` |
| J14 | v0.9.0-alpha.N | `00_specs/playbook/16` | — + `00_specs/playbook/23` QA matrix | all |
| J15 | v1.0.0-beta/rc | `00_specs/playbook/17` | — | all |
| J16 | v1.0.0 | `00_specs/playbook/18` | — | release |
| J18 | post-v1 | `00_specs/playbook/20` | `23`, `22` elevated | `privilege:shizuku`, `appops`, `packages`, settings |
| J19 | post-v1 | `00_specs/playbook/21` | `23` | `privilege:adb`, `feature:adb` |
| J20 | v2.x | `00_specs/playbook/22` | POST-V1 sections of all v2 files | needs new V2 PRD first |

## Per-journey procedure (run this, in order, at the start of each journey)

1. **Audit** — `git log`, current version, what the skeleton/previous journeys actually built. List which modules/files the journey will touch. (Contract Step 1.)
2. **Read the gate doc** — the `00_specs/playbook/NN` file for the journey. Its checkboxes define done.
3. **Read the v2 spec(s)** — `00_specs/feature_plans_v2/features/NN_*.md`. v2 wins on scope/UX detail. Extract only the `V1 CORE` (+ agreed `V1 OPTIONAL`) sections — never pull POST-V1/EXPERIMENTAL into the milestone.
4. **Conflict check** — v2 vs older docs vs repo reality. Classify per `00_specs/feature_plans_v2/01_PLAN_PRECEDENCE_AND_MIGRATION.md`; log in `docs/02_foundations/PLAN_MIGRATION_LOG.md`; get owner approval if it touches frozen scope/architecture/milestones.
5. **Write the plan** — `docs/01_plans/YYYY-MM-DD-jNN-<name>.md` using the template below. Commit + push.
6. **Execute** — task-by-task via `subagent-driven-development` or `executing-plans`. Commit per task, push immediately.
7. **Run the release gate** — the journey doc's gate section + universal gate (`00_specs/playbook/02`): CHANGELOG entry → `versionName`/bump `versionCode` → `git tag vX.Y.Z` → push with `--tags` → tick tracker (`01_plans/10_...md`) + queue item.

## Plan document template

````markdown
# JNN <Name> (vX.Y.Z) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** <one sentence — what ships in this milestone>

**Architecture:** <what it builds on — name the existing modules/contracts it consumes>

**Tech Stack:** Kotlin, Compose M3, Hilt, <journey-specific libs>

## Global Constraints
- Rules: root `AGENTS.md`; gate doc: `00_specs/playbook/NN_*.md`; specs: `00_specs/feature_plans_v2/features/NN_*.md`
- Package root `io.github.droidkit`; minSdk 28 / target 36
- Honesty: NOT_SUPPORTED for absent hardware, PERMISSION_REQUIRED for denial, never FAIL for absence; no fabricated values
- Risk levels 0–4 confirmations on mutations; allow-listed actions only
- JDK: `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`
- Commit per task; `git push origin main` after every commit

### Task N: <component>
**Files:** Create/Modify/Test — exact paths under the owning `feature/*` or `core/*` module
**Interfaces:** Consumes: <existing signatures> / Produces: <what later tasks use>
- [ ] Steps: failing test → run → implement → pass → commit (`git push` included)

### Final task: release gate
- [ ] `:app:assembleDebug` + real-device pass over the journey's checklist
- [ ] CHANGELOG + versionName/versionCode + tag `vX.Y.Z` + push --tags
- [ ] Tracker + execution plan updated
````

## Standard task shapes (reuse these patterns)

- **New real screen in a stub module:** upgrade `build.gradle.kts` (`droidkit.hilt` + `core.system`/needed deps) → VM (`@HiltViewModel`, inject probe/manager) → Screen (sectioned, explicit states) → register `Tool` in the feature's `FeatureContribution` → compile → commit.
- **New capability-gated tool:** add `Tool` with `requiredCapabilities`/`requiredPermissions`/`requiredFeatures`/`minSdk`/`safetyLevel`; UI consumes `ToolResolution` — `Ready` → content, `Blocked` → `BlockedStateView` with reasons, `NeedsSetup` → provider setup CTA.
- **Mutating action (NFC write, erase, etc.):** risk level ≥2 → consequence text + explicit confirm + re-check capability immediately before execution.
- **Hardware-observation test:** instruction card + explicit user Pass/Fail/Skip — never auto-pass.
- **Result states:** emit `TestResultState` values `PASS / FAIL / NOT_TESTED / NOT_SUPPORTED / PERMISSION_REQUIRED / UNKNOWN`; persist via `DiagnosticResultEntity` only after J08's session engine exists.
- **Elevated tools (post-V1):** `requiredCapabilities` = `SHIZUKU_SERVICE`/`ADB_SHELL`/`ROOT`; provider stubs already render `Unavailable` — feature work waits for its provider journey (J18/J19/J20).

## What NOT to do in any journey

- No new modules without updating `docs/02_foundations/ENGINEERING_FOUNDATION.md` + a migration-log entry
- No direct Shizuku/ADB/root checks in feature code — declare requirements, `CapabilityManager` resolves
- No arbitrary shell strings from UI input
- No marking checklist items done for scaffolding alone
- No re-planning a journey that already has a written plan — fix the plan inline instead
