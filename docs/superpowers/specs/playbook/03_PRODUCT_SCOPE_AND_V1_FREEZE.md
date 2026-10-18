# Product Scope and V1 Freeze

## Vision
DroidKit is a local-first native Android toolkit for technicians, power users, developers, and support staff.

## Core principles
- Useful without elevated privileges.
- Native Kotlin + Jetpack Compose.
- Capability-based privilege architecture.
- Honest diagnostics: unavailable is not failed.
- Safe-by-default actions.
- Modular tools and searchable registry.
- No cloud/backend requirement for V1.

## V1 proposed modules
### Core
ToolRegistry, CapabilityManager, StandardAndroidProvider, PermissionManager, navigation, search, design system, Room, DataStore.

### Device Inspector
Build/OS, CPU/ABI, memory, storage, display, battery, sensors, camera capability, hardware/connectivity summary.

### Phone Doctor
Display, touch, multitouch, audio, microphone, vibration, camera, flash, supported sensors, connectivity capability checks, charging/battery observations.

### Reports
Diagnostic sessions, snapshots, history, notes, export/share.

### App Inspector
Visible installed packages, package metadata, components, permissions, APK metadata and safe standard actions.

### USB Lab
Host detection, device permission, VID/PID, exposed metadata, interfaces, endpoints, accessible descriptors.

### NFC Lab
Hardware/state, technologies, NDEF read, supported write/erase/format, export.

### Network Lab
Basic connection/IP information permitted by Android, ping, DNS lookup, connectivity diagnostics.

## Explicitly post-V1
- Shizuku privileged tools
- AppOps mutation
- Advanced package control
- Wireless ADB
- Advanced logs
- Root provider

## Non-goals
- FRP/credential/lock bypass
- Exploit framework
- Manufacturer flashing/unbricking suite
- Fake RAM/cleaner/booster claims
- ChipGenius-equivalent NAND internals when Android does not expose them
- Remote surveillance/control platform

## Freeze procedure
Before marking V1 frozen:
- [ ] Every V1 tool has a feasibility record.
- [ ] Every V1 tool has permission/hardware/API requirements.
- [ ] Every V1 tool has unsupported/failure UX.
- [ ] V1 UI screen inventory is complete.
- [ ] V1 effort is accepted.
- [ ] Post-V1 items are removed from V1 backlog.
- [ ] `V1_SCOPE_FROZEN = TRUE` recorded by project owner.
