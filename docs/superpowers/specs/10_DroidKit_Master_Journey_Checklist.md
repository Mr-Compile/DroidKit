# DroidKit - Master Journey and Execution Checklist

This file is the project-wide progress tracker. Update `[ ]` to `[x]` only when the item is actually completed and verified. Keep the version milestones synchronized with `CHANGELOG.md`, Git tags, and Android `versionName` / `versionCode`.

## Current Status
- [x] Initial product idea established
- [x] Target users identified
- [x] Native Android direction selected
- [x] Kotlin + Jetpack Compose selected
- [x] High-level architecture selected
- [x] Capability-provider concept defined
- [x] Initial PRD drafted
- [x] Initial roadmap drafted
- [x] Planning documents packaged
- [x] Pre-development specification completed
- [x] V1 scope frozen
- [x] Development started

---

# Journey 1 - Product Discovery and Planning

## 1.1 Product Definition
- [x] Define DroidKit concept
- [x] Define technician use case
- [x] Define power-user use case
- [x] Define developer use case
- [x] Decide local-first V1
- [x] Decide that Shizuku is optional
- [x] Decide that Wireless ADB is optional
- [x] Postpone root to a future phase
- [x] Finalize public product name
- [x] Finalize application/package ID
- [x] Decide initial distribution: GitHub, Play Store, direct APK, or combination
- [x] Write one-sentence product positioning statement

## 1.2 Product Requirements
- [x] Draft PRD
- [x] Draft V1 feature groups
- [x] Draft V2/advanced feature groups
- [x] Define non-goals
- [x] Define safety philosophy
- [x] Review PRD line by line
- [x] Remove features that do not belong in V1
- [x] Add any missing must-have V1 requirements
- [x] Approve PRD
- [x] Freeze V1 scope

Completion gate:
- [x] V1 has a fixed feature list and no feature enters V1 without an explicit scope change.

---

# Journey 2 - Pre-Development Research

## 2.1 Android Compatibility
- [x] Verify minimum supported Android version
- [x] Verify current compileSdk
- [x] Verify current targetSdk
- [x] Define supported Android version matrix
- [x] Define representative OEM/device test matrix
- [x] Document Android-version-specific behavior

## 2.2 API Feasibility Spikes
For every V1 tool verify API, permission, hardware, restrictions, OEM differences, and fallback behavior.

- [x] Device/build information
- [x] CPU/ABI information
- [x] RAM information
- [x] Storage information
- [x] Display information
- [x] Battery information
- [x] Sensor inventory
- [x] Display/dead-pixel diagnostic
- [x] Touch diagnostic
- [x] Multitouch diagnostic
- [x] Speaker diagnostic
- [x] Microphone diagnostic
- [x] Vibration/haptics diagnostic
- [x] Camera diagnostic
- [x] Flash diagnostic
- [x] Proximity sensor diagnostic
- [x] Light sensor diagnostic
- [x] Accelerometer diagnostic
- [x] Gyroscope diagnostic
- [x] Magnetometer diagnostic
- [x] Wi-Fi/network capability diagnostics
- [x] Bluetooth/BLE capability diagnostics
- [x] GPS/location capability diagnostics
- [x] USB Host / UsbManager inspection
- [x] NFC read/write behavior
- [x] PackageManager/package visibility
- [x] Application component inspection
- [x] Permission inspection
- [x] Diagnostic report/PDF/export behavior

## 2.3 Advanced Capability Research
Not required to block V1 unless the V1 scope changes.

- [ ] Review current Shizuku APIs/documentation
- [ ] Define Shizuku capability map
- [ ] Research Wireless ADB implementation options
- [ ] Define secure ADB key storage approach
- [ ] Research AppOps limitations
- [ ] Research advanced package-management limitations
- [ ] Research log access limitations
- [ ] Research root provider only when future milestone begins

Completion gate:
- [x] Every V1 feature has a verified feasibility note.
- [x] Unsupported/limited behavior is documented instead of guessed.

---

# Journey 3 - UX and Product Design

