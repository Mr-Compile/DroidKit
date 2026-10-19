# Friendly Error System
## Golden rule
Never show a raw exception as the primary message when DroidKit can explain it in human language. Never hide useful sanitized technical details from a technician who asks for them.

## Error anatomy
Friendly title; what happened; reason if actually known; impact; primary recovery action; optional secondary action; technical details; severity/recoverability.

Conceptual flow:
System/domain error -> mapper -> semantic UI message/state -> shared UI component.

Avoid scattered `catch(Exception) { "Something went wrong" }`.

## Tone
Direct, calm, non-blaming and accurate.

Prefer: `This API key wasn't accepted. Check that it is active and was copied completely.`
Avoid: `You entered an invalid key.`

## Recovery
Permission -> Allow/Open Settings.
Disabled service -> Open Settings.
USB disconnect -> Reconnect/Try Again.
Offline AI -> Continue Without AI/Retry.
Shizuku stopped -> setup/open guidance.
Rate limit -> Try Later.

## Technical details
Expandable and sanitized. May show safe codes/provider state/HTTP status/VID-PID/timestamp. Never secrets/tokens.
