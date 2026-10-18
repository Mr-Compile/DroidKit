# DroidKit Product Bible

Version: Planning Baseline 1.0
Purpose: Canonical product vision and feature universe for DroidKit.

> This document answers: **What is DroidKit, who is it for, what should it become, and which ideas belong to V1, post-V1, experimental, or rejected scope?**
>
> It is intentionally separate from the Development Playbook, which answers **how and when to build it**.

---

# 1. Product Identity

## Working Name
DroidKit

## Product Description
DroidKit is a native Android technician, diagnostics, developer, and power-user toolkit. It combines trustworthy hardware diagnostics, device inspection, application/package inspection, USB/NFC/network utilities, diagnostic reporting, and optional advanced capabilities through Shizuku, Wireless ADB, and potentially root in later versions.

## One-Line Positioning
**A modular Android toolbox for inspecting, diagnosing, testing, and managing Android devices—from normal Android access to optional advanced capabilities.**

## Product Character
DroidKit should feel:
- Technical but understandable
- Trustworthy
- Fast
- Local-first
- Transparent about limitations
- Useful without root
- Modular rather than bloated
- Safe when performing system-changing operations

---

# 2. Target Users

## Phone Technicians
Use DroidKit to inspect used/repaired devices, test hardware, diagnose common issues, inspect USB/NFC behavior, and generate reports.

## Used-Phone Buyers and Sellers
Run guided tests before buying/selling a device and preserve a diagnostic summary.

## Android Power Users
Inspect applications, permissions, packages, hardware, network information, and optional elevated controls.

## Android Developers
Inspect packages/components, test intents/deep links, view device/display information, and access developer-focused utilities.

## IT / Technical Support
Use repeatable device inspection and diagnostic workflows without needing many unrelated apps.

---

# 3. Problems DroidKit Solves

Android technical workflows are fragmented across:
- Settings screens
- Separate diagnostic apps
- Package managers
- NFC utilities
- USB utilities
- ADB commands
- Shizuku-based utilities
- Developer tools

DroidKit should consolidate the useful parts into a coherent toolbox while clearly showing which capabilities are available on the current device.

---

# 4. Product Principles

1. **Useful without elevated privileges.**
2. **Native Android first.**
3. **Capabilities, not privilege backends.**
4. **Never fabricate unavailable information.**
5. **Unsupported is different from failed.**
6. **Permissions are requested contextually.**
7. **Dangerous operations explain consequences.**
8. **Local-first by default.**
9. **Hardware and Android-version differences are first-class states.**
10. **A tool should solve a real problem, not exist only to make the feature list longer.**
11. **Advanced features must not make basic features unstable.**
12. **DroidKit is not a security-bypass or exploitation toolkit.**

---

# 5. Capability Philosophy

DroidKit tools depend on capabilities rather than directly on a privilege technology.

```text
Tool
  ↓
Required Capability
  ↓
CapabilityManager
  ↓
Available Provider
  ├── Standard Android
  ├── Shizuku
  ├── Wireless ADB
  └── Root (future/optional)
```

Examples:

```text
NFC Reader
Requires: NFC_HARDWARE + supported NFC APIs
Provider: Standard Android

AppOps Manager
Requires: APP_OPS_CONTROL
Possible providers: Shizuku / ADB / Root

Advanced Package Control
Requires: ADVANCED_PACKAGE_CONTROL
Possible providers: Shizuku / ADB / Root
```

A feature must not be written as "a Shizuku feature" if its actual requirement is a capability that another provider could satisfy.

---

# 6. Complete Feature Universe

The names below are conceptual product areas. Some may be merged into DroidKit categories rather than appearing as separate branded modules.

## DeviceScope / Device Inspector
- Manufacturer/model
- Device/build information
- Android version
- API level
- Security patch where exposed
- CPU/ABI
- RAM
- Storage
- Display metrics
- Battery information
- Sensors
- Camera capabilities
- Connectivity/hardware features
- Copy/share device summary

## PhoneDoctor
- Full guided diagnostic
- Quick diagnostic tests
- Display colors
- Dead-pixel visual inspection
- Touch grid
- Multitouch
- Speaker
- Microphone
- Vibration
- Front/rear camera
- Flash
- Proximity
- Light sensor
- Accelerometer
- Gyroscope
- Magnetometer
- Charging/battery observations
- Connectivity capability checks
- Result history
- Diagnostic reports

