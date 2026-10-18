# DroidKit - Development Roadmap and Initial Backlog

## Milestone M0 - Foundation
DK-001 Create native Kotlin/Compose project
DK-002 Configure Gradle Kotlin DSL
DK-003 Configure Hilt
DK-004 Create core design system
DK-005 Configure Navigation Compose
DK-006 Create ToolRegistry
DK-007 Create Capability model
DK-008 Create CapabilityManager
DK-009 Create StandardAndroidProvider
DK-010 Create PermissionManager
DK-011 Configure Room
DK-012 Configure DataStore
DK-013 Create hardware/API feature detector
DK-014 Build Home shell
DK-015 Build Tools search/category shell
DK-016 Build Settings shell

Exit: app launches, navigates, detects device capabilities, and lists registered tools.

## Milestone M1 - Device Inspector
DK-100 Device/build information
DK-101 CPU/ABI
DK-102 memory
DK-103 storage
DK-104 display
DK-105 battery
DK-106 sensor inventory
DK-107 camera capability summary
DK-108 connectivity/hardware summary

Exit: useful device report on a standard non-rooted phone.

## Milestone M2 - Phone Doctor
DK-200 DiagnosticSession engine
DK-201 Display test
DK-202 Touch grid
DK-203 Multitouch
DK-204 Speaker
DK-205 Microphone
DK-206 Vibration
DK-207 Front/rear camera
DK-208 Flash
DK-209 Proximity
DK-210 Light sensor
DK-211 Accelerometer
DK-212 Gyroscope
DK-213 Magnetometer
DK-214 Connectivity capability checks
DK-215 Charging/battery test
DK-216 Summary/review

Exit: complete guided diagnostic session.

## Milestone M3 - Reports
DK-300 Persist sessions/results
DK-301 Diagnostic history
DK-302 Device snapshot
DK-303 Report layout
DK-304 PDF/export/share flow
DK-305 Report privacy review

## Milestone M4 - App Inspector
DK-400 Package list
DK-401 Filters
DK-402 Package detail
DK-403 Components
DK-404 Permissions
DK-405 APK metadata
DK-406 Standard actions

## Milestone M5 - USB and NFC
DK-500 USB detection/permission flow
DK-501 USB device metadata
DK-502 Interfaces/endpoints/descriptors
DK-510 NFC state/scan
DK-511 NDEF parser
DK-512 Supported write actions
DK-513 NFC export

## Milestone M6 - Shizuku
DK-600 Add Shizuku dependency after current docs/API review
DK-601 Provider lifecycle
DK-602 Permission/setup flow
DK-603 Capability mapping
DK-604 Error/reconnect handling
DK-605 First elevated read-only tool

## Milestone M7 - Advanced App Controls
DK-700 AppOps inspection/control where supported
DK-701 Elevated package actions
DK-702 Advanced permission actions
DK-703 Risk confirmations/audit history

## Milestone M8 - Wireless ADB
DK-800 Research/select ADB implementation
DK-801 Pairing UX
DK-802 Session manager
DK-803 Secure key storage
DK-804 Structured action executor
DK-805 Advanced terminal only if product scope approves it

## Milestone M9 - Developer Lab
Intents, activities, deep links, display metrics, logs where authorized, developer shortcuts.

## Milestone M10 - Release
Compatibility matrix
Accessibility
Performance
Crash handling
Privacy/security review
Real-device testing
Beta
Release

## Validation Rule
For implementation tasks, first identify changed files/features and validate those areas. Do not automatically run the entire test suite or broad static analysis unless the change affects broader integration or there is a specific reason.
