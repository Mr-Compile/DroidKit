# Changelog

## [0.9.0-alpha.1] - Unreleased

### Added
- About DroidKit (Settings → About DroidKit): identity header with the
  official mark, description, dynamic version/build from PackageManager,
  Check for updates against GitHub Releases (typed results — offline,
  no published releases, update available), What is DroidKit, a Features
  page driven by ToolRegistry, What's New from local release notes, Help
  & FAQ, Report a problem/Send feedback via GitHub Issues, configuration-
  ready Privacy Policy and Terms rows, an Open-Source Licenses page, and
  developer/GitHub links — all routed as `settings/about*` destinations
- Insights (`feature:insights`): on-device analytics dashboard computed
  entirely from Room/DataStore — latest-session verdict ring, per-session
  pass-rate bars with day labels, "tests needing attention" ranking
  (fails weighed double), storage free-space trendline, activity counters
  (diagnostics, tests, tools, scans, snapshots, reports, elevated
  actions) and most-used tools. 30-day / all-time range chips, honest
  empty states, summary card on Home, tool entry under Reports
- Elevated capability providers (J19/J20 groundwork): wireless ADB provider
  (custom SPAKE2 pairing, TLS/plain sessions, RSA-2048 identity in
  AndroidKeyStore, structured allow-listed actions) and a root `su`
  executor — routed through a shared `ElevatedOps`/`ELEVATED_SHELL`
  abstraction so features never touch provider implementations. No
  third-party privilege app required. Auto-connect (Shizuku-style
  start-with-app): once a device has paired and connected, DroidKit
  rediscovers `_adb-tls-connect` and reopens the session on app start and
  lazily when an elevated action finds the session dead — toggleable on the
  Wireless ADB screen, paused by a manual Disconnect
- Elevated feature screens: AppOps viewer + mode controls, package
  controls (disable/clear data/uninstall-for-user with high-impact
  confirmation), and the ADB Lab (pair, connect, structured actions)
- Lab depth: user-initiated 15s BLE scan (Bluetooth), TCP port
  reachability probes (Network), display modes inventory (Display),
  audio routing/device inventory (Audio), Camera2 capability inventory
  (Camera), deep-link tester and elevated log viewer (Developer)
- DroidKit Intelligence (J21 core): `core:ai` module — `AIProvider`
  abstraction, Groq provider (OpenAI-compatible chat completions), BYOK
  key encrypted under AndroidKeyStore AES/GCM, `PrivacyFilter` secret
  redaction + context minimization, typed errors, enable toggle, model
  selection, test-connection and disconnect in Settings → DroidKit
  Intelligence, plus optional "Summarize with AI" on report detail
- Developer Lab V1 subset (approved scope): intent launcher with curated
  system-destination list plus custom action/data/package intents with
  preview and honest failure states, and display metrics (resolution, dp
  size, density, scaled density, physical dpi, refresh rate, supported
  modes, HDR types)
- DroidKit Intelligence expansion: ten fixed-persona assistants
  (PromptRegistry), contextual AI chat at `settings/ai_chat`, and
  structured proposals (OpenTool / read-only elevated) parsed against an
  approved schema, validated via ToolRegistry + CapabilityManager, and
  executed only after user confirmation
- Runtime permission controls: `pm grant`/`pm revoke` + `dumpsys`
  runtime-permission parsing in Package Controls, and a local elevated
  action audit log (Room) surfaced in Settings
- Technician workflow (post-V1): session intake fields
  (customer/device labels), technician signature configured in Settings
  and stamped on reports, diagnostic presets (Full / used-phone check),
  and session compare — before/after diff of test states and device
  snapshot fields
- Storage snapshots: capture per-volume free/used and diff two points in
  time (Storage Lab)
- NFC scan history (local, truncated tag IDs) and NDEF write presets
- Sensor session recording with CSV share (Sensor Lab), display motion
  test patterns (Display Lab), pinned tools on Home, and a read-only
  BLE GATT service/characteristic explorer
- Audio & Haptics, Camera & Flash, and Display & Touch lab index screens
  deep-linking into the standalone Phone Doctor tests (`diagnose/test/*`)
- USB class inspectors: per-interface class/subclass/protocol
  interpretation (HID boot devices, mass-storage BOT/UAS, CDC serial,
  UVC video, Bluetooth radios, RNDIS…) plus a whole-device class
  inspector — honest about Android's UsbManager limits
- Inspection certificate export (Reports → Certificate, PDF): technician
  signature, intake labels, result tally, per-category checklist, and a
  not-a-guarantee notice
- Network Lab: full interface inventory (name, MTU, up/loopback/multicast
  flags, addresses) and bounded 10s user-initiated mDNS/NSD service
  discovery with on-demand resolve
- Sensor Lab: orientation helper (azimuth/pitch/roll + reported accuracy
  from the rotation-vector sensor, NOT_SUPPORTED when absent)
- Onboarding: one-time 4-page intro (Welcome → honest-results → optional
  capabilities → local-first privacy) gated by a DataStore flag, per the
  J03 screen spec
