# DroidKit Master Execution Plan — Consolidated Queue

Date: 2026-10-18 (paths updated 2026-10-19 after doc consolidation)
Sources read: all files under `docs/superpowers/specs/` — canonical packages are `specs/playbook/` (AI-friendly markdown + docx twin), `specs/planning_package/` (superseded draft + docx twins), `specs/feature_plans_v2/` (markdown only) — plus `docs/superpowers/DroidKit_Complete_Development_Playbook.docx` and the existing plan `plans/2026-10-18-droidkit-app-skeleton.md`. Zips and loose duplicate copies were deleted 2026-10-19.
Repo state at planning time: git repo on `main`, docs + `.gitignore` + `LICENSE` only, no Android project code.

---

## 1. Source-of-Truth Hierarchy

When two documents disagree, the higher one wins:

1. **Verified repository reality + Android API behavior** (always wins)
2. `specs/playbook/AGENTS.md` + `01_AI_OPERATING_CONTRACT.md` — rules
3. Frozen scope: playbook `03_PRODUCT_SCOPE_AND_V1_FREEZE.md` + `30_FEATURE_SCOPE_MATRIX_V2.md` scope tiers
4. **Improved Feature Plans v2** (`specs/feature_plans_v2/`) — authoritative for feature scope, sub-tools, and UX intent per its own `01_PLAN_PRECEDENCE_AND_MIGRATION.md` (Level C)
5. Playbook journey docs `04`–`22` — milestone contents and release gates
6. `specs/2026-10-18-droidkit-app-skeleton-design.md` + skeleton plan (current sprint decisions)
7. `specs/DROIDKIT_PRODUCT_BIBLE.md` — product vision, feature universe
8. `specs/10_DroidKit_Master_Journey_Checklist.md` — living progress tracker
9. Planning Package v2 (`specs/planning_package/`) — superseded draft; consult only for history

v2 conflict protocol (mandatory): when a v2 spec disagrees with an older doc, classify the conflict (wording / scope expansion / breaking change / impossible assumption), propose migration, get owner approval if it touches frozen scope/architecture/destructive behavior/milestones, implement, then record it in `PLAN_MIGRATION_LOG.md` (template: v2 file `31`).

Known doc conflicts already resolved by newer docs:
- minSdk: Master Plan says 26 → skeleton design spec says **28** (wins).
- Kotlin: design spec says 2.2.20 → skeleton plan says 2.3.20 with 2.2.20 fallback (plan wins).
- Milestone names: v2 package uses M0–M10/DK-xxx → playbook journeys **J01–J20** are canonical.

## 2. Duplicate Register

These documents restate the same plan. One canonical copy each; the rest are reference-only. Do not re-plan from duplicates.

