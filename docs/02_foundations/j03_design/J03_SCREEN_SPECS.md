# J03 Screen Specifications

Date: 2026-10-19
Low-fi spec + state model per screen from `playbook/05` checklist. Every screen must render all applicable states before its feature journey is considered implemented. State legend: **L**oading, **C**ontent, **E**mpty, **P**ermission-required, **U**nsupported, **E**rror.

## 1. Onboarding
One-time intro: what DroidKit is, honesty principles ("never fabricates"), capability model, privacy (local-first). Pages: Welcome → Capabilities → Privacy → Done.
- States: C only (static).
- Actions: Next/Skip → `DataStore` flag `onboardingComplete`.

## 2. Home (Dashboard tab)
- Layout: Device summary card (`probe.summary()`) → Capability status card (providers ready/setup/unavailable) → Quick tools row (favorites) → Recent tools → "Run Diagnostic" CTA.
- States: L (first frame), C, U per-card (absent hardware rows read `Not exposed`).
- Actions: card → tool route; CTA → `diagnostics` route.

## 3. Tools (Search/Category)
- Layout: `SearchField` → `FilterChips` (categories + availability) → `ToolCard` list from `ToolRegistry.search()`.
- States: L, C, E ("No tools match — clear filters"), U badges via `ToolResolution`.
- Actions: tap → route if `Ready`; blocked → explain; needs-setup → setup route.

## 4. Device Inspector
- Layout: sectioned cards — Identity → OS → Hardware → Memory/Storage → Display → Battery → Capabilities. Per J07 plan.
- States: L (brief), C, partial (null fields → `Not exposed` rows), section-U (whole section `NOT_SUPPORTED`), E (runCatching failure card).
- Actions: copy field, copy section, share summary, launch related lab.

## 5. Diagnostic — Start
- Layout: "Full Diagnostic" vs "Quick Test" mode cards → test list preview → estimated per-test instructions note → start.
- States: C, U (lists absent-sensor tests pre-marked `NOT_SUPPORTED`).
- Actions: pick mode → session starts (J08).

## 6. Diagnostic — Test (running)
- Layout: `DiagnosticInstructionCard` (what to do) → live surface (touch grid / tone / preview) → `ResultControls` (Pass/Fail/Skip) → progress (n/N).
- States: L (test init), C, P (mic/camera/location prompts inline), U (auto-skip absent hardware → `NOT_SUPPORTED`, never counted fail), E (executor crash → mark `UNKNOWN`, continue).
- Actions: verdict buttons, retry, skip; rotation/process-recreation safe.

## 7. Diagnostic — Summary
- Layout: overall counts by state → per-test `StatusBadge` rows → save/discard → note field.
- States: C.
- Actions: save → `DiagnosticSessionEntity` + results; reopen later.

## 8. Diagnostic — History
- Layout: session rows (date, status, counts) → filters (status/date).
- States: L, C, E ("No sessions yet — run your first diagnostic").
- Actions: tap → detail; long-press → delete confirm.

## 9. Report Detail / Export
- Layout: snapshot card → results grouped → notes → preview of exportable fields (privacy rule) → export/share/delete.
- States: L, C, P (none expected — FileProvider), E (export fail → retry).
- Actions: export (D4 format), `ACTION_SEND` FileProvider URI, delete w/ confirm.

## 10. Apps — List
- Layout: `SearchField` → `FilterChips` (user/system, granted-permission filters) → package rows (icon, name, version).
- States: L, C, E ("No visible packages match"), U-banner: "Showing packages visible to DroidKit" (permanent honest note).
- Actions: tap → detail.

## 11. Apps — Detail / Components / Permissions
- Layout (tabs or sections): Info (version, SDKs, source dir) → Components (exported-first lists) → Permissions (granted/denied groups) → APK metadata → safe actions (Launch, App info settings).
- States: C, per-section E ("No receivers"), U for restricted fields.
- Actions: copy package name, launch (exported only), open system app-info.

## 12. USB — List / Detail
- Layout: device rows (VID:PID, name-or-`Not exposed`, class) → permission prompt if ungranted → detail sections: Identity → Classification → Interfaces → Endpoints → Descriptors.
- States: L, C, E ("No USB devices attached"), P (per-device USB grant), U (no host feature / OTG off → honest note).
- Actions: request permission, refresh, copy VID:PID, export summary.

## 13. NFC — Scan / Result / Write confirm
- Layout: scan screen (hold-tag prompt + listening state) → result sections (tech list, NDEF records parsed) → write flow: record editor → size preview → `RiskConfirmationDialog` → result.
- States: L (polling), C, P (NFC off → "enable NFC" settings CTA), U (no NFC / read-only tag), E (tag lost mid-write → honest error).
- Actions: copy UID, export record, write (risk 2, confirmation mandatory).

## 14. Network Tools
- Layout: Connection Inspector card → Ping (host field + result rows) → DNS lookup → layer-by-layer diagnostics list.
- States: L (queries running), C, E (no active network), P (location-dependent SSID → prompt), U, E (timeout rows honest).
- Actions: copy IP/SSID, run diagnostics.

## 15. Settings / Advanced Access / About
- Layout (sections): Appearance (theme + dynamic color) → Permission Center (own permissions + rationale + settings links) → Advanced Access (provider status cards: Standard ready; Shizuku/ADB/Root `Unavailable — post-V1` + explain) → Safety & Data (delete diagnostic history, privacy notes) → About (version, license, links).
- States: C; provider cards U-labeled honestly.
- Actions: theme toggle, open system settings, delete-history (risk 2 confirm).

## Accessibility acceptance per screen
- 48dp touch targets; every status has icon+text; semantic labels on interactive elements; logical focus order; works at 2x font scale; TalkBack announces state changes (live regions on diagnostic verdict + sensor live values use `liveRegion` semantics sparingly — throttle announcements).
