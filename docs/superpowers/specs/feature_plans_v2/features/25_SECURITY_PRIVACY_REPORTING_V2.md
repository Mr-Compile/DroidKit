# Security, Privacy & Audit Experience v2

## Product-wide improvement
Security/privacy should be visible UX, not only internal rules.

## Safety Center
Explain risk levels, advanced access, destructive confirmations, local data, report exports.

## Action Result
For elevated/mutating operations record enough local context to explain what DroidKit attempted and whether it succeeded, without creating a sensitive surveillance log.

## Report privacy
Before export, show categories included. Allow optional technician notes. Avoid unnecessary persistent identifiers.

## Data controls
View report storage, delete reports, clear recent/favorites if desired, reset setup preferences.

## Security invariants
No credential/FRP/lock bypass; no stealth monitoring; no arbitrary unvalidated privileged command path; secrets never logged.

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
