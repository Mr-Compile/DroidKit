# Real-Device Validation Runbook

Scope: everything implemented without hardware access — the elevated
provider layer, post-V1 workflow features, and the latest remainder wave.
Record defects per `playbook/23` (ID, device, Android version, steps,
expected, actual, logs/screenshots, severity).

Install:

```
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Capture logs while testing:

```
adb logcat -v time | findstr /i "droidkit"
```

For every item: PASS / FAIL / NOT SUPPORTED is acceptable — what matters
is that unsupported shows as NOT_SUPPORTED or PERMISSION_REQUIRED, never
as a crash or a fabricated value.

---

## 1. Elevated providers (Capability Center → Advanced access)

| # | Steps | Expected |
|---|-------|----------|
| 1.1 | Open Settings → Advanced access | All providers listed with honest status — no "ready" claims before grants |
| 1.2 | Wireless ADB: enable wireless debugging → guided pairing (code or QR) → connect | ADB provider flips to Ready; elevated tools light up |
| 1.3 | With ADB ready → AppOps screen → change one app mode | Mode changes; revert works; audit log entry appears in Settings |
| 1.4 | Wireless ADB: enable wireless debugging → Pair → enter host:port + code | Pairing completes; status shows connected |
| 1.5 | ADB Lab → Connect (host:port) → run a structured action (WhoAmI / GetProp) | Real output; audit log records it |
| 1.6 | Root (rooted device only): grant su in your root manager | Provider Ready after uid=0 check; elevated tools work via root |
| 1.7 | Non-rooted / no ADB session device | Every elevated tool shows capability-unavailable, no crash |

## 2. Reports / technician workflow

| # | Steps | Expected |
|---|-------|----------|
| 2.1 | Diagnostics → start session → intake dialog (customer/device label) → Full preset | Session runs, intake saved |
| 2.2 | Run a second session after changing something → Reports → Compare | Diff shows FIXED/REGRESSED/NEW/REMOVED correctly |
| 2.3 | Settings → set technician name → report → Export → Certificate | PDF shares; signature + intake labels present; tally correct |
| 2.4 | Export TEXT and PDF of the same session | Both open; content matches screen; privacy preview shown |
| 2.5 | Used-phone preset | Only used-phone tests run; results tagged with preset |

## 3. Connectivity labs

| # | Steps | Expected |
|---|-------|----------|
| 3.1 | USB: attach a flash drive/mouse via OTG → USB Lab → device detail | VID/PID, interfaces, and the class-inspector text ("Mass storage — Bulk-Only", "HID boot mouse"…) |
| 3.2 | USB: attach/detach during screen open | List updates; no stale entries; unsupported hub shows honest values |
| 3.3 | Network → Interfaces card | All interfaces (lo, wlan0…) with MTU, flags, real addresses |
| 3.4 | Network → Service discovery → `_ipp._tcp` Discover (network with a printer) | Services appear within 10s; Resolve shows host:port |
| 3.5 | Same on a network with no such service | "No services found" — not a failure state |
| 3.6 | Bluetooth → scan (BLE peripherals nearby) | Bounded 15s scan lists devices; stops itself |
| 3.7 | Tap a scannable device → GATT explorer | Services/characteristics listed read-only; disconnect works |
| 3.8 | NFC → scan a tag → Ndef write preset → confirm | Tag written; history row shows truncated ID |

## 4. Sensors / display / storage

| # | Steps | Expected |
|---|-------|----------|
| 4.1 | Sensors → Orientation | Azimuth changes on rotation; accuracy hint shown |
| 4.2 | Device without rotation-vector sensor (if available) | NOT_SUPPORTED empty state, not a crash |
| 4.3 | Live sensor monitor → record → share CSV | CSV opens with timestamped samples |
| 4.4 | Display → motion pattern | Stripes animate smoothly; exits cleanly |
| 4.5 | Storage Lab → snapshot → free space → second snapshot → compare | Per-volume delta shows correct sign/magnitude |

## 5. AI (optional — needs Groq key)

| # | Steps | Expected |
|---|-------|----------|
| 5.1 | Settings → Intelligence → paste key → Test | Success; key never redisplayed |
| 5.2 | Report detail → Summarize with AI | Summary renders; contains no device secrets |
| 5.3 | Chat → ask it to open a tool → proposal card → confirm | Navigates only after your tap |
| 5.4 | Airplane mode + AI on | Typed OFFLINE error, core tools unaffected |

## 6. Regression smoke

- [ ] Fresh install → Home renders, all registered tools reachable
- [ ] Permission denial paths show PERMISSION_REQUIRED everywhere new
- [ ] Rotation on each new screen — no state loss/crash
- [ ] Dark theme pass on new cards

---

## 7. Evidence matrix (fill per device — required to close DK-060/061/062, DK-070/071)

Record one row per physical device. "N/A" is valid only with a reason
(e.g., rooted device unavailable → Root column N/A + note).

| Field | Value |
|-------|-------|
| Device / model | _fill_ |
| Android version (API) | _fill_ |
| OEM / skin | _fill_ |
| Build fingerprint | _fill_ |
| App versionName/versionCode tested | _fill_ |
| Date / tester | _fill_ |

### Per-device results

| Gate | Item | Result | Notes/log ref |
|------|------|--------|---------------|
| DK-060 | Wireless ADB pair + connect → Ready (runbook 1.2) | PENDING | |
| DK-060 | Elevated action via ADB provider (1.3) | PENDING | |
| DK-061 | Wireless ADB pair + connect (1.4) | PENDING | |
| DK-061 | Structured action + audit log (1.5) | PENDING | |
| DK-062 | Root grant → provider Ready (1.6) | PENDING | |
| DK-062 | Elevated tool via root | PENDING | |
| DK-021 | Runtime permission deny → rationale → permanent deny → Open Settings | PENDING | |
| DK-022 | Provider status flips live after grant/connect (no restart) | PENDING | |
| DK-070 | API-level behaviors on this device (per table below) | PENDING | |
| DK-071 | OEM-specific findings | PENDING | |
| DK-042 | TalkBack / fontScale 1.3 / contrast checklist | PENDING | |

### API-level matrix (fill across devices)

| Behavior | API 28–29 | API 30–32 | API 33–35 | API 36 |
|----------|-----------|-----------|-----------|--------|
| Package visibility (`<queries>` MAIN+LAUNCHER) | n/a (pre-30) | PENDING | PENDING | PENDING |
| Runtime permission permanent-denial flow | PENDING | PENDING | PENDING | PENDING |
| Wireless ADB pairing | n/a (pre-30) | PENDING | PENDING | PENDING |
| Notification / nearby-wifi / bt permissions | PENDING | PENDING | PENDING | PENDING |
| Scoped-storage file access | PENDING | PENDING | PENDING | PENDING |

### OEM matrix (fill per OEM encountered)

| OEM/skin | Finding | Repro | Impact | Mitigation |
|----------|---------|-------|--------|------------|
| _e.g., Samsung OneUI_ | _fill_ | _fill_ | _fill_ | _fill_ |

> No devices were attached when this section was written (adb devices:
> empty, 2026-10-20). DK-060/061/062 remain BLOCKED until at least one
> row above is completed with real results.
