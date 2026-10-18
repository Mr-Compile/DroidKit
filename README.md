# DroidKit

A native Android toolbox for inspecting, diagnosing, and managing Android devices. Built for phone technicians, used-phone buyers, power users, and developers, and designed to be useful on a normal non-rooted phone.

## Status

Planning and specification are complete: PRD approved, V1 scope frozen, architecture ratified. No app code has landed yet; repository bootstrap is the current milestone. Progress is tracked in the [Master Journey Checklist](docs/superpowers/specs/10_DroidKit_Master_Journey_Checklist.md). Releases will be published through GitHub Releases.

## What it does

V1 ships a set of tools that work with standard Android permissions:

- **Device Inspector**: build, CPU/ABI, memory, storage, display, battery, sensors, camera and connectivity capabilities.
- **Phone Doctor**: guided hardware diagnostics covering display and dead-pixel checks, touch and multitouch, speaker, microphone, vibration, camera, flash, sensors, and charging observation.
- **Reports**: save diagnostic sessions with a device snapshot and technician notes, then export or share them.
- **App Inspector**: installed packages, metadata, components, requested permissions, APK info.
- **Permission Center**: inspect runtime permission status and get guidance for special access.
- **Labs**: Battery, Storage, Sensor (inventory and live values), USB host device inspection, NFC tag read/write, and Network tools (connection info, ping, DNS).

Optional in V1: basic Bluetooth info and a small Developer Lab (intent launcher, display metrics).

## How tools get access

Every tool declares what it needs: hardware features, permissions, and capabilities such as `APP_OPS_CONTROL`. A `CapabilityManager` resolves each requirement against the available providers:

```text
Tool -> Required Capability -> CapabilityManager -> Provider
                                                   ├── Standard Android
                                                   ├── Shizuku        (post-V1)
                                                   ├── Wireless ADB   (post-V1)
                                                   └── Root           (future, optional)
```

Standard Android is the primary provider. Shizuku and wireless ADB arrive after V1 as optional upgrades for tools that need elevated access. When a provider is missing, the tool shows an availability state instead of breaking the app.

## Honest diagnostics

Every test reports an explicit state: `PASS`, `FAIL`, `NOT_TESTED`, `NOT_SUPPORTED`, `PERMISSION_REQUIRED`, or `UNKNOWN`. A phone without a gyroscope reports `NOT_SUPPORTED`, not `FAIL`. DroidKit never fabricates values the hardware or API does not expose, and tests that rely on human observation ask the user to confirm the result.

## Safety boundaries

Mutating actions carry a risk level (0-4), and high-impact actions require explicit confirmation with stated consequences. Permanently out of scope: FRP or lock bypass, credential extraction, stealth surveillance, exploit delivery, fake cleaner/booster claims, and invented battery-health percentages.

## Tech stack

- Kotlin + Jetpack Compose (Material 3), MVVM + Clean Architecture
- Hilt, Coroutines/Flow, Room, DataStore, Navigation Compose
- Multi-module Gradle build: `app`, `core`, `privilege`, `feature` (31 planned modules)
- minSdk 28, targetSdk 36
- Local-first: no backend, no account, data stays on the device

## Roadmap

| Version | Milestone |
|---------|-----------|
| v0.1    | Foundation: design system, tool registry, capability engine |
| v0.2    | Device Inspector |
| v0.3    | Phone Doctor core |
| v0.4    | Hardware diagnostics |
| v0.5    | Reports |
| v0.6    | App Inspector |
| v0.7    | USB Lab |
| v0.8    | NFC + Network |
| v0.9-alpha | Integrated alpha |
| v1.0    | Stable |

Post-V1 candidates: Shizuku provider, AppOps control, elevated package actions, wireless ADB, advanced logs. Each requires its own feasibility and security review before entering a release scope.

## Documentation

- [Product Bible](docs/superpowers/specs/DROIDKIT_PRODUCT_BIBLE.md): vision, target users, full feature universe
- [Development Playbook](docs/superpowers/specs/playbook/): milestone journeys J01-J20 with gates
- [Engineering Foundation](docs/ENGINEERING_FOUNDATION.md): ratified stack, modules, contracts
- [PRD Approval](docs/PRD_APPROVAL.md): final V1 scope decisions
- [AGENTS.md](AGENTS.md): operating rules for AI-assisted development

## License

[Apache-2.0](LICENSE)
