# Security & Privacy Review — DK-073 (V1 static pass)
Date: 2026-10-20 · Scope: repo-manifest, secrets, storage, provider and AI
surface review runnable without a device.

## Secrets & signing
- Repo-wide scan (kt/kts/xml/properties/toml): no API keys, private keys,
  or literal credentials. Only `PrivacyFilter` redaction patterns and
  test fixtures match key-shaped strings.
- `.gitignore` covers `*.jks *.keystore *.p12 *.pfx *.pem .env .env.*` —
  signing material cannot be committed accidentally.
- Release signing is env-driven (`DROIDKIT_*`); CI reads `secrets.*` only.
- BYOK AI key: AES-256-GCM under `AndroidKeyStore` (`AiSettingsStore`),
  ciphertext only in DataStore — plaintext never on disk, logs, or BuildConfig.
- `android:allowBackup="false"` added — diagnostic/report/AI store data no
  longer eligible for cloud or device-to-device backup.

## Manifest / components
- Only `MainActivity` is exported (MAIN/LAUNCHER — required).
- `FileProvider` `exported=false` + `grantUriPermissions` for share flows.
- No `QUERY_ALL_PACKAGES`; `<queries>` is MAIN+LAUNCHER only.
- No `usesCleartextTraffic`/`networkSecurityConfig` override → platform
  defaults (cleartext denied for targetSdk 28+).
- `core:ai` declares only INTERNET + ACCESS_NETWORK_STATE.

## Provider layer
- Elevated work is typed `ElevatedAction` with constructor-validated
  parameters; no raw shell strings reach a command line.
- Provider order SHIZUKU → ROOT → ADB stays inside `core:privileges`.
- Audit log table records elevated actions (Room).

## AI privacy
- AI off by default, BYOK, optional — core works offline/no-key.
- Outbound context passes `AiContextBuilder`/`PrivacyFilter`
  (gsk_/sk-/PEM redaction verified by `PrivacyFilterTest`).
- Model-proposed actions parse into approved schemas; mutations need
  explicit confirmation — no arbitrary command execution path exists.

## Open items for the device/review pass
- [ ] Elevated-action audit log: verify entries on real grant flows.
- [ ] Keystore-unavailable device (no TEE): `secretKey()` failure path —
  confirm `apiKey()` returns null instead of crashing (runCatching present,
  device-verify anyway).
- [ ] Report export share: confirm no secret fields reach PDF/text output.
- [ ] Play-integrity/attestation considerations for GitHub-release APK —
  decide whether v1.0.0 ships signed-by-owner only.