- NFC Lab expansion (J13 scope extension, owner-approved): auto-scan with
  pause/resume and scan pulse; read-only technology detail dump
  (`NfcTechProbe` — NfcA ATQA/SAK, NfcB/NfcF/NfcV protocol fields, IsoDep
  historical/hi-layer bytes, MifareClassic geometry, MifareUltralight
  unprotected page dump); richer NDEF decode (Smart Poster, vCard,
  Wi-Fi WSC credential, external/AAR, MIME) with per-record raw hex
  toggle and Open/Copy record actions; multi-record NDEF Writer covering
  Text, Link (+social presets), Phone, SMS, Email, Contact, Wi-Fi, Geo,
  App, MIME and Raw hex with capacity preview; armed Duplicate
  (NDEF-content copy — tag UID and protected regions never copied) and
  Format ops; copyable/shareable plain-text tag report. `makeReadOnly`
  remains excluded; Tag Doctor gains a Copyable check
- Location/GPS diagnostic upgraded to a live read (owner-approved V1 scope
  extension): the Phone Doctor connectivity.location test now shows provider
  state, precise-vs-approximate grant and the newest cached fix (coordinates,
  accuracy, age), plus a "Get live fix" button that proves GNSS acquisition
  with a 20 s timeout — timeout reports UNKNOWN, never FAIL. Runs behind the
  standard permission gate (ACCESS_COARSE_LOCATION), so denial records
  PERMISSION_REQUIRED; raw coordinates stay on screen and never enter notes,
  reports or AI fields
- Shared risk surface (V5): `RiskBadge`, `ConsequencePanel`,
  `ConfirmationSheet` (L0–L4 levels, action-named confirm buttons) and
  `AIInsightCard` in `core:ui`; the package-controls mutation dialog
  migrated to it
- Friendly error catalog: `strings_errors.xml` copy resolved through
  `UiProblem.catalogKey` — permission, hardware, API, provider and AI
  failures render friendly title/message while technical details stay
  verbatim underneath
- Capability Center (Settings → Advanced access): live provider status
  for Standard Android / wireless ADB / Root with capability lists,
  limitations and setup/recovery actions — the spec'd Shizuku row is
  omitted because the native ADB provider superseded it (migration log
  2026-10-23)
- Candidate-9 branding applied: layered splash window background plus
  Android 12+ system-splash attributes (no artificial delay); J03 token
  doc gained the v5 semantic-name mapping
- Settings backup: SAF export/import of preferences as versioned JSON —
  theme, dynamic color, favorites, recents, usage counts, technician
  name and the auto-connect toggle; device pairing state, onboarding
  and AI keys are structurally unexportable, and import confirms before
  overwriting
- ADB terminal (separately approved J19 item): `adb/terminal` behind the
  `adb_terminal_enabled` opt-in and an L4 confirmation — commands run
  through `ElevatedOps.runRawCommand` (single line, ≤512 chars) and are
  logged verbatim to the elevated-action audit. Not an `ElevatedAction`;
  the allow-list invariant stands
- Technician presets (Room v6): custom diagnostic presets — name plus a
  test checklist grouped by category, edit/delete, selectable on the
  Diagnose picker; sessions persist their resolved test list
  (`diagnostic_sessions.testIds`) so resume stays exact after the preset
  is edited or deleted
- Multi-device history: Device/Customer dropdown filters on Reports fed
  by distinct intake labels; report cards show the intake labels

### Fixed
- Phone Doctor flash test could leave the LED dark on devices where CameraX
  applies `enableTorch` only to an active capture session — the test bound
  the camera with no use case, so the torch request never reached the HAL.
  The test now drives the LED through `CameraManager.setTorchMode` (the same
  path as the system quick-settings tile), picks the flash-capable camera
  via `FLASH_INFO_AVAILABLE`, tracks real torch state through `TorchCallback`,
  and surfaces `CameraAccessException`s (e.g. camera busy) instead of
  silently no-oping
- Audio/camera/display tool entries previously opened stale placeholder
  screens claiming tests "ship in Phase 3" — the tests already shipped in
  J08/J09; labs now launch them
- App icon in App Inspector detail now carries a content description
  (J14 accessibility finding)
- Stale "Phase N" copy on post-V1 stub screens now references the J18–J19
  journey names

### Docs
- Release notes draft (`docs/05_release/RELEASE_NOTES_v1.0.0_DRAFT.md`) and
  privacy policy draft (`docs/05_release/PRIVACY_POLICY_DRAFT.md`) for owner review
- Tracker/J14 status resynced; plan files marked IMPLEMENTED where done

## [0.8.0] - Unreleased

### Added
- NFC Lab: hardware/enabled detection with settings shortcut, reader-mode
  tag scan, technology list, NDEF read (text/URI decode), writable/capacity
  reads, Tag Doctor verdicts (PASS/WARNING/UNSUPPORTED) with
  secure-credential honesty note
- NDEF Writer: text/URI record builder with byte-size preview, armed
  present-tag write with confirmation, and erase-to-empty-NDEF flow;
  NdefFormatable fallback for unformatted tags