## 3.1 Information Architecture
- [x] Define Home / Tools / Diagnose / Settings concept
- [x] Define Tool Registry concept
- [x] Define tool availability states
- [x] Define diagnostic result states
- [x] Finalize navigation graph
- [x] Finalize categories
- [x] Finalize search behavior
- [x] Finalize favorites behavior
- [x] Finalize recent-tools behavior

## 3.2 Wireframes
- [x] Onboarding
- [x] Home
- [x] Tools
- [x] Search results
- [x] Tool details
- [x] Device Inspector
- [x] Battery
- [x] Storage
- [x] Sensors
- [x] Diagnostic start
- [x] Diagnostic test template
- [x] Diagnostic summary
- [x] Diagnostic history
- [x] Report view
- [x] Apps list
- [x] App details
- [x] Permissions/components
- [x] USB Lab
- [x] USB device details
- [x] NFC Lab
- [x] NFC scan/result
- [x] Network tools
- [x] Settings
- [x] Advanced access setup
- [x] About

## 3.3 Design System
- [x] Typography scale
- [x] Spacing scale
- [x] Shapes/radii
- [x] Light theme
- [x] Dark theme
- [x] Semantic status styles
- [x] Tool cards
- [x] Information rows
- [x] Confirmation dialogs
- [x] Empty/error/unsupported states
- [x] Diagnostic controls
- [x] Accessibility rules

Completion gate:
- [x] Core screens have approved low-fidelity wireframes.
- [x] Reusable UI components are identified before implementation.

---

# Journey 4 - Final Technical Specification

## 4.1 Architecture
- [x] Kotlin selected
- [x] Jetpack Compose selected
- [x] MVVM/Clean Architecture direction selected
- [x] Hilt selected
- [x] Coroutines + Flow selected
- [x] Room selected
- [x] DataStore selected
- [x] Capability-provider architecture selected
- [x] Finalize Gradle module boundaries
- [x] Finalize dependency rules between modules
- [x] Finalize repository/use-case conventions
- [x] Finalize result/error model
- [x] Finalize logging strategy
- [x] Finalize permission architecture
- [x] Finalize capability-resolution contract
- [x] Finalize ToolDefinition contract

## 4.2 Persistence
- [x] Initial Room entities proposed
- [x] Finalize DiagnosticSession schema
- [x] Finalize DiagnosticResult schema
- [x] Finalize DeviceSnapshot schema
- [x] Finalize SavedReport schema
- [x] Define migrations strategy
- [x] Define retention/deletion behavior
- [x] Define export privacy rules

## 4.3 Engineering Standards
- [x] Kotlin style/conventions
- [x] Compose conventions
- [x] Package naming conventions
- [x] Git commit convention
- [x] Branch naming convention
- [x] Pull request/review checklist
- [x] Definition of Done
- [x] Testing rules
- [x] Release checklist

Completion gate:
- [x] Another developer or coding AI can implement M0 using the specification without inventing major architecture decisions.

---

# Journey 5 - Repository and Project Setup

Target release: `v0.1.0`

- [x] Create repository
- [x] Add README
- [x] Add LICENSE decision/file — Apache-2.0 (decision D3)
- [x] Add `.gitignore`
- [x] Create Android project
- [x] Configure Gradle Kotlin DSL
- [x] Configure version catalog if used
- [x] Configure Compose
- [x] Configure Hilt
- [x] Configure Coroutines
- [x] Configure Navigation Compose
- [x] Configure Room
- [x] Configure DataStore
- [x] Add unit-test foundation
- [x] Add instrumented-test foundation — runner + AppSmokeTest; device run pending hardware
- [ ] Add CI only if useful at this stage — deferred
- [x] Add CHANGELOG.md
- [x] Establish versionName/versionCode scheme — `0.1.0` / `1` in convention plugin
- [x] Tag first project baseline — `v0.1.0` tagged at merge to `main`

---

# Journey 6 - v0.1.0 Foundation

