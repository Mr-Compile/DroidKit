# AI & Online Services

## Destination
Settings → AI & Online Services

## Goal
Make online and AI-powered behavior transparent and controllable.

If Groq or another AI provider exists, show appropriate user-facing status/configuration without exposing secrets.

Potential information:
- AI features enabled/disabled
- Provider name where useful
- Online/offline requirement
- Which DroidKit features use AI
- Privacy/data explanation
- Service availability/error state

Before sending diagnostics or device details to AI, clearly identify the categories of information being sent when meaningful. Never silently include personal files, tokens, credentials or unrelated sensitive information.

AI output must be treated as assistance, not guaranteed diagnosis.

API keys must not be embedded in the distributable client if doing so exposes the secret. Prefer a secure backend/proxy or another architecture appropriate to the existing project.
