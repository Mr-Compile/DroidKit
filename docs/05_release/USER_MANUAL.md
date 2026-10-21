# DroidKit — User Manual

> Tracks the current development build (v0.9.0-alpha.1). This manual is a
> per-release document — update it when routes or features ship/change.

DroidKit is a native Android technician/power-user toolkit: device
inspection, hardware diagnostics (Phone Doctor), repair reports, and scoped
elevated access — all offline-first. Optional AI features are opt-in and
bring-your-own-key.

**Honesty rules used throughout the app:**

- Hardware that doesn't exist reports **NOT_SUPPORTED**, never a failure.
- A permission you haven't granted reports **PERMISSION_REQUIRED** — the rest
  of the app keeps working.
- A value Android hides shows **Not exposed** instead of a guess.
- Elevated actions are typed and allow-listed; DroidKit never runs arbitrary
  shell commands, and never performs credential/lock bypasses.

Everything is stored on-device unless you explicitly use the opt-in AI or a
manual update check.

---

# Part A — Getting around

## A.1 First run

The splash screen leads into a four-page onboarding flow (once, until you
finish or skip). Onboarding explains the toolset, asks nothing it doesn't
need, and lands on **Home**. Revisit it via the About screens if needed.

## A.2 The four tabs

| Tab | What it's for |
|---|---|
| **Home** | Device snapshot, elevated-access status, quick actions, pinned favorites, and the Insights card. |
| **Tools** | The full catalog — browse by category, search (tools *and* pages), tap ⓘ on any tool for what it needs, ☆ to pin it to Home. |
| **Diagnose** | Guided diagnostic presets, per-test quick checks, and session history. |
| **Settings** | Theme, report signature, Advanced access (elevated providers), AI assistant, Safety, Support & privacy, About. |

## A.3 Navigation model

- **Bottom nav** (phones) or **nav rail** (wider layouts) switches between the
  four tabs.
- **Back** (Android back/gesture bar triangle in the mockup) unwinds the stack
  to the calling screen; Home returns to `home`.
- **Two-pane**: on expanded layouts, Apps, USB, and Reports show list + detail
  side-by-side instead of pushing a new page.
- **Tools search** matches tool names, keywords, descriptions, and non-tool
  pages (Settings, Permission Center, Help, Reports, Insights, and each Phone
  Doctor test).
- Every tool card shows an **availability badge**:

  | Badge | Meaning |
  |---|---|
  | **Ready** | Runs now. |
  | **Setup required** | Needs a provider step — tap ⓘ or open Permission Center / ADB Lab. |
  | **Permission required** | Needs an Android runtime permission; asked for only when you start. |
  | **Unavailable / Not supported / Needs API x** | The device or Android version can't provide it. Not a bug. |

## A.4 Per-tab walkthrough

**Home** — hero device card (model, Android, patch, RAM, display), elevated
access status with provider dots, a Start diagnostic shortcut, quick tools,
favorites, recents, and a link into Insights.

**Tools** — search field, category chips (Device / Diagnostics / Apps /
Connectivity / Hardware / Developer / Reports), tool cards with icon, status
badge, ⓘ details, and ☆ favorite. Searching also surfaces matching pages.

**Diagnose** — two presets: **Full diagnostic** (all 21 checks) and
**Used-phone check** (focused intake subset). Start → optional intake labels
(customer/device) → guided runner → review → save. **Quick tests** lists every
test grouped by category; **History** links into saved reports.

**Settings** — Appearance (theme, Material You toggle), Reports (technician
signature), Advanced access (provider status + grant actions, ADB Lab link,
elevated-action history), DroidKit Intelligence (AI), Safety statement,
Support & privacy links, About.

## A.5 Common tasks

- **Run a hardware check:** Diagnose → pick a preset → Start → follow each
  test → review → Save session. The report appears in Reports.
- **Grant a permission mid-flow:** tests that need one show a gate first —
  tap Grant, accept the system dialog.
- **Export a report:** Reports → open a session → Export (text/PDF).
- **Prove a repair:** Reports → Compare sessions → pick before/after →
  transitions (FIXED / IMPROVED / REGRESSED / CHANGED).
- **See health trends:** Home → Insights card (verdict ring, pass-rate trend,
  tests needing attention, activity).

---

# Part B — Complete routing reference

