# DroidKit Navigation QA Matrix

Use PASS / FAIL / BLOCKED / NOT TESTED with evidence.

## App Start
- Clean install → onboarding
- Finish onboarding → Home
- Skip onboarding → intended destination
- Relaunch completed onboarding → Home
- Process recreation at root

## Top-Level Navigation
For Home, Tools, Diagnostics, Settings:
- select destination
- reselect same destination
- rapidly switch destinations
- Back after switching
- state preserved when returning
- no duplicate destinations

## Tool Entry Sources
For Home, Ready Now, Favorites, Recents, Tools, Search, Category:
- open tool
- toolbar Up
- system Back
- return source is correct
- query/filter/scroll preserved where expected
- no duplicate tool instance from double tap

## Nested Tools
- list → detail → Back
- detail → action → Back
- parameterized routes use concrete args
- invalid/missing args fail safely, never crash

## Diagnostics
- Diagnostics → preset → intake → session
- test → next/retry/skip
- Back during test
- Back during session
- exit confirmation if progress loss is possible
- complete → review/report
- Back after completion does not reopen invalid transient steps

## Reports / Insights
- list → detail → Back
- compare → Back
- export/share preview → cancel/return
- external share sheet → return

## Permissions / External Settings
- permission grant → return updated
- permission deny → return correct blocked state
- permanent deny → settings → return updated
- cancel external settings → stable state
- repeated open/return

## Capability Providers
- Shizuku unavailable → setup/grant → return → Ready
- Shizuku revoked → return → Blocked
- ADB pair/connect → return → updated
- ADB disconnect → updated
- Root available/unavailable → correct refresh

## Hardware Lifecycle
Camera / BLE / Bluetooth / NFC / USB / Sensors / Audio:
- enter/start
- Back cleanup
- return/restart
- background/foreground
- rotate/recreate
- no leaked receiver/callback/job/session

## Failure Cases
- unresolved route placeholder
- invalid route arg
- missing tool id
- unavailable hardware
- denied permission
- provider unavailable
- external activity unavailable
All must fail safely with user-friendly recovery.
