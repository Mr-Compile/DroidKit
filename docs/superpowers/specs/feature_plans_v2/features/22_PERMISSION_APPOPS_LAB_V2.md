# Permission & AppOps Lab v2

## V1 CORE
Permission Inspector: app requested permissions, grant state where reliably accessible, permission groups/categories, open Android app-permission/settings surfaces.

## Permission Center
DroidKit's own permissions: explain why each is needed, current status, request only in context, route permanently denied permissions to settings.

## POST-V1 — Elevated
AppOps Inspector via verified Shizuku/ADB capability, supported operation state, structured approved changes.

## High-risk rules
Target package clearly shown; current value -> requested value preview; consequence explanation; confirmation; execute through CapabilityManager; result; recovery guidance.

## Boundary
Do not claim standard Android can arbitrarily grant/revoke every permission for every app.

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
