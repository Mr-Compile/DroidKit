# NFC Lab v2

## V1 CORE
NFC hardware/enabled state; auto-scan reader mode; technology list; NDEF capability; NDEF records (text/URI plus Smart Poster, vCard, Wi-Fi WSC, external/AAR, MIME); capacity/writable state where exposed; multi-record NDEF write; erase/format/duplicate with preview and confirmation; copy/export report.

## Tag Inspector
Show technologies such as NfcA/NfcB/NfcF/NfcV/IsoDep/MifareClassic/MifareUltralight when Android reports them. Read-only detail dump per technology (ATQA/SAK, protocol bytes, transceive limits, Ultralight page dump of unprotected pages). Present tag identifiers only according to Android behavior and privacy rules.

## NDEF Writer
Multi-record composer: Text, Link (URI presets incl. social), Phone, SMS, Email, Contact (vCard), Wi-Fi (WSC credential), Location (geo:), App (AAR), MIME and Raw (explicit TNF/type/payload hex). Preview record count and byte size against last scanned tag capacity before write. Detect read-only/insufficient capacity.

## Tag Ops
Duplicate: copy the scanned tag's raw NDEF message to the next presented writable tag — record content only, never the tag UID or protected regions. Format: armed `NdefFormatable` op for non-NDEF tags. Erase: empty-TNF write. `makeReadOnly` remains excluded (irreversible).

## Tag Doctor
Readable? NDEF? Writable? Capacity? Valid records? Copyable? Unsupported/protected technology? Produce PASS/WARNING/UNSUPPORTED rather than claiming all cards are clonable.

## Developer Lab
TNF, type, payload interpretation, raw record representation, per-record raw hex toggle, technology detail dump, advanced protocol tools only for explicitly supported safe use cases.

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
