# J02 Feasibility — Per-Tool Research Records

Date: 2026-10-19
Scope: 22 V1 audit items from `playbook/04` + v2 sub-tools (Tag Doctor, NDEF Writer, Component Explorer, Sensor Live Monitor, Network diagnostics, Component Launcher, APK Inspector, Permission Center, Capability Center).
minSdk 28 assumed throughout; anything above API 28 is flagged in Minimum API.

---

## A. Device Intelligence

### Build / device info
- User value: identify exact device/OS for support + diagnostics
- Android API/classes: `android.os.Build` fields (`MANUFACTURER`, `MODEL`, `DEVICE`, `PRODUCT`, `VERSION.RELEASE`, `SDK_INT`, `SECURITY_PATCH` API 23)
- Minimum API: 28 covers all
- Runtime/manifest/special permissions: none
- Hardware feature: none
- Package visibility / OS restrictions: serial/IMEI fields NOT exposed without privileged access — exclude
- Standard access level: full
- Elevated access opportunities: none needed
- OEM differences: `SECURITY_PATCH` string format varies (may be null on odd OEM builds)
- Offline behavior: full
- Unsupported behavior: show `Not exposed` for null patch string
- Privacy/security: low; include in reports with disclosure
- Test devices required: any 1 device
- Decision: V1
- Confidence: Verified

### CPU / ABI
- User value: 32/64-bit + ABI for support decisions
- Android API/classes: `Build.SUPPORTED_ABIS` (API 21), `/proc/cpuinfo` (partially readable)
- Minimum API: 28
- Runtime/manifest/special permissions: none
- Hardware feature: none
- Package visibility / OS restrictions: core frequencies/governors not reliably readable post-API 26; `/proc/cpuinfo` core count partially readable
- Standard access level: ABI + core count only
- Elevated: none needed
- OEM differences: `cpuinfo` field names vary (x86 vs ARM); big.LITTLE layout not exposed
- Offline: full
- Unsupported behavior: report ABIs + core count; label frequency/arch detail `Not exposed`
- Privacy: none
- Test devices: ARM + (x86 emulator optional)
- Decision: V1
- Confidence: Verified (ABI/count); frequency details → show only where readable

### Memory
- Android API/classes: `ActivityManager.MemoryInfo` (`totalMem`, `availMem`, `lowMemory`, `threshold`)
- Minimum API: 28
- Permissions: none
- Hardware: none
- Restrictions: per-process limits (`Runtime.maxMemory`) are app-context, label clearly
- Standard access: full
- OEM: totalMem can differ slightly from marketing RAM (carve-outs) — label as "reported by OS"
- Offline: full
- Unsupported: none expected
- Privacy: none
- Test devices: 1
- Decision: V1
- Confidence: Verified

### Storage
- Android API/classes: `StatFs` on `Environment.getDataDirectory()`, `StorageManager`/`StorageVolume` (API 24+), `context.getExternalFilesDirs`
- Minimum API: 28
- Permissions: none for volumes list; reading other apps' private dirs impossible (correctly)
- Hardware: none
- Restrictions: SD card presence requires a volume; adoptable storage edge cases; physical wear/NAND health NOT exposed — hard boundary
- Standard access: capacity/free/used + volume list
- OEM: volume naming/labels vary
- Offline: full
- Unsupported: volumes list may be internal-only on devices without SD — show as such
- Privacy: show only filesystem metadata, not file contents
- Test devices: 1 internal-only + 1 with SD if available
- Decision: V1
- Confidence: Verified

### Display
- Android API/classes: `DisplayMetrics`, `Display` (`refreshRate`, `supportedModes` API 23, `hdrCapabilities` API 26), `WindowManager` default display API 30 (`context.display` API 30+; `DisplayManager` fallback below)
- Minimum API: 28 (mode enumeration API 23+ covered)
- Permissions: none
- Hardware: display panel
- Restrictions: panel manufacturer/model not exposed; actual color profile not exposed
- OEM: refresh modes may report variable/adaptive ranges (Samsung 48–120); foldables report per-fold metrics
- Offline: full
- Unsupported: HDR capability flag may be absent — `Not exposed`
- Privacy: none
- Test devices: 60 Hz + high-refresh device
- Decision: V1
- Confidence: Verified

