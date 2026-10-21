# DroidKit — UI Mockup

A static, zero-dependency HTML replica of the DroidKit Android app — every
screen from the real navigation graph, clickable like the real app. No build,
no emulator, no device.

## Two files

- **`index.html`** — canonical mockup. Mirrors the app's *current* UI and stays
  synchronized with implementation per `AGENTS.md`.
- **`improved-index.html`** — design-direction preview: the same replica
  restyled in the "Dark Instrument" language (v5 spec: Dark Technical +
  Minimalism + Bento + Aurora accents + restrained glass, minus the Material
  look). Ink surfaces, green→teal gradient accents, hairline borders,
  letterspaced micro-labels, monospace technical values, status dots — the
  splash's identity carried through the whole app. Also demos the component
  layer the reskin adds: `.res` verdict pills on every PASS/FAIL/state
  surface, a shared `.gatecard` for permission/elevated-access gates,
  per-reason dots in tool availability, card press feedback, and a real
  loading vocabulary (`.spin` inline loaders, `.skeleton` shimmer). Dark-first,
  light theme included (toggle in Settings). Proposal only — not yet the app.

## Open it

Double-click `index.html` / `improved-index.html`, or serve the folder:

```bash
npx http-server mockups        # or
python -m http.server -d mockups
```

## What's inside

- **Phone frame** (left) — a Pixel-style device frame running the app replica.
  Bottom nav (Home / Tools / Diagnose / Settings), Android gesture bar
  (triangle = back, circle = home), system status bar.
- **Flow map** (right) — every route in `DroidKitNavHost`, grouped by area.
  Click a node to jump the phone to that screen; the current route shows above
  the phone.

## Mock device state

Simulates a Pixel 8 Pro, Android 15, mid-setup:

- CAMERA granted; RECORD_AUDIO / BLUETOOTH_CONNECT / BLUETOOTH_SCAN not
  granted (try the Microphone test or Bluetooth Lab to see the permission
  gates).
- STANDARD provider ready; ADB needs pairing; ROOT unavailable — V2 tools
  show real **Blocked** badges and stub screens show blocked reasons.
- Four saved diagnostic sessions are seeded in Reports / Diagnose history —
  they tell one story (Aug intake: dead pixels + touch + mic faults → Oct recheck
  → post-repair pass on Oct 19) and feed the Insights charts. One session is
  older than 30 days to demo the range filter.
- Three storage snapshots are seeded with declining free space.
- A USB flash disk and an NDEF-formatted NFC tag are "attached" (Start scan in
  NFC Lab finds the tag after ~1.5 s).

## Try these flows

1. **Diagnose → Start Full Diagnostic** — guided runner through all 21 Phone
   Doctor tests (tap colors, fill the touch grid, play the real 440 Hz tone),
   then the review screen, then Save session — the report lands in Reports.
2. **Tools** — search, category chips, favorite stars, Ready/Blocked badges;
   blocked tools on the ADB/AppOps/Packages stubs explain why.
3. **Apps → any app** — App Inspector detail with launch/settings buttons.
4. **NFC Lab** — auto-scan finds the tag ~1.5 s after opening; technology
   detail dump, decoded NDEF records with raw-hex toggle, Tag Doctor
   verdicts, and Duplicate/Erase ops. Try **Write** for the multi-record
   composer (11 record types) and the armed-write confirmation flow.
5. **Settings** — theme control switches the phone between dark/light.
6. **Home → Insights card** (or Tools → Reports category → Device Insights) —
   the on-device analytics screen: verdict donut + pass-rate trend across
   sessions, tests needing attention, storage sparkline, activity stats, and
   most-used tools. Flip the 30 days / All time chips to see the Aug session
   drop out. Run a diagnostic or capture a snapshot and the charts react.

## Fidelity notes

- Layouts mirror the real Composables 1:1 (cards, SectionHeaders, InfoRows
  with copy buttons, SuggestionChips, TestResultBar, AlertDialogs, the export
  bottom sheet).
- All device data is mocked; buttons that would leave the app (intents, share
  sheets, system settings) show a toast instead.
