# 11 — Chips & Status System

Statuses are semantic data, not decoration.

## Families
Capability: Supported / Unsupported / Unknown
Permission: Granted / Required / Denied / Permanently denied where detectable
Provider: Connected / Disconnected / Unavailable / Checking
Availability: Ready / Requirements missing / Unsupported
Operation: Idle / Running / Completed / Warning / Failed / Cancelled where applicable
Diagnostics: Not tested / Testing / Passed / Warning / Failed / Skipped

## Rules
- Icon + text when useful; never color alone.
- A success chip requires verified success.
- Keep independent concepts independent: supported != permission granted != provider connected.
- Avoid raw technical strings.
- Refresh status after resume/return from Android settings/provider grant flows.
- Do not show stale provider/capability state.
- Chips must have semantic descriptions for assistive tech.

Create one reusable status presentation layer mapped from existing domain state, not a second domain model.
