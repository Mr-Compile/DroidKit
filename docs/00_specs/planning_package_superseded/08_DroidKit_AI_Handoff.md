# DroidKit - AI Handoff / Implementation Context

You are assisting with DroidKit, a native Android toolkit for technicians, developers, and power users.

## Fixed Direction
- Kotlin + Jetpack Compose
- Native Android, not Flutter
- Clean Architecture + MVVM
- Hilt
- Coroutines/Flow
- Room
- DataStore
- Navigation Compose
- Local-first V1
- Shizuku and Wireless ADB are optional capability providers, not hard dependencies
- Root is future/optional

## Core Architectural Rule
Features depend on abstract capabilities, not directly on Shizuku, ADB, or root.

Bad:
Feature screen -> if Shizuku running -> command

Preferred:
Feature -> Use Case -> CapabilityManager -> CapabilityProvider -> implementation

## Product Rule
The application must remain useful on a normal non-rooted Android device.

## Safety Rule
Read-only inspection is preferred. Mutating/high-impact actions require explicit explanation and confirmation. Do not add security bypass, FRP bypass, credential bypass, stealth, persistence, or other abusive functionality.

## V1 Priorities
1. Core architecture
2. Device Inspector
3. Phone Doctor
4. Diagnostic Reports
5. App Inspector
6. USB Lab
7. NFC Lab

Advanced V2:
8. Shizuku
9. AppOps/permission/package advanced controls
10. Wireless ADB
11. Developer Lab

## Implementation Discipline
Before coding a tool:
1. Verify Android API feasibility for targeted versions.
2. Identify permissions/hardware.
3. Define capability requirement.
4. Define risk level.
5. Define unsupported/error states.
6. Add ToolRegistry entry.
7. Implement repository/use case/UI.
8. Test changed area.

## Validation Preference
Only test files/features actually changed by the task. Do not run the full test suite or broad analysis automatically unless broader integration is affected or there is a concrete reason.

## Do Not Assume
- Battery "health percentage" is universally exposed.
- USB flash controller/NAND internals are available through Android USB APIs.
- Package visibility is unrestricted on modern Android.
- Wi-Fi identifiers and network details are always accessible.
- Logcat is freely accessible for other apps.
- A permission or system API behaves identically across OEMs.

Verify these during technical spikes.

## First Coding Milestone
M0 Foundation:
Compose project, Hilt, navigation, design system, ToolRegistry, CapabilityManager, StandardAndroidProvider, PermissionManager, Room/DataStore, hardware/API detection, Home/Tools/Settings shells.

Do not start with Shizuku.
