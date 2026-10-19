# Shared UX Foundation

Audit existing components before adding anything.

Create/reuse consistent patterns for:
- Loading
- Success
- Empty results
- Offline
- Permission required
- Unsupported device/OS
- Error
- Disabled/unconfigured functionality

Each state should explain what happened and provide the most useful next action such as Retry, Grant Permission, Open Settings, Learn More or Go Back.

Avoid raw exceptions and implementation terminology in user-facing UI.

Where appropriate, centralize reusable behavior for:
- External links
- Friendly errors
- App/package metadata
- Connectivity awareness
- Capability checks
- Section headers/settings rows

Do not over-engineer. Only introduce abstractions that reduce actual duplication in the current repository.
