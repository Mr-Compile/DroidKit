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
- [ ] Development started

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

- [ ] Create repository
- [ ] Add README
- [ ] Add LICENSE decision/file
- [ ] Add `.gitignore`
- [ ] Create Android project
- [ ] Configure Gradle Kotlin DSL
- [ ] Configure version catalog if used
- [ ] Configure Compose
- [ ] Configure Hilt
- [ ] Configure Coroutines
- [ ] Configure Navigation Compose
- [ ] Configure Room
- [ ] Configure DataStore
- [ ] Add unit-test foundation
- [ ] Add instrumented-test foundation
- [ ] Add CI only if useful at this stage
- [ ] Add CHANGELOG.md
- [ ] Establish versionName/versionCode scheme
- [ ] Tag first project baseline if desired

---

# Journey 6 - v0.1.0 Foundation

- [ ] Core theme/design tokens
- [ ] Reusable Compose components
- [ ] Navigation shell
- [ ] Home shell
- [ ] Tools shell
- [ ] Diagnose shell
- [ ] Settings shell
- [ ] ToolRegistry
- [ ] Tool categories
- [ ] Tool search foundation
- [ ] Capability model
- [ ] CapabilityManager
- [ ] StandardAndroidProvider
- [ ] PermissionManager
- [ ] Hardware/API detector
- [ ] Room database foundation
- [ ] DataStore foundation
- [ ] Error/result model
- [ ] Basic logging
- [ ] Unit tests for ToolRegistry
- [ ] Unit tests for capability resolution
- [ ] Focused UI/navigation validation

Release gate:
- [ ] Foundation architecture reviewed
- [ ] No feature directly depends on Shizuku/ADB/root
- [ ] App launches and core navigation works
- [ ] `v0.1.0` tagged
- [ ] CHANGELOG updated

---

# Journey 7 - v0.2.0 Device Inspector

- [ ] Device/build information
- [ ] Android/API/security patch information where exposed
- [ ] CPU/ABI
- [ ] RAM
- [ ] Storage
- [ ] Display
- [ ] Battery
- [ ] Sensor inventory
- [ ] Camera capability summary
- [ ] Connectivity/hardware summary
- [ ] Copy/share useful device summary
- [ ] Unsupported fields handled correctly
- [ ] Real-device verification

Release gate:
- [ ] Device Inspector works on supported Android versions
- [ ] No unavailable values are fabricated
- [ ] `v0.2.0` tagged
- [ ] CHANGELOG updated

---

# Journey 8 - v0.3.0 Phone Doctor Core

- [ ] DiagnosticSession engine
- [ ] Diagnostic result model
- [ ] Display color test
- [ ] Dead-pixel test
- [ ] Touch grid
- [ ] Multitouch test
- [ ] Vibration/haptics
- [ ] Accelerometer
- [ ] Gyroscope
- [ ] Proximity
- [ ] Light sensor
- [ ] Magnetometer
- [ ] PASS state
- [ ] FAIL state
- [ ] NOT TESTED state
- [ ] NOT SUPPORTED state
- [ ] PERMISSION REQUIRED state
- [ ] UNKNOWN state

Release gate:
- [ ] Guided diagnostic flow works end-to-end for core tests
- [ ] `v0.3.0` tagged
- [ ] CHANGELOG updated

---

# Journey 9 - v0.4.0 Phone Doctor Hardware

- [ ] Speaker test
- [ ] Microphone test
- [ ] Front camera
- [ ] Rear camera
- [ ] Flash
- [ ] Wi-Fi capability check
- [ ] Bluetooth capability check
- [ ] Location/GPS capability check
- [ ] NFC capability check
- [ ] USB capability check
- [ ] Charging/battery diagnostic
- [ ] Full Diagnostic sequencing
- [ ] Skip/retry behavior
- [ ] Diagnostic summary

Release gate:
- [ ] Full diagnostic completes without crashes when hardware is missing
- [ ] `v0.4.0` tagged
- [ ] CHANGELOG updated

---

# Journey 10 - v0.5.0 Diagnostic Reports

- [ ] Persist sessions
- [ ] Persist individual results
- [ ] Capture DeviceSnapshot
- [ ] Diagnostic history
- [ ] Report detail screen
- [ ] Technician/user notes
- [ ] Report generation
- [ ] PDF or selected export format
- [ ] Share flow
- [ ] Delete report/session
- [ ] Privacy review
- [ ] Export verification

Release gate:
- [ ] A complete diagnostic can be saved and exported
- [ ] `v0.5.0` tagged
- [ ] CHANGELOG updated

---

# Journey 11 - v0.6.0 App Inspector

- [ ] Package list
- [ ] User/system filtering
- [ ] Search
- [ ] Package details
- [ ] Version information
- [ ] Min/target SDK where accessible
- [ ] Activities
- [ ] Services
- [ ] Receivers
- [ ] Providers
- [ ] Permission inspection
- [ ] APK metadata/path where accessible
- [ ] Launch supported app
- [ ] Supported APK export if approved/allowed
- [ ] Package visibility compatibility handling

Release gate:
- [ ] Modern Android package-visibility behavior is handled correctly
- [ ] `v0.6.0` tagged
- [ ] CHANGELOG updated

---

# Journey 12 - v0.7.0 USB Lab

- [ ] USB Host support detection
- [ ] Device attach/detach handling
- [ ] USB permission flow
- [ ] VID
- [ ] PID
- [ ] Manufacturer where exposed
- [ ] Product where exposed
- [ ] Serial where authorized/exposed
- [ ] Interfaces
- [ ] Endpoints
- [ ] Accessible descriptors
- [ ] Copy/export summary
- [ ] Unsupported-device handling
- [ ] Test with multiple USB device classes

Release gate:
- [ ] USB Lab does not claim ChipGenius-level NAND/controller access
- [ ] `v0.7.0` tagged
- [ ] CHANGELOG updated

---

# Journey 13 - v0.8.0 NFC and Network Lab

## NFC
- [ ] NFC hardware detection
- [ ] NFC enabled-state flow
- [ ] Tag scan
- [ ] Technology detection
- [ ] NDEF parsing
- [ ] URI/text record display
- [ ] Supported NDEF writing
- [ ] Supported erase/format behavior
- [ ] Confirmation before mutating a tag
- [ ] Export/copy record details

## Network
- [ ] Connection information
- [ ] IP information allowed by Android
- [ ] Ping
- [ ] DNS lookup
- [ ] Basic connectivity diagnostics
- [ ] Wi-Fi restrictions handled by API/version

Release gate:
- [ ] NFC operations tested with representative tags
- [ ] Network tools do not overpromise restricted identifiers
- [ ] `v0.8.0` tagged
- [ ] CHANGELOG updated

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
