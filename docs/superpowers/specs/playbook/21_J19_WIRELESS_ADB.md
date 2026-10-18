# Journey 19 - Wireless ADB Provider

## Research first
- [ ] Current Android Wireless Debugging behavior
- [ ] Pairing/auth protocol strategy
- [ ] Library/license decision
- [ ] Key generation/storage
- [ ] Session lifecycle
- [ ] Network threat model
- [ ] Capability map

## Product principle
Prefer structured actions over a raw terminal:
User action -> validated parameters -> known command/action -> result parser -> UI result.

## Implementation
- [ ] Pairing UI
- [ ] Secure credential/key storage
- [ ] Session manager
- [ ] Capability provider
- [ ] Reconnect/timeout
- [ ] Structured package actions
- [ ] Device property actions
- [ ] Authorized log actions
- [ ] Carefully scoped reboot/system actions
- [ ] Optional terminal only after separate approval

## Gate
- [ ] Invalid pairing and disconnect tested
- [ ] Sensitive key material protected
- [ ] Command injection risks reviewed
- [ ] App remains useful without ADB
