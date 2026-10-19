# Tool Registry, Search, Home & Settings v2

## ToolRegistry
Canonical metadata for every tool/sub-tool: stable ID, title, description, category, icon, route, keywords, min API, hardware, permissions, capabilities, risk, scope tier.

## Search
Search title/description/keywords/categories. Results show availability badge: Ready, Permission, Setup Required, Unsupported, Experimental.

## Home
Device summary, capability status, Full Diagnostic CTA, quick/favorite tools, recent tools/reports, relevant setup warning.

## Favorites/Recent
Local only by default. Never let recent history expose sensitive values on Home.

## Settings
Appearance, accessibility preferences, report/data controls, Permission Center, Capability Center, safety confirmations, about/version/licenses.

## POST-V1
Custom technician dashboard and tool pinning.

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
