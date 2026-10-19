# DroidKit

Android technician & power toolkit — device inspection, guided hardware
diagnostics, app inspection, USB/NFC utilities and reporting. Useful without
elevated access; Shizuku / wireless ADB / (future) root are optional capability
providers that unlock extra tools.

## Status

v0.8.0 — all frozen V1 features implemented and tagged (`v0.1.0`–`v0.8.0` on
`main`): Device Inspector, Phone Doctor (21-test guided diagnostic with saved
reports + text/PDF export), App Inspector, Permission Center, USB Lab,
NFC Lab, Network Lab, Bluetooth basics, Developer Lab (intent launcher +
display metrics) and the Audio/Camera/Display lab entry points. The app is
local-first: no accounts, no telemetry, no background work. Shizuku /
wireless ADB / root providers are post-V1 stubs in the capability chain.

Now in Journey 14 (Integrated Alpha): static integration review passed
(`docs/audits/J14_INTEGRATION_REVIEW.md`); the remaining gates are real-device
and OEM-matrix runs, then `v0.9.0-alpha.1`. See `docs/superpowers/specs/`
for the playbook and master checklist.

## Build

Requirements: Android Studio (bundled JBR is the JDK), Android SDK 36.

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"   # Windows/git-bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
```

## Structure

- `app/` — application wiring, NavHost, bottom navigation
- `core/` — common (tool model/registry), privileges (capability contracts +
  resolver), permissions, system probes, database, datastore, ui (design system)
- `privilege/` — capability providers: standard (working), shizuku/adb/root (stubs)
- `feature/` — one module per feature area; each exposes a `FeatureContribution`
  and a `NavGraphBuilder` extension
- `build-logic/` — Gradle convention plugins

License: Apache-2.0 (see `LICENSE`).