## AppControl / App Inspector
- Installed application list as permitted by Android
- User/system classification where reliable
- Disabled state where visible
- Package metadata
- Version information
- min/target SDK where exposed
- APK/source metadata
- Activities
- Services
- Receivers
- Providers
- Requested permissions
- Launch supported apps/components
- APK export only where technically/legal/product-policy appropriate
- Elevated package controls in later versions

## PermissionX / Permission Lab
- Permission inspection
- Runtime permission status where accessible
- Special-permission guidance
- AppOps inspection in advanced versions
- Approved AppOps changes through elevated capability providers
- Permission-related troubleshooting

## ADB Pocket
- Wireless Debugging setup/pairing
- Secure ADB session
- Structured device actions
- Package actions
- Device property inspection
- Authorized logging tools
- Carefully scoped system actions
- Optional advanced terminal only if separately approved

## NetScope / Network Lab
- Active connection summary
- IP information permitted by Android
- Ping
- DNS lookup
- Connectivity test
- Wi-Fi information permitted by OS/version/permissions
- Network monitoring concepts where technically justified

## IntentLab
- Intent launcher
- Activity launcher
- Deep-link tester
- URI testing
- Component inspection
- Developer troubleshooting

## SensorLab
- Sensor inventory
- Live sensor values
- Sensor availability
- Sensor-specific diagnostics
- Sampling/visualization where useful

## APK Inspector
- APK metadata
- Package name
- Version
- SDK information
- Declared components
- Requested permissions
- Manifest-related information available through supported parsing
- Signing/certificate information where supported and useful
- No unauthorized code tampering or bypass functionality

## LogScope
- DroidKit application logs
- Device/system logs only where Android/elevated access authorizes them
- Filtering
- Export for troubleshooting
- Privacy-aware redaction where appropriate

## SettingsX
- Searchable shortcuts into supported Android settings
- Device configuration discovery
- Developer-oriented settings shortcuts
- No hidden-setting claims unless verified

## RepairDesk Mobile
Potential technician workflow:
- Start customer/device diagnostic session
- Device snapshot
- Guided tests
- Technician notes
- Repair-before/after comparison
- Exportable report
- Optional future shop/customer features only if justified

## BatteryLab
- Level
- Charging state
- Temperature
- Voltage/current metrics where exposed
- Charging source/status where available
- Battery observations over a diagnostic session
- Never invent a universal battery-health percentage

## StorageScope
- Storage totals/availability
- Storage categories only where reliable
- Volume information
- Storage troubleshooting
- No fake "cleaner/booster" claims

## DisplayDoctor
- Display metrics
- Resolution/density
- Refresh information where exposed
- Color test
- White/black test
- Dead-pixel visual inspection
- Touch overlay/testing

## USB Doctor / USB Lab
- USB Host support
- Connected-device detection
- Attach/detach
- Android USB permission
- VID/PID
- Device class/subclass/protocol where exposed
- Manufacturer/product/serial where exposed
- Interfaces
- Endpoints
- Accessible descriptors
- Copy/export summary

Hard boundary: standard Android USB access must not be marketed as equivalent to ChipGenius or low-level NAND/controller diagnostic tools.

## NFC Lab
- NFC hardware/state
- Tag detection
- Tag technology list
- NDEF detection
- Text records
- URI records
- Raw NDEF record representation
- Supported write
- Supported erase/format
- Copy/export
- Clone NDEF content only where technically supported and appropriate; do not imply cloning secure credentials/cards.

## Bluetooth Lab
- Bluetooth hardware/state
- Paired/bonded information permitted by Android
- BLE scanning with appropriate permissions
- Device/service information where exposed
- Connectivity diagnostics
- Advanced tooling only after feasibility review

## DevPocket / Developer Lab
- IntentLab
- Activity launcher
- Deep-link tester
- Package/component inspector
- Display information
- Developer setting shortcuts
- Logging where authorized
- Device properties
- Useful developer diagnostics

---

# 7. V1 Product Scope

V1 should prove DroidKit is useful on a normal, non-rooted phone.

## V1 Core
- Design system
- Navigation
- Tool Registry
- Capability Manager
- Standard Android provider
- Permission Manager
- Hardware/API detection
- Search
- Favorites/recent tools where approved
- Room
- DataStore

## V1 Device Inspector
- Build/OS
- CPU/ABI
- Memory
- Storage
- Display
- Battery
- Sensor inventory
- Camera capability summary
- Hardware/connectivity capability summary

