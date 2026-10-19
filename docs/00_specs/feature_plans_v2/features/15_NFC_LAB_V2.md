# NFC Lab v2

## V1 CORE
NFC hardware/enabled state; tag scanner; technology list; NDEF capability; NDEF records; text/URI parsing; capacity/writable state where exposed; supported NDEF write; mutation confirmation; copy/export.

## Tag Inspector
Show technologies such as NfcA/NfcB/NfcF/NfcV/IsoDep/MifareUltralight when Android reports them. Present tag identifiers only according to Android behavior and privacy rules.

## NDEF Writer
Text, URL/URI, approved MIME/custom NDEF records. Preview byte/record size before write. Detect read-only/insufficient capacity.

## Tag Doctor
Readable? NDEF? Writable? Capacity? Valid records? Unsupported/protected technology? Produce PASS/WARNING/UNSUPPORTED rather than claiming all cards are clonable.

## Developer Lab
TNF, type, payload interpretation, raw record representation, technology details, advanced protocol tools only for explicitly supported safe use cases.

## POST-V1
Write presets/templates, tag history, reusable profiles, carefully scoped technology-specific utilities.

## Hard boundary
Do not promise cloning of bank, transit, access-control, encrypted, secure-element, or cryptographically protected credentials.

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
