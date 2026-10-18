# Reports & Technician Workflow v2

## V1 CORE
DiagnosticSession, DiagnosticResult, DeviceSnapshot, notes, history, report detail, export/share.

## Report structure
Report ID/date/app version; device summary; overall status; grouped tests; failures/warnings; unsupported/skipped tests; notes; limitations/disclaimer.

## Improved features
- Filter history by status/date.
- Reopen report without rerunning tests.
- Copy concise summary.
- Share PDF or approved structured format.
- Delete individual/all local reports.
- Clearly preview sensitive fields before export.

## POST-V1
Repair intake, customer/device label, before/after comparison, technician signature/name configured locally, used-phone certificate template, batch/shop workflow.

## Privacy
No cloud requirement. Do not silently collect persistent identifiers. Reports should explain what they contain.

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
