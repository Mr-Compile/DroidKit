# DroidKit - Product and System Flows

## 1. First Launch
Launch
-> Onboarding
-> Explain Standard vs Advanced capabilities
-> Detect Android version and hardware
-> Request only permissions needed for selected tools
-> Dashboard

Do not request every permission at startup.

## 2. Tool Launch Flow
User selects tool
-> ToolRegistry metadata loaded
-> CapabilityManager evaluates requirements
-> If available: open tool
-> If permission missing: explain + request
-> If hardware absent: show NOT SUPPORTED
-> If elevated provider required: show setup choices
-> If OS unsupported: show compatibility explanation

## 3. Elevated Tool Flow
Select elevated tool
-> Resolve capability
-> Shizuku available?
   -> service running?
   -> DroidKit permission granted?
   -> execute through Shizuku provider
-> otherwise Wireless ADB available?
   -> paired/session valid?
   -> execute through ADB provider
-> otherwise root provider available? (future)
-> otherwise show setup guidance and keep app stable

## 4. Full Diagnostic Flow
Dashboard
-> Diagnose
-> Start Full Diagnostic
-> Create DiagnosticSession
-> Capture DeviceSnapshot
-> Guided tests:
   Display
   Touch
   Multitouch
   Audio
   Microphone
   Vibration
   Cameras
   Flash
   Sensors
   Connectivity
   Charging/Battery
-> User/system records each result
-> Review summary
-> Add technician notes
-> Save
-> Export/share report

Result states:
PASS
FAIL
NOT TESTED
NOT SUPPORTED
PERMISSION REQUIRED
UNKNOWN

## 5. USB Flow
Open USB Lab
-> Check USB host capability
-> Observe connected devices
-> Request device permission where Android requires it
-> Read device metadata
-> List interfaces/endpoints
-> Display descriptors/data that Android exposes
-> Export/copy summary

## 6. NFC Flow
Open NFC Lab
-> Check NFC hardware
-> Check NFC enabled
-> Prompt user to enable if disabled
-> Scan tag
-> Detect tag technologies
-> Parse NDEF if available
-> Display records
-> Optional supported write/erase/format action
-> Confirmation for mutating action
-> Result

## 7. App Inspector Flow
Open Apps
-> Query visible/allowed installed packages
-> Filter User/System/Disabled as supported
-> Select app
-> Package overview
-> Components
-> Permissions
-> APK metadata
-> Standard actions
-> Elevated actions shown only when capability exists

## 8. High-Risk Action Flow
User chooses destructive/system-changing action
-> Risk classification
-> Explain exact consequence
-> Show target package/resource
-> Require explicit confirmation
-> Capability/permission re-check
-> Execute
-> Capture result
-> Show recovery/reversal guidance where applicable

## 9. Search Flow
Tools tab
-> Search query
-> ToolRegistry keyword/category search
-> Results ranked
-> Availability badge:
   Available
   Permission needed
   Shizuku/ADB needed
   Hardware unavailable
   Unsupported Android version
-> Launch selected tool

## 10. Navigation Concept
Bottom navigation:
Home | Tools | Diagnose | Settings

Home:
Device summary, capability status, quick tools, recent tools.

Tools:
Search + categories + favorites.

Diagnose:
Quick tests + Full Diagnostic + history.

Settings:
Appearance, advanced access setup, safety, reports/data, about.