### Battery
- Android API/classes: sticky `ACTION_BATTERY_CHANGED` (level/scale/status/plugged/temp/voltage/technology/health), `BatteryManager.getIntProperty` (`BATTERY_PROPERTY_CAPACITY` API 21, `CURRENT_NOW`, `CHARGE_COUNTER`), `PowerManager.isPowerSaveMode`
- Minimum API: 28
- Permissions: none
- Hardware: battery
- Restrictions: `CURRENT_NOW` unsupported on many SoCs (returns 0/MIN_VALUE); health %/cycle count NOT exposed — hard boundary
- OEM: `CURRENT_NOW` sign convention differs (µA charge vs discharge); temp in tenths °C
- Offline: full
- Unsupported: each property individually — `Not exposed` per field
- Privacy: none
- Test devices: 2+ different SoC vendors
- Decision: V1
- Confidence: Verified (sticky intent fields); Needs device test (`CURRENT_NOW`)

### Sensor inventory
- Android API/classes: `SensorManager.getSensorList(TYPE_ALL)`, `Sensor` getters (name/vendor/type/stringType/version/maxRange/resolution/power/minDelay/isWakeUp/reportingMode)
- Minimum API: 28
- Permissions: none for inventory; `BODY_SENSORS` only needed for body sensors at read time
- Hardware: sensors
- Restrictions: vendor drivers expose varying metadata quality
- OEM: sensor names/vendors differ wildly (Bosch/ST/ams + OEM custom)
- Offline: full
- Unsupported: none — empty list is a valid honest state
- Privacy: none
- Test devices: 2 devices for variance
- Decision: V1
- Confidence: Verified

## B. Phone Doctor / Human-Observation Tests

### Touch / multitouch
- User value: dead zones, digitizer faults, multi-finger tracking
- Android API/classes: custom `View.onTouchEvent`, `MotionEvent.getPointerCount`, pointer history
- Minimum API: 28
- Permissions: none
- Hardware: digitizer
- Restrictions: no API reports touch hardware health — human-observation test by design
- OEM: edge gestures (Samsung/Xiaomi) can intercept edge touches — instruct full-screen test
- Offline: full
- Unsupported: if `MotionEvent` never fires, likely dead input — report as observation, not auto-FAIL
- Privacy: none
- Test devices: 1 physical device
- Decision: V1
- Confidence: Verified (as manual test)

### Speaker
- Android API/classes: `ToneGenerator`/`MediaPlayer` test tone; `AudioManager.STREAM_MUSIC` volume control
- Minimum API: 28
- Permissions: none
- Hardware: speaker(s)
- Restrictions: channel balance detection is human-observed; no API for speaker condition
- OEM: mono vs stereo varies; call earpiece is separate routing
- Offline: full
- Unsupported: none expected
- Privacy: none
- Test devices: 1
- Decision: V1 (manual Pass/Fail)
- Confidence: Verified

### Microphone
- Android API/classes: `MediaRecorder` (record + `maxAmplitude`), or `AudioRecord`
- Minimum API: 28
- Permissions: `RECORD_AUDIO` (dangerous, runtime) — plus API 31 `micIndicator` surfacing
- Hardware: mic
- Restrictions: some OEMs restrict concurrent mic use; mic-blocked (privacy toggle API 32+?) → amplitude reads zero → PERMISSION_REQUIRED/blocked state, not FAIL
- OEM: privacy toggles (Xiaomi MIUI, Samsung) can mute system-wide
- Offline: full
- Unsupported: missing mic → NOT_SUPPORTED
- Privacy: recording must not leave the device; discard immediately
- Test devices: 1
- Decision: V1
- Confidence: Verified

