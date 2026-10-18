# Environment Configuration

## `.env.example`
Tracked. Variable names and fake placeholders only.

## `.env`
Untracked local developer configuration only.

## Runtime BYOK key
Entered by the user inside DroidKit and stored with an Android security design appropriate to the current platform. It is not copied into `.env`.

## Production server key
Stored only in backend/deployment secret management.

## Suggested names
GROQ_API_KEY
DROIDKIT_AI_PROVIDER
DROIDKIT_AI_BASE_URL

Never call a sensitive credential `PUBLIC_*`.

## Logging
Redact Authorization headers, API keys, tokens, cookies, and sensitive credentials.

## Release rule
Release builds must never silently bundle a developer API key.
