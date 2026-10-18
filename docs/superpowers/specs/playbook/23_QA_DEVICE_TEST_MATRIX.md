# QA and Device Test Foundation

## Test layers
1. Pure unit tests: parsers, use cases, capability resolution, registry.
2. Module compilation.
3. Compose UI tests for critical state/navigation.
4. Instrumented Android API tests.
5. Manual real-device hardware tests.
6. Release regression.

## Representative device families
Test where available; do not claim universal OEM support from one device.
- Pixel/AOSP-like
- Samsung
- Xiaomi/Redmi/POCO
- OPPO/Realme
- Vivo
- Transsion brands where relevant

## Android versions
Maintain a matrix from minSdk through current supported versions, prioritizing versions actually used by target users.

## Scenario matrix
- Fresh install
- Upgrade
- Permission granted
- Permission denied
- Permanently denied
- Hardware present
- Hardware absent
- NFC/Bluetooth/location disabled
- Offline
- Low storage
- USB attach/detach
- Background/foreground
- Rotation/config change
- Process recreation where relevant
- Dark/light theme
- Large font
- TalkBack/basic accessibility

## Defect record
ID, version, device, Android version, OEM skin, steps, expected, actual, logs/screenshots, severity, regression test.
