# DroidKit Definition of Done

A task is not DONE merely because code exists.

## Code-level DoD
- Current implementation was inspected before editing.
- Existing architecture/components were reused where appropriate.
- No duplicate registry/capability/permission/error/navigation system was introduced.
- No unrelated refactor was bundled into the task.
- Error/unsupported/permission states remain truthful.
- Privileged actions remain allow-listed and validated.
- Sensitive information is not newly logged or exposed.

## Verification DoD
- Relevant unit tests added/updated.
- Debug build passes.
- Full unit suite passes for cross-cutting changes.
- Lint/static analysis passes once configured.
- Instrumented/UI test added where JVM testing cannot prove behavior and automation is practical.
- Device-required behavior is not called verified until run on a real/emulated target appropriate to that behavior.

## UX DoD
- Loading/empty/error/unsupported/permission/provider states are considered.
- User receives a useful recovery action when recovery is possible.
- Technical details do not replace friendly primary copy.
- Accessibility semantics are present for new/changed interactive UI.
- Status is not communicated by color alone.
- Large text does not make the changed primary flow unusable.

## Completion evidence
Every completed task must report:
- changed files
- tests
- commands/results
- screenshots/device notes when relevant
- known limitations
- follow-up work
