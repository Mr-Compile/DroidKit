# App & APK Inspector v2

## V1 CORE — App Inspector
Visible installed apps; user/system categorization where reliable; search/filter; package/version; target/min SDK where exposed; install/update time where exposed; requested permissions; exported/declared components; source/APK metadata accessible to Android; safe launch/open settings actions.

## Component Explorer
Activities, services, receivers, providers with exported/enabled metadata where accessible. Developer-focused detail view.

## Permission Inspector
Requested permissions grouped by granted/denied where APIs allow accurate determination. Link to Android app settings for standard management.

## APK Inspector
Inspect accessible APK metadata. Do not promise decompilation or signature/security conclusions without verified implementation.

## POST-V1
Shizuku/AppOps inspection, structured package controls, advanced permission controls.

## Safety
Destructive package actions must be elevated, capability-gated, target-confirmed, and reversible where possible.

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