- [x] Core theme/design tokens
- [x] Reusable Compose components
- [x] Navigation shell
- [x] Home shell
- [x] Tools shell
- [x] Diagnose shell
- [x] Settings shell
- [x] ToolRegistry
- [x] Tool categories
- [x] Tool search foundation
- [x] Capability model
- [x] CapabilityManager
- [x] StandardAndroidProvider
- [x] PermissionManager
- [x] Hardware/API detector — `DeviceSystemProbe`
- [x] Room database foundation
- [x] DataStore foundation
- [x] Error/result model — `ToolResolution` / `TestResultState`
- [x] Basic logging — `DLog` facade
- [x] Unit tests for ToolRegistry — 4 tests pass
- [x] Unit tests for capability resolution — 8 tests pass
- [ ] Focused UI/navigation validation — requires attached device/emulator

Release gate:
- [x] Foundation architecture reviewed — 31-module graph, DI verified via Hilt compile
- [x] No feature directly depends on Shizuku/ADB/root — audited: zero `privilege.*` imports in `feature/`
- [ ] App launches and core navigation works — `assembleDebug` passes; launch needs device
- [x] `v0.1.0` tagged
- [x] CHANGELOG updated

---

# Journey 7 - v0.2.0 Device Inspector

- [x] Device/build information
- [x] Android/API/security patch information where exposed
- [x] CPU/ABI
- [x] RAM
- [x] Storage — dashboard summary + Storage Lab volumes
- [x] Display — resolution, density, refresh rate, supported modes
- [x] Battery — Battery Lab (level, status, temp, voltage, tech, health-as-reported)
- [x] Sensor inventory — Sensor Lab + live monitor for safe types
- [x] Camera capability summary — count by facing via CameraManager
- [x] Connectivity/hardware summary — NFC/BT/BLE/USB-host/telephony/location features
- [x] Copy/share useful device summary — per-field copy, copy-section, share intent
- [x] Unsupported fields handled correctly — `Not exposed` on null/−1 reads
- [ ] Real-device verification — requires attached device/emulator

Release gate:
- [ ] Device Inspector works on supported Android versions — requires device run
- [x] No unavailable values are fabricated — all null/−1 fields render `Not exposed`
- [x] `v0.2.0` tagged
- [x] CHANGELOG updated

---

# Journey 8 - v0.3.0 Phone Doctor Core

- [x] DiagnosticSession engine
- [x] Diagnostic result model
- [x] Display color test
- [x] Dead-pixel test
- [x] Touch grid
- [x] Multitouch test
- [x] Vibration/haptics
- [x] Accelerometer
- [x] Gyroscope
- [x] Proximity
- [x] Light sensor
- [x] Magnetometer
- [x] PASS state
- [x] FAIL state
- [x] NOT TESTED state
- [x] NOT SUPPORTED state
- [x] PERMISSION REQUIRED state — TestResultState supports it; no CORE test needs a runtime permission
- [x] UNKNOWN state — in enum; surfaces only for unresolvable reads, none in CORE tests today

Release gate:
- [ ] Guided diagnostic flow works end-to-end for core tests — code-complete; needs device run
- [x] `v0.3.0` tagged
- [x] CHANGELOG updated

---

# Journey 9 - v0.4.0 Phone Doctor Hardware

- [x] Speaker test
- [x] Microphone test — behind RECORD_AUDIO permission gate; denial → PERMISSION_REQUIRED
- [x] Front camera — CameraX preview; absent lens → NOT_SUPPORTED
- [x] Rear camera
- [x] Flash — torch toggle; no flash unit → NOT_SUPPORTED
- [x] Wi-Fi capability check — auto-recorded (capability + enabled state in note)
- [x] Bluetooth capability check
- [x] Location/GPS capability check
- [x] NFC capability check
- [x] USB capability check
- [x] Charging/battery diagnostic — live level/status/plug readings
- [x] Full Diagnostic sequencing — engine runs all 21 tests (CORE + HARDWARE)
- [x] Skip/retry behavior — built J08; retry re-queues failed/skipped
- [x] Diagnostic summary — state counts + category grouping on review screen

Release gate:
- [ ] Full diagnostic completes without crashes when hardware is missing — designed-in (NOT_SUPPORTED auto-results, no crashes by construction); needs device run
- [x] `v0.4.0` tagged
- [x] CHANGELOG updated

