# DroidKit Privacy Policy (DRAFT)

> **Status: DRAFT for owner review** — required for distribution (J15 item,
> decision D2: GitHub Releases + direct APK). Review before publishing;
> update the contact section and effective date.

**Effective date:** TBD (on release)

DroidKit ("the app") is a local-first Android inspection and diagnostics
tool. This policy describes what the app reads, stores, and transmits.

## Data the app reads

- **Device information** — build, SoC/ABI, memory, storage, display, battery,
  sensor and connectivity capability data, read through public Android APIs.
- **Installed packages** — the set of apps visible under Android 11+
  package-visibility rules. DroidKit does not request
  `QUERY_ALL_PACKAGES`; apps Android hides are not listed.
- **USB/NFC/Bluetooth details** — device descriptors, tag contents and bonded
  device metadata, read only when you open those labs and grant the
  corresponding permission.
- **Diagnostics you run** — test results, timestamps, and notes you type.

## Data the app stores

- Diagnostic sessions, per-test results, device snapshots, saved reports and
  preferences (favorites, recents) are stored **on-device only** in the app's
  private storage (Room database + DataStore). They are never transmitted.
- You can delete sessions and reports individually or entirely from the
  Reports tab. Uninstalling removes all app data.

## Data the app transmits

- **Nothing, by default.** DroidKit has no analytics, no crash reporting, no
  accounts, no ads and no telemetry.
- **Network Lab only when you ask:** ping and DNS lookups contact the host
  you type. That is the only network egress, it is user-initiated, and the
  results stay on-device.
- **Sharing is user-initiated:** report export produces a text/PDF file that
  you choose to share through the Android share sheet. The export sheet
  lists every field class included before anything leaves the app.

## Permissions

Runtime permissions (camera, microphone, Bluetooth connect, NFC) are
requested only when you start the feature that needs them. Declining a
permission limits that feature; the rest of the app keeps working.

## Future optional providers

Shizuku, wireless ADB and the "DroidKit Intelligence" AI add-on are post-V1
options that are disabled by default. Any future provider that transmits
data will get its own disclosure and consent flow before activation, and the
core app will keep working with it disabled.

## Changes

Policy changes ship with release notes in the repository.

## Contact

TBD — repository issue tracker until a contact address is published:
https://github.com/Mr-Compile/DroidKit/issues
