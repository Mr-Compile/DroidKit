# DroidKit Improved Feature Plans v2

This package is an **improvement layer** over the earlier DroidKit planning documents. It does not erase the old plans.

## Conflict rule
When an older DroidKit document conflicts with this package:

1. Verified Android/API reality wins over every plan.
2. `01_PLAN_PRECEDENCE_AND_MIGRATION.md` decides which planning generation is authoritative.
3. These `v2` feature specifications supersede older feature descriptions for **feature scope and UX intent**.
4. The existing architecture rules remain authoritative unless a v2 file explicitly says `ARCHITECTURE CHANGE`.
5. Existing safety, privacy, capability-provider, targeted-testing, and Definition-of-Done rules remain mandatory.
6. An AI must never silently rewrite old decisions. It must record the conflict and proposed migration.
7. A feature is not considered implemented merely because it appears in this plan.

## Purpose
The earlier plans defined the DroidKit journey. These files deepen each major feature the same way NFC Lab was expanded: sub-tools, user flows, states, technician value, developer value, privilege levels, limitations, future expansion, and acceptance gates.

## How the coding AI should use this
AUDIT REPO -> READ OLD PLAN -> READ V2 FEATURE FILE -> COMPARE -> CREATE MIGRATION NOTE -> IMPLEMENT CURRENT APPROVED SCOPE -> TARGETED VALIDATION -> UPDATE CHECKLIST.

Start with `01_PLAN_PRECEDENCE_AND_MIGRATION.md`, then `02_IMPROVED_PRODUCT_MAP.md`, then the feature file for the task.
