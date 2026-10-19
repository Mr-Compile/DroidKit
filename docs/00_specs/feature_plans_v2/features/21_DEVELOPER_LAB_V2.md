# Developer Lab v2

## V1 OPTIONAL / POST-V1 depending scope
Intent launcher, deep-link tester, package/component explorer, display metrics, device capability viewer.

## Intent Lab
Build explicit safe intents using structured fields: action, data URI, MIME type, package/component, supported extras. Preview before launch.

## Deep Link Tester
Enter URI -> resolve candidates where Android permits -> launch explicitly with confirmation.

## Component Launcher
Only launch exported/accessible components Android allows. Explain failures.

## POST-V1
Authorized log viewer, Shizuku/ADB-backed structured developer actions, activity history for user-triggered actions.

## Safety
Do not turn Developer Lab into an unrestricted arbitrary command/exploit console by default.

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
