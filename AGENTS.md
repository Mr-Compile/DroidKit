# AGENTS.md - DroidKit Repository Rules

## Mission
Build DroidKit incrementally as a safe, native Android technician/power-user toolkit.

## Read before editing
1. `docs/superpowers/specs/playbook/01_AI_OPERATING_CONTRACT.md` — session workflow rules
2. `docs/superpowers/specs/playbook/02_MASTER_JOURNEY.md` — milestone map (J01–J20) + universal gate
3. Current journey document — `docs/superpowers/specs/playbook/04`–`22`, per active milestone
4. Feature spec — `docs/superpowers/specs/feature_plans_v2/features/` (authoritative for feature scope/UX)
5. Progress tracker — `docs/superpowers/specs/10_DroidKit_Master_Journey_Checklist.md`
6. Relevant architecture/design documents — `docs/superpowers/specs/`, `docs/superpowers/plans/`

## Source-of-truth order
Verified repo/Android reality > this file + operating contract > frozen V1 scope (`playbook/03` + `feature_plans_v2/30`) > v2 feature specs > playbook journey gates > current sprint plans > Product Bible > tracker > `planning_package/` (history only).

Conflicts are classified and logged in `docs/PLAN_MIGRATION_LOG.md` — never silently rewrite an old decision. Full queue: `docs/superpowers/plans/2026-10-18-droidkit-master-execution-plan.md`.

## Mandatory workflow
AUDIT -> PLAN -> IMPLEMENT -> TARGETED VALIDATION -> SELF-REVIEW -> UPDATE CHECKLIST

## Scope
- Identify changed files before validation.
- Only test changed files/features by default.
- Do not run full test suite or broad analysis unless shared integration changed or release validation requires it.
- Never modify unrelated code just because it can be improved.

## Architecture
- Kotlin + Compose.
- MVVM/Clean Architecture.
- Features request capabilities through CapabilityManager.
- No direct feature dependency on Shizuku/ADB/root.
- Standard Android provider must remain first-class.
- Room for structured persistent app data; DataStore for preferences.
- Explicit UI states and typed results.

## Android honesty
- Do not fabricate unavailable hardware/system values.
- Missing hardware = NOT_SUPPORTED, not FAIL.
- Permission denial = PERMISSION_REQUIRED, not FAIL.
- Human-observation diagnostics require user confirmation where automation is unreliable.
- Verify API/version/OEM restrictions before promising behavior.

## Safety
- Risk classify mutating operations (levels 0–4, see `playbook/24`).
- High-impact actions require explicit consequence + confirmation.
- Prefer structured allow-listed actions over arbitrary shell.
- No credential/FRP/lock bypass, stealth surveillance, unauthorized access, or exploit features.

## Quality
A task is not complete until implemented, validated, self-reviewed, and documented.
Never claim tests passed unless they were actually executed.
Never mark checklist items complete based only on scaffolding.
DoD: `playbook/27`. Audit templates: `playbook/26`.

## Git
- Commit per completed task; smallest coherent change.
- **Sync immediately: push to `origin` right after every commit.** The local repo must never hold unpushed work.

## Versions
Use SemVer for versionName and monotonically increasing Android versionCode.
Update CHANGELOG and Git tags only when appropriate to the milestone/release (`playbook/25`).
