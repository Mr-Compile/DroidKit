# DroidKit Docs — Map

Numbered in workflow order. Read `00` for the rules, `01` for what to do next.

| Folder | Purpose | Phase |
|---|---|---|
| `00_specs/` | Canonical spec library — the sources of truth | Reference (always) |
| `01_plans/` | Master execution queue, per-journey plans, journey checklist tracker | Planning |
| `02_foundations/` | Phase A gate evidence — what was decided before code | J01–J04 (done) |
| `03_design_system/` | Brand assets + UI/UX design system packages | J03 → ongoing (Q16b) |
| `04_audits/` | Audit reports produced during implementation | J14+ (ongoing) |
| `05_release/` | Release notes, privacy policy, device validation runbook | J15–J16 (pending) |
| `06_research/` | Research notes (post-V1 capability research) | Reference |

## What's inside `00_specs/`

- `playbook/` — journey docs J01–J20 + operating rules (`01`, `02`, `23`–`29`) + `AGENTS.md` spec copy + playbook `.docx` twin
- `feature_plans_v2/` — authoritative per-feature scope/UX (files `10`–`25`) + governance (`00`–`02`, `30`–`32`)
- `ai_security_addon/` — AI/Groq + secrets governance (`00`–`11`)
- `planning_package_superseded/` — earlier planning draft, kept for history only
- `DROIDKIT_PRODUCT_BIBLE.md`, `2026-10-18-droidkit-app-skeleton-design.md`, `2026-10-19-insights-analytics-mockup-design.md`

## What's inside `01_plans/`

- `2026-10-18-droidkit-master-execution-plan.md` — **the queue** (Q1–Q23 + Q6b + Q16b)
- `10_DroidKit_Master_Journey_Checklist.md` — the single progress tracker
- `2026-10-*.md` — per-journey/per-feature implementation plans, dated in execution order
- `droidkit_v1_completion_plan/` — V1 completion AI execution pack (master prompt, tracker, DoD, device verification matrix)
- `droidkit_navigation_plan/` — navigation hardening AI pack (master navigation prompt, QA matrix, route contract, AI-first prompt)
- `droidkit_ux_plan/` — UX enhancement AI pack (permissions/privacy/search/settings/a11y; executed via `2026-10-19-ux-enhancement-execution.md`)

## What's inside `02_foundations/`

- `PRD_APPROVAL.md` — J01 tail: PRD approval + V1-OPTIONAL calls (Q3)
- `j02_feasibility/` — J02 tool records + API/permission/hardware matrices (Q4)
- `j03_design/` — J03 design tokens, components, screen specs (Q5)
- `ENGINEERING_FOUNDATION.md` — J04 ratified architecture decisions (Q6)
- `PLAN_MIGRATION_LOG.md` — conflict-resolution log (v2 precedence protocol)

## What's inside `03_design_system/`

- `branding/` — mark/logo SVG sources + usage rules
- `uiux_v5/` — **canonical** UI/UX design system (Material 3 + Dark Technical + Bento + Aurora)
- `uiux_v4_superseded/` — earlier package, detail reference only

## History of this layout

Reorganized 2026-10-19: previously `superpowers/specs`, `superpowers/plans`, and loose folders
(`design`, `feasibility`, `audits`, `branding`, `release`, `research`) plus root docs.
Deleted duplicates: byte-identical playbook `.docx` at the old `superpowers/` root and both
`DroidKit_UI_UX_Design_System_v*.zip` archives (extracted folders are canonical).
