# DroidKit — UI Mockup

A static, zero-dependency HTML replica of the DroidKit Android app — every
screen from the real navigation graph, clickable like the real app. No build,
no emulator, no device.

## Open it

Double-click `index.html`, or serve the folder:

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
- STANDARD provider ready; ADB needs pairing; SHIZUKU/ROOT unavailable —
  V2 tools show real **Blocked** badges and stub screens show blocked reasons.
- One saved diagnostic report is seeded in Reports / Diagnose history.
- A USB flash disk and an NDEF-formatted NFC tag are "attached" (Start scan in
  NFC Lab finds the tag after ~1.5 s).

## Try these flows

1. **Diagnose → Start Full Diagnostic** — guided runner through all 21 Phone
   Doctor tests (tap colors, fill the touch grid, play the real 440 Hz tone),
   then the review screen, then Save session — the report lands in Reports.
2. **Tools** — search, category chips, favorite stars, Ready/Blocked badges;
   blocked tools on the ADB/AppOps/Packages stubs explain why.
3. **Apps → any app** — App Inspector detail with launch/settings buttons.
4. **NFC Lab → Start scan** — tag appears with Tag Doctor verdicts; try
   Write / erase for the armed-write confirmation flow.
5. **Settings** — theme radios switch the phone between dark/light.

## Fidelity notes

- Layouts mirror the real Composables 1:1 (cards, SectionHeaders, InfoRows
  with copy buttons, SuggestionChips, TestResultBar, AlertDialogs, the export
  bottom sheet).
- All device data is mocked; buttons that would leave the app (intents, share
  sheets, system settings) show a toast instead.
