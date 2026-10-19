# Design Asset Rules

## Included references
`assets/references/droidkit_logo_candidates_reference.png`
`assets/references/droidkit_splash_reference.png`

## Production assets
Before release, create clean source assets from the selected Candidate 4 identity:
- app icon foreground/background/adaptive icon
- monochrome icon where Android requires/supports it
- wordmark if actually needed
- vector/appropriate raster mark
- splash icon asset

Do not crop the candidate board and ship that as the app icon.

## Asset behavior
- Logo mark must work without wordmark.
- Test at launcher-icon sizes.
- Avoid baked-in background when adaptive icon requires layers.
- Decorative splash background should not be essential to recognition.
- Provide light/dark-compatible brand usage.

## AI rule
If exact production logo vector is not yet available, use a placeholder/reference and create a tracked design task. Do not invent a subtly different logo on each screen.
