# Capability Center & Advanced Access v2

## Capability Center
Single screen showing:
Standard Android: Ready/limitations
Shizuku: Installed/running/authorized/capabilities
Wireless ADB: Supported/paired/connected
Root: Future/not configured

## V1 CORE
Capability model and StandardAndroidProvider. Advanced providers can be represented as future/not configured without fake controls.

## POST-V1 Shizuku
Setup guide, permission, lifecycle, capability map, reconnect, structured actions.

## POST-V1 Wireless ADB
Pairing, secure keys, connection/session, structured actions.

## FUTURE Root
Only after explicit product/security review.

## Provider selection
A feature requests a named capability. CapabilityManager resolves a suitable provider. Features never contain 'if Shizuku else ADB' business logic.

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
