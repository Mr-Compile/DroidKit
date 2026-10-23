# Journey 19 - Wireless ADB Provider

Status: implementation complete and statically verified. Custom SPAKE2 +
TLS/plain protocol (no third-party ADB library), RSA-2048 identity in
AndroidKeyStore, actions routed through the `ElevatedOps` allow-list.
Guided pairing added 2026-10-20 (design:
`docs/superpowers/specs/2026-10-20-wireless-adb-guided-pairing-design.md`):
mDNS auto-discovery of `_adb-tls-pairing._tcp` / `_adb-tls-connect._tcp`
via `NsdManager` + multicast lock, foreground `AdbPairingService` with a
Shizuku-style `RemoteInput` notification for the 6-digit code, QR pairing
(`WIFI:T:ADB;S:<name>;P:<secret>;;`) in both directions — DroidKit displays
a QR for another device to scan, or imports a screenshot of a
device-displayed QR and decodes it via ZXing — plus the manual host/port
fallback. `AdbSessionSetup` exposes the guided flow; feature code stays
provider-agnostic.
Auto-connect (2026-10-23, Shizuku-style start-with-app): `wireless_adb_paired`
is persisted on the first proven connect; while `wireless_adb_auto_connect`
is on (default) the provider runs a bounded `_adb-tls-connect` rediscovery on
app start and a throttled lazy reconnect inside `runShell` when a session
died — surfaced via `AdbSessionSetup.autoConnect()`/`autoConnectEnabled`. A
manual Disconnect suppresses auto-connect until the next explicit connect.
Pairing/connect round-trips not yet run on hardware — Gate stays open.

## Research first
- [x] Current Android Wireless Debugging behavior
- [x] Pairing/auth protocol strategy
- [x] Library/license decision
- [x] Key generation/storage
- [x] Session lifecycle
- [x] Network threat model
- [x] Capability map

## Product principle
Prefer structured actions over a raw terminal:
User action -> validated parameters -> known command/action -> result parser -> UI result.

## Implementation
- [x] Pairing UI (guided mDNS + notification code entry + QR + manual fallback)
- [x] Secure credential/key storage
- [x] Session manager
- [x] Capability provider
- [x] Reconnect/timeout
- [x] Structured package actions
- [x] Device property actions
- [x] Authorized log actions
- [x] Carefully scoped reboot/system actions
- [x] mDNS endpoint discovery (`AdbDiscovery`, `_adb-tls-pairing._tcp` / `_adb-tls-connect._tcp`)
- [x] Foreground pairing service + notification RemoteInput (`AdbPairingService`)
- [x] QR pairing — display for other device + screenshot decode (`AdbQrPayload`, ZXing)
- [x] Auto-connect on app start + lazy session reconnect (`AdbSessionSetup.autoConnect`, toggleable, default on)
- [x] Optional terminal — separately approved; `ElevatedOps.runRawCommand` (not an `ElevatedAction`), `adb_terminal_enabled` opt-in with L4 confirmation, single-line ≤512 chars, every command audit-logged verbatim

## Gate
- [ ] Invalid pairing and disconnect tested
- [ ] Sensitive key material protected
- [ ] Command injection risks reviewed
- [ ] App remains useful without ADB