DroidKit uses a single NavHost; feature modules each contribute a graph.
Argument routes take `{placeholders}`.

## B.1 First run

| Route | Screen | Reach via |
|---|---|---|
| `onboarding` | 4-page intro | First launch (until completed) |

## B.2 Top-level tabs

| Route | Screen | Reach via |
|---|---|---|
| `home` | Home dashboard | Bottom nav / rail |
| `tools` | Tool catalog + search | Bottom nav / rail |
| `diagnose` | Diagnostics hub | Bottom nav / rail |
| `settings` | Settings | Bottom nav / rail |

## B.3 Diagnostics flow

| Route | Screen | Reach via | Needs |
|---|---|---|---|
| `diagnose/guided` | Guided runner (sequential tests) | Diagnose → Start preset | Per-test permissions |
| `diagnose/test/{testId}` | Single Phone Doctor test host | Diagnose → Quick tests; lab screens (Display/Audio/Camera) | See test table |
| `diagnose/review` | Session review + save | End of guided run | — |
| `reports` | Saved sessions list | Diagnose → Saved reports; Tools → Reports | — |
| `reports/detail/{sessionId}` | Report detail + export/AI summary | Reports → row | — |
| `reports/compare` | Before/after session diff | Reports → Compare | — |
| `insights` | On-device analytics | Home card; Tools → Reports category | — |

## B.4 Device labs

| Route | Screen | Reach via | Needs |
|---|---|---|---|
| `deviceinfo` | Device Overview | Tools → Device | — |
| `deviceinfo/storage` | Storage Lab | Device Overview; Tools | — |
| `battery` | Battery Lab | Tools → Device | Fuel-gauge read: elevated |
| `sensors` | Sensor Lab (inventory) | Tools → Device | — |
| `sensors/live` | Live sensor picker | Sensor Lab | — |
| `sensors/live/{type}` | Live monitor for one sensor | Picker row | Sensor present |
| `sensors/orientation` | Orientation helper | Tools → Device | Rotation-vector sensor |

## B.5 Hardware & connectivity

| Route | Screen | Reach via | Needs |
|---|---|---|---|
| `display` | Display & Touch hub | Tools | — |
| `display/modes` | Display modes/HDR | Display hub | — |
| `display/motion` | Motion pattern | Display hub | — |
| `audio` | Audio & Haptics hub | Tools | Mic test: RECORD_AUDIO; vibration: VIBRATE |
| `audio/routing` | Audio routing detail | Audio hub | — |
| `camera` | Camera & Flash hub | Tools | CAMERA perm + camera hardware |
| `camera/details` | Camera2 capabilities | Camera hub | Camera hardware |
| `network` | Network Lab | Tools | INTERNET + NETWORK_STATE |
| `network/saved` | Saved Wi-Fi networks | Network Lab | **Root only** |
| `bluetooth` | Bluetooth Lab | Tools | BT_CONNECT + BT hardware, API 31+ |
| `bluetooth/gatt/{address}` | GATT explorer | Bluetooth → device row | BLUETOOTH_CONNECT; BLE hardware |
| `usb` | USB Lab | Tools | USB host hardware |
| `usb/device/{deviceName}` | USB device detail | USB Lab → device row | USB host |
| `nfc` | NFC Lab (read, Tag Doctor) | Tools | NFC perm + hardware |
| `nfc/write` | NFC write/erase | NFC Lab → Write | NFC + writable tag |

## B.6 Apps & system

| Route | Screen | Reach via | Needs |
|---|---|---|---|
| `apps` | App Inspector (list) | Tools → Apps | — |
| `apps/detail/{packageName}` | App detail | Apps → row | — |
| `permissions` | Permission Center | Settings → Permissions & Access | — |
| `appops` | AppOps viewer/controls | Tools → Apps | Elevated provider |
| `packages` | Package controls (disable/clear/uninstall-for-user) | Tools → Apps | Elevated provider |
| `adb` | Wireless ADB Lab (pair/connect/actions/logcat) | Tools → Developer; Settings → Advanced access; gate CTAs | ADB pairing/connect |
| `files` | Restricted storage browser (Android/data + copy-out) | Tools → Apps | Elevated provider |
| `developer` | Developer Lab (intents, metrics, deep links, logs) | Tools → Developer | Log viewer: elevated |

## B.7 Settings & about