---

# Journey 10 - v0.5.0 Diagnostic Reports

- [x] Persist sessions — Room `diagnostic_sessions` (built J08)
- [x] Persist individual results — `diagnostic_results` rows per test
- [x] Capture DeviceSnapshot — `device_snapshots` on session start
- [x] Diagnostic history — reports tab lists sessions with result tallies
- [x] Report detail screen — `reports/detail/{id}` reopenable without rerun
- [x] Technician/user notes — captured at review, shown in detail + report
- [x] Report generation — `ReportBuilder` canonical text (5 unit tests)
- [x] PDF or selected export format — text + framework-PDF, no deps
- [x] Share flow — FileProvider `content://` share with read grant
- [x] Delete report/session — confirm dialog; results deleted with session
- [x] Privacy review — export sheet lists every field class before share
- [x] Export verification — file write + share intent verified in code; device share-sheet run pending

Release gate:
- [ ] A complete diagnostic can be saved and exported — pipeline complete; needs device run
- [x] `v0.5.0` tagged
- [x] CHANGELOG updated

---

# Journey 11 - v0.6.0 App Inspector

- [x] Package list — visible apps via PackageManager
- [x] User/system filtering — FLAG_SYSTEM chips
- [x] Search — label + package name
- [x] Package details — version/SDK/installer/times/path
- [x] Version information — name + long code
- [x] Min/target SDK where accessible — `minSdkVersion` null-safe, absent → Not exposed
- [x] Activities — Component Explorer per app
- [x] Services
- [x] Receivers
- [x] Providers
- [x] Permission inspection — granted/not-granted groups + settings link
- [x] APK metadata/path where accessible — sourceDir shown
- [x] Launch supported app — `getLaunchIntentForPackage`, absent → honest error
- [-] Supported APK export if approved/allowed — deferred POST-V1 (frozen decision)
- [x] Package visibility compatibility handling — visible-only reads + caveat header

Release gate:
- [x] Modern Android package-visibility behavior is handled correctly — no QUERY_ALL_PACKAGES; visible-set counted and disclosed
- [x] `v0.6.0` tagged
- [x] CHANGELOG updated

---

# Journey 12 - v0.7.0 USB Lab

- [x] USB Host support detection — capability card; absent host → NOT_SUPPORTED state
- [x] Device attach/detach handling — system-broadcast receiver (NOT_EXPORTED), list refreshes
- [x] USB permission flow — `requestPermission` + response receiver; serial gated on grant
- [x] VID
- [x] PID
- [x] Manufacturer where exposed — null → `Not exposed`
- [x] Product where exposed — null → `Not exposed`
- [x] Serial where authorized/exposed — read only after grant; SecurityException-safe
- [x] Interfaces — class/subclass/protocol + endpoint count per interface
- [x] Endpoints — address, direction, transfer type, max packet
- [x] Accessible descriptors — configurations with power flags; honest empty hints when permission withheld
- [x] Copy/export summary — clipboard copy + share-sheet summary
- [x] Unsupported-device handling — no host feature → NOT_SUPPORTED card, never FAIL
- [ ] Test with multiple USB device classes — requires attached hardware; device run pending

Release gate:
- [x] USB Lab does not claim ChipGenius-level NAND/controller access — limitation stated on detail screen
- [x] `v0.7.0` tagged
- [x] CHANGELOG updated

---

# Journey 13 - v0.8.0 NFC and Network Lab

## NFC
- [x] NFC hardware detection — `hasSystemFeature(FEATURE_NFC)` + adapter presence
- [x] NFC enabled-state flow — off-state card with link to system NFC settings
- [x] Tag scan — `enableReaderMode` with lifecycle-scoped start/stop
- [x] Technology detection — `tag.techList` rendered per tag
- [x] NDEF parsing — records with TNF name, type/payload hex
- [x] URI/text record display — RTD_TEXT (UTF-8/16) and RTD_URI prefix-table decode
- [x] Supported NDEF writing — text/URI records, capacity+writable checks, armed write
- [x] Supported erase/format behavior — erase writes empty-TNF message; NdefFormatable fallback
- [x] Confirmation before mutating a tag — write and erase behind explicit confirm dialogs
- [x] Export/copy record details — per-record copy via `InfoRow` copy action

