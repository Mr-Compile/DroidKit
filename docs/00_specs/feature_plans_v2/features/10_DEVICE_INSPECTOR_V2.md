# Device Inspector v2

## Goal
Turn Device Inspector from a static information page into DroidKit's central device intelligence dashboard.

## V1 CORE
### Identity & OS
Manufacturer, brand, model, device/product identifiers that Android safely exposes, Android release/API, build information, security patch where exposed.

### Processor & Runtime
Supported ABIs, architecture/runtime information Android exposes, CPU/core information only where reliable.

### Memory
Total/available memory, low-memory state where exposed, app/runtime memory context.

### Storage
Internal storage totals/free space, volumes accessible to the app, storage health warnings based on observable free-space thresholds—not fake hardware health.

### Display
Resolution, density, refresh/display modes where exposed, orientation and relevant metrics.

### Battery
Level, charging state/source where exposed, temperature/voltage/current/capacity properties only when Android/device reports them reliably.

### Hardware Inventory
Sensors, cameras, NFC, Bluetooth/BLE, USB Host, telephony capability, location feature, biometrics capability at a high level where appropriate.

## Improved UX
Overview -> OS -> Hardware -> Memory/Storage -> Display -> Battery -> Capabilities.
Allow Copy Field, Copy Section, Share Summary, and Launch Related Lab.

## POST-V1
Compare snapshots, technician device profile, change detection after repair/update.

## Hard boundaries
No invented battery-health %, NAND health, hidden serials, bootloader/security state unless supported and verified.

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