## V1 Phone Doctor
- Display
- Dead-pixel visual test
- Touch
- Multitouch
- Speaker
- Microphone
- Vibration
- Camera
- Flash
- Supported sensors
- Connectivity capability checks
- Charging/battery observations

## V1 Reports
- Diagnostic session
- Device snapshot
- Individual results
- Notes
- History
- Export/share

## V1 App Inspector
- Visible packages
- Metadata
- Components
- Permissions
- APK/source metadata where accessible
- Safe standard actions

## V1 USB Lab
- Android USB Host inspection features

## V1 NFC Lab
- Supported NFC/NDEF inspection and writing

## V1 Network Lab
- Basic diagnostics that normal Android access reliably supports

---

# 8. Post-V1 Product Scope

Candidate post-V1 areas:
- Shizuku provider
- AppOps inspection/control
- Elevated package actions
- Advanced permission controls
- Wireless ADB
- Advanced logs
- IntentLab/Developer Lab expansion
- Bluetooth Lab expansion
- Additional technician workflows

These are candidates, not automatic promises. Each requires a new feasibility and security review.

---

# 9. Future / Experimental Ideas

- Optional root provider
- Extension/plugin system
- Repair-shop workflow
- Multi-device history
- Before/after repair reports
- Optional encrypted synchronization
- Team/shop features
- Desktop companion
- Advanced device inventory
- Additional supported USB device protocols
- Additional sensor visualization
- Technician presets/checklists

Future ideas belong here until formally promoted into a release scope.

---

# 10. Rejected / Out-of-Scope Direction

DroidKit should not become:
- FRP bypass software
- Lock-screen/password bypass software
- Credential extraction software
- Stealth surveillance software
- Malware/persistence toolkit
- Exploit delivery platform
- Unauthorized remote-control system
- Fake optimizer/RAM booster
- Misleading battery-health estimator
- Universal firmware flashing/unbricking suite
- Tool that claims inaccessible hardware information as fact

---

# 11. Diagnostic Result Model

Every diagnostic must use explicit states:

- PASS
- FAIL
- NOT_TESTED
- NOT_SUPPORTED
- PERMISSION_REQUIRED
- UNKNOWN

Examples:
- Device has no gyroscope -> NOT_SUPPORTED
- User denied microphone permission -> PERMISSION_REQUIRED
- User did not run speaker test -> NOT_TESTED
- User confirms speaker output is distorted -> FAIL
- Test cannot reliably determine outcome -> UNKNOWN

Do not convert these into FAIL simply to produce a score.

---

# 12. Full Diagnostic Experience

```text
Start Full Diagnostic
        ↓
Create DiagnosticSession
        ↓
Capture DeviceSnapshot
        ↓
Display
        ↓
Touch / Multitouch
        ↓
Audio / Microphone
        ↓
Vibration
        ↓
Camera / Flash
        ↓
Sensors
        ↓
Connectivity Capabilities
        ↓
Battery / Charging
        ↓
Review
        ↓
Technician/User Notes
        ↓
Save
        ↓
Report / Export / Share
```

Tests should explain what the user is supposed to observe.

---

# 13. Technician Workflow

Potential workflow:

```text
Receive Device
  ↓
Open DroidKit
  ↓
Capture Device Snapshot
  ↓
Run Full Diagnostic
  ↓
Record Failures / Notes
  ↓
Perform Repair Outside DroidKit
  ↓
Run Relevant Tests Again
  ↓
Generate Final Report
```

Future RepairDesk functionality may compare before/after sessions.

---

# 14. Used-Phone Inspection Workflow

```text
Open DroidKit
  ↓
Device Inspector
  ↓
Full Diagnostic
  ↓
Check Display / Touch / Audio / Camera / Sensors
  ↓
Check Battery Observations
  ↓
Check Connectivity
  ↓
Review Summary
  ↓
Save/Share Report
```

DroidKit should avoid presenting the report as a guarantee of device condition. It records tests and observable information.

---

# 15. Power-User Workflow

```text
Tools
  ↓
Search by task
  ↓
Inspect Device/App/Permission
  ↓
CapabilityManager checks access
  ↓
Use Standard provider where possible
  ↓
Optional Shizuku/ADB provider when advanced access is required
```

---

# 16. Developer Workflow

