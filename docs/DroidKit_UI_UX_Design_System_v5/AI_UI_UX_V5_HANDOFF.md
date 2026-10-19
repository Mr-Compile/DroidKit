# AI UI/UX v5 Handoff
Before coding, audit AGENTS.md, current Compose theme/components, navigation, ViewModels, ToolRegistry, CapabilityManager, PermissionManager, domain errors, strings and uncommitted work.

Compare current code vs older plans vs v5. Report conflicts, reusable code, files to change and targeted validation.

Rules:
- Preserve domain architecture.
- Use Material 3 as foundation, not generic identity.
- Use Dark Technical + Minimalism for work screens.
- Bento only where overview hierarchy benefits.
- Aurora/gradient only as controlled brand accents.
- Glass rarely.
- Use semantic tokens/shared components.
- Never hard-code availability.
- Map errors to friendly actionable states.
- Never show raw exceptions as primary copy.
- Keep sanitized technical details optionally available.
- Unsupported, Permission Required, Skipped and Unknown are never automatically Failed.
- Add meaningful Compose/TalkBack semantics.
- Never rely on color alone.
- Respect large text/adaptive widths.
- AI content must be distinct from measured data.
- Never expose secrets.

After editing, identify changed files first and run targeted validation. Broaden only for shared/integration impact.