| Duplicate artifact | Duplicates | Action |
|---|---|---|
| `specs/10_DroidKit_Master_Journey_Checklist.md` | `DroidKit_Master_Journey_Checklist.docx` at `specs/planning_package/` (Word twin), journey table inside Playbook docx | DONE 2026-10-19: specs-root md is the tracker; docx kept as human-readable twin inside `planning_package/`; other copies deleted. |
| `DroidKit_Master_Planning_Document.docx` | Planning package docs 00–09 condensed | DONE: one copy kept at `specs/planning_package/`; specs-root copy deleted; md files are canonical. |
| `DroidKit_Complete_Development_Playbook.docx` | Playbook md files 00–29 + AGENTS.md | DONE: kept at `docs/superpowers/` root for easy reading + twin inside `specs/playbook/`; md files are canonical. |
| `DroidKit_AI_Groq_Security_Addon_v3.zip` | — | DONE 2026-10-19: extracted to `specs/ai_security_addon/` (canonical); zip deleted as redundant copy. Plans: Q6b secrets hardening + Q23/J21 AI journey (`plans/2026-10-19-ai-security-addon.md`). |
| PRD content | `01_DroidKit_PRD.md`, playbook `03`, Product Bible §7, Master Plan §6/§9 | Product Bible = "what"; playbook `03` = freeze gate. One approval task (Q3), not four. |
| Architecture | `02_Architecture...md`, playbook `06`, Product Bible §25–26, skeleton design spec | Skeleton design spec is the concrete decision; playbook `06` is the gate. |
| Roadmap | `07_Roadmap...md` (M0–M10), `02_MASTER_JOURNEY.md` (J01–J20), journey checklist, `25_VERSIONING`, Master Plan §9 | J-numbering + SemVer map in `25_VERSIONING` is canonical. M/DK IDs map onto journeys; do not create a second backlog. |
| AI rules | `01_AI_OPERATING_CONTRACT.md`, `AGENTS.md`, `08_DroidKit_AI_Handoff.md`, "AI Progress Update Rule" in checklist, validation notes in `07_Roadmap` | `AGENTS.md` + contract are canonical (Q2). Handoff/checklist rule is an older subset. |
| Definition of Done | playbook `27`, checklist's two DoD sections | playbook `27` canonical; checklist sections agree — no conflict. |
| Device test matrix | playbook `23`, checklist Journey 14/15 items, `07_Roadmap` M10 | playbook `23` canonical. |
| Skeleton work | existing 21-task plan ↔ J05 bootstrap + J06 foundation checklists | Existing plan already covers both journeys' build items. Queue it once (Q8); journeys' gates still apply at release. |
| Feature descriptions in playbook journeys 09–15, Product Bible §6–7, PRD scope lists | Improved Feature Plans v2 files `10`–`25` | NOT duplicates — v2 refines scope/UX and wins on feature detail. Playbook keeps milestone gates + versioning. Read v2 file first when planning a feature journey. |
| Scope tiers | v2 `30_FEATURE_SCOPE_MATRIX_V2.md` ↔ V1 lists in PRD/Bible/playbook `03` | Matrix is the default tier map; J02 feasibility can move items; freeze (Q3) records the final call. |

Planning package v2 as a whole is superseded by the Playbook + Product Bible. Keep it extracted for reference; never schedule work "because v2 says so" without checking the playbook. The same applies in reverse to feature detail: never implement a feature from only the playbook checklist when a v2 spec exists.

## 3. Universal Rules (apply to every queued task)

Consolidated from `AGENTS.md`, `01_AI_OPERATING_CONTRACT.md`, `24_SECURITY_PRIVACY_SAFETY.md`, and `27_DEFINITION_OF_DONE.md`. These bind all files/tasks below.

**Workflow** — AUDIT → PLAN → IMPLEMENT → TARGETED VALIDATE → SELF-REVIEW → UPDATE CHECKLIST → VERSION/TAG only at gates.
- Audit repo + read current journey doc before editing; list files expected to change.
- Validate only the changed scope. Full suite/broad lint only when shared contracts, DI, navigation, or DB schema changed, or at release gates.
- Mark checklist `[x]` only after validation. Blockers → `[BLOCKED: reason]`, never `[x]`.
- Smallest coherent change; no unrelated edits; no silent new dependencies.

**Architecture**
- Kotlin + Compose, MVVM/Clean. `UI → ViewModel → UseCase → Repository → DataSource → CapabilityProvider`.
- Features declare required capabilities; they never check Shizuku/ADB/root directly. `CapabilityManager` resolves via provider chain Standard → Shizuku → ADB → Root(future).
- Room for structured data; DataStore for preferences. Explicit UI states (loading/content/empty/permission/unsupported/error).
- `feature/* → core/*`; `privilege/* → core:privileges`; `app` assembles sets. Nothing depends on `feature/*`.

**Honesty**
- Never fabricate values (serials, battery health %, cycle counts, NAND internals).
- Missing hardware = `NOT_SUPPORTED`; denial = `PERMISSION_REQUIRED`; not `FAIL`.
- Human-observation tests require explicit user Pass/Fail — no fake auto-pass.
- Result states: `PASS / FAIL / NOT_TESTED / NOT_SUPPORTED / PERMISSION_REQUIRED / UNKNOWN`.

**Safety**
- Risk levels 0–4 scale confirmation strength. Mutating actions: explain consequence + target + confirm, re-check capability right before execution.
- Allow-listed structured actions only — no arbitrary shell from UI input.
- Prohibited forever: FRP/credential/lock bypass, stealth surveillance, persistence, exploit delivery, fake cleaner/booster claims, ChipGenius-level USB claims.
- No secrets in source; ADB keys in secure storage (post-V1); local-first data; reports disclose exported fields.

**Versioning** — SemVer `versionName`, monotonic `versionCode`, CHANGELOG + git tag only when a milestone actually ships.