Developer-oriented tools may include:
- Package/component inspection
- Activity launcher
- Intent/deep-link testing
- Display metrics
- Device/build information
- Authorized logs
- Network diagnostics
- USB/NFC testing

These should remain discoverable within the same Tool Registry rather than becoming a disconnected second app.

---

# 17. Tool Registry Vision

Every tool declares:
- Stable ID
- Name
- Description
- Category
- Icon
- Route
- Search keywords
- Minimum Android API
- Hardware requirements
- Permission requirements
- Capability requirements
- Risk level
- Availability state

The registry powers:
- Search
- Categories
- Favorites
- Recent tools
- Availability badges
- Future command palette/quick actions

---

# 18. Availability UX

Possible tool states:
- AVAILABLE
- PERMISSION REQUIRED
- SETUP REQUIRED
- HARDWARE NOT AVAILABLE
- UNSUPPORTED ANDROID VERSION
- TEMPORARILY UNAVAILABLE
- ERROR

Never show a permanently disabled control without an explanation.

---

# 19. Privilege Providers

## Standard Android
Primary provider and the foundation of V1.

## Shizuku
Post-V1 provider for selected privileged operations. DroidKit must continue functioning if Shizuku is missing, stopped, or permission is denied.

## Wireless ADB
Post-V1 provider. Prefer structured actions rather than exposing raw shell commands as the default product experience.

## Root
Optional future provider. Must never be required for normal DroidKit use.

---

# 20. Risk Model

## Level 0 — Safe
Read-only information and diagnostics.

## Level 1 — Minor
Opening settings, launching supported components.

## Level 2 — System Change
Supported setting, permission, or AppOps changes.

## Level 3 — High Impact
Disable package, clear data, uninstall, or similarly destructive actions.

## Level 4 — Advanced
Root/system operations.

The UI should scale confirmation strength with risk.

---

# 21. Data Model Vision

Potential Room entities:
- DiagnosticSession
- DiagnosticResult
- DeviceSnapshot
- SavedReport

Potential local preference data:
- Theme
- Onboarding
- Favorites
- Recent-tool preferences
- Advanced setup preferences
- Report preferences

Data models should evolve only when a real feature needs them.

---

# 22. Report Vision

A report may contain:
- DroidKit version
- Test date/time
- Device model/manufacturer
- Android/API
- Relevant device snapshot
- Test categories
- Individual test results
- Unsupported/skipped tests
- Notes
- Summary counts

Reports should clearly distinguish measured facts from user-observed test outcomes.

---

# 23. UI Vision

Primary navigation:

```text
Home | Tools | Diagnose | Settings
```

## Home
- Device identity
- Capability/access status
- Quick tools
- Full Diagnostic
- Recent tools/results

## Tools
- Search
- Categories
- Favorites
- Availability indicators

## Diagnose
- Full Diagnostic
- Quick tests
- History
- Reports

## Settings
- Appearance
- Permissions/setup
- Advanced access
- Safety
- Data/reports
- About

---

# 24. Design Foundation

DroidKit should use a reusable Compose design system rather than one-off screen styling.

Required foundations:
- Typography
- Spacing
- Shape/radius
- Surface hierarchy
- Semantic state tokens
- Light/dark themes
- Icons
- Tool cards
- Information rows
- Status badges
- Empty/error/unsupported states
- Risk dialogs
- Diagnostic controls

Accessibility:
- Adequate touch targets
- Dynamic type/font scaling
- Semantic labels
- Logical focus
- Contrast
- No color-only meaning

---

# 25. Architecture Vision

```text
Compose UI
   ↓
ViewModel
   ↓
Use Cases
   ↓
Repository Interfaces
   ↓
Data Sources / Android Services
   ↓
Capability Providers
```

Suggested module families:
- app
- core
- privilege
- feature

No feature should own privilege-specific architecture.

---

# 26. Technology Direction

- Kotlin
- Jetpack Compose
- MVVM + Clean Architecture
- Hilt
- Coroutines + Flow
- Room
- DataStore
- Navigation Compose
- WorkManager only when justified
- Kotlin Serialization where useful
- OkHttp for network tools
- CameraX/CameraManager
- Android USB Host APIs
- Android NFC APIs
- Android Bluetooth APIs
- Android Sensor APIs

Exact dependency versions, targetSdk, compileSdk, and implementation APIs must be checked at development time.

---

# 27. Version Vision

Proposed development sequence:

