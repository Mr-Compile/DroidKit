# Remaining Journeys Gap Plan — 2026-10-19

Purpose: close out every unimplemented item in the master execution queue
(`2026-10-18-droidkit-master-execution-plan.md` Q16–Q23) and the Journey
Checklist. Supplements, does not replace, the master plan.

## Audit baseline (verified 2026-10-19)

- Q1–Q15 DONE: J1–J13 implemented, tagged `v0.1.0`–`v0.8.0`,
  `versionName 0.8.0` / `versionCode 8`, 21 feature modules, unit tests green.
- `mockups/` — static HTML UI replica (not a repo milestone; excluded from
  journey gates).

## Remaining work, classified

### Class A — Agent-codeable (can implement now, no hardware)

| Item | Source | Notes |
|---|---|---|
| A1. J14 leftovers that are pure code/docs | checklist J14; Q16 | Static integration review already PASS. Remaining codeable bits: close the 3 minor open findings in `docs/04_audits/J14_INTEGRATION_REVIEW.md` if still open; mark device-bound items explicitly blocked. |
| ~~A2. J18 Shizuku provider~~ DONE (static) | playbook 20, Q20 | Implemented per `01_plans/2026-10-19-j18-shizuku.md`: real `ShizukuProvider` (api 13.1.5), `ElevatedOps` DI + routing, Settings grant UX, functional AppOps lab, `ElevatedActionTest`/`AppOpsParserTest` green. Release items still device-bound. |
| ~~A3. J19 Wireless ADB provider~~ DONE (static) | playbook 21, Q21 | Implemented: custom SPAKE2 pairing + TLS/plain transport, AndroidKeyStore RSA identity, session manager, `WirelessAdbProvider`, ADB lab UI (pair/connect/props/logcat/reboot-confirm). No external ADB lib — licensing item resolved. Pairing/reconnect QA is device-bound. |
| A4. J21 AI add-on (post-V1) — PARTIAL | `ai_security_addon/`, Q23 | AI-0/1/2 done: `AIProvider`/`GroqProvider`/`AiContextBuilder`/`PrivacyFilter`, typed errors, 20s timeout, BYOK key AES/GCM under AndroidKeyStore, Settings AI section with test/disconnect. Still open: PromptRegistry, structured-output schemas, AI-3 assistants, AI-4 structured actions, AI-5 QA incl. live-key test. |
| A5. V2 feature spec codeable items | `feature_plans_v2/features/10–25` | Post-V1 per scope matrix. Each needs its own plan doc before implementation; pick-up order TBD by owner. |

### Class B — Hardware/tester-blocked (cannot be closed by an agent)

| Item | Blocker |
|---|---|
| J6/J7/J9/J12/J13 device-verification checkboxes | Needs attached Android device/emulator + USB devices + NFC tags |
| J14 device matrix (Pixel/Samsung/Xiaomi/OPPO/Vivo/Transsion) | Physical devices |
| J14 alpha feedback + triage | Human testers |
| J15 regression/upgrade/denied-permission/no-hardware/offline runs | Devices |
| J15 signing key generation + secure backup | Owner action (keystore custody) |
| J15 beta acceptance, J16 distribution + monitoring | Testers/owner |

### Class C — Owner decisions required before code

| Decision | Blocks |
|---|---|
| Approve J21 scope beyond core+setup (assistants, structured actions) — BYOK already implemented | A4 remainder |
| ~~J18 Shizuku dependency approval~~ — dep added (`dev.rikka.shizuku:api:13.1.5`); ratify or revert | A2 release |
| ~~J19 ADB library/protocol choice~~ — resolved: custom implementation, no third-party ADB lib | A3 release |
| V2 PRD + which v2 feature specs are committed (J20 says none auto-committed) | A5 |

## Execution order

1. **A1** — finish codeable J14 residue; tick what's genuinely done, annotate
   device-blocked items as "device run pending" (already the convention).
2. **Owner decisions** (Class C) — collect in one ask; cheapest to bundle.
3. **A2 → A3** — post-V1 provider journeys, each with a per-journey plan doc
   first, then implement → targeted validation → checklist tick per AGENTS.md
   workflow. Release as post-v1 minors only after device verification.
4. **A4** — AI add-on, phased AI-0→AI-5 per `ai_security_addon/`; key stays
   runtime-BYOK (gitignored `.env` is dev-only), never in source/BuildConfig.
5. **A5** — v2 feature specs, one plan doc each, after V2 PRD exists.
6. **Class B** — scheduled when hardware/testers/keystore are available;
   checklists stay `[ ]` until actually verified (repo rule: never mark
   complete on scaffolding).

## Hard rules carried into every remaining item

- High-impact actions: exact consequence + explicit confirmation.
- No fabricated hardware values; missing = NOT_SUPPORTED, denial = PERMISSION_REQUIRED.
- No credential/FRP/lock bypass, stealth, or unauthorized access features.
- Real secrets never in tracked files; audit diffs before commit.
- Per-journey plan doc before implementation; checklist ticked only after validation.
