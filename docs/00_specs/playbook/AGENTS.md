# AGENTS.md - DroidKit Repository Rules

## Mission
Build DroidKit incrementally as a safe, native Android technician/power-user toolkit.

## Read before editing
1. docs/01_AI_OPERATING_CONTRACT.md
2. docs/02_MASTER_JOURNEY.md
3. current journey document
4. relevant architecture/design documents

## Mandatory workflow
AUDIT -> PLAN -> IMPLEMENT -> TARGETED VALIDATION -> SELF-REVIEW -> UPDATE CHECKLIST

## Scope
- Identify changed files before validation.
- Only test changed files/features by default.
- Do not run full test suite or broad analysis unless shared integration changed or release validation requires it.
- Never modify unrelated code just because it can be improved.

## Architecture
- Kotlin + Compose.
- MVVM/Clean Architecture.
- Features request capabilities through CapabilityManager.
- No direct feature dependency on Shizuku/ADB/root.
- Standard Android provider must remain first-class.
- Room for structured persistent app data; DataStore for preferences.
- Explicit UI states and typed results.

## Android honesty
- Do not fabricate unavailable hardware/system values.
- Missing hardware = NOT_SUPPORTED, not FAIL.
- Permission denial = PERMISSION_REQUIRED, not FAIL.
- Human-observation diagnostics require user confirmation where automation is unreliable.
- Verify API/version/OEM restrictions before promising behavior.

## Safety
- Risk classify mutating operations.
- High-impact actions require explicit consequence + confirmation.
- Prefer structured allow-listed actions over arbitrary shell.
- No credential/FRP/lock bypass, stealth surveillance, unauthorized access, or exploit features.

## Quality
A task is not complete until implemented, validated, self-reviewed, and documented.
Never claim tests passed unless they were actually executed.
Never mark checklist items complete based only on scaffolding.

## Versions
Use SemVer for versionName and monotonically increasing Android versionCode.
Update CHANGELOG and Git tags only when appropriate to the milestone/release.