```text
v0.1.0 Foundation
v0.2.0 Device Inspector
v0.3.0 Phone Doctor Core
v0.4.0 Hardware Diagnostics
v0.5.0 Reports
v0.6.0 App Inspector
v0.7.0 USB Lab
v0.8.0 NFC + Network
v0.9.0-alpha.N
v1.0.0-beta.N
v1.0.0-rc.N
v1.0.0 Stable
```

Post-V1 version numbers should be assigned when scope is approved rather than promised far in advance.

---

# 28. Long-Term Product Vision

DroidKit should grow by adding verified tools to a stable platform rather than repeatedly redesigning the application.

Long-term structure:

```text
DroidKit Core
   │
   ├── Device & Hardware
   ├── Diagnostics
   ├── Applications & Packages
   ├── Permissions
   ├── USB
   ├── NFC
   ├── Bluetooth
   ├── Network
   ├── Developer Tools
   ├── Reports
   │
   └── Capability Engine
          ├── Standard Android
          ├── Shizuku
          ├── Wireless ADB
          └── Root (optional)
```

The core should be stable enough that adding a new tool generally means:
1. Research feasibility.
2. Define ToolDefinition.
3. Define required capability.
4. Implement data/domain/UI.
5. Add tests.
6. Register tool.
7. Release.

---

# 29. Product Decision Rules

When considering a new idea, ask:

1. Does it solve a real technician/power-user/developer problem?
2. Can Android reliably support it?
3. Does it require elevated access?
4. Can it degrade gracefully?
5. What permissions are required?
6. What data does it expose/store?
7. What is its risk level?
8. Can it be tested?
9. Does it belong in DroidKit or a separate product?
10. Which release should own it?

Possible decisions:
- ACCEPT V1
- ACCEPT POST-V1
- EXPERIMENTAL
- DEFER
- REJECT

---

# 30. Source-of-Truth Relationship

The DroidKit project should maintain three distinct layers:

```text
PRODUCT BIBLE
What DroidKit is, who it serves, and the complete idea universe.
        ↓
DEVELOPMENT PLAYBOOK
How, when, and in what order DroidKit is built.
        ↓
AGENTS.md
How the coding AI must behave while working on the repository.
```

Priority during implementation:
1. Verified current Android behavior and repository reality
2. Safety/security requirements
3. Frozen release scope
4. Architecture/design foundations
5. Product Bible
6. Future ideas

The Product Bible does not override verified platform limitations.

---

# 31. Product Bible Maintenance Rules

When a new idea appears:
- Add it to Future/Experimental first.
- Research feasibility.
- Decide scope.
- Promote it only after approval.

When an idea is rejected:
- Keep a short record explaining why so another AI does not repeatedly reintroduce it.

When Android changes:
- Update feasibility documentation.
- Do not silently rewrite the product promise.

When a release is frozen:
- New ideas go to the next release/future list unless the freeze is explicitly reopened.

---

# 32. Product Bible Checklist

## Identity
- [x] Product concept defined
- [x] Target users defined
- [x] Product principles defined
- [x] Capability philosophy defined

## Feature universe
- [x] Device tools captured
- [x] Diagnostics captured
- [x] App/package tools captured
- [x] Permission/AppOps direction captured
- [x] USB captured
- [x] NFC captured
- [x] Network captured
- [x] Bluetooth direction captured
- [x] Developer tools captured
- [x] Reports captured
- [x] Shizuku direction captured
- [x] Wireless ADB direction captured
- [x] Optional root direction captured
- [x] RepairDesk/future technician direction captured

## Scope
- [x] V1 proposed scope recorded
- [x] Post-V1 scope recorded
- [x] Future ideas recorded
- [x] Rejected/out-of-scope direction recorded
- [ ] V1 feasibility audit completed
- [ ] V1 scope formally frozen

## Implementation handoff
- [x] Product Bible created
- [x] Development Playbook created
- [x] AGENTS.md rules created
- [ ] Product Bible copied into repository docs
- [ ] Coding AI instructed to use the source-of-truth hierarchy

---

# Final Product Statement

DroidKit is intended to become a trustworthy Android technical toolbox rather than a collection of gimmicks. Its value comes from combining reliable device information, guided diagnostics, inspection tools, technician reporting, and progressively more powerful capability providers behind one consistent architecture and user experience.

The product should always prefer **accurate, explainable, supported behavior** over impressive-looking but unreliable claims.
