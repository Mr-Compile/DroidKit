# Journey 18 - Shizuku Capability Provider

Do this after stable V1 unless roadmap is explicitly changed.

Status: implementation complete and statically verified
(`assembleDebug` + `testDebugUnitTest` green). Real-device grant flow and
API matrix not yet run — Gate items stay open until hardware testing.

## Research
- [x] Re-read current official Shizuku documentation
- [x] Supported Android versions
- [x] Service lifecycle
- [x] Permission flow
- [x] Binder/API boundaries
- [x] Reconnect/error handling
- [x] Distribution implications
- [x] Capability map

## Architecture
Feature -> CapabilityManager -> ShizukuProvider
Never Feature -> Shizuku directly.

## Initial scope
- [x] Provider availability
- [x] Permission/setup screen
- [x] Read-only elevated proof
- [x] AppOps inspection where supported
- [x] Approved AppOps changes
- [x] Approved package controls
- [ ] Approved permission controls
- [x] Risk confirmation
- [x] Result/audit feedback

## Gate
- [ ] Standard app behavior unaffected when Shizuku is absent/stopped
- [ ] No hidden privilege assumptions
- [ ] Compatibility/device testing complete
