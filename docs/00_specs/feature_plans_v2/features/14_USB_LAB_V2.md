# USB Lab v2

## Goal
Make Android USB Host inspection useful for technicians and developers while being explicit about what Android cannot see.

## V1 CORE
USB Host capability, connected-device list, attach/detach, permission flow, VID/PID, class/subclass/protocol, manufacturer/product/serial where exposed, configurations/interfaces/endpoints/descriptors accessible through Android, copy/export.

## Device detail UX
Identity -> USB Classification -> Interfaces -> Endpoints -> Descriptors -> Permission/connection status.

## POST-V1
Known VID/PID labeling database, class-specific inspectors for safely supported protocols, session log, USB serial helper if a vetted library/protocol is adopted.

## EXPERIMENTAL
Protocol-specific diagnostics for explicitly supported devices.

## Hard boundary
Generic Android USB APIs do not justify promises of flash-controller model, NAND Flash ID, bad blocks, firmware reflashing, or ChipGenius-style internals.

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
