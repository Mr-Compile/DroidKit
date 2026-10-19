# Sensor Lab v2

## V1 CORE
Inventory all Android-reported sensors with name/vendor/version/type, wake-up/dynamic properties where exposed, and link supported sensors to live diagnostic tools.

## Live Monitor
Accelerometer, gyroscope, magnetometer, light, proximity and other safe sensors. Show numeric values, units, update rate, and simple live visualization.

## Technician tools
Presence test, responsiveness observation, min/max sample, calibration guidance only when Android/device supports a legitimate procedure.

## POST-V1
Recording/export of short sensor sessions, orientation helper, comparison after repair.

## Boundary
Do not call a sensor 'calibrated' or 'healthy' merely because values exist.

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
