# Bluetooth Lab v2

## Goal
Add a dedicated Bluetooth/BLE technician and developer lab rather than leaving Bluetooth as only a capability check.

## V1 OPTIONAL
Adapter support/state, classic/BLE capability, required permission handling, paired/bonded devices only as Android permits.

## POST-V1
BLE scanner with user-initiated scan; advertisement inspector; RSSI observation; service/characteristic explorer after explicit connection; copy/export session details.

## Safety/privacy
Nearby-device scanning is permission-sensitive and can reveal nearby devices. Make scans user-initiated, visible, time-bounded, and never background-surveillance behavior.

## EXPERIMENTAL
Known GATT profile helpers for explicitly supported devices.

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
