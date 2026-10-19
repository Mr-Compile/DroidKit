# Compose Mapping Guide

This is conceptual; audit actual package names before coding.

## Suggested shared UI mapping
`core:ui/theme/`
- Color.kt / semantic color scheme
- Type.kt
- Shape.kt
- Spacing.kt
- DroidKitTheme.kt

`core:ui/components/`
- cards
- rows
- states
- status
- diagnostics
- risk
- AI

## Feature pattern
`FeatureRoute`:
collect state, navigation callbacks, permission/setup callbacks.

`FeatureScreen`:
stateless/state-driven composable suitable for previews/tests.

`FeatureViewModel`:
owns orchestration/state; no Compose types where avoidable.

## Preview strategy
Preview representative states:
Content
PermissionRequired
Unsupported
Error
Dark/Light
Large text where feasible

## Strings
User-visible strings belong in resources/localization strategy, not scattered literals.

## Test tags
Use sparingly for stable automation hooks; accessibility content descriptions are not test-ID dumping grounds.
