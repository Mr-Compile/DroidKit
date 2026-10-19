# DroidKit - Feature, API, Capability, and Release Matrix

Legend:
- Standard = normal Android framework access
- Elevated = Shizuku or ADB can provide additional supported access
- Root = optional future backend
- Exact behavior must be verified against the Android versions targeted during implementation.

| Feature | Primary Android/API Area | Standard | Elevated | Root | Planned |
|---|---|---:|---:|---:|---|
| Device/build info | Build, PackageManager | Yes | No | No | V1 |
| CPU/ABI | Build.SUPPORTED_ABIS, system data | Yes | No | No | V1 |
| Memory | ActivityManager | Yes | No | No | V1 |
| Storage | Storage APIs | Yes | No | No | V1 |
| Display metrics | Display/Window APIs | Yes | No | No | V1 |
| Battery status/temp/charging | BatteryManager / battery intents | Yes | No | No | V1 |
| Sensor inventory | SensorManager | Yes | No | No | V1 |
| Touch/multitouch test | Compose/View input APIs | Yes | No | No | V1 |
| Display/dead-pixel test | Compose/display UI | Yes | No | No | V1 |
| Speaker test | Audio APIs | Yes | No | No | V1 |
| Microphone test | AudioRecord/MediaRecorder as appropriate | Permission | No | No | V1 |
| Camera test | CameraX/CameraManager | Permission | No | No | V1 |
| Flash test | Camera APIs | Permission/feature | No | No | V1 |
| Vibration test | Vibrator APIs | Yes | No | No | V1 |
| Wi-Fi/network info | Connectivity/Wi-Fi APIs | Limited by OS | Maybe | Maybe | V1 |
| Bluetooth/BLE inspection | Bluetooth APIs | Permission | No | No | V1 |
| Location/GPS capability test | Location APIs | Permission | No | No | V1 |
| USB inspection | UsbManager | Yes/USB permission | No | No | V1 |
| NFC read/write | NFC APIs | Yes/hardware | No | No | V1 |
| Package metadata | PackageManager | Yes, subject to package visibility | No | No | V1 |
| App components | PackageManager | Yes, subject to OS restrictions | No | No | V1 |
| Permission inspection | PackageManager | Yes | No | No | V1 |
| AppOps control | AppOps/system privileges | Limited | Yes | Yes | V2 |
| Advanced package control | Package/system privileges | Limited | Yes | Yes | V2 |
| Logcat/advanced logs | logging permissions/shell | Limited | Yes | Yes | V2 |
| Wireless ADB actions | ADB protocol | No | Yes via ADB | N/A | V2 |
| Root system operations | su/root APIs | No | No | Yes | Future |

## Feasibility Rule
Before implementation, each feature receives a short technical spike verifying:
- Android versions supported
- Permissions
- OEM differences
- Data actually exposed
- Play distribution implications if relevant
- Behavior without hardware
- Behavior when permission is denied
