# Design Tokens

Use semantic token names. Exact color values should be sampled/refined from the approved logo/splash during implementation and checked for contrast.

## Color roles
`brandPrimary` — DroidKit green
`brandSecondary` — teal/cyan companion
`brandGradientStart`
`brandGradientEnd`

`background`
`surface`
`surfaceVariant`
`surfaceElevated`
`outline`
`divider`

`textPrimary`
`textSecondary`
`textDisabled`
`onBrand`

`statusSuccess`
`statusWarning`
`statusError`
`statusInfo`
`statusUnsupported`

Never use status color alone; pair with icon + label.

## Dark theme
Near-black background, slightly lifted graphite surfaces, bright but controlled brand accents.

## Light theme
Warm/neutral white background, subtle gray surfaces, darker brand accent variants where necessary for contrast.

## Spacing scale
4, 8, 12, 16, 20, 24, 32, 40, 48 dp.
Default screen horizontal padding: 16-24 dp depending on width.

## Shape
Small 8dp
Medium 12dp
Large 16dp
Hero 20-24dp
Pills only for chips/status badges.

## Typography roles
Display/Brand
Screen Title
Section Title
Body
Supporting
Label
Technical/Data
Monospace only for IDs, package names, addresses, raw descriptors, hashes, commands.

## Motion
150-250ms typical UI transitions.
Use motion to explain hierarchy/state, not decoration.
Respect reduced-motion/platform accessibility expectations.
Avoid looping decorative animations.

## Iconography
Material-style/simple geometric icons.
Consistent stroke/weight.
Every icon-only actionable control gets a semantic label.
