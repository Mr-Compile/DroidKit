# J02 Compatibility Matrices

Date: 2026-10-19
Source: `J02_TOOL_RECORDS.md` records. minSdk 28 / target 36.

## 1. API-level matrix

| Tool | Min API needed | Notes on 28 vs 29+ |
|---|---|---|
| Build/device info | 23+ | covered |
| CPU/ABI | 21+ | covered |
| Memory | 16+ | covered |
| Storage | 24+ | covered |
| Display | 23+ modes / 26+ HDR | covered |
| Battery | 21+ | covered |
| Sensor inventory/live | 20+ | covered |
| Touch/multitouch | 1+ | covered |
| Speaker | 1+ | covered |
| Microphone | 1+ | RECORD_AUDIO runtime |
| Vibration | 26+ for `VibrationEffect` | covered |
| Camera/flash | 23+ torch / 21+ chars | covered |
| Wi-Fi capability | 28+ | SSID needs location since 29 |
| Bluetooth capability | 18+ / 31 for CONNECT perm | guard `BLUETOOTH_CONNECT` to API 31+ |
| Location/GPS capability | 1+ | covered |
| USB host/lab | 21+ endpoints, **29** for device name/serial fields | show `Not exposed` on API 28 |
| NFC/NDEF/Tag Doctor | 19+ reader mode | covered |
| Package visibility/components | 28+; visibility narrowing from 30 | `<queries>` declarations needed on 30+ |
| Permission inspection | 28+ | signing cert API 28 fits exactly |
| APK Inspector (metadata) | 24+ minSdk field / 28 signing | covered |
| Component Explorer/Launcher | 28+ | covered |
| Permission Center | 23+ AppOps self | covered |
| Network tools | 21+ LinkProperties / **29+** DnsResolver | InetAddress fallback on 28 |
| PDF/export/share | 19+ PdfDocument | covered |
| Capability Center | — | internal state |

**Outcome:** minSdk 28 is safe; the only guarded features are USB name/serial fields (29+) and `DnsResolver` (29+).

## 2. OEM risk matrix

| Tool | Samsung | Xiaomi/Redmi | OPPO/Realme | Vivo | Transsion | Pixel/AOSP |
|---|---|---|---|---|---|---|
| Battery CURRENT_NOW | medium (SoC-dependent) | medium | medium | medium | high (often absent) | low |
| Camera torch | low | medium (camera-in-use quirks) | medium | medium | medium | low |
| Mic record | low | medium (privacy toggle) | medium (privacy toggle) | medium | medium | low |
| Display modes | medium (adaptive 48–120) | medium | medium | medium | high | low |
| Sensor metadata quality | low | medium | medium | medium | high | low |
| Touch edge zones | medium (edge panels) | medium (gesture zones) | medium | medium | medium | low |
| Wi-Fi SSID rules | low | low | low | low | medium | low |
| USB OTG | low | **high (manual OTG toggle exists)** | medium | medium | high | low |
| NFC polling strength | low | medium | medium | medium | high | low |
| Package visibility (cloned/work apps) | medium (Dual Apps/Secure Folder) | medium (Second Space) | medium | medium | medium | low |
| Permission overlays | low | **high (MIUI autostart/battery perms not in AppOps)** | medium | medium | medium | low |
| AppOps other-app reads | blocked since API 29 (all OEMs) — scope removed | | | | | |

Legend: low = standard behavior expected; medium = known quirks to handle/document; high = verify on real hardware before claiming support.

## 3. Permission matrix

| Permission | Type | Needed for | When requested |
|---|---|---|---|
| INTERNET | normal | ping, DNS, network diagnostics | manifest at install |
| ACCESS_NETWORK_STATE | normal | Connection Inspector | manifest |
| ACCESS_WIFI_STATE | normal | Wi-Fi capability | manifest |
| NFC | normal | NFC Lab | manifest |
| VIBRATE | normal | vibration test | manifest |
| RECORD_AUDIO | dangerous | mic test | runtime, at mic test only |
| CAMERA | dangerous | camera preview test | runtime, at camera test only |
| BLUETOOTH_CONNECT | dangerous (API 31+) | bonded-device info | runtime on 31+ only |
| ACCESS_FINE_LOCATION | dangerous | Wi-Fi SSID/BSSID (29+), BT bonded on ≤30 | runtime, contextual, optional |
| QUERY_ALL_PACKAGES | restricted | "all apps" | **NOT used** — scoped visibility via `<queries>` |

Rules: no all-permissions-at-startup; every denial → PERMISSION_REQUIRED state with retry path; never treat denial as FAIL.

## 4. Hardware matrix

| Hardware | Required for | Absent → |
|---|---|---|
| Display/touch | touch tests | core — always present |
| Speaker | speaker test | NOT_SUPPORTED |
| Microphone | mic test | NOT_SUPPORTED |
| Vibrator | vibration test | NOT_SUPPORTED (`hasVibrator`) |
| Camera | camera test | NOT_SUPPORTED |
| Flash | flash test | NOT_SUPPORTED (per-camera `FLASH_INFO_AVAILABLE`) |
| Sensors (each) | sensor tests + live monitor | NOT_SUPPORTED per sensor |
| Wi-Fi | Wi-Fi capability check | NOT_SUPPORTED |
| Bluetooth | BT capability check | NOT_SUPPORTED |
| GNSS | location capability | NOT_SUPPORTED |
| NFC | NFC Lab | NOT_SUPPORTED; disabled → setup prompt |
| USB host | USB Lab | NOT_SUPPORTED; OTG off → honest note |
| Telephony | capability flag only | reported, not required |

## 5. Feature decision matrix

| Decision | Tools |
|---|---|
| V1 CORE (freeze candidates) | All of section A–G records except those below |
| V1 OPTIONAL | Bluetooth basic, Component Launcher + Display Metrics (Developer Lab) |
| POST-V1 | BLE scanner, AppOps mutation, elevated controls, APK export, sensor recording, snapshot compare, used-phone/repair presets |
| REJECTED | serial/IMEI reads, NAND/controller internals, "all packages" claims, composite health scores, fake boosters |

## 6. Known limitations (to publish in-app where relevant)

- Package list = "packages visible to DroidKit" — never "all installed apps" (API 30+).
- Battery shows only platform-exposed values; no health %, no cycle count, `CURRENT_NOW` absent on many devices.
- USB device name/serial fields require API 29+ and per-device permission; serial hidden without grant.
- NFC secure elements (payment/transit cards' secure parts) are not readable by design.
- Human-observation tests (touch, speaker, mic, camera preview) report user verdicts — they are not automated pass/fail.
- Magnetometer/gyro values reflect driver calibration state; accuracy flags are unreliable on several OEMs.
- Wi-Fi SSID/BSSID shown only with location permission granted and location ON (API 29+).
- Ping uses ICMP when reachable else TCP probe — result labeled by method; a fail means "not reachable this way," not "down."
- Storage "free space" is filesystem-visible, unrelated to NAND wear.
- OTG may require a manual OEM toggle; absence reported honestly.
