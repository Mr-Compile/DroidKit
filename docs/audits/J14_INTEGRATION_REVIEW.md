# J14 Integrated Alpha — Static Integration Review

Date: 2026-10-19. Reviewer: agent (static code audit — no device run).
Scope: post-J13 tree at `v0.8.0` (main `0e99050`+). This covers the
code-reviewable J14 items; device-matrix, OEM, tester-feedback and
hardware-dependent items remain open and require physical devices.

## Results

### Cross-module navigation — PASS
- All 21 `FeatureContribution` graphs are invoked inside `DroidKitNavHost`
  and every `Tool.route` resolves to a registered `composable` destination
  (36 tools → feature routes, plus `apps/detail`, `usb/device`, `nfc/write`,
  `reports/detail`, diagnostics guided/review/test sub-routes).
- Detail screens reach the shared parent-entry ViewModel
  (`nav.getBackStackEntry(PARENT_ROUTE)`) — reachable only through their
  list screen, so the parent entry always exists in-app. Back-stack
  restore keeps the whole stack. Low residual risk; noted.

### Permission flow — PASS
- Merged manifest declares exactly the permissions the app requests:
  CAMERA, RECORD_AUDIO (diagnostics), BLUETOOTH_CONNECT (31+), BLUETOOTH
  (≤30), NFC, INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, VIBRATE.
- Every runtime permission is requested in-context
  (`rememberLauncherForActivityResult`), never at startup.
- Denial paths: diagnostics emit `PERMISSION_REQUIRED` via `PermissionGate`;
  Bluetooth screen keeps the bonded list gated with an inline grant button.
- Deliberate non-requests: FINE_LOCATION (SSID/BSSID not read — restriction
  disclosed on the Network screen); BLUETOOTH_SCAN is referenced only by the
  `ToolPhase.V2` BLE tool and will be declared when that tool ships.

### Crash/error handling — PASS
- All framework reads in J12/J13 are `runCatching`-guarded
  (UsbManager, NfcAdapter, BluetoothAdapter, ConnectivityManager reads,
  serial numbers, bonded devices, link properties).
- Receivers/callbacks are lifecycle-scoped: USB attach + permission
  receivers unregistered in `onCleared`; Bluetooth state receiver same;
  NetworkCallback unregistered in `onCleared`; NFC reader mode enabled/
  disabled inside `DisposableEffect` tied to scan/armed state.
- Detail-entry pattern (`getBackStackEntry`) is the only throw-prone call;
  in-app reachability analysis above keeps it safe.

### Accessibility — PASS (static)
- All interactive elements are Material3 components with default
  semantics; the one `Icon` carries `contentDescription`; app icon in App
  detail is decorative (minor: could get a description later).
- State text is not color-only: verdicts/state rows render the enum text
  (PASS/WARNING/NOT_SUPPORTED…).

### Performance — PASS (static)
- Lists use `LazyColumn` keyed items; I/O is off the main thread
  (`Dispatchers.IO` for ping/DNS/package reads; USB/NFC/BT reads are
  small one-shot calls; NFC reader callback parses on the binder thread).
- No polling loops: attach/detach, adapter-state, and network changes are
  broadcast/callback-driven.

### Battery/background — PASS
- No services, no wake locks, no background scan loops. NFC reader mode and
  all receivers live only while their screen/VM lives.

### Storage/privacy — PASS
- Room `version = 1`, `exportSchema = false` — no migration paths required
  yet (first schema). Local-first: no network egress anywhere; reports
  disclose exported fields before share (J10); tag/device identifiers only
  read post-permission.

### Offline behavior — PASS
- App is fully local-first; Network Lab renders an explicit
  "No active network" state and per-layer UNKNOWN/FAIL instead of errors.

## Open findings (recorded, not fixed)
- `bluetooth.scan` tool is `ToolPhase.V2` and references BLUETOOTH_SCAN,
  which is intentionally undeclared until the BLE scanner ships.
- Ping uses `InetAddress.isReachable` — ICMP echo is often filtered; the
  screen states this caveat. TCP-based reachability is POST-V1.
- App icon `Image` in `AppDetailScreen` is decorative without a
  contentDescription — minor a11y nit.

## Items still requiring hardware (out of agent scope)
- Android-version matrix runs, representative OEM devices
  (Samsung/Xiaomi/OPPO/Vivo/Transsion/Pixel), USB multi-class devices,
  NFC tag variety, real ping/DNS behavior, launch/navigation smoke run.
- Alpha tester feedback loop, defect-severity triage, `v0.9.0-alpha.N`
  version bump + tag (deferred until device verification).
