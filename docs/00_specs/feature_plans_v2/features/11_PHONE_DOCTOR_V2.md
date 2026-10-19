# Phone Doctor v2

## Goal
A guided technician-quality diagnostic system, not just a collection of buttons.

## Modes
### Full Diagnostic — V1 CORE
Guided ordered session with instructions, result capture, skip/retry, notes, final report.

### Quick Tests — V1 CORE
Launch one test without creating a full workflow unless the user chooses to save it.

### Used Phone Check — POST-V1
Fast inspection preset for second-hand phone evaluation.

### Repair Verification — POST-V1
Run selected tests before and after repair and compare results.

## Test groups
Display: RGB, white/black, dead/stuck pixel visual check, brightness behavior.
Touch: grid coverage, multitouch points, gesture response.
Audio: speaker playback, microphone record/playback with explicit user judgment.
Haptics: vibration.
Camera: front/rear preview, switching, focus/flash capabilities where supported.
Sensors: proximity, light, accelerometer, gyroscope, magnetometer and detected extras.
Connectivity: Wi-Fi/Bluetooth/NFC/location/USB capability and enabled-state checks.
Power: charging state and observable battery metrics.
Buttons: only buttons/events Android can legitimately observe; document limitations.

## Result semantics
PASS, FAIL, NOT_TESTED, NOT_SUPPORTED, PERMISSION_REQUIRED, UNKNOWN.
Manual tests never auto-pass simply because an API call succeeded.

## Improved workflow
Prepare -> Device Snapshot -> Guided Tests -> Review Failed/Skipped -> Technician Notes -> Save -> Export.

## Shared implementation requirements
- Explicit loading/content/permission-required/unsupported/error states.
- Hardware absence must not be reported as a failed test.
- Never fabricate inaccessible Android values.
- Every mutating operation requires a preview/confirmation appropriate to risk.
- Register every sub-tool in ToolRegistry where it is independently launchable.
- CapabilityManager owns elevated-provider selection.
- Keep business logic out of Compose UI.
- Record known OEM/API limitations.

## Completion gate
- [ ] Feasibility verified.
- [ ] Scope tier confirmed.
- [ ] UX states implemented.
- [ ] Permissions handled.
- [ ] Unsupported hardware/API handled.
- [ ] Targeted validation passed.
- [ ] Real-device validation completed where hardware-dependent.
- [ ] Documentation/checklist updated.
