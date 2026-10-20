# Device Capability Depth — 2026-10-21

Owner-approved scope extension (migration-log entry 2026-10-21): "unlock the
capabilities of the phone we have." Three concrete deliverables: iOS-style
battery health, owner-facing saved Wi-Fi passwords (root-only), and a deeper
Device Inspector.

## Status

| # | Item | State |
|---|------|-------|
| 1 | `ElevatedAction.requiredProvider` pin + read-only actions | implemented + tested |
| 2 | Saved Wi-Fi Networks tool (root) | implemented + tested |
| 3 | Device Inspector depth (standard + elevated tiers) | implemented + tested |
| 4 | Docs, mockup, migration log | done |
| 5 | Real-device validation | pending — hardware/root required |

## Architecture decisions

- **Provider pinning**: `ElevatedAction.requiredProvider` — when set,
  `ElevatedOps.execute` runs only on that kind; a pinned action returns
  `requires <KIND>` instead of degrading. `ReadWifiConfigStore` is pinned to
  ROOT because `/data/misc/**` is not readable by the shell identity
  (Shizuku/wireless-ADB).
- **Credential contract**: secrets are display-only — FLAG_SECURE screen,
  masked until reveal-on-tap, no clipboard/share/export, never persisted,
  never audit-logged (only the command string is logged), never sent to AI.
  Derived 64-hex PSKs labeled "PSK (hex)", never shown as the passphrase.
- **Honesty tiers**: standard API fields → always shown; elevated reads →
  on-demand "Advanced detail" card labeled with the serving provider;
  unexposed values render "Not exposed", never fabricated.

## Implementation map

- `core/privileges/ElevatedOps.kt` — pin mechanism; `ReadWifiConfigStore`
  (root), `ReadSystemProps`, `ReadThermalZones`, `ReadCpuInfo`,
  `ReadMeminfo`, `ReadPartitions`, `DumpBatteryDetail`,
  `ReadLearnedBatteryCapacity`, `DumpWifiLink`, `ReadPowerSupply`.
- `feature/network/` — `SavedWifiParser` (WifiConfigStore.xml +
  wpa_supplicant.conf, `== path` sectioning, hex-PSK detection,
  wpa escapes), `SavedNetworksViewModel` (requires-ROOT/empty/ready/failed
  states), `SavedNetworksScreen` (FLAG_SECURE + reveal), `network.saved`
  tool (`Capability.ROOT`, V2, route `network/saved`), Network Lab entry card.
- `core/system/DeviceSystemProbe.kt` — fingerprint/build IDs/tags, kernel
  (`/proc/version`), uptime + approx deep sleep, ART, SoC model/manufacturer
  (API 31+), baseband, bootloader, codec counts, Widevine level, HDR types,
  wide-color-gamut, charge counter + cycle count (API 34).
- `feature/deviceinfo/` — `DeviceDeepProbe` (elevated snapshot: curated
  getprop allow-list, thermal zones, /proc cpuinfo+meminfo+partitions,
  dumpsys battery + learned capacity, dumpsys wifi link slice), on-demand
  load in `DeviceInfoViewModel`, `AdvancedDetailRows` card in the screen.
- `feature/battery/` — `FuelGauge` (charge_full/design ratio, direct SoH,
  cycle count), `BatteryHealth` (AccuBattery-style session estimator in
  DataStore), Battery Lab "Health" + "Capacity estimate" sections, elevated
  measure via `ReadPowerSupply`, Permission Center link.

## Known limitations

- Wi-Fi store paths vary by Android/OEM (apexdata XML on API 29+,
  `/data/misc/wifi` legacy, `wpa_supplicant.conf` pre-10); root may still be
  denied by the root manager or SELinux — the tool reports it honestly.
- Fuel-gauge sysfs nodes are OEM-dependent; many devices expose none —
  measured health % shows only when `charge_full(_design)`/`soh`/`asoc`
  nodes are readable. The session estimator is labeled an estimate, never a
  measured value.
- Elevated Advanced detail depends on whichever provider is active; each
  action failure lists under "Not exposed".

## Validation

- `:core:privileges:testDebugUnitTest` — action command safety, injection
  rejection, provider routing/pinning.
- `:feature:network:testDebugUnitTest` — `SavedWifiParserTest` (XML + conf
  fixtures, hex PSK, open network, escapes, source selection).
- `:feature:deviceinfo:testDebugUnitTest` — `DeviceDeepProbeParserTest`
  (props, thermal, cpuinfo, meminfo, partitions, battery dump, learned
  capacity units, wifi link).
- `:feature:battery:testDebugUnitTest`, `:core:system:testDebugUnitTest` —
  fuel-gauge parsing/estimator and probe formatting.
- Mockup: `network/saved` screen + Saved networks card + expanded Device
  Inspector + Advanced detail card.
