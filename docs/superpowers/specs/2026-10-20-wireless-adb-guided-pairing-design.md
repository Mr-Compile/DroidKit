# Wireless ADB guided pairing — design

Date: 2026-10-20
Journey: J19 (Wireless ADB provider)
Status: approved by user, ready for implementation

## Problem

The ADB lab screen requires the user to type IP, pairing port, 6-digit code,
connect host, and connect port. Shizuku-style apps need only the pairing code —
the endpoint is discovered automatically via mDNS. Users also expect both
developer-options entry points ("Pair device with pairing code" and "Pair device
with QR code") to work.

## What already exists

- `AdbPairing` — full `adb pair` equivalent (TLS + SPAKE2 + HKDF-SHA256 +
  AES-128-GCM peer-info). Reuse as-is.
- `AdbConnection` — ADB wire protocol with `A_STLS` TLS upgrade. Reuse as-is.
- `AdbKeys` — RSA-2048 identity in AndroidKeyStore. Reuse as-is.
- `WirelessAdbProvider` — implements `AdbSessionSetup` (pair/connect/disconnect).
- `AdbScreen`/`AdbViewModel` — manual host/port/code UI.

## Constraints

- Feature code never imports `privilege.*` modules — guided flow must surface
  through `core:privileges` interfaces.
- Some OEMs stop advertising `_adb-tls-pairing._tcp` when the dev-settings
  pairing dialog backgrounds — so a foreground service + notification
  RemoteInput (Shizuku's approach) is the robust primary path, with an in-app
  code field as fallback.
- Same-device camera scanning of a QR on the same screen is physically
  impossible. QR support therefore means:
  (a) we display a `WIFI:T:ADB` QR for *another* device to scan, and
  (b) screenshot-decode for devices whose dev settings *display* a pairing QR.
- Pairing port != connect port. `_adb-tls-pairing._tcp` and
  `_adb-tls-connect._tcp` are discovered independently.
- Pairing codes/secrets are transient — never persisted or logged.
- No arbitrary shell: existing `ElevatedAction` allow-list is unchanged.

## Design

### core/privileges — new file `AdbGuidedSetup.kt`

```kotlin
data class AdbEndpoint(val host: String, val port: Int, val serviceName: String)

data class AdbQrPayload(val serviceName: String, val secret: String) {
    fun encode(): String = "WIFI:T:ADB;S:$serviceName;P:$secret;;"
    companion object {
        fun generate(prefix: String = "droidkit"): AdbQrPayload
        fun parse(qrText: String): AdbQrPayload?
    }
}

sealed interface AdbPairingState {
    data object Idle
    data class Searching(val forQr: Boolean)
    data class EndpointFound(val endpoint: AdbEndpoint)
    data object Pairing
    data class Paired(val summary: String)
    data object SearchingConnect
    data object Connecting
    data class Connected(val summary: String)
    data class Failed(val message: String)
}
```

`AdbSessionSetup` gains (non-breaking for other impls — ADB is the only one):

- `val pairingState: StateFlow<AdbPairingState>`
- `fun startPairingSearch(): Result<Unit>` — starts mDNS discovery +
  foreground pairing service (notification flow).
- `fun stopPairingSearch()`
- `suspend fun pairWithDiscovered(code: String): Result<String>`
- `suspend fun connectDiscovered(): Result<String>` — discovers
  `_adb-tls-connect._tcp` and connects (also enables reconnect-without-repair).
- `fun startQrPairing(payload: AdbQrPayload): Result<Unit>` — discovery filtered
  to `payload.serviceName`; on match auto-pairs with `payload.secret`, then
  auto-connects.

### privilege/adb

- `AdbDiscovery` — wraps `NsdManager` discovery + resolve, holds a
  `WifiManager.MulticastLock`, emits resolved `AdbEndpoint`s on a Flow,
  supports an optional service-name filter.
- `AdbPairingService` — `Service` (foregroundServiceType `dataSync`) that
  observes `WirelessAdbProvider.instance.pairingState` and renders a
  notification: searching → reply-input (RemoteInput) → working → result.
  Reply calls `provider.pairWithDiscovered(code)`. Stop action cancels.
  Provider instance exposed via `companion object` (DI singleton is created
  before the service can ever be started from the UI).
- `WirelessAdbProvider(context)` — owns a private `CoroutineScope`,
  `AdbDiscovery`, and `pairingState`. Auto-chains: pair success → connect
  discovery → connect. Same-device endpoints probed on `127.0.0.1` first;
  QR/remote endpoints use the resolved host.

### Permissions / manifest (privilege/adb)

`CHANGE_WIFI_MULTICAST_STATE`, `ACCESS_WIFI_STATE`, `ACCESS_NETWORK_STATE`,
`POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`,
`<service android:name=".AdbPairingService" android:exported="false"
android:foregroundServiceType="dataSync"/>`

### feature/adb

- Guided card: status line driven by `pairingState`, "Start pairing" button
  (requests `POST_NOTIFICATIONS` on 33+, starts search + deep-link button to
  `Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS` with
  `:settings:fragment_args_key = "toggle_adb_wireless"`), 6-digit code field
  (in-app fallback), auto connect.
- QR card: "Show QR to pair another device" (zxing render of
  `AdbQrPayload.generate().encode()`) and "Import pairing QR screenshot"
  (Photo Picker → zxing decode → `startQrPairing`).
- "Advanced — manual endpoint" card keeps the existing host/port/code fields.
- `com.google.zxing:core` added to catalog + feature build (QR render + decode;
  approved by user).

## Validation

- Unit tests: `AdbQrPayload` parse/generate round-trip in core/privileges.
- `./gradlew :privilege:adb:assembleDebug :feature:adb:assembleDebug`
  plus existing module tests.
- Real-device round-trip remains pending hardware (J19 gate stays open).

## Security

- Pairing codes/QR secrets: transient, never logged, never persisted.
- ADB private key stays in AndroidKeyStore.
- No new `ElevatedAction`s; no raw shell.
