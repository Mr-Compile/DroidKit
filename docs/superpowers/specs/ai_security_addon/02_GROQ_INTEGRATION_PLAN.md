# Groq Integration Plan

## Research first
Before implementation, verify current official Groq API documentation, authentication, endpoints/SDK guidance, supported models, rate limits, and terms. Do not freeze old model names into planning.

## BYOK mode
For a GitHub/power-user build, users may provide their own Groq key.

- Never commit the key.
- Never place a real key in source, resources, BuildConfig, tests, screenshots, logs, reports, or documentation.
- A user-entered runtime key is not stored in a plaintext `.env` on Android.
- Use an Android credential-storage design reviewed against current platform security guidance.
- Provide Test Connection and Clear/Disconnect.
- Never display the full saved key.

## Hosted mode
For a future commercial/hosted DroidKit:
DroidKit -> authenticated DroidKit backend -> Groq.

The production Groq key stays server-side. Add authentication, quotas, abuse controls, rate limits, rotation, and privacy controls.

## Important
A development `.env` is useful for local tooling but does NOT make a secret safe if that value is compiled into a distributed APK.

## Error states
NO_KEY, OFFLINE, AUTH_ERROR, RATE_LIMITED, PROVIDER_ERROR, MALFORMED_RESPONSE, DISABLED.