## Network
- [x] Connection information — active transports, capabilities, link properties
- [x] IP information allowed by Android — local addresses, DNS servers, MTU, private DNS
- [x] Ping — `InetAddress.isReachable` samples with ICMP-filtering caveat
- [x] DNS lookup — `getAllByName` with elapsed time
- [x] Basic connectivity diagnostics — per-layer results, no aggregate score
- [x] Wi-Fi restrictions handled by API/version — SSID/BSSID/MAC not read; restriction noted

## Bluetooth (V1 OPTIONAL basic)
- [x] Adapter support/state — presence, enabled, classic+LE features
- [x] Permission handling — BLUETOOTH_CONNECT gate on API 31+, request in-context
- [x] Bonded devices — name/address/type/bond as Android permits
- [x] BLE scanner deferred — POST-V1, `ToolPhase.V2` in registry

Release gate:
- [ ] NFC operations tested with representative tags — requires hardware; device run pending
- [x] Network tools do not overpromise restricted identifiers — documented on screen
- [x] `v0.8.0` tagged
- [x] CHANGELOG updated

---

# Journey 14 - v0.9.0 Alpha

- [ ] V1 feature implementation complete
- [ ] Cross-feature integration review
- [ ] Navigation review
- [ ] Permission-flow review
- [ ] Crash/error handling review
- [ ] Accessibility pass
- [ ] Performance pass
- [ ] Battery/background behavior review
- [ ] Storage/privacy review
- [ ] Test Android version matrix
- [ ] Test representative Samsung device
- [ ] Test representative Xiaomi/Redmi/POCO device
- [ ] Test representative OPPO/Realme device
- [ ] Test representative Vivo device
- [ ] Test representative Transsion device where available
- [ ] Test Pixel/AOSP-like device where available
- [ ] Alpha tester feedback collected
- [ ] Alpha issues triaged

Version examples:
- [ ] `v0.9.0-alpha.1`
- [ ] `v0.9.0-alpha.2`
- [ ] Additional alpha builds as required

---

# Journey 15 - Beta / Release Candidate

Recommended naming can use prerelease SemVer without forcing `0.10.0`.

- [ ] Feature freeze
- [ ] Fix alpha blockers
- [ ] Regression testing
- [ ] Report/export testing
- [ ] Upgrade/database migration testing
- [ ] Clean-install testing
- [ ] Permission-denied testing
- [ ] No-hardware testing
- [ ] Offline testing
- [ ] Release notes drafted
- [ ] Privacy policy prepared if distribution requires it
- [ ] Store/listing assets prepared if applicable
- [ ] Signing/release key process finalized
- [ ] Backup signing material securely
- [ ] Beta testers complete acceptance testing

Version examples:
- [ ] `v1.0.0-beta.1`
- [ ] `v1.0.0-beta.2`
- [ ] `v1.0.0-rc.1`

---

# Journey 16 - DroidKit v1.0.0 Stable

- [ ] All V1 acceptance criteria met
- [ ] No known release-blocking crashes
- [ ] Compatibility matrix documented
- [ ] README updated
- [ ] CHANGELOG updated
- [ ] Version name/code updated
- [ ] Release build signed
- [ ] Release build verified
- [ ] Git tag `v1.0.0`
- [ ] Release notes published
- [ ] APK/store release published
- [ ] Post-release monitoring started

---

# Journey 17 - v1.x Maintenance

For every patch:
- [ ] Reproduce issue
- [ ] Identify affected files/features
- [ ] Implement smallest safe fix
- [ ] Validate changed area
- [ ] Run broader tests only when integration risk requires it
- [ ] Update CHANGELOG
- [ ] Increment patch version
- [ ] Tag release

Examples:
- `v1.0.1` - bug fixes
- `v1.0.2` - bug fixes
- `v1.1.0` - new backward-compatible feature group

---