### Vibration
- Android API/classes: `Vibrator`/`VibratorManager` (API 31), `VibrationEffect` (API 26), `hasVibrator`/`hasAmplitudeControl`
- Minimum API: 28
- Permissions: `VIBRATE` (normal, manifest-only)
- Hardware: vibrator motor
- Restrictions: amplitude control only where `hasAmplitudeControl()`; effect presets API 29+
- OEM: some budget devices lack motor → `hasVibrator=false` → NOT_SUPPORTED
- Offline: full
- Unsupported: no motor → NOT_SUPPORTED
- Privacy: none
- Test devices: 1
- Decision: V1 (human confirm)
- Confidence: Verified

### Camera / flash
- Android API/classes: `CameraManager.cameraIdList`, `CameraCharacteristics` (LENS_FACING, FLASH_INFO_AVAILABLE, capabilities), `setTorchMode` (API 23), camera preview via CameraX/Camera2 for live test
- Minimum API: 28
- Permissions: none for inventory/torch (`setTorchMode` needs no runtime permission); `CAMERA` runtime permission for live preview test
- Hardware: cameras, flash
- Restrictions: camera in use by another app → `CameraAccessException` → retry/blocked state; flash absent on front
- OEM: torch fails on some devices while camera open; logical/multi-camera ids vary
- Offline: full
- Unsupported: no camera → NOT_SUPPORTED
- Privacy: preview never saved
- Test devices: 1
- Decision: V1
- Confidence: Verified (inventory/torch); Needs device test (live preview flow)

### Core sensor live tests (accel/gyro/prox/light/mag)
- Android API/classes: `SensorManager.registerListener` + `SensorEventListener`, `getDefaultSensor`
- Minimum API: 28
- Permissions: none for these five types (`BODY_SENSORS` not needed — heart-rate excluded)
- Hardware: per-sensor
- Restrictions: sampling rates OEM-throttled; doze/batched delivery — use `SENSOR_DELAY_NORMAL` + note
- OEM: magnetometer uncalibrated state common (accuracy flag unreliable)
- Offline: full
- Unsupported: absent → NOT_SUPPORTED
- Privacy: readings stay on device
- Test devices: 1
- Decision: V1
- Confidence: Verified

### Charging / battery observation
- Same APIs as Battery (above) + observe plug/unplug transitions; human confirms cable/charging state
- Decision: V1
- Confidence: Verified

## C. Connectivity Capability Checks (diagnostic-tier, not full labs)

### Wi-Fi capability
- Android API/classes: `hasSystemFeature(WIFI)`, `WifiManager.isWifiEnabled` (deprecated 29 → `ConnectivityManager`), `WifiInfo` for link details (restricted)
- Minimum API: 28
- Permissions: `ACCESS_WIFI_STATE` (normal); SSID/BSSID need `ACCESS_FINE_LOCATION` + location ON (API 29+)
- Hardware: Wi-Fi radio
- Restrictions: MAC randomization (per-network since API 30); SSID hidden without location grant — show `PERMISSION_REQUIRED`
- OEM: some expose different capability flags
- Offline: capability check is offline
- Unsupported: no Wi-Fi → NOT_SUPPORTED
- Privacy: SSID is sensitive — disclose in reports
- Test devices: 1
- Decision: V1
- Confidence: Verified

### Bluetooth capability
- Android API/classes: `BluetoothAdapter` (state), `hasSystemFeature(BLUETOOTH/BLE)`, `bondedDevices` (needs `BLUETOOTH_CONNECT` API 31+)
- Minimum API: 28
- Permissions: `BLUETOOTH_CONNECT` runtime on API 31+ for device info; none for adapter-state check on ≤30
- Hardware: BT radio
- Restrictions: BLE scan excluded (POST-V1); airplane mode states
- OEM: adapter enable latency varies
- Offline: full
- Unsupported: no BT → NOT_SUPPORTED
- Privacy: bonded list is sensitive — disclose
- Test devices: 1
- Decision: V1 OPTIONAL (basic capability only)
- Confidence: Verified

