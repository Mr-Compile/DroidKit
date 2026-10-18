# Camera, Display & Audio Labs v2

## Camera Lab
V1: enumerate cameras/capabilities, front/rear preview, flash support, resolutions/capabilities where Camera2/CameraX exposes them, diagnostic capture/preview.
Post-V1: deeper Camera2 characteristic inspector for developers.

## Display Lab
V1: resolution/density/refresh modes, full-screen color/dead-pixel tests, touch visualization, multitouch.
Post-V1: motion/refresh test patterns and screenshot-safe reference patterns.

## Audio Lab
V1: speaker test tones/media, microphone record/playback, available output/input routing information where Android exposes it.
Post-V1: channel/frequency-oriented diagnostic helpers with careful claims.

## Rule
These labs feed Phone Doctor but are also independently launchable from Tools.

## Shared implementation requirements
- Explicit loading/content/permission-required/unsupported/error states.
- Hardware absence must not be reported as a failed test.
- Never fabricate inaccessible Android values.
- Every mutating operation requires a preview/confirmation appropriate to risk.
- Register every sub-tool in ToolRegistry where it is independently launchable.
- CapabilityManager owns elevated-provider selection.
- Keep business logic out of Compose UI.
- Record known OEM/API limitations.

## Completion gate
- [ ] Feasibility verified.
- [ ] Scope tier confirmed.
- [ ] UX states implemented.
- [ ] Permissions handled.
- [ ] Unsupported hardware/API handled.
- [ ] Targeted validation passed.
- [ ] Real-device validation completed where hardware-dependent.
- [ ] Documentation/checklist updated.