- Network Lab: connection inspector (transports, capabilities, link
  properties, DNS servers, MTU, private DNS), ping probe with ICMP-filtering
  caveat, DNS lookup, and per-layer connectivity diagnostics (no aggregate
  "health score"); live network callback refresh
- Bluetooth Lab (V1 basic): adapter presence/enabled, classic+BLE feature
  detection, BLUETOOTH_CONNECT permission gate, bonded-device list
  (name/address/type/bond as permitted), system enable prompt
- `NetTools`, `NfcTagParser`, `BtNames` helpers with unit tests
  (4 + 9 + 2 tests)

## [0.7.0] - Unreleased

### Added
- USB Lab: host-mode capability card, live attach/detach device list,
  per-device detail (device path, VID/PID, USB version, manufacturer,
  product), classification names per USB-IF class codes
- Interfaces → endpoints tree (class codes, endpoint address, direction,
  transfer type, max packet) and configurations (power flags)
- USB permission flow: system request with result receiver; serial number
  gated behind granted permission, descriptor hints when withheld
- Copy/share device summary; explicit note that flash-controller/NAND/
  firmware internals are not exposed by Android USB APIs
- `UsbDescriptors` helpers with unit tests (5 tests)

## [0.6.0] - Unreleased

### Added
- App Inspector: visible installed apps with search and user/system
  filters, honest Android 11+ package-visibility caveat
- App detail: icon, version name/code, target/min SDK, install/update
  times, installer, APK source path, launch and app-settings actions
- Permission Inspector: requested permissions grouped granted/not granted
- Component Explorer: activities, services, receivers, providers with
  exported flags per app

## [0.5.0] - Unreleased

### Added
- Report detail screen: reopenable session view with device snapshot,
  grouped results with notes, and technician notes
- `ReportBuilder` canonical text report (spec-ordered sections, honest
  empty/unsupported wording) plus concise clipboard summary
- Export: text (.txt) and PDF (.pdf via framework PdfDocument) written to
  cache, shared through FileProvider with a sensitive-field preview sheet
- Report history: status filter chips, per-session delete with confirm,
  delete-all with confirm; saved-report records persisted

## [0.4.0] - Unreleased

### Added
- Phone Doctor hardware set (full sequence now 21 tests): speaker 440 Hz
  tone, microphone record/playback with RECORD_AUDIO permission gate,
  front/rear camera preview (CameraX), torch flash test, five auto-recorded
  connectivity capability checks (Wi-Fi, Bluetooth, GNSS, NFC, USB host/
  accessory), charging/battery check with live readings
- `PermissionGate`: runtime-permission wrapper reporting PERMISSION_REQUIRED
  on denial; CAMERA and RECORD_AUDIO declared with optional uses-features
- Session review: state-count summary header and per-category grouping;
  quick tests grouped by category on the Diagnose tab

## [0.3.0] - Unreleased

### Added
- Phone Doctor: guided diagnostic session engine (ordered 10-test CORE
  sequence, Pass/Fail/Skip judgment, retry of failed and skipped tests,
  technician notes, Room-persisted results)
- Display tests: fullscreen R/G/B/white/black color cycle and dead-pixel check
- Touch tests: 8x12 grid coverage tracker (Pass gated at 90% coverage) and
  multitouch point counter (per-pointer markers, max-count capture)
- Sensor tests: accelerometer, gyroscope, proximity, light and magnetometer
  screens with live readings; absent hardware reports NOT_SUPPORTED
- Vibration test via VibratorManager (VIBRATE permission declared)
- Session review screen with per-test result states and save flow

## [0.2.0] - Unreleased

### Added
- Device Inspector rebuild: sectioned dashboard (Identity, OS, Hardware,
  Memory/Storage, Display, Battery, Hardware features, Access providers) with
  per-field copy, copy-section, and share-summary
- Storage Lab: volume list with capacity/free-space reads and low-space warning
- Battery Lab: charge/status/source, temperature/voltage/technology/health,
  instantaneous current and battery-saver state, warm-battery notice
- Sensor Lab: full inventory (vendor, type, range, resolution, power, wake-up)
  plus live monitor for safe sensor types
- `DeviceSystemProbe` extensions: `memory`, `batteryStatus`, `cameraSummary`,
  `displayModeRatesHz`, richer `ProbeSensor` fields, `formatBytes`/`formatHz`
- `core:ui` `InfoRow` and `SectionCard` primitives

## [0.1.0] - Unreleased

### Added
- Multi-module project skeleton (app, 7 core, 4 privilege, 21 feature modules)
- Design system (Material3, dynamic color, shared components)
- ToolRegistry with search/category/favorites foundations
- CapabilityManager + provider chain (Standard ready; Shizuku/ADB/Root stubs)
- PermissionManager, DeviceSystemProbe (hardware/API detection)
- Room schema v1 (diagnostic sessions, results, technician notes)
- DataStore settings (theme, favorites, recents)
- Home / Tools / Diagnose / Settings navigation shells
- Device Inspector screen with live system probes
