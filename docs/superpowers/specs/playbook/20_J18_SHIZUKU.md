# Journey 18 - Shizuku Capability Provider

Do this after stable V1 unless roadmap is explicitly changed.

## Research
- [ ] Re-read current official Shizuku documentation
- [ ] Supported Android versions
- [ ] Service lifecycle
- [ ] Permission flow
- [ ] Binder/API boundaries
- [ ] Reconnect/error handling
- [ ] Distribution implications
- [ ] Capability map

## Architecture
Feature -> CapabilityManager -> ShizukuProvider
Never Feature -> Shizuku directly.

## Initial scope
- [ ] Provider availability
- [ ] Permission/setup screen
- [ ] Read-only elevated proof
- [ ] AppOps inspection where supported
- [ ] Approved AppOps changes
- [ ] Approved package controls
- [ ] Approved permission controls
- [ ] Risk confirmation
- [ ] Result/audit feedback

## Gate
- [ ] Standard app behavior unaffected when Shizuku is absent/stopped
- [ ] No hidden privilege assumptions
- [ ] Compatibility/device testing complete
