# J13 NFC + Network + Bluetooth (v0.8.0) Implementation Plan

> **STATUS: IMPLEMENTED** — all 4 tasks executed on branch `feature/j13-nfc-network`
> (2026-10-19). `:app:compileDebugKotlin` green; unit tests pass (NetTools 4,
> BtNames 2, NfcTagParser 9). Open gate items: real NFC tag runs + device run of
> ping/DNS, `v0.8.0` tag at merge to `main`. Deviation: ping via
> `InetAddress.isReachable` (no extra deps) with on-screen ICMP caveat; BLE scan
> tool kept in registry at `ToolPhase.V2`.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the NFC, Network and Bluetooth stubs with real V1 labs. NFC: hardware/enabled state, reader-mode tag scan with technology list, NDEF parse (text/URI), Tag Doctor verdicts, NDEF write + erase with preview/confirmation. Network: connection inspector, link properties, ping, DNS lookup, layer-by-layer diagnostics. Bluetooth (V1 OPTIONAL basic only): adapter/capability state, permission-gated bonded devices. Tag `v0.8.0`.

**Architecture:** `feature:nfc`, `feature:network`, `feature:bluetooth`. Hilt ViewModels own framework reads; Compose screens collect StateFlows. NFC scan uses `NfcAdapter.enableReaderMode` on the hosting Activity (Compose `LocalContext`); mutating tag ops (write/erase) use an armed "present tag" flow behind a confirmation dialog. Ping uses `InetAddress.isReachable` with honest ICMP caveat; DNS uses `getAllByName`. No new dependencies.

**Tech Stack:** Kotlin, Compose M3, framework `android.nfc`, `android.net`, `android.bluetooth` only.

## Global Constraints

- Requires: skeleton + J07 (`InfoRow`/`SectionCard`), `requiredFeatures`/`requiredPermissions` wired in `CapabilityManager`
- Specs: `specs/feature_plans_v2/features/15_NFC_LAB_V2.md`, `16_NETWORK_LAB_V2.md`, `17_BLUETOOTH_LAB_V2.md`; gate: playbook `15`
- **Hard boundaries:** no credential cloning claims (bank/transit/access/secure-element tags); no fake network-health score — each layer reported separately; no restricted identifier promises (SSID/BSSID/MAC only where Android permits, else honest note); BLE scanner stays POST-V1
- Honesty: missing NFC/BT hardware → NOT_SUPPORTED; NFC off → enabled-state UX, not error; permission denial → PERMISSION_REQUIRED rows
- Safety: NDEF write/erase are mutating ops → preview (type, size, target) + confirmation; irreversible `makeReadOnly` not offered in V1
- Commit per task, push after every commit; `JAVA_HOME` export for builds

---

### Task 1: Network Lab — inspector, ping, DNS, layer diagnostics

**Files:**
- Modify: `feature/network/build.gradle.kts` — `droidkit.hilt` + junit
- Modify: `feature/network/src/main/AndroidManifest.xml` — INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE
- Create: `feature/network/.../NetTools.kt` — `pingHost(host, count, timeoutMs)` → samples+loss (JVM-testable), `lookupHost(host)` → addresses+ms, `diagnosticLayerName/…` constants
- Create: `feature/network/.../NetworkViewModel.kt` — ConnectivityManager active network + capabilities + LinkProperties snapshot; ping/dns suspend runners; layer results StateFlow
- Create: `feature/network/.../NetworkScreen.kt` — Inspector card, Diagnostics card (per-layer PASS/FAIL/UNKNOWN), Ping card, DNS card
- Modify: `feature/network/.../NetworkFeature.kt` — real tools (inspector, ping, dns, diagnostics), drop stub
- Create: `feature/network/src/test/.../NetToolsTest.kt`

- [x] **Step 1: VM + tools + manifest**
- [x] **Step 2: Screen + routes**
- [x] **Step 3: Compile + commit** `feat(network): connection inspector, ping, dns lookup, layer diagnostics`

---

### Task 2: Bluetooth Lab — adapter state + bonded devices (V1 basic)

**Files:**
- Modify: `feature/bluetooth/build.gradle.kts` — `droidkit.hilt` + junit
- Modify: `feature/bluetooth/src/main/AndroidManifest.xml` — BLUETOOTH, BLUETOOTH_CONNECT
- Create: `feature/bluetooth/.../BluetoothViewModel.kt` — adapter presence/enabled, classic+BLE features, `hasConnectPermission()`, bonded device list (SecurityException-safe), state receiver
- Create: `feature/bluetooth/.../BluetoothScreen.kt` — capability card, permission-gated bonded list (name/address/type/bond state), enable-prompt UX (no silent enable), BLE-scan marked POST-V1
- Modify: `feature/bluetooth/.../BluetoothFeature.kt` — status tool real; scan tool `phase = V2`

- [x] **Step 1: VM + manifest**
- [x] **Step 2: Screen + permission request (ActivityResult BLUETOOTH_CONNECT on 31+)**
- [x] **Step 3: Compile + commit** `feat(bluetooth): adapter state and bonded-device inspector`

---

### Task 3: NFC Lab — scan, inspect, doctor, write, erase

**Files:**
- Modify: `feature/nfc/build.gradle.kts` — `droidkit.hilt` + junit
- Modify: `feature/nfc/src/main/AndroidManifest.xml` — NFC permission + uses-feature nfc required=false
- Create: `feature/nfc/.../NfcTagInfo.kt` — tag parse: id hex, techList, NDEF records → `NdefRecordInfo(tnf, type, payloadHex, text/uri decoded)`; pure decoders `decodeTextRecord`, `decodeUriRecord`, `uriPrefix` (unit-testable), `hex`
- Create: `feature/nfc/.../NfcViewModel.kt` — adapter state, `startScan/stopScan(activity)` via enableReaderMode, `tag: StateFlow<NfcTagInfo?>`, `armedAction` (write/erase) + `armWrite(NdefMessage)`/`armErase()`, doctor verdict builder
- Create: `feature/nfc/.../NfcScreen.kt` — capability/enabled cards, scan toggle, tag result (Identity → Technologies → NDEF records → Doctor), erase action
- Create: `feature/nfc/.../NfcWriteScreen.kt` — record type picker (text/URI), payload input, size preview, confirm dialog → arm → present-tag write result
- Modify: `feature/nfc/.../NfcFeature.kt` — routes `nfc`, `nfc/write`
- Create: `feature/nfc/src/test/.../NfcTagParserTest.kt`

- [x] **Step 1: Parser + VM + manifest**
- [x] **Step 2: Scan/doctor screen + writer screen**
- [x] **Step 3: Compile + commit** `feat(nfc): tag scan, ndef read/write, tag doctor`

---

### Task 4: Release gate — 0.8.0, changelog, checklist, plan, queue

- [x] **Step 1: `0.8.0`/`8` + CHANGELOG**
- [x] **Step 2: J13 ticks + banner + Q15 DONE; hardware-dependent rows → device notes**
- [x] **Step 3: `:app:assembleDebug` green**
- [x] **Step 4: Commit + push**

## Execution notes

- Fill `- [ ]` → `- [x]` as each step lands
- `v0.8.0` tag at merge to `main`
