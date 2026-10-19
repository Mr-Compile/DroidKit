# AI + Secrets Acceptance Checklist

## Architecture
- [ ] AIProvider abstraction
- [ ] Groq isolated behind provider
- [ ] core works with AI disabled
- [ ] context builder/privacy filter
- [ ] typed provider errors

## Secrets
- [ ] `.env.example` placeholders only
- [ ] `.env` ignored
- [ ] local/signing secret files ignored
- [ ] no secrets in source/resources/BuildConfig
- [ ] runtime BYOK storage reviewed
- [ ] Authorization/key logging redacted
- [ ] CI secrets external to YAML plaintext
- [ ] secret scanning/push protection considered

## UX
- [ ] online-processing disclosure
- [ ] no-key/offline/auth/rate-limit/provider states
- [ ] clear/disconnect
- [ ] contextual AI entry points

## Safety
- [ ] raw diagnostic data preserved
- [ ] uncertainty represented
- [ ] no arbitrary shell
- [ ] structured action validation
- [ ] risk confirmation
- [ ] prompt-injection tests
- [ ] secrets excluded from context

## Validation
- [ ] targeted unit/network/redaction/malformed-response tests
- [ ] offline manual check
- [ ] repository diff scanned before release
