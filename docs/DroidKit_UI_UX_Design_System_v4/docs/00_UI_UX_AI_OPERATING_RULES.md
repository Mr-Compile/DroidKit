# UI/UX AI Operating Rules

## Before editing UI
The AI must audit:
- existing Compose theme/tokens/components
- navigation routes
- ViewModels and UiState models
- ToolRegistry metadata
- CapabilityManager states
- PermissionManager behavior
- existing strings/resources
- changed/uncommitted files

Then report:
Current screen -> existing behavior -> v4 target -> reusable components -> files to change -> validation plan.

## Rules
- Never rewrite business logic just to make a screen easier to build.
- Compose consumes state; it does not own system/privilege logic.
- Reuse semantic tokens and components before creating one-off styling.
- No raw hex colors, arbitrary spacing, or duplicated typography inside feature screens once tokens exist.
- Do not hard-code availability. Render ToolRegistry/CapabilityManager results.
- Permission-required, unsupported, setup-required, offline, empty, loading, success, and error states must be explicit where relevant.
- Never hide important actions behind unlabeled icons.
- Destructive actions must visually communicate risk without relying on color alone.
- Avoid excessive glow/glass effects. DroidKit should feel technical and premium, not noisy.
- Preserve Android conventions and back behavior.
- Identify changed files first and run targeted UI/Compose validation. Broaden only for shared theme/navigation/component changes or release QA.

## State-driven UI contract
Prefer a screen model similar to:
`Loading | Content(data) | Empty | PermissionRequired | SetupRequired | Unsupported(reason) | Error(message, retry)`

Do not infer capability from UI text.

## Responsive/adaptive rule
Build layouts from window size and available width, not from a single phone screenshot.
