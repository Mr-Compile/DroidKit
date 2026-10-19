# Advanced Capability Research Notes

Date: 2026-10-19
Scope: answers the unchecked J2 research items in
`10_DroidKit_Master_Journey_Checklist.md` (Shizuku APIs, capability map,
Wireless ADB options, ADB key storage, AppOps/package/log limits, root).

## Shizuku APIs / capability map

- Dependency: `dev.rikka.shizuku:api:13.1.5` (stable API surface since 11.x;
  `pingBinder`, `checkSelfPermission`, `requestPermission`,
  `newProcess` via reflection).
- DroidKit capability map: providers claim `ELEVATED_SHELL` (generic shell
  capability shared by Shizuku/Root/ADB) plus provider-specific
  `SHIZUKU_SERVICE`. Features request capabilities, never providers.
- Lifecycle: binder present → permission state → `Ready`/`NeedsSetup`/
  `Unavailable` via `ProviderStatus`; re-evaluated per resolve.

## Wireless ADB implementation options

- Evaluated: third-party ADB client libraries vs. in-repo protocol.
- Chosen: hand-rolled wire protocol (`AdbProtocol`) + SPAKE2 pairing
  (`Spake2`) + optional TLS transport in `privilege/adb`. Rationale: no
  unmaintained dependency, full auditability, smallest attack surface.
- Licensing: no external ADB code; nothing to reconcile.

## Secure ADB key storage

- RSA-2048 identity generated and held in AndroidKeyStore (`AdbKeys`);
  private key never leaves the TEE/Keystore boundary. Only the public key
  (ADB pubkey format) is transmitted during pairing.

## AppOps limitations (observed during J18 implementation)

- `appops get <pkg>` output format varies across API levels/OEMs
  (attribution tags, extra columns); the parser takes the first `OP: mode`
  token per line and ignores the rest — resilient by design.
- `appops set` silently no-ops for ops the platform doesn't expose for the
  package; callers must re-`get` to verify. The AppOps screen re-queries
  after mutation.
- Some ops are privileged (`MANAGE_*`, usage stats) — deny/ignore can be
  overridden by device policy; treat results as best-effort.

## Advanced package-management limitations

- `pm disable-user`/`pm clear`/`pm uninstall --user 0` operate on the
  *calling user*; work-profile/secondary-user targeting needs
  `--user <id>` which we hard-pin to 0 by design.
- System-critical packages can refuse disable/uninstall — shell exits 0
  but state unchanged; verify via `pm list packages -d` after mutation.
- `pm install-existing` only works when the APK is still installed for
  another user; otherwise exits non-zero (surfaced to UI).

## Log access limitations

- `logcat -d` via shell user sees more than app sandbox but still filters
  some buffers (`events`, `system` partially restricted per device).
- Bounded read (`-t <lines>`) is mandatory to avoid unbounded dumps;
  hard-capped at 5000 lines in `ElevatedAction.ReadLog`.
- Log contents are untrusted data — may contain secrets; `PrivacyFilter`
  redacts before any AI use.

## Root provider

- `RootProvider` implemented: `su` binary probe across known paths →
  `su -c id` confirmation → `Ready`. Executor routed via `ElevatedOps`
  preference order SHIZUKU → ROOT → ADB.
- Value call (keep/expand root features) remains an owner decision —
  tracked under J20.
