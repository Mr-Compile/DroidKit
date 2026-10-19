# Semantics & Accessibility Contract

Accessibility is part of the component API.

## Compose semantics
- Interactive elements need meaningful accessible names.
- Use roles/states where appropriate.
- Merge descendant semantics for a card that acts as one control.
- Do not create duplicate TalkBack announcements from decorative text/icons.
- Decorative icons/images should not pollute accessibility output.
- Dynamic status changes should be announced only when useful, not spammed.
- Headings should be semantically identifiable where supported by the Compose implementation.
- Error text must be associated with the affected control.

## Touch
Target at least Android-recommended comfortable touch sizes; do not make tiny technical icons the only hit target.

## PASS/FAIL
Accessible label examples:
`Display test, Passed`
`Proximity sensor, Failed`
`NFC, Not supported`
Not merely green/red dots.

## Tool card semantics
Announce:
Tool name -> short purpose -> availability -> action.
Example:
`USB Lab. Inspect connected USB devices. Ready. Button.`

## Data rows
Prefer one coherent announcement:
`Battery temperature, 39 degrees Celsius`
rather than separate unlabeled fragments.

## Progress
Diagnostics expose progress semantically:
`Diagnostic progress, 7 of 14 tests`.

## Text scaling
Layouts must survive large font sizes without clipped actions or hidden values. Allow wrapping; switch row layouts to stacked layouts when needed.

## Contrast
All text/status/action states must meet appropriate contrast. Brand gradient is decorative unless foreground contrast is verified.

## Keyboard/focus
Logical focus order for tablets/desktop-like environments and hardware keyboards.

## Accessibility test checklist
- TalkBack critical flows
- large font
- display scaling
- light/dark
- color-blind-safe status meaning
- landscape/adaptive width
