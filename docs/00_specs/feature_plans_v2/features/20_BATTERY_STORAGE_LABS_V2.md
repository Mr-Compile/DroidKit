# Battery & Storage Labs v2

## Battery Lab — V1 CORE
Level, charging state, power source where exposed, temperature, voltage, current/capacity properties where supported, battery saver/power context where appropriate.

### Technician interpretation
Label raw values and source. Provide warnings such as unusually high temperature only with conservative documented thresholds; never present a universal battery-health percentage without a reliable source.

## Storage Lab — V1 CORE
Internal volume capacity/free/used, app-accessible volumes, free-space warnings, storage directories relevant to DroidKit, report/export.

## POST-V1
Storage change snapshots, large DroidKit-generated-file management, report cleanup assistant.

## Hard boundary
Do not equate filesystem free space with NAND physical health, wear, bad blocks, or controller health.

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