### Location/GPS capability
- Android API/classes: `hasSystemFeature(LOCATION_GPS)`, `LocationManager` providers (`isProviderEnabled(GPS_PROVIDER)`), fused availability is a Play-services question — avoid depending on it
- Minimum API: 28
- Permissions: none to query providers; `ACCESS_FINE_LOCATION` needed only for an actual fix (not in V1 diagnostic)
- Hardware: GNSS
- Restrictions: provider list vs actual fix are different things — report capability/enabled state only
- OEM: provider naming varies (gps/fused/network)
- Offline: full
- Unsupported: no GPS → NOT_SUPPORTED
- Privacy: location is sensitive — capability check only in V1
- Test devices: 1
- Decision: V1
- Confidence: Verified

### NFC capability
- Android API/classes: `NfcAdapter.getDefaultAdapter`, `isEnabled`
- Minimum API: 28
- Permissions: `NFC` (normal)
- Hardware: NFC
- Restrictions: OEM power-saving can disable NFC silently
- Offline: full
- Unsupported: absent → NOT_SUPPORTED; present-disabled → prompt settings
- Privacy: none
- Test devices: 1 NFC device
- Decision: V1
- Confidence: Verified

### USB capability
- Android API/classes: `hasSystemFeature(USB_HOST)`, `UsbManager`
- Minimum API: 28 (host API 12+)
- Permissions: runtime USB permission via `requestPermission` broadcast (per device)
- Hardware: USB controller + OTG
- Restrictions: OTG support is hardware+firmware — `hasSystemFeature` can lie on odd OEMs; note in matrix
- OEM: OTG toggle exists on some OEMs (Xiaomi "OTG" setting)
- Offline: full
- Unsupported: no host → NOT_SUPPORTED
- Privacy: none
- Test devices: OTG-capable device + adapters/peripherals
- Decision: V1
- Confidence: Needs device test (OTG quirks aside from API)

## D. Application Intelligence

### Package visibility / installed packages
- Android API/classes: `PackageManager.getInstalledPackages`/`getInstalledApplications` (flag `MATCH_UNINSTALLED_PACKAGES` if needed)
- Minimum API: 28
- Permissions: API 30+ — only `<queries>`-visible packages returned unless `QUERY_ALL_PACKAGES` (Play-policy-restricted — NOT used; we distribute via GitHub so technically allowed, but scoped visibility is more honest; V1 shows "packages visible to DroidKit")
- Restrictions: V1 must never claim "all installed apps"; `<queries>` declares intent filters for common categories
- OEM: work profiles / cloned apps appear separately on Samsung/Xiaomi
- Offline: full
- Unsupported: none — empty visibility is a valid state
- Privacy: package list is sensitive — disclose in reports
- Test devices: 1 API 30+ device
- Decision: V1
- Confidence: Verified

### Package components
- Android API/classes: `PackageInfo.activities/services/receivers/providers`, `GET_COMPONENTS`/`GET_DISABLED_COMPONENTS` flags
- Minimum API: 28
- Permissions: same visibility rules as above
- Restrictions: only for visible packages; unexported components listed but not launchable
- OEM: none significant for listing
- Offline: full
- Unsupported: none
- Privacy: metadata only
- Test devices: 1
- Decision: V1
- Confidence: Verified

### Component Explorer (v2)
- Same APIs as Package components; adds per-component detail (exported flag, permission, intent filters)
- Restrictions: launch attempt only for exported components; enable/disable needs elevation — POST-V1
- Decision: V1 (inspection only)
- Confidence: Verified

### Permission inspection
- Android API/classes: `PackageInfo.requestedPermissions` + `requestedPermissionsFlags` (granted flag), `PackageManager.checkPermission`, `AppOpsManager` for op-level state (own app only reliably post-29)
- Minimum API: 28
- Permissions: none
- Restrictions: runtime grant state visible for visible packages; AppOps for other apps restricted since API 29 — V1 shows manifest-level + granted/denied grouping only
- OEM: OEM permission overlays (MIUI autostart etc.) not exposed — document as limitation
- Offline: full
- Unsupported: partial data → label honestly
- Privacy: none beyond package list
- Test devices: 1
- Decision: V1
- Confidence: Verified

