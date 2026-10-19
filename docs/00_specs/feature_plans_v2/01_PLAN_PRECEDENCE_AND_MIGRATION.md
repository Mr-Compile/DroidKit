# Plan Precedence and Migration Rules

## Why this exists
DroidKit already has planning documents. These improved plans intentionally refine them without creating uncontrolled contradictions.

## Authority order
### Level A — Reality
Verified Android SDK behavior, device testing, current dependency documentation, security constraints, and distribution rules.

### Level B — Repository governance
`AGENTS.md`, AI operating contract, security/privacy rules, Definition of Done, scoped-validation rule.

### Level C — Improved v2 specifications
This package. It supersedes older descriptions of what a named feature should become.

### Level D — Earlier roadmap/feature plans
Still valid for historical intent, milestones, architecture direction, and anything not replaced by v2.

### Level E — Brainstorming
Ideas are not requirements until promoted into an approved feature specification.

## Mandatory conflict procedure
When the AI finds a conflict:
- [ ] Quote/summarize OLD decision.
- [ ] Identify NEW v2 decision.
- [ ] Check repository reality.
- [ ] Check whether architecture/data/API changes are required.
- [ ] Classify: wording refinement / scope expansion / breaking plan change / impossible assumption.
- [ ] Propose migration.
- [ ] Get owner approval if it changes frozen scope, architecture, destructive behavior, or release milestone.
- [ ] Only then implement.
- [ ] Record resolution in `PLAN_MIGRATION_LOG.md`.

## Scope rule
Expanded ideas are divided into:
- `V1 CORE`: intended for first stable product if feasibility confirms.
- `V1 OPTIONAL`: useful but can move without blocking V1.
- `POST-V1`: planned enhancement.
- `EXPERIMENTAL`: research only.
- `REJECTED`: intentionally not built.

Do not pull POST-V1/EXPERIMENTAL work into the current milestone simply because this document describes it.

## Architecture preservation
All tools continue to use:
UI -> ViewModel -> Use Case -> Repository -> System/Data Source -> CapabilityManager/Provider when privilege is required.

Standard Android remains first-class. Shizuku, Wireless ADB, and Root are providers, not feature architecture.

## Testing preservation
Identify changed files/features first. Validate the changed scope. Broaden validation only when shared integration or release risk justifies it.
