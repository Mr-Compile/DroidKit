# Network Lab v2

## V1 CORE
Active transport summary, permitted local/network IP information, DNS servers where available, ping/reachability, DNS lookup, basic connection diagnostics, copy/export.

## Tools
Connection Inspector; Ping; DNS Lookup; Host Reachability; Network Snapshot.

## Improved diagnostic
Network available? Internet capability? DNS resolution? Target reachable? Latency samples? Report each layer separately instead of a single fake 'network health' score.

## POST-V1
Port reachability for user-specified legitimate endpoints, route/interface detail where Android permits, history/comparison, local service discovery where safe and justified.

## Saved Wi-Fi Networks (owner-approved, root-only)
Reads the protected Wi-Fi config store on the owner's own device — `WifiConfigStore.xml` (API 29+, apexdata path first) or `wpa_supplicant.conf` (older builds) — and lists SSID/security/PSK. Implemented now (migration-log 2026-10-21) because some OEM builds (e.g. ColorOS) hide or cannot share saved passwords.

- Provider-pinned to ROOT: `ElevatedAction.ReadWifiConfigStore.requiredProvider = ROOT`. Shizuku and wireless ADB run as the shell identity and cannot read `/data/misc/**` — the action never falls back.
- Secrets are display-only: FLAG_SECURE screen, masked until reveal-on-tap, no clipboard/share/export, never persisted, never audit-logged (stdout), never sent to AI context.
- Derived 64-hex PSKs are labeled "PSK (hex)", never presented as the typed passphrase.
- Honest states: "requires ROOT" when no root grant; "no saved networks" when the store parses empty; parser tolerates OEM/XML variants and skips unrecognized blocks rather than guessing.
- Not credential recovery for other devices, not a lock/share bypass — owner-facing access only.

## Boundaries
Android increasingly restricts Wi-Fi identifiers and network data. Do not imply access to restricted MAC/BSSID/SSID fields without correct permissions/API conditions. Saved-password access additionally requires root on every supported version; on devices where the root manager or SELinux still denies the read, the tool reports the failure honestly.

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
