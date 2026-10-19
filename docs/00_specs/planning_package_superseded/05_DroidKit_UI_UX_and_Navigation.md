# DroidKit - UI/UX and Navigation Plan

## Design Direction
A technical utility should feel precise, fast, and trustworthy. Prefer clear information hierarchy over decorative complexity. Support light and dark themes.

## Main Navigation
Home | Tools | Diagnose | Settings

## Home
- Device identity summary
- Android version
- Capability status:
  Standard active
  Shizuku state
  ADB state
  Root state (future)
- Quick tools
- Full Diagnostic CTA
- Recent tools
- Recent diagnostic result

## Tools
- Search field
- Favorites
- Categories:
  Device
  Diagnostics
  Apps
  Permissions
  USB
  NFC
  Network
  Developer
  Advanced
- Each tool card can show an availability/status badge.

## Diagnose
- Full Diagnostic
- Quick Tests
- Diagnostic history
- Saved reports

## Tool Availability UX
Never simply disable a tool with no explanation.

Examples:
AVAILABLE
PERMISSION REQUIRED
NFC NOT PRESENT
REQUIRES SHIZUKU OR ADB
NOT SUPPORTED ON THIS ANDROID VERSION

## Risk UX
Level 0: no confirmation.
Level 1: contextual explanation when useful.
Level 2: confirmation.
Level 3: strong confirmation with consequence and target.
Level 4: advanced mode + strong warning + confirmation.

## Diagnostic UX
Each guided test should have:
- What is being tested
- Simple instruction
- Observable output
- Pass/Fail/Skip controls where human judgment is required
- Automatic result only when technically reliable
- Unsupported state

## Example Screen Map
Splash
-> Onboarding
-> Home

Home
-> Device Inspector
-> Tool detail
-> Full Diagnostic
-> Recent report

Tools
-> Category
-> Tool
-> Capability/setup screen if blocked

Diagnose
-> Full Diagnostic
-> Test sequence
-> Summary
-> Save report
-> Export/share

Settings
-> Appearance
-> Shizuku setup
-> Wireless ADB setup
-> Data/report preferences
-> Safety
-> About
