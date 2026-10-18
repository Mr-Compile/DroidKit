# DroidKit PRD Approval Record

Date: 2026-10-19
Status: APPROVED by owner
Inputs reviewed: `planning_package/01_DroidKit_PRD.md` (draft), `playbook/03_PRODUCT_SCOPE_AND_V1_FREEZE.md`, `DROIDKIT_PRODUCT_BIBLE.md` §7, `feature_plans_v2/30_FEATURE_SCOPE_MATRIX_V2.md`

## Review outcome

The PRD was reviewed line by line against the playbook scope and the v2 feature plans. Verdict: **approve with clarifications** — the document is sound; these items were adjusted to match newer specs:

| PRD line item | Outcome | Reason |
|---|---|---|
| Goals / Non-Goals | Kept as written | Consistent with playbook `03` and Product Bible |
| V1 Core (registry/capability/permission/search/favorites/Room+DataStore) | Kept | Skeleton plan already implements |
| Device Inspector scope | Expanded per v2 `10` | Dashboard UX (sections, copy field/section, share, launch-lab links) + Battery Lab + Storage Lab + Sensor Lab inventory/monitor — all V1 CORE |
| Phone Doctor scope | Expanded per v2 `11` + `19` | Full vs Quick-Test modes, session engine, skip/retry, camera/display/audio labs feed diagnostics |
| Reports scope | Expanded per v2 `12` | Technician workflow: history filters, notes, snapshot, export with sensitive-field preview, delete individual/all |
| App Inspector scope | Expanded per v2 `13` | Component Explorer, permission grouping, APK metadata inspector; elevated controls stay POST-V1 |
| "APK export where allowed" | Moved to POST-V1 | Owner call — see decisions below |
| USB Lab scope | Expanded per v2 `14` | Detail UX layering (Identity → Classification → Interfaces → Endpoints → Descriptors); hard boundary unchanged |
| NFC Lab scope | Expanded per v2 `15` | Tag Inspector, NDEF read + Writer w/ size preview, Tag Doctor — no secure-credential cloning claims |
| Network Lab scope | Expanded per v2 `16` | Connection Inspector, ping, DNS, layer-by-layer diagnostics — no fake health score |
| Bluetooth Lab | Scoped to V1 OPTIONAL basic | Owner call — see decisions below |
| Developer Lab | Scoped to V1 OPTIONAL subset | Owner call — see decisions below |
| Sensor Lab, Permission Inspector, Tool Registry/Search | Kept — V1 CORE | Matrix tier confirmed |
| Functional Requirement Pattern | Kept — maps to `Tool` model | Implemented in skeleton `:core:common` |
| MVP success criterion | Kept | "install on non-rooted phone, inspect, guided diagnostic, save, report" |

## V1 scope decision (final)

**V1 CORE:** ToolRegistry/search/favorites, CapabilityManager, PermissionManager, Standard provider, design system, navigation, Room+DataStore — Device Inspector dashboard, Battery Lab, Storage Lab, Sensor Lab (inventory + live monitor), Phone Doctor (display, touch, multitouch, vibration, accel/gyro/proximity/light/magnetometer, speaker, mic, camera, flash, connectivity capability checks, charging observation), Reports (sessions, history, notes, snapshot, export/share/delete), App Inspector (packages, metadata, components, permissions, APK metadata), Permission Center, USB Lab, NFC Lab (inspect, NDEF read/write, Tag Doctor), Network Lab (connection info, ping, DNS, diagnostics).

**V1 OPTIONAL:** Bluetooth Lab basic (adapter/capability state + bonded-device info), Developer Lab (Intent Launcher + Display Metrics only).

**POST-V1:** Shizuku/AppOps mutation, advanced package controls, Wireless ADB, APK export, BLE scanner, sensor recording, snapshot compare, used-phone/repair presets, advanced logs.

**EXPERIMENTAL:** root provider, plugins, multi-device history, encrypted sync, desktop companion, repair-shop workflow.

**REJECTED (permanent):** FRP/credential/lock bypass, stealth surveillance, exploit delivery, fake cleaner/booster claims, ChipGenius-level NAND/controller claims, remote control platform, cloud requirement.

## Decisions needing owner sign-off — ALL DECIDED 2026-10-19

| # | Question | Decision |
|---|---|---|
| 1 | Bluetooth Lab tier | **V1 OPTIONAL basic** — adapter/capability state + bonded-device info as permitted; BLE scanner stays POST-V1 — *approved by owner* |
| 2 | Developer Lab V1 subset | **Intent Launcher + Display Metrics only** — *approved by owner* |
| 3 | APK export | **POST-V1** — deferred pending legal/policy review — *approved by owner* |

## Open decisions

- **D4 — report export format:** deferred to J10 (v0.5.0 Reports) planning per master execution plan. v2 wording: "PDF or approved structured format."
- **D5 — Play Store timing:** resolved by distribution decision (GitHub Releases + direct APK); revisit only if distribution changes.
