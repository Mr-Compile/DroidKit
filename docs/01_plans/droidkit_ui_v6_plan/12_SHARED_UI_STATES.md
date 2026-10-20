# 12 — Loading / Empty / Error / Blocked States

Standardize:
- Loading/Checking
- Content
- Empty
- Permission required
- Provider required
- Unsupported hardware/API
- Offline
- Warning
- Error
- Success/completed where a persistent state is useful

Every recoverable state should offer the correct next action: Retry, Grant permission, Open Settings, Connect provider, Learn more, etc.

Never surface exception class names, stderr, stack traces or raw `t.message` as primary user copy. Preserve technical detail only in appropriate logs/debug/audit surfaces.
