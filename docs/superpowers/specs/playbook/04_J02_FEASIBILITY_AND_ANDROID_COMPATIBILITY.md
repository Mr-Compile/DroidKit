# Journey 02 - Feasibility and Android Compatibility

## Objective
Prove what DroidKit can reliably do before implementation.

## Required research record for every tool
- Feature name
- User value
- Android API/classes
- Minimum API
- Runtime/manifest/special permissions
- Hardware feature
- Package visibility or OS restrictions
- Standard access level
- Elevated access opportunities
- OEM differences
- Offline behavior
- Unsupported behavior
- Privacy/security concerns
- Test devices required
- Decision: V1 / Post-V1 / Reject
- Confidence: Verified / Needs device test / Experimental

## V1 audit checklist
- [ ] Build/device info
- [ ] CPU/ABI
- [ ] Memory
- [ ] Storage
- [ ] Display
- [ ] Battery
- [ ] Sensors
- [ ] Touch/multitouch
- [ ] Speaker
- [ ] Microphone
- [ ] Vibration
- [ ] Camera/flash
- [ ] Proximity/light/accelerometer/gyro/magnetometer
- [ ] Wi-Fi/network
- [ ] Bluetooth
- [ ] Location/GPS capability
- [ ] USB Host
- [ ] NFC/NDEF
- [ ] Package visibility
- [ ] Package components
- [ ] Permission inspection
- [ ] PDF/export/share

## Required compatibility outputs
- [ ] Android API-level matrix
- [ ] OEM risk matrix
- [ ] Permission matrix
- [ ] Hardware matrix
- [ ] Feature decision matrix
- [ ] Known limitations list

## Gate
Do not start feature development until V1 features are classified and the scope is frozen.