## 4. Master Task Queue

Queue order = dependency order. Each item lists canonical docs, deliverables, and its gate.

### Phase A — Doc reconciliation and pre-development (J01–J04 tails)

**Q1. Consolidate the doc set.**
- DONE 2026-10-19 (differently than originally drafted): deleted the 3 source zips + specs-root duplicate copies; flattened `specs/_extracted/` into `specs/playbook/`, `specs/planning_package/`, `specs/feature_plans_v2/`; restored the journey checklist md to `specs/` root as the tracker. No `_archive/` dir — superseded docs live in `planning_package/` itself.
- Remaining: none beyond the pointer updates already made in this file.
- Depends on: nothing.

**Q2. Adopt repo rules.**
- Copy playbook `AGENTS.md` to repo root; copy `01_AI_OPERATING_CONTRACT.md`, `02_MASTER_JOURNEY.md`, `23`, `24`, `25`, `26`, `27`, `28` into `docs/` (or `docs/superpowers/`) so agents read rules without unzipping.
- Also adopt v2 governance files: `00_README_START_HERE`, `01_PLAN_PRECEDENCE_AND_MIGRATION`, `02_IMPROVED_PRODUCT_MAP`, `30_FEATURE_SCOPE_MATRIX_V2`, and create empty `docs/PLAN_MIGRATION_LOG.md` from template `31`. Session prompt `32` stays as the per-feature kickoff template.
- Gate: `AGENTS.md` exists at root; another AI can find rules cold.

**Q3. Approve PRD + freeze V1 scope (J01 tail → J02 gate).** — DONE 2026-10-19
- Docs: `01_DroidKit_PRD.md` (v2, as draft input), playbook `03`, Product Bible §7, `30_FEATURE_SCOPE_MATRIX_V2.md` for tier defaults.
- Deliverables: PRD reviewed line-by-line; post-V1 items removed from V1 backlog; V1-OPTIONAL calls made (Bluetooth basic, Developer Lab safe tools, APK export); `V1_SCOPE_FROZEN = TRUE` recorded.
- Evidence: `docs/PRD_APPROVAL.md` (all three V1-OPTIONAL calls approved as recommended); freeze recorded in playbook `03` freeze record + checklist.
- Requires feasibility evidence → depends on Q4.

**Q4. J02 feasibility + compatibility audit.** — DONE 2026-10-19
- Doc: playbook `04`. Per-tool research record (API, min API, permissions, hardware, OEM, offline, unsupported UX, decision, confidence) for all 22 V1 audit items, expanded to the v2 sub-tools (Tag Doctor, NDEF Writer, Component Explorer, Sensor Live Monitor, Network layer diagnostics, etc.).
- Deliverables: API-level matrix, OEM risk matrix, permission matrix, hardware matrix, feature decision matrix, known-limitations list.
- Evidence: `docs/feasibility/J02_TOOL_RECORDS.md` (~30 records) + `docs/feasibility/J02_MATRICES.md` (all six matrices).
- Gate: every V1 tool classified Verified / Needs-device-test / Experimental; scope freezable.

**Q5. J03 design foundation.** — DONE 2026-10-19
- Doc: playbook `05`. Tokens (type/space/shape/elevation/semantic color/motion), light+dark themes, ~16 core components, screen specs + state models for the 24 wireframe targets in checklist §3.2.
- Evidence: `docs/design/J03_DESIGN_TOKENS.md`, `J03_COMPONENTS.md`, `J03_SCREEN_SPECS.md` (all 24 wireframe targets + state models + a11y rules).
- Partially overlaps skeleton plan Task 9 (`:core:ui`) — skeleton builds the primitives; screen specs are now written for J07+ feature work.
- Gate: each feature screen has state model + low-fi spec before implementation.

**Q6. J04 engineering foundation (mostly already decided).** — DONE 2026-10-19
- Doc: playbook `06` vs skeleton design spec. Already decided: module map (31 modules), `ToolDefinition`, `CapabilityProvider`, `CapabilityManager`, `ToolResolution`, result/diagnostic states, Room entities, DI assembly.
- Evidence: `docs/ENGINEERING_FOUNDATION.md` ratifies module boundaries, dependency rules, contracts, package/git/branch/commit conventions, testing rules, DoD, logging strategy, report-format decision (deferred to J10/Q12), privacy/data behavior.
- Gate: a fresh implementer needs no new architecture decisions for J05–J06.