| Route | Screen | Reach via |
|---|---|---|
| `settings/ai_chat` | DroidKit Chat | Settings → DroidKit Intelligence; Tools → Device |
| `settings/privacy` | Privacy Center | Settings → Support & privacy |
| `settings/about` | About | Settings → About card |
| `settings/about/what_is` | What is DroidKit | About → row |
| `settings/about/features` | Feature list (links to tools) | About → row |
| `settings/about/whats_new` | Release notes | About / Settings → row |
| `settings/about/help` | Help & FAQ | Settings → Support & privacy |
| `settings/about/licenses` | Open-source licenses | About → row |

## B.8 Non-screen surfaces

- **System-style permission dialog** — shown by a permission gate or grant
  action; only in the foreground, only when you start the tool.
- **Session intake sheet** — optional customer/device labels before a guided
  run.
- **Tool-info dialog** — ⓘ on any tool card: category, availability reasons,
  impact level, elevated capability, permissions, hardware, API floor, release
  phase.
- **Danger confirms** — irreversible/high-impact actions (package disable,
  clear data, reboot) always restate the consequence first.
- **Export bottom sheet** — report export preview.
- **Toasts** — lightweight confirmations (copied, saved, paired); they never
  carry secrets.

## B.9 Entry points besides Tools

- Home: quick tools, favorites, recents, Insights card, diagnostic shortcut.
- Settings rows: Permission Center, ADB Lab, AI Chat, About tree.
- Lab screens deep-link into `diagnose/test/{id}` (e.g. Display → Dead pixel).
- AI chat proposals can navigate to a tool after your confirmation.

---

# Part C — Technician reference

## C.1 Phone Doctor catalog (all 21 tests)

Route: `diagnose/test/{id}` (also run inside `diagnose/guided`).

| ID | Test | Cat | What to check | Needs |
|---|---|---|---|---|
| `display.color` | Display colors | Display | Uniform coverage, banding, tint shifts, dim patches on R/G/B/W/K | — |
| `display.dead_pixel` | Dead-pixel check | Display | Stuck/dark/bright pixels on every color | — |
| `touch.grid` | Touch grid | Touch | Drag across all cells; any cell that won't fill = dead zone | — |
| `touch.multitouch` | Multitouch | Touch | Each finger gets its own marker; count simultaneous points | — |
| `haptics.vibration` | Vibration | Haptics | Tap Vibrate; weak/absent buzz → motor/flex issue | — |
| `sensor.accelerometer` | Accelerometer | Sensors | X/Y/Z change smoothly with tilt | Sensor present |
| `sensor.gyroscope` | Gyroscope | Sensors | Tracks rotation, settles near zero when still | Sensor present |
| `sensor.proximity` | Proximity | Sensors | Near/far switch when covered | Sensor present |
| `sensor.light` | Light sensor | Sensors | Lux rises/falls with lighting | Sensor present |
| `sensor.magnetometer` | Magnetometer | Sensors | Field strength responds to orientation/magnet | Sensor present |
| `audio.speaker` | Speaker | Audio | Clear 440 Hz tone; no distortion/crackle/silence | — |
| `audio.microphone` | Microphone | Audio | Record a few seconds, play back — voice audible/clear | RECORD_AUDIO |
| `camera.front` | Front camera | Camera | Live preview; cover lens to confirm real feed | CAMERA |
| `camera.rear` | Rear camera | Camera | Same, rear lens | CAMERA |
| `camera.flash` | Flash | Camera | Torch toggles; LED lights clearly | CAMERA |
| `connectivity.wifi` | Wi-Fi | Connectivity | Capability + radio enabled (auto-recorded) | Wi-Fi hardware |
| `connectivity.bluetooth` | Bluetooth | Connectivity | Capability + adapter state (auto-recorded) | BT hardware |
| `connectivity.location` | Location / GPS | Connectivity | GNSS capability + location enabled (auto) | Location hardware |
| `connectivity.nfc` | NFC | Connectivity | Capability + adapter state (auto) | NFC hardware |
| `connectivity.usb` | USB | Connectivity | USB host/accessory capability (auto) | USB hardware |
| `power.charging` | Charging / battery | Power | Battery readings; plug a charger — state changes, plug source appears | — |

Result states: **PASS / FAIL / NOT TESTED / NOT SUPPORTED / PERMISSION
REQUIRED**. Connectivity tests record automatically; observation tests need
your verdict (or Skip).

