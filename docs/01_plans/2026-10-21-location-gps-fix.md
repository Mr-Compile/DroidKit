# Phone Doctor location test — live coordinates read

Date: 2026-10-21
Status: design approved, pending implementation
Scope: `feature:diagnostics` + manifest + mockup + tracker entries

## Background

`connectivity.location` (Phone Doctor → Connectivity) is a capability-only
check: `FEATURE_LOCATION_GPS` plus `LocationManager.isLocationEnabled`. J02
feasibility (`J02_TOOL_RECORDS.md`) explicitly scoped an actual fix out of V1:
"ACCESS_FINE_LOCATION needed only for an actual fix (not in V1 diagnostic)".
Owner approved extending it — the test now reads coordinates while keeping the
capability check intact.

## Goals

- Technician sees the device's current coordinates: lat/long, accuracy,
  provider, fix age.
- Prove GNSS acquires a fix on demand, not only that the hardware exists.
- Guided runner stays instant — no multi-second stall inside a sweep.

## Non-goals

- No continuous tracking, logging, map, or geocoding.
- No FusedLocationProvider / Play Services dependency — framework
  `LocationManager` only.
- Raw coordinates never enter report/share/AI fields (privacy default, matches
  the PLAN_MIGRATION_LOG identifier-exclusion decision).

## Design

### Routing

New `PhoneDoctorRoute.LocationCheck`. In `routeFor`,
`test.id == "connectivity.location"` matches before the generic `capabilityKey`
branch. `PhoneDoctorTestScreen` dispatches to the new `LocationCheckScreen`.
The catalog invariant (`PhoneDoctorCatalogTest`: no test resolves to
`Unsupported`) is unaffected.

### Permission

Screen content wrapped in the existing
`PermissionGate(ACCESS_FINE_LOCATION)`:

- denied in the guided runner → auto-emits `PERMISSION_REQUIRED`
- permanent denial → "Open Settings" recovery

Manifest: declare `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION` uncapped in
`feature/diagnostics/src/main/AndroidManifest.xml`. Bluetooth's declaration is
`maxSdkVersion="30"` and does not cover API 31+; Android 12+ needs both
permissions declared for the precise/approximate chooser.

### Reads

Instant, on entry with permission granted:

- `FEATURE_LOCATION_GPS` (existing)
- `isLocationEnabled` + enabled providers (gps / network / fused)
- `getLastKnownLocation` across providers → newest fix: lat/long, accuracy,
  provider, age
- null everywhere → "No cached fix", never fabricated

Live fix ("Get live fix" button):

- API 30+: `LocationManager.getCurrentLocation(provider, CancellationSignal,
  executor, consumer)`
- API 28–29: `requestSingleUpdate(provider, listener, looper)` (deprecated on
  30+, still correct below it)
- Provider: `gps` when enabled and precise granted, else `network`
- ~20 s timeout via `CancellationSignal` (legacy path: `removeUpdates` +
  handler timeout); cancel on dispose
- Android 12+ approximate-only grant → network fixes still work; the GPS row
  notes "precise location off"

### Result matrix

| Condition | State |
|---|---|
| No GNSS feature | NOT_SUPPORTED (unchanged) |
| Permission denied | PERMISSION_REQUIRED |
| Location off | "Location is off" + `Settings.ACTION_LOCATION_SOURCE_SETTINGS` button |
| Live-fix timeout | UNKNOWN — "no fix within 20 s — may be indoors or obstructed", never FAIL |
| Fix obtained | shown on screen; PASS note carries provider + accuracy + age |

### Guided runner (`autoResult = true`)

Emit stays instant: NOT_SUPPORTED / PERMISSION_REQUIRED / PASS with a note like
`Location enabled: yes · last fix: gps, ~12 m, 40 s ago` (or "no cached fix").
The live-fix button is standalone-only; the runner never waits for GNSS.

### Privacy

Raw lat/long is screen-only. The emitted note — which flows into
report/share/AI fields — carries fix quality (provider, accuracy, age), never
coordinates. Consistent with the SIM/Android-ID exclusion already logged.

## Files

- `feature/diagnostics/.../tests/LocationCheckScreen.kt` — new
- `feature/diagnostics/.../PhoneDoctorRoute.kt` — `LocationCheck` enum + id branch
- `feature/diagnostics/.../PhoneDoctorTestScreen.kt` — dispatch case
- `feature/diagnostics/.../PhoneDoctorTest.kt` — updated instructions string
- `feature/diagnostics/src/main/AndroidManifest.xml` — FINE + COARSE declarations
- `mockups/index.html` — location test screen update (same commit, per AGENTS.md)
- `docs/02_foundations/PLAN_MIGRATION_LOG.md` — V1-scope extension entry
- `docs/01_plans/10_DroidKit_Master_Journey_Checklist.md` — checklist line

## Validation

- `:feature:diagnostics` compiles; `PhoneDoctorCatalogTest` green.
- On device: deny → PERMISSION_REQUIRED; location off → settings link; indoor
  timeout → honest UNKNOWN; outdoor fix → real coordinates on screen.
