# DroidKit

Android technician & power-user toolkit — device inspection, guided hardware
diagnostics with saved reports, app and permission inspection, USB/NFC/
network/Bluetooth utilities, and optional elevated tooling through Shizuku or
wireless ADB. Local-first: no accounts, no telemetry, no background work.
An optional, off-by-default AI assistant (BYOK, Groq) can summarize findings
and propose allow-listed actions — the app works fully without it.

## Status

`v0.8.0` on `main`; `v0.9.0-alpha.1` in progress (see `CHANGELOG.md`).

- **V1 scope complete** (tagged `v0.1.0`–`v0.8.0`): Device Inspector, Phone
  Doctor (21-test guided diagnostic with reports + text/PDF export), App
  Inspector, Permission Center, USB Lab, NFC Lab, Network Lab, Bluetooth
  basics, Developer Lab, and the Audio/Camera/Display lab entry points.
- **Post-V1 implemented, awaiting real-device gates**: Shizuku provider
  (status, grant flow, shell exec), wireless ADB provider (custom SPAKE2
  pairing, TLS/plain sessions, RSA-2048 identity in AndroidKeyStore), root
  `su` executor, AppOps viewer/controls, Package Controls, ADB Lab, and
  DroidKit Intelligence (`core:ai`).
- Journey state and remaining gates: `docs/superpowers/specs/10_DroidKit_Master_Journey_Checklist.md`.

## Features

- **Home / Tools** — device summary, capability-provider status, recent and
  pinned tools, full searchable tool catalog with category filters and
  readiness badges (blocked tools explain why).
- **Phone Doctor** — 21-test guided diagnostic (display colors/dead pixel,
  touch grid + multitouch, vibration, 5 sensor tests, speaker, microphone,
  front/rear camera, flash, Wi-Fi/Bluetooth/GNSS/NFC/USB capability checks,
  charging) with Pass/Fail/Skip, notes, retry, and Room-persisted sessions.
  Each test is also launchable standalone from the lab hubs.
- **Reports** — session history with status filters, reopenable detail with
  device snapshot, text and PDF export via FileProvider, session compare
  (before/after diff), inspection-certificate PDF with technician signature,
  and per-report or delete-all management.
- **Device Inspector / Storage Lab** — identity, OS, hardware, memory,
  storage, display, battery and feature inventory with per-field copy/share;
  volume list plus storage snapshots diffed across two points in time.
- **Battery Lab** — level, status, plug, temperature, voltage, technology,
  health, instantaneous current, battery-saver state.
- **Sensor Lab** — full inventory (vendor, range, resolution, power, wake-up),
  live monitor, orientation helper (azimuth/pitch/roll), and session
  recording with CSV share.
- **Display / Audio / Camera labs** — standalone Phone Doctor test hubs plus
  display-modes inventory, motion test patterns, audio routing/device
  inventory, and a Camera2 capability inventory.
- **Network Lab** — connection inspector (transports, capabilities, link
  properties, DNS, MTU), ping and DNS probes, per-layer diagnostics,
  interface inventory, TCP port reachability, and 10s user-initiated mDNS
  discovery.
- **Bluetooth Lab** — adapter state, classic/LE capability, bonded devices,
  permission-gated BLE scan, and a read-only GATT service/characteristic
  explorer.
- **USB Lab** — host-mode detection, live attach/detach list, per-device
  descriptors (VID/PID, interfaces, endpoints, configurations), USB-IF class
  inspectors, and the system permission flow — honest about what Android's
  UsbManager cannot expose.
- **NFC Lab** — reader-mode tag scan, technology/NDEF decode, Tag Doctor
  verdicts, NDEF text/URI write and erase flows with confirmation, write
  presets, and local scan history.
- **App Inspector** — visible installed apps with search and user/system
  filters, per-app detail (versions, SDK targets, installer, components,
  granted/denied permissions), launch and app-settings actions.
- **Permission Center** — DroidKit's own declared permissions and runtime
  grant state, with just-in-time requests when a test needs one.
- **Developer Lab** — curated system-settings intent launcher, custom intent
  builder with preview, display metrics, deep-link tester, and elevated log
  viewer.
- **Elevated tools** (require Shizuku or wireless ADB) — AppOps viewer and
  mode controls, Package Controls (disable, clear data, uninstall-for-user,
  runtime-permission grant/revoke) with high-impact confirmations, ADB Lab
  (pair, connect, structured allow-listed actions), and a local audit log.
- **Settings** — theme, technician signature and session intake defaults,
  elevated-action audit log, and DroidKit Intelligence.
- **DroidKit Intelligence** — opt-in Groq assistant: BYOK key stored
  AES/GCM-encrypted under AndroidKeyStore, `PrivacyFilter` redaction, ten
  fixed-persona assistants, contextual chat, and structured proposals
  (navigation + read-only elevated reads) that execute only on user tap.
- **Onboarding** — one-time 4-page intro covering honest results, optional
  capabilities, and local-first privacy.

## Architecture

Kotlin + Jetpack Compose, MVVM, multi-module:

- `app/` — application wiring, NavHost, bottom navigation, onboarding
- `core/` — `common` (tool model/registry), `ui` (M3 design system),
  `database` (Room: sessions, results, reports, audit log), `datastore`
  (preferences), `permissions`, `system` (device probes), `privileges`
  (capability contracts, `CapabilityManager`, `ElevatedOps`), `ai`
  (`AIProvider`/`AiUseCase`, Groq client, `PrivacyFilter`)
- `privilege/` — capability providers: `standard` (always available),
  `shizuku`, `adb` (wireless ADB), `root`
- `feature/` — one module per feature area (21 modules); each exposes a
  `FeatureContribution` (tools, hub definitions) and a `NavGraphBuilder`
- `build-logic/` — Gradle convention plugins
- `mockups/` — static HTML replica of the UI for design review (V1-era;
  slightly behind current feature set)

Design rules: features request capabilities through `CapabilityManager` and
never import provider implementations; elevated work goes through typed,
allow-listed `ElevatedAction`s only — no free-form shell; missing hardware
reports NOT_SUPPORTED, denied permissions report PERMISSION_REQUIRED, and
nothing fabricates values the platform can't provide.

## Capability providers

| Provider | Status | Unlocks |
|---|---|---|
| Standard Android API | Always on | All V1 labs and diagnostics |
| Shizuku | Implemented (J18) | AppOps inspection + mode changes, package controls |
| Wireless ADB | Implemented (J19) | ADB Lab, package controls, elevated logs |
| Root (`su`) | Implemented (J20 groundwork) | Reserved for future elevated actions |

All three are optional — every screen degrades to a clear blocked/permission
state when a provider is absent.

## Build

Requirements: Android Studio (bundled JBR is the JDK), Android SDK 36.
`minSdk 28`, `targetSdk 36`.

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"   # Windows/git-bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
```

## Docs

- `AGENTS.md` — contributor/agent rules and architecture contracts
- `docs/superpowers/specs/` — playbook journeys (J01–J21) and feature specs
- `docs/release/` — release notes drafts
- `CHANGELOG.md` — per-version changes

License: Apache-2.0 (see `LICENSE`).