## C.2 Session workflow

Preset → optional intake labels (customer ref, device label — stamped on the
report, stored locally) → guided runner with a progress line → review screen
(counts per state, retry failed/skipped, technician notes) → **Save session**
→ report in `reports`. Reports carry app version, device snapshot, per-test
results and notes, and your technician signature (Settings → Reports).

## C.3 Reports

List → detail (device block + per-test results + notes + AI summary option)
→ export as text/PDF via the bottom sheet → or delete. **Compare sessions**
diffs two saved sessions into FIXED / IMPROVED / REGRESSED / CHANGED /
ADDED / REMOVED transitions plus device changes.

## C.4 Elevated access

Two providers, checked in Settings → **Advanced access** (dots: ready /
setup required / unavailable):

1. **Wireless ADB** — pair in the ADB Lab: auto-discovery (notification code
   or QR in/out), manual host/port fallback, then Connect. Unlocks the
   `shell` (uid 2000) channel: AppOps, package controls, files browser,
   logcat, device actions, advanced device detail.
2. **Root** — when present it's preferred for the paths `shell` can't reach
   (e.g. saved Wi-Fi store, `/data` areas).

Elevated work runs only through typed `ElevatedAction`s — validated
parameters, no free-form shell. Every elevated call is logged to the
elevated-action history (Settings → Advanced access).

## C.5 Wireless ADB Lab (`adb`)

Pairing card (auto-discovery with code entry, QR show/import, manual
fallback) → Connect card → once connected: device properties, authorized
logcat reads (line count + tag filter), and scoped power actions (Reboot —
danger-confirmed).

## C.6 Restricted storage browser (`files`)

Browses `/sdcard` including `Android/data` and `Android/obb` as the shell
user — the paths SAF can no longer grant on Android 11+. Path bar with Up +
`data`/`obb` shortcuts; each entry has a copy-out action landing in
`/sdcard/Download/DroidKit/` (visible in any file manager). Paths are
validated to shared storage; nothing outside it can be touched.

## C.7 AI features (opt-in)

Settings → DroidKit Intelligence: enable, paste your own Groq key (stored
encrypted), pick a model, optionally auto-summarize results. AI surfaces:
report summaries, explain/repair on failed tests, and `settings/ai_chat`
(proposals are validated and always need your confirmation before acting).
Context is minimized and secrets-redacted before anything leaves; core tools
work fully with AI off.

## C.8 Safety & honesty

Impact levels 0–4 (safe → advanced); high-impact actions show the exact
consequence and need explicit confirmation. No credential/FRP/lock bypass,
no stealth features. Missing hardware = NOT_SUPPORTED; hidden values = Not
exposed; denied permission = PERMISSION_REQUIRED.

## C.9 Troubleshooting

| Symptom | Cause | Action |
|---|---|---|
| Tool badge: Permission required | Runtime permission not granted | Open the tool → grant when asked |
| Tool badge: Setup required | No elevated provider connected | Settings → Advanced access, or pair in ADB Lab |
| Badge: Unavailable / Not supported | Hardware/feature/API missing on this device | Nothing to fix — device limit |
| Field: "Not exposed" | Android/OEM hides the value | Normal; not a failure |
| ADB pairing finds nothing | mDNS blocked (guest Wi-Fi), pairing screen closed | Use manual host/port; reopen "Pair device" |
| Mic/camera/BT tests gated | Permission not yet granted | Grant in the gate dialog |
| Elevated action fails | Provider disconnected | Re-check Advanced access status dots |

---

# Appendix

**Glossary**

- **AppOps** — per-app operation modes (usage access, overlays, background).
- **Wireless ADB** — Android's own debugging channel over Wi-Fi; DroidKit
  implements pairing + connect itself (no external app, no Shizuku).
- **Elevated provider** — the channel that runs typed shell actions (ADB
  `shell` uid 2000, or root uid 0).
- **NDEF** — NFC Data Exchange Format; Tag Doctor checks readability,
  formatting, capacity.
- **GATT** — BLE attribute profile; the explorer is read-only.
- **BYOK** — bring your own key: the Groq key is yours, stored encrypted,
  stripped from all non-provider traffic.

**See also** — in-app Help & FAQ (`settings/about/help`), Privacy Center
(`settings/privacy`), `docs/05_release/DEVICE_VALIDATION_RUNBOOK.md`.
