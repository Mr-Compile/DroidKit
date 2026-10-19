# DroidKit v1.0.0 — Release Notes (DRAFT)

> **Status: DRAFT for owner review** — written ahead of the beta/RC cycle (J15).
> Do not publish until the J14 alpha device matrix and J15 acceptance gates
> pass. Version, date and links are placeholders until the release is cut.

DroidKit is a native Android toolkit for technicians and power users:
inspect a device, run guided hardware diagnostics, audit apps, probe USB/NFC
hardware, and export technician reports. Everything runs on-device; no
account, no cloud sync, no elevated access required. Shizuku / wireless ADB /
root are optional post-V1 capability providers — the core app works without
them.

## Highlights

- **Device Inspector** — build/SoC/ABI, RAM, storage volumes, display modes,
  battery readings, sensor inventory with live monitor, camera and
  connectivity capability summary. Per-field copy and share; unavailable
  values render "Not exposed" instead of fabricated data.
- **Phone Doctor** — guided 21-test hardware diagnostic (display colors,
  dead-pixel, touch grid, multitouch, vibration, five sensors, speaker,
  microphone, front/rear camera, flash, Wi-Fi/Bluetooth/GPS/NFC/USB
  capability checks, charging). Six honest result states including
  NOT_SUPPORTED and PERMISSION_REQUIRED — missing hardware is never a FAIL.
- **Audio, Camera & Display labs** — standalone launch points into the
  Phone Doctor tests, no session required.
- **Reports** — every guided session persists a device snapshot, per-test
  results and technician notes; reopen, copy a concise summary, or export as
  text/PDF via the share sheet after a field-disclosure preview.
- **App Inspector** — visible-package list with search and user/system
  filters; per-app version, SDK targets, installer, component explorer
  (activities/services/receivers/providers), declared and granted
  permissions, launch and system-settings shortcuts. Honors Android 11+
  package-visibility rules — no QUERY_ALL_PACKAGES.
- **Permission Center** — DroidKit's own declared permissions with runtime
  grant state and a shortcut to system app settings.
- **USB Lab** — host-mode detection, attach/detach tracking, permission-gated
  device reads (VID/PID, manufacturer, product, serial where authorized),
  interfaces/endpoints/configuration descriptors, copy/share summary.
  Honest scope note: no NAND/controller-level inspection.
- **NFC Lab** — reader-mode tag scan, technology list, NDEF record parsing
  (text/URI decode), armed write/erase behind explicit confirmations,
  Tag Doctor verdicts, NdefFormatable fallback.
- **Network Lab** — active connection inspector (transports, capabilities,
  link properties, DNS servers, MTU, private DNS state), ping probe, DNS
  lookup, per-layer connectivity diagnostics. Restricted identifiers
  (SSID/BSSID/MAC) are disclosed as restricted, never guessed.
- **Bluetooth Lab** — adapter presence/enabled state, classic+BLE features,
  permission-gated bonded-device list. BLE scanning is post-V1.
- **Developer Lab** — intent launcher (curated system screens + custom
  action/data/package with preview) and display metrics (resolution, dp
  size, density, refresh rate, HDR).
- **Tools catalog** — searchable registry with categories, favorites and
  recents; every tool shows capability/permission/hardware resolution
  before launch.

## Known limitations (J14 audit)

- Ping uses `InetAddress.isReachable`; ICMP is often filtered by networks —
  a failed ping does not prove the host is down (noted on screen).
- Wi-Fi SSID/BSSID/MAC are not read — restricted behind location permission
  and OEM policy; the app discloses this instead of fabricating values.
- USB reads stop at what `UsbManager` exposes; controller/NAND internals are
  out of scope by design.
- Post-V1 tools (Shizuku, wireless ADB, BLE scan, AppOps mutation, package
  controls, log viewer) are visible in the registry marked V2 and open
  explanatory placeholders.

## Requirements

- Android 9 (API 28) or later.
- Runtime permissions are requested in context only — camera and microphone
  for the matching diagnostics, Bluetooth connect on Android 12+, NFC when
  the lab is used. Denial produces a PERMISSION_REQUIRED state, never a
  crash.

## Links

- Repository: https://github.com/Mr-Compile/DroidKit
- Changelog: `CHANGELOG.md`
- Privacy policy: `docs/05_release/PRIVACY_POLICY_DRAFT.md` (draft until published)