### Permission Center (v2 — DroidKit's own)
- Android API/classes: `Context.checkSelfPermission`, `PackageManager.getPackageInfo(own)`; AppOps self via `AppOpsManager.checkOpNoThrow` (API 23+)
- Minimum API: 28
- Permissions: none
- Restrictions: own-app only — always reliable
- Offline: full
- Decision: V1
- Confidence: Verified

### APK Inspector (v2 — metadata only)
- Android API/classes: `ApplicationInfo.sourceDir`, `PackageInfo` fields (versionCode/Name, minSdk via API 24+... actually `applicationInfo.minSdkVersion` API 24+, `targetSdkVersion`), signing info via `GET_SIGNING_CERTIFICATES` (API 28+)
- Minimum API: 28 (signing-info API fits exactly)
- Permissions: visibility rules only
- Restrictions: metadata only — NO decompile/malware claims; raw APK read of other apps is unreliable post-scoped-storage → metadata scope only in V1
- Decision: V1 (metadata only)
- Confidence: Verified

### Component Launcher (v2 — Developer Lab V1 OPTIONAL)
- Android API/classes: `startActivity` with explicit `Intent`, `queryIntentActivities` to check exportability, deep-link intents
- Minimum API: 28
- Permissions: none to launch; target must be exported
- Restrictions: non-exported components can't launch — show as such; exported-but-permission-gated targets may throw SecurityException → caught + reported
- Decision: V1 OPTIONAL
- Confidence: Verified

## E. Hardware Labs

### USB Lab (attach/detach, VID/PID, interfaces)
- Android API/classes: `UsbManager.getDeviceList`, `ACTION_USB_DEVICE_ATTACHED/DETACHED`, `requestPermission`, `UsbDevice` (vendorId/productId/deviceClass/deviceSubclass/deviceProtocol/manufacturerName API 29/productName API 29/serialNumber API 29 — serial needs permission granted), `UsbConfiguration`/`UsbInterface`/`UsbEndpoint` (API 21+)
- Minimum API: 28 (name fields API 29 → guard with `if sdkInt >= 29` → `Not exposed` on 28)
- Permissions: runtime per-device USB permission
- Hardware: host + peripheral
- Restrictions: `getRawDescriptors` API 21+ needs opened connection; serial requires permission; ChipGenius internals NEVER — hard boundary
- OEM: permission dialog presentation varies
- Offline: full
- Unsupported: no host/no devices → NOT_SUPPORTED / empty honest state
- Privacy: serial numbers sensitive — disclose
- Test devices: OTG + mouse/flash drive/serial adapter if available
- Decision: V1
- Confidence: Verified (API); Needs device test (multi-class coverage)

### NFC Lab (Tag Inspector + NDEF read)
- Android API/classes: `NfcAdapter.ReaderMode` (API 19) or foreground dispatch, `Tag.getTechList` (NfcA/B/F/V, IsoDep, MifareClassic, MifareUltralight, Ndef, NdefFormatable), `Ndef.get(tag)` → `getNdefMessage`, `NdefRecord` (TNF/RTD_TEXT/RTD_URI parse)
- Minimum API: 28
- Permissions: `NFC` (normal)
- Hardware: NFC + physical tags
- Restrictions: secure elements (cards, eSE) not readable — document; MifareClassic requires legacy keys for sector data — V1 = tag/tech/NDEF only
- OEM: reader-mode antenna strength varies (polling loop needed on some)
- Offline: full
- Unsupported: no NFC → NOT_SUPPORTED; unsupported tag tech → honest list
- Privacy: tag UIDs disclosed in reports
- Test devices: NFC tags (Type 2/4/5), various
- Decision: V1
- Confidence: Verified (API); Needs device test (tag variety)