# Journey 18 - Shizuku Journey

Suggested target: a post-v1 minor release such as `v1.5.0`, adjusted according to actual roadmap.

## Research
- [ ] Re-check current Shizuku documentation
- [ ] Define supported Android versions
- [ ] Define Shizuku lifecycle/state model
- [ ] Define permission flow
- [ ] Define reconnect/error behavior
- [ ] Define exact capabilities DroidKit will expose

## Implementation
- [ ] ShizukuProvider
- [ ] Provider availability detection
- [ ] Permission request
- [ ] Capability registration
- [ ] Read-only elevated proof-of-concept
- [ ] AppOps inspection
- [ ] Approved AppOps mutation
- [ ] Advanced package controls
- [ ] Advanced permission controls
- [ ] Risk confirmations
- [ ] Audit/result history where useful

Release:
- [ ] Shizuku features tested across supported versions
- [ ] App works normally without Shizuku
- [ ] Release version tagged
- [ ] CHANGELOG updated

---

# Journey 19 - Wireless ADB Journey

Suggested target: later `1.x`, for example `v1.7.0`, but do not lock the number until roadmap review.

## Research
- [ ] Select ADB protocol/library strategy
- [ ] Review licensing
- [ ] Define pairing flow
- [ ] Define authentication/key storage
- [ ] Define connection/session lifecycle
- [ ] Define supported structured actions
- [ ] Define threat/security model

## Implementation
- [ ] Pairing UI
- [ ] Secure key storage
- [ ] Session manager
- [ ] Capability provider
- [ ] Structured command/action executor
- [ ] Package actions
- [ ] Device property actions
- [ ] Authorized log tools
- [ ] Reboot/system actions only where appropriate
- [ ] Terminal only if explicitly approved as product scope

Release:
- [ ] ADB disconnect/reconnect tested
- [ ] Invalid pairing tested
- [ ] Security review complete
- [ ] App remains usable without ADB
- [ ] Release version tagged

---

# Journey 20 - DroidKit 2.x / Future

Potential directions; none are automatically committed.

- [ ] Decide whether root support provides enough value
- [ ] RootProvider design
- [ ] Extension/plugin architecture research
- [ ] Technician repair-shop mode
- [ ] Multi-device history
- [ ] Optional encrypted synchronization
- [ ] Team/shop accounts only if a backend becomes justified
- [ ] Additional hardware labs
- [ ] Additional developer tools
- [ ] Desktop companion only if a strong use case exists

Before `v2.0.0`:
- [ ] Define what makes it a major release
- [ ] Migration plan
- [ ] Compatibility plan
- [ ] Deprecation plan
- [ ] V2 PRD
- [ ] V2 architecture review

---

# Journey 21 - DroidKit Intelligence (AI add-on)

Post-V1 add-on journey (`specs/ai_security_addon/` + `plans/2026-10-19-ai-security-addon.md` Part B). **Not part of the frozen V1 scope** — starts only after owner approval, likely as a minor post-v1 release.

## AI-0 Research
- [ ] Verify current Groq API documentation (auth, endpoints, models, limits, terms)
- [ ] Decide BYOK vs hosted-backend mode
- [ ] Threat-model credentials and context
- [ ] Define AIProvider contract
- [ ] Define privacy UX
- [ ] Define structured output schemas

## AI-1 Core
- [ ] AIProvider
- [ ] GroqProvider
- [ ] AIContextBuilder
- [ ] PrivacyFilter
- [ ] PromptRegistry
- [ ] Model/config registry
- [ ] Typed errors
- [ ] Timeout/retry policy
- [ ] Secret redaction

## AI-2 Setup
- [ ] AI settings screen
- [ ] BYOK key entry
- [ ] Reviewed runtime credential storage
- [ ] Test connection
- [ ] Clear/Disconnect
- [ ] Offline/no-key states

## AI-3 Assistants
- [ ] Device Explainer
- [ ] Diagnostic Analyzer
- [ ] Repair Assistant
- [ ] Report Assistant
- [ ] USB Assistant
- [ ] NFC Assistant
- [ ] App Inspector Assistant
- [ ] Network Troubleshooter
- [ ] Tool Recommender
- [ ] Contextual Chat

