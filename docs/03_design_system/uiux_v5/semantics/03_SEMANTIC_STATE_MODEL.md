# Semantic State Model
The UI must understand *why* an action is unavailable, not merely `enabled=false`.

## Screen states
Loading / Content / Empty / PermissionRequired / SetupRequired / Unsupported / Offline / Error.

## Tool availability
Ready / PermissionRequired / SetupRequired / HardwareUnavailable / UnsupportedAndroidVersion / ProviderUnavailable / Offline / TemporarilyUnavailable / Error.

Runtime truth comes from CapabilityManager, PermissionManager and domain logic.

## Diagnostic results
PASS / FAIL / NOT_TESTED / NOT_SUPPORTED / PERMISSION_REQUIRED / UNKNOWN.

Unsupported hardware is not FAIL. Permission denial is not FAIL. Skipped is not FAIL. Unknown must not be guessed.

## Feedback
INFO / SUCCESS / WARNING / ERROR / CRITICAL.
Never rely on color alone.
