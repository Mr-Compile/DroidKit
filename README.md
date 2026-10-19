# DroidKit

Android technician & power toolkit — device inspection, guided hardware
diagnostics, app inspection, USB/NFC utilities and reporting. Useful without
elevated access; Shizuku / wireless ADB / (future) root are optional capability
providers that unlock extra tools.

## Status

v0.1.0 Foundation — multi-module skeleton. All feature areas are registered in
the ToolRegistry and render capability-gated states; foundation services
(registry, capability resolution, permissions, system probes, Room, DataStore,
design system, navigation) are functional.

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

License: TBD.
