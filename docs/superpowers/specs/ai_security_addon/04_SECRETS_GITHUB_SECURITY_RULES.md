# Secrets and GitHub Security Rules

## Golden rule
No real secret belongs in Git history.

Includes API keys, backend tokens, signing passwords, service credentials, private keys, ADB keys, database passwords, webhook secrets, and access/refresh tokens.

## Commit
- `.env.example` with fake placeholders
- `.gitignore`
- documentation with placeholders only

## Never commit
- `.env`
- real `.env.*`
- `local.properties`
- keystores/signing property files
- private key files
- production credentials

## Android warning
Secrets placed in BuildConfig, strings.xml, manifest metadata, assets, native libraries, or obfuscated code can still be recovered from an APK. Never ship developer-owned production secrets in the client.

## AI coding-agent rules
Before commit/push:
1. Inspect changed files.
2. Search the diff for credentials/tokens.
3. Verify new local secret files are ignored.
4. Ensure examples use placeholders only.
5. Never print secrets in summaries.
6. Never automatically commit `.env`.
7. Never replace tracked placeholders with real keys.
8. If a secret reached Git history, treat it as compromised and rotate/revoke it.

## GitHub Actions
Use GitHub repository/environment secret mechanisms rather than plaintext workflow YAML. Never echo secrets. Use minimum workflow permissions and do not expose secrets to untrusted PR execution.

## Defense in depth
Enable available GitHub secret scanning/push protection where supported. Consider local/CI secret scanning.

## Signing
Release signing credentials are separate secrets. Never commit keystores or passwords.
