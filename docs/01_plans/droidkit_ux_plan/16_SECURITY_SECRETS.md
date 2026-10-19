# Security, API Keys and Repository Hygiene

## Mandatory
Never commit real API keys, credentials, tokens or private endpoints.

Audit the repository for existing secret handling before adding AI/network features.

Use environment/configuration mechanisms appropriate to the existing stack. Add example/template configuration with placeholder values only.

Ensure secret-bearing local files are ignored by Git.

Important: `.env` alone does not make a secret safe if its value is compiled into a client APK. Any provider credential that must remain secret should normally live behind a trusted backend/proxy rather than inside the distributed Android application.

Never:
- Print secrets in logs
- Include secrets in crash reports
- Show secrets in About/diagnostic screens
- Commit populated local config
- Place secrets in screenshots/test fixtures

If a key was previously committed, removing it from the latest file is insufficient: flag it for rotation and repository-history remediation.
