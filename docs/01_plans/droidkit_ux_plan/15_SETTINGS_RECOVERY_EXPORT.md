# Settings Recovery, Reset and Export

Implement only if DroidKit has enough persistent configuration to justify these controls.

## Reset to Defaults
- Clearly state what will be reset.
- Require confirmation for destructive changes.
- Do not erase unrelated user files.
- Restore documented defaults consistently.

## Export Settings
If useful, export only non-secret configuration intended to be portable.
Never export API secrets, auth tokens or sensitive identifiers by default.

## Import/Restore
Validate schema/version before applying imported settings.
Reject malformed or incompatible input safely.
Show a preview or clear explanation when practical.

If these capabilities are not justified yet, document them as deferred rather than adding empty UI.