## AI-4 Structured Actions
- [ ] Allow-listed action schema
- [ ] Validator
- [ ] ToolRegistry mapping
- [ ] Risk Engine integration
- [ ] Explicit confirmation
- [ ] Reject arbitrary model commands

## AI-5 QA
- [ ] No/invalid key
- [ ] Offline/timeout
- [ ] Rate limit/provider outage
- [ ] Malformed response
- [ ] Prompt injection in tool data
- [ ] Secret-redaction tests
- [ ] Context-minimization review
- [ ] AI-disabled core regression

Completion gate:
- [ ] `11_AI_SECURITY_ACCEPTANCE_CHECKLIST.md` all green
- [ ] Core app fully functional with AI disabled

---

# Versioning Checklist

Use Semantic Versioning:

`MAJOR.MINOR.PATCH`

Examples:
- `0.1.0` - foundation milestone
- `0.2.0` - Device Inspector
- `0.3.0` - Phone Doctor core
- `0.4.0` - Phone Doctor hardware
- `0.5.0` - reports
- `0.6.0` - App Inspector
- `0.7.0` - USB Lab
- `0.8.0` - NFC/Network
- `0.9.0-alpha.1` - integrated alpha
- `1.0.0-beta.1` - V1 beta
- `1.0.0-rc.1` - release candidate
- `1.0.0` - stable V1
- `1.0.1` - patch
- `1.1.0` - backward-compatible feature release
- `2.0.0` - major compatibility/product change

For every release:
- [ ] Increment Android `versionCode`
- [ ] Set correct `versionName`
- [ ] Update CHANGELOG
- [ ] Run required scoped validation
- [ ] Build release artifact
- [ ] Verify install/upgrade
- [ ] Create Git tag
- [ ] Write release notes

Important: `versionCode` only needs to monotonically increase. Do not depend on a clever numeric formula if it could become restrictive later.

---

# Definition of Done - Feature

A feature is DONE only when:
- [ ] Requirement is implemented
- [ ] Unsupported states are handled
- [ ] Permission-denied state is handled
- [ ] Hardware-missing state is handled when relevant
- [ ] Error state is handled
- [ ] Risk confirmation exists when required
- [ ] UI follows the design system
- [ ] Accessibility basics are covered
- [ ] Changed files/features were validated
- [ ] Relevant tests pass
- [ ] Documentation is updated
- [ ] No unrelated feature was silently added

---

# Definition of Done - Milestone

A milestone is DONE only when:
- [ ] All required feature checklists are complete
- [ ] Integration issues are resolved
- [ ] No release-blocking defects remain
- [ ] CHANGELOG is updated
- [ ] Version is updated
- [ ] Release artifact is verified
- [ ] Git tag is created when the milestone is released

---

# AI Progress Update Rule

When another AI works on DroidKit, instruct it to:
1. Read `08_DroidKit_AI_Handoff.md`.
2. Read this journey checklist.
3. Identify the current milestone.
4. Work only on the requested/current task.
5. Mark `[x]` only for work actually completed and validated.
6. Do not mark future work complete because scaffolding exists.
7. Add a short note beside an item when implementation differs from the original plan.
8. Update CHANGELOG/version only when a release or versioned milestone actually occurs.
9. Identify changed files before validation.
10. Do not run the full test suite or broad analysis unless the changed area requires broader integration validation.

---

# Immediate Next Actions

These are the next unchecked tasks that should happen before full development:

- [ ] Review and approve the PRD
- [ ] Freeze DroidKit V1 scope
- [ ] Complete V1 Android API feasibility research
- [ ] Confirm minSdk / compileSdk / targetSdk
- [ ] Finalize product/package ID
- [ ] Decide distribution strategy
- [ ] Create low-fidelity wireframes
- [ ] Finalize module boundaries
- [ ] Finalize Room schema
- [ ] Define Definition of Done and engineering conventions
- [ ] Convert milestones into repository issues
- [ ] Begin `v0.1.0` Foundation