**Q6b. Secrets/GitHub hardening (add-on Part A, chore before code).** — DONE 2026-10-19
- Doc: `specs/ai_security_addon/` + `plans/2026-10-19-ai-security-addon.md` Part A.
- Deliverables: `.gitignore` secret/signing entries, `.env.example` placeholders, AI+secrets rules merged into `AGENTS.md`, incident playbook referenced.
- Gate: `git check-ignore` covers `.env`/keystores; `.env.example` placeholders only.

### Phase B — Build the skeleton (J05 + J06)

**Q7. J05 repository bootstrap — DONE (2026-10-19, `feature/j05-j06-skeleton`).**
- The 21-task plan's Tasks 1–2, 20 cover: repo files, Gradle Kotlin DSL, version catalog, SDK pins (min 28 / compile+target 36), Compose/Hilt/Room/DataStore/Nav, test foundations, README/CHANGELOG/.gitignore, LICENSE decision.
- LICENSE resolved: Apache-2.0 at repo root (D3). Signing strategy note remains open for release.
- Gate: clean checkout builds; `AGENTS.md` present. — PASS

**Q8. Execute `plans/2026-10-18-droidkit-app-skeleton.md` (J06, v0.1.0) — DONE (2026-10-19, `feature/j05-j06-skeleton`).**
- All 21 tasks executed, commit per task group. `:core:*` (registry, capabilities, permissions, system probe, Room, DataStore, design system), `:privilege:*` (Standard real; Shizuku/ADB/Root `Unavailable` stubs), all `:feature:*` shells wired to bottom nav, `:app` DI assembly, unit tests (ToolRegistry 4, CapabilityManager 8 — all pass), `assembleDebug` green.
- Plan deviations documented at top of the plan file (Kotlin 2.2.20 fallback, Hilt 2.59.2, AGP 9 source-set workaround, manifest theme fix, empty-test-source fix).
- Release gate status: build passes ✓, no feature touches Shizuku/ADB/root directly ✓ (audited), CHANGELOG entry ✓, `versionName 0.1.0`/`versionCode 1` ✓, checklist items ticked ✓. Remaining before gate closes: app launch + navigation on a device/emulator, tag `v0.1.0` (at merge to `main`).

### Phase C — V1 feature journeys (one queue item per journey; plan each when reached)

**Q9. J07 → v0.2.0 Device Intelligence — DONE (2026-10-19, `feature/j07-device-intelligence`).** Plan `2026-10-19-j07-device-intelligence.md` marked IMPLEMENTED: sectioned dashboard (copy/share/lab links), Storage Lab, Battery Lab, Sensor inventory + live monitor; probe extended (`memory`, `batteryStatus`, `cameraSummary`, `displayModeRatesHz`); `InfoRow`/`SectionCard` in `core:ui`; `:core:system` unit tests pass; version `0.2.0`/`2`, CHANGELOG updated. Gate: no fabricated values ✓ (all absent reads render `Not exposed`); remaining: real-device verification + `v0.2.0` tag at merge to `main`.

**Q10. J08 → v0.3.0 Phone Doctor Core — DONE (2026-10-19, `feature/j08-phone-doctor`).** Plan `2026-10-19-j08-phone-doctor-core.md` marked IMPLEMENTED: SessionEngine + 10-test CORE registry (5 unit tests), display/touch/sensor/vibration test screens, guided runner + review with Room-persisted results and retry, version `0.3.0`/`3`, CHANGELOG updated. Gate: honest states (absent hardware → NOT_SUPPORTED, manual tests never auto-pass) ✓; remaining: device run + `v0.3.0` tag at merge to `main`. Original spec follows. Docs: playbook `10` (gate) + v2 `11_PHONE_DOCTOR_V2` + v2 `19_CAMERA_DISPLAY_AUDIO_LABS_V2`. Session engine, snapshot capture, Full vs Quick-Test modes, display/dead-pixel/touch/multitouch/vibration/5 sensor tests, all 6 result states, manual tests never auto-pass, skip/retry, rotation handling. Camera/Display/Audio labs feed diagnostics and stay independently launchable. Gate: guided core session completes end-to-end; tag `v0.3.0`.