### NDEF Writer (v2)
- Android API/classes: `Ndef.writeNdefMessage`, `NdefFormatable.format` for unformatted, `getMaxSize` for size preview
- Minimum API: 28
- Permissions: `NFC`
- Hardware: writable tag
- Restrictions: read-only tags → catch + honest error; mutation → risk level 2 confirmation + size preview
- OEM: tag compatibility variance (some tags accept only certain TNF)
- Offline: full
- Unsupported: read-only/locked tag → report honestly
- Privacy: none beyond tag data
- Test devices: writable NDEF tags
- Decision: V1
- Confidence: Verified (API); Needs device test (tag variety)

### Tag Doctor (v2)
- Android API/classes: tech list + `Ndef.getMaxSize`/`getType`/`isWritable`, connection probe
- Minimum API: 28
- Restrictions: reports PASS/WARNING/UNSUPPORTED — never claims security-state beyond what's readable
- Decision: V1
- Confidence: Verified (API); Needs device test (tag variety)

## F. Network Lab

### Connection Inspector
- Android API/classes: `ConnectivityManager` (`activeNetwork`, `getNetworkCapabilities`, transports WIFI/CELLULAR/ETHERNET/VPN), `LinkProperties` (addresses/DNS/routes API 21+)
- Minimum API: 28
- Permissions: `ACCESS_NETWORK_STATE` (normal)
- Restrictions: MAC/SSID restricted (see Wi-Fi); mobile vs unmetered flags available
- OEM: captive-portal detection state varies
- Offline: local interface info still readable
- Unsupported: no active network → honest empty state
- Privacy: IP/SSID disclosure in reports
- Test devices: Wi-Fi + cellular
- Decision: V1
- Confidence: Verified

### Ping / reachability
- Android API/classes: `InetAddress.isReachable(timeout)` (ICMP — often blocked by carriers, report honestly) or TCP connect probe to host:port via `Socket`
- Minimum API: 28
- Permissions: `INTERNET`
- Restrictions: ICMP blocked on some networks → show method used + result ambiguity; label TCP-vs-ICMP
- Offline: fails → honest failure state
- Privacy: user-entered host only
- Test devices: online
- Decision: V1
- Confidence: Verified

### DNS lookup
- Android API/classes: `InetAddress.getAllByName`, `DnsResolver` (API 29+; plain `InetAddress` on 28)
- Minimum API: 28 (DnsResolver guarded to 29+)
- Permissions: `INTERNET`
- Restrictions: private-DNS/VPN resolvers affect results — note resolver path used
- Offline: fails honestly
- Decision: V1
- Confidence: Verified

### Network layer diagnostics (v2)
- Layer model: interface/link → IP assignment → DNS resolution → reachability; each layer reports independently — NO composite "health score" (v2 hard rule)
- Same APIs as above, composed
- Decision: V1
- Confidence: Verified

## G. Platform

### PDF/export/share
- Android API/classes: `PdfDocument` (API 19) for PDF; plain-text summary; `FileProvider` + `ACTION_SEND` for sharing
- Minimum API: 28
- Permissions: none (app-private storage + FileProvider URI grant)
- Restrictions: PDF is D4 decision — implement at J10
- OEM: share-sheet differences cosmetic
- Offline: full
- Privacy: exported fields previewed before share (v2 rule)
- Test devices: 1
- Decision: V1 (format decided at J10)
- Confidence: Verified

### Capability Center (v2 `23` — V1 shows providers)
- Android API/classes: internal state — reads `CapabilityManager.providerStatuses()`; settings entries to provider docs
- Minimum API: 28
- Permissions: none
- Restrictions: V1 = display + setup guidance only; no provider actions (Shizuku/ADB stubs stay `Unavailable`)
- Decision: V1 (standard-core surface)
- Confidence: Verified

---

## Summary counts

- Verified: 27 tools
- Needs device test (API verified, hardware/OEM variance): Battery `CURRENT_NOW`, camera preview flow, USB multi-class, NFC/NDEF/Tag Doctor tag variety, USB OTG host
- Experimental: none in V1 scope
- Rejected at audit stage: serial/IMEI reads, NAND/controller internals, frequency/arch deep detail, "all packages" visibility claims, AppOps for other apps, composite network health score
