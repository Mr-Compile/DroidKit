# DroidKit - Product Requirements Document (PRD)

## Product
DroidKit - Android Technician & Power Toolkit

## Problem
Android users and technicians often need separate apps or terminal commands for diagnostics, package inspection, USB/NFC inspection, permissions, ADB operations, and device information. DroidKit should provide a coherent toolkit with clear capability requirements and safe UX.

## Goals
- Provide useful diagnostics with normal Android access.
- Provide deep device/app inspection without requiring root.
- Add optional Shizuku and Wireless ADB capabilities.
- Produce useful technician diagnostic reports.
- Keep all V1 data local by default.
- Make each tool discoverable through a searchable tool registry.

## Non-Goals for V1
- Full device flashing/unbricking suite
- Manufacturer-specific bootloader exploitation
- FRP/security bypass functionality
- Cloud account platform
- Remote device control platform
- Root requirement
- Unsupported claims such as reading flash-controller internals when Android does not expose them

## V1 Proposed Scope
### Core
- Compose design system
- Navigation
- Tool Registry
- Capability Manager
- Permission Manager
- Hardware/feature detector
- Search
- Favorites/recent tools
- Room + DataStore foundation

### Device Inspector
- Device/model/build
- Android/API/security patch where exposed
- CPU/ABI
- Memory
- Storage
- Display metrics
- Battery information
- Sensors list
- Camera capabilities summary
- Connectivity/hardware feature summary

### Phone Doctor
- Display color/dead-pixel test
- Touch grid
- Multitouch
- Speaker test
- Microphone test
- Vibration/haptics
- Front/rear camera
- Flash
- Proximity/light/accelerometer/gyroscope/magnetometer where present
- Wi-Fi/Bluetooth/NFC/GPS/USB capability checks
- Charging/battery observations
- PASS / FAIL / NOT TESTED / NOT SUPPORTED / PERMISSION REQUIRED / UNKNOWN result states

### Reports
- Diagnostic session history
- Device snapshot
- Individual test results
- Notes
- Export/share report

### App Inspector
- User/system apps
- Package metadata
- Version/min/target SDK where accessible
- Components
- Declared/requested permissions
- Launch app
- APK path/metadata
- Supported APK export where allowed

### USB Lab
- Connected USB devices
- VID/PID
- Manufacturer/product/serial where exposed and authorized
- Interfaces/endpoints
- Accessible descriptor information

### NFC Lab
- Detect tag
- Technologies
- NDEF records
- Read
- Supported write
- Supported erase/format actions
- Export record details

## V2 / Advanced
- Shizuku provider
- AppOps manager
- Elevated package controls
- Advanced permission controls
- Wireless ADB pairing/session engine
- ADB action library
- Logcat/log viewer where authorized
- Advanced developer utilities

## Future / Experimental
- Optional root provider
- Repair-shop cloud synchronization
- Multi-device technician inventory/history
- Plugins/extensions if a stable internal tool contract is established

## Functional Requirement Pattern
Every tool must define:
- Tool ID
- Name and description
- Category
- Route
- Minimum Android/API
- Required hardware
- Runtime permissions
- Required capabilities
- Risk level
- Availability state
- Unsupported reason
- Result/output model

## Quality Requirements
- No fake "RAM booster" or misleading optimization claims.
- Unsupported values are labeled unavailable rather than guessed.
- Destructive actions are reversible where possible and clearly explained.
- The app remains usable without Shizuku/ADB/root.
- Tools should be independently testable.
- Compatibility behavior must be explicit by Android version.

## Success Criteria for MVP
A user can install DroidKit on a non-rooted phone, inspect the device, run a guided diagnostic session, save results, and generate a useful report without Shizuku or ADB.