**Q11. J09 → v0.4.0 Hardware Diagnostics — DONE (2026-10-19, `feature/j09-phone-doctor-hw`).** Plan `2026-10-19-j09-phone-doctor-hardware.md` marked IMPLEMENTED: 11 hardware tests → 21-test sequence; speaker/mic/camera/flash behind permission gates, 5 auto connectivity checks, charging check; review summary + grouping; version `0.4.0`/`4`, CHANGELOG updated. Gate: absence-safe by design (NOT_SUPPORTED auto-results), device run pending; `v0.4.0` tag at merge. Original spec follows. Docs: playbook `11` (gate) + v2 `11`/`19`/`18`. Speaker, mic, front/rear camera, flash, Wi-Fi/BT/GPS/NFC/USB capability checks, charging observation, full-diagnostic sequencing, permission-denial flows, sensor technician tools (presence/responsiveness/min-max). Gate: full diagnostic never crashes on absent hardware/permission; tag `v0.4.0`.

**Q12. J10 → v0.5.0 Reports — DONE (2026-10-19, `feature/j10-diagnostic-reports`).** Plan `2026-10-19-j10-diagnostic-reports.md` marked IMPLEMENTED: `ReportBuilder` (5 unit tests), reopenable detail screen, text + framework-PDF export via FileProvider with sensitive-field preview, copy summary, per-session/delete-all flows, status filters. Export format decision resolved: canonical text + PDF (v2's "approved structured format"). Version `0.5.0`/`5`, CHANGELOG updated. Gate: pipeline complete, device run pending; `v0.5.0` tag at merge. Original spec follows. Docs: playbook `12` (gate) + v2 `12_REPORTS_TECHNICIAN_WORKFLOW_V2`. Persist sessions/results, history with status/date filters, report detail + reopen without rerun, notes, snapshot, generator, copy concise summary, export, sensitive-field preview before export, FileProvider share, delete individual/all, privacy review. Gate: save → reopen → export → share → delete works; tag `v0.5.0`.

**Q13. J11 → v0.6.0 App Intelligence + Permission Center — DONE (2026-10-19, `feature/j11-app-inspector`).** Plan `2026-10-19-j11-app-inspector.md` marked IMPLEMENTED: visible-package list with search/filters + honest 11+ visibility caveat, detail (version/SDK/installer/times/path, launch/settings), permission groups, component explorer, plus own-app Permission Center. APK export deferred POST-V1. Version `0.6.0`/`6`, CHANGELOG updated. Gate: no all-packages claim ✓; `v0.6.0` tag at merge. Original spec follows. Docs: playbook `13` (gate) + v2 `13_APP_APK_INSPECTOR_V2` + v2 `22_PERMISSION_APPOPS_LAB_V2` (V1 CORE parts only).

**Q14. J12 → v0.7.0 USB Lab — DONE (2026-10-19, `feature/j12-usb-lab`).** Plan `2026-10-19-j12-usb-lab.md` marked IMPLEMENTED: host capability card, attach/detach receiver, permission flow (PendingIntent + response receiver), Identity → Classification → Interfaces → Endpoints → Configurations detail, permission-gated serial, copy/share summary, ChipGenius limitation note; `UsbDescriptorsTest` (5 tests). Version `0.7.0`/`7`, CHANGELOG updated. Gate: multi-class device test pending hardware; `v0.7.0` tag at merge to `main`. Original spec follows. Docs: playbook `14` (gate) + v2 `14_USB_LAB_V2`. Host detection, attach/detach, permission flow, VID/PID/class/subclass/protocol, manufacturer/product/serial where exposed, interfaces/endpoints/descriptors, detail UX (Identity → Classification → Interfaces → Endpoints → Descriptors), export summary. Hard boundary unchanged: no NAND/controller/ChipGenius claims. Gate: multiple USB classes tested; tag `v0.7.0`.

**Q15. J13 → v0.8.0 NFC + Network + Bluetooth — DONE (2026-10-19, `feature/j13-nfc-network`).** Plan `2026-10-19-j13-nfc-network.md` marked IMPLEMENTED: reader-mode tag scan + tech list + NDEF read, Tag Doctor, armed write/erase with confirmations; connection inspector + link properties, ping, DNS lookup, layer diagnostics; adapter state + permission-gated bonded devices. Version `0.8.0`/`8`, CHANGELOG updated. Gate: real-tag/device runs pending; `v0.8.0` tag at merge to `main`. Original spec follows. Docs: playbook `15` (gate) + v2 `15_NFC_LAB_V2` + v2 `16_NETWORK_LAB_V2` + v2 `17_BLUETOOTH_LAB_V2` (V1 OPTIONAL basic only). NFC: Tag Inspector (NfcA/B/F/V/IsoDep/Mifare techs), NDEF read + Writer with size preview, Tag Doctor (PASS/WARNING/UNSUPPORTED), mutation confirmation, export — no secure-credential cloning claims. Network: Connection Inspector, ping, DNS lookup, layer-by-layer diagnostics (no fake health score). Bluetooth: adapter/capability state + bonded-device info as permitted; BLE scanner stays POST-V1. Gate: real tags tested; restrictions documented; tag `v0.8.0`.

### Phase D — V1 release train (J14–J17)

**Q16. J14 → v0.9.0-alpha.N Integrated Alpha — PARTIAL (2026-10-19).** Static integration review done: `docs/audits/J14_INTEGRATION_REVIEW.md` (navigation, permission-flow, crash, a11y, perf, battery, storage/privacy, offline — all PASS, 3 minor open findings). **2026-10-19 re-audit + fixes:** audio/camera/display lab stubs wired to live Phone Doctor tests (new `LabIndexScreen`), Developer Lab V1 subset shipped (intent launcher + display metrics — closes the last unimplemented approved-V1 feature), app-icon a11y finding closed, stale "Phase N" stub copy corrected, `assembleDebug` + all unit tests green, tracker resynced. Remaining items are device/tester-bound: version matrix, OEM devices, USB/NFC hardware runs, alpha feedback, defect triage, `v0.9.0-alpha.N` bump + tag. **Blocked on hardware/testers.** Original spec follows. Doc: playbook `16` + `23` + checklist Journey 14. Integration/navigation/permission/crash audits, a11y + perf passes, upgrade + clean-install, offline, OEM device matrix (Pixel/Samsung/Xiaomi/OPPO/Vivo/Transsion where available), alpha feedback + severity triage. Exit: no critical blocker; core flows verified on representative devices; limitations documented.

**Q17. J15 → v1.0.0-beta.N / rc.N — BLOCKED (owner/testers/signing).** Feature freeze decision, regression + upgrade/denied-permission/no-hardware/offline runs (devices), signing-key process + secure backup (owner), store assets, beta acceptance (testers). **2026-10-19:** docs-side items done — release notes draft (`docs/release/RELEASE_NOTES_v1.0.0_DRAFT.md`) + privacy policy draft (`docs/PRIVACY_POLICY_DRAFT.md`) + README refresh; all await owner review. No other codeable items remain before device validation. Doc: playbook `17` + `23`. Feature freeze, regression, upgrade/clean-install/denied-permission/no-hardware/low-storage/offline/rotation tests, a11y + privacy/security acceptance, signing key process + secure backup, release notes, privacy policy if distribution requires, store assets, beta acceptance. RC gate: blockers-only after RC1.

**Q18. J16 → v1.0.0 Stable — BLOCKED (depends on J14/J15 + signing/distribution).** Acceptance criteria, compat matrix, signed release build, `v1.0.0` tag, release notes, distribution, monitoring. Doc: playbook `18`. All V1 acceptance criteria, compat matrix + limitations published, README/CHANGELOG final, signed build verified install+upgrade, tag `v1.0.0`, release notes, distribution, monitoring started.

**Q19. J17 maintenance process — ONGOING (no code needed).** Bug flow + severity + patch cadence already defined in playbook `19`/`28`; applies when post-release issues exist. Doc: playbook `19` + `28` issue template. Bug flow REPORT→REPRODUCE→CLASSIFY→ROOT-CAUSE→FIX→targeted validate; severity P0–P3; patch checklist; `v1.0.x` / `v1.x` cadence. Ongoing, not a release gate.

### Phase E — Post-V1 providers and V2 (J18–J20)

**Q20. J18 Shizuku provider (post-v1 minor, e.g. v1.5.0) — DEFERRED (roadmap-gated: post-V1).** Not agent-started now: scheduled after V1 stable; needs a Shizuku dependency decision (new external dep), lifecycle/permission research, and device verification. `privilege:shizuku` stub already in place. Original spec follows. Docs: playbook `20` + checklist Journey 18 + v2 `23_CAPABILITY_ADVANCED_ACCESS_V2` + v2 `22` (elevated parts). Research current Shizuku docs first (lifecycle, permission, capability map), then `ShizukuProvider`, setup UI, read-only proof, AppOps inspect → approved mutations, advanced package/permission controls, risk confirmations. Gate: app fully functional without Shizuku.

**Q21. J19 Wireless ADB provider (later 1.x) — DEFERRED (roadmap-gated: post-V1).** Protocol/library + license research, pairing UX, secure key storage, session manager — all post-V1; `privilege:adb` stub in place. Original spec follows. Docs: playbook `21` + checklist Journey 19 + v2 `23`. Research protocol/library/license, pairing UX, secure key storage, session manager, structured allow-listed actions (package/property/log/scoped reboot), terminal only if separately approved. Gate: injection review done; app useful without ADB.

**Q22. J20 V2 / optional root / future — DEFERRED (needs V2 PRD + owner approval).** Docs: playbook `22` + Product Bible §9 + POST-V1/EXPERIMENTAL sections across all v2 feature files (snapshot compare, repair-intake/used-phone presets, BLE inspector, sensor recording, USB VID/PID DB + serial helpers, NFC presets/history, port reachability, Developer Lab expansion, custom dashboard, shop workflow). Nothing committed: RootProvider evaluation, plugin architecture, multi-device history, encrypted sync, desktop companion. Requires V2 PRD + threat model + migration/deprecation plans before any build.

**Q23. J21 DroidKit Intelligence — AI add-on (post-V1, e.g. v1.6.0) — SKIPPED (requires owner approval per spec: scope + BYOK/backend mode decision).** Original spec follows. Docs: `specs/ai_security_addon/` + `plans/2026-10-19-ai-security-addon.md` Part B. Journey AI-0..AI-5: Groq research + BYOK/backend mode decision + threat model → `AIProvider`/`GroqProvider`/`AIContextBuilder`/`PrivacyFilter` → settings UX → assistants → allow-listed structured actions → QA incl. prompt-injection + redaction. Detailed plan written at AI-0 (spec requires verifying current Groq docs first). Non-negotiable: core works with AI disabled; never a V1 scope change. Gate: owner approves scope + mode; acceptance checklist `11` green.

## 5. Decisions

| # | Decision | Resolution | Lands |
|---|---|---|---|
| D1 | Product name | **DECIDED: DroidKit** | Q3 — tick checklist item |
| D2 | Distribution | **DECIDED: GitHub Releases + direct APK** | Q3/Q17 — no Play requirement for V1; signing via own keystore; privacy policy still written (local-first story) but no store deadline |
| D3 | LICENSE | **DECIDED: Apache-2.0** — added at repo root; Android-ecosystem standard, permissive, patent grant. Swap before first release if MIT/GPL preferred | Q7 — tick bootstrap item |
| D4 | Report export format | **OPEN** — decide at Q12; v2 wants "PDF or approved structured format" | Q12 |
| D5 | Play Store timing | **Resolved by D2** — not the V1 channel; revisit only if distribution changes | — |

Already decided in design spec (no re-litigation unless new evidence): `applicationId io.github.droidkit`, minSdk 28, targetSdk/compileSdk 36, multi-module layout, project root = `C:\wamp64\www\DroidKit`.

## 6. How to Run This Queue

1. Confirm D1–D5 (only D1/D2/D3 matter before code; D4/D5 can wait).
2. Approve Q1–Q2 (doc moves + rule adoption) — they are one-session chores.
3. Q3–Q6 are research/decision journeys: produce the matrices and freeze V1.
4. Q7–Q8: execute the existing skeleton plan task-by-task (`subagent-driven-development` or `executing-plans` per its header).
5. From Q9 onward: write a per-journey plan doc before each milestone, execute, then run the journey's release gate (CHANGELOG → version → tag → checklist).
6. Keep `specs/10_DroidKit_Master_Journey_Checklist.md` as the single progress tracker — tick items only after validation per §3.
