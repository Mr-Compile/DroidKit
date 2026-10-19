# Journey 19 - Wireless ADB Provider

Status: implementation complete and statically verified. Custom SPAKE2 +
TLS/plain protocol (no third-party ADB library), RSA-2048 identity in
AndroidKeyStore, actions routed through the `ElevatedOps` allow-list.
Pairing/connect round-trips not yet run on hardware — Gate stays open.

## Research first
- [x] Current Android Wireless Debugging behavior
- [x] Pairing/auth protocol strategy
- [x] Library/license decision
- [x] Key generation/storage
- [x] Session lifecycle
- [x] Network threat model
- [x] Capability map

## Product principle
Prefer structured actions over a raw terminal:
User action -> validated parameters -> known command/action -> result parser -> UI result.

## Implementation
- [x] Pairing UI
- [x] Secure credential/key storage
- [x] Session manager
- [x] Capability provider
- [x] Reconnect/timeout
- [x] Structured package actions
- [x] Device property actions
- [x] Authorized log actions
- [x] Carefully scoped reboot/system actions
- [ ] Optional terminal only after separate approval

## Gate
- [ ] Invalid pairing and disconnect tested
- [ ] Sensitive key material protected
- [ ] Command injection risks reviewed
- [ ] App remains useful without ADB
