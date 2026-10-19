# J03 Design Tokens

Date: 2026-10-19
Maps to Material3 in `:core:ui` (skeleton Task 9). Values are the defaults; implementers use token names, never raw literals.

## Typography (M3 `Typography`)

| Token | Size/Weight | Use |
|---|---|---|
| `headlineSmall` | 24 / w600 | Screen titles |
| `titleMedium` | 16 / w600 | Section card titles |
| `titleSmall` | 14 / w500 | Card headers, subsections |
| `bodyLarge` | 16 / w400 | Primary content |
| `bodyMedium` | 14 / w400 | Info rows, tool descriptions |
| `bodySmall` | 12 / w400 | Hints, timestamps |
| `labelLarge` | 14 / w500 | Buttons, chips |
| `labelSmall` | 11 / w500 | Status badges, capability tags |
| `bodyMedium` (mono) | `FontFamily.Monospace` | VID/PID, serials, hex, paths, commands |

## Spacing ramp

| Token | dp | Use |
|---|---|---|
| `spaceXs` | 4 | Inline gaps, icon-text |
| `spaceSm` | 8 | Row internal padding, chip gaps |
| `spaceMd` | 12 | Card internal padding (dense) |
| `spaceLg` | 16 | Card padding, screen horizontal padding |
| `spaceXl` | 24 | Between sections |
| `spaceXxl` | 32 | Top-of-screen breathing room |
| `touchTarget` | 48 | Minimum interactive target (a11y floor) |
| `listItem` | 56 | Standard row height |
| `cardGap` | 8 | `spacedBy` between cards |

## Shape / radius

| Token | dp | Use |
|---|---|---|
| `shapeSm` | 8 | Chips, badges, small buttons |
| `shapeMd` | 12 | Cards, dialogs |
| `shapeLg` | 16 | Bottom sheets, prominent cards |
| `shapeFull` | 50% | FABs, status dots |

## Elevation / surfaces

| Token | dp | Use |
|---|---|---|
| `level0` | 0 | App background |
| `level1` | 1 | Cards at rest |
| `level2` | 3 | Raised cards, top bar |
| `level3` | 6 | Dialogs, sheets |
| `level4` | 8 | FAB, menus |

## Semantic colors (M3 `ColorScheme` roles)

| Token | M3 role | Use |
|---|---|---|
| `statusPass` | `primary` (green-leaning custom or tertiary) | PASS results, ready badges |
| `statusFail` | `error` | FAIL results, destructive accents |
| `statusNotTested` | `outline`/`surfaceVariant` text | NOT_TESTED, neutral |
| `statusNotSupported` | `tertiary`/muted | NOT_SUPPORTED, unavailable |
| `statusPermission` | `secondary` | PERMISSION_REQUIRED, prompts |
| `statusUnknown` | `outlineVariant` | UNKNOWN |
| `risk0`–`risk4` | pass → error gradient (tertiary→error) | Risk level accents on confirmations |
| Diagnostic states must pair color + icon + label — never color alone | | |

## Icon rules

- Material Symbols set; `imageVector` constants, no asset PNGs.
- Meaningful icons get `contentDescription`; decorative get `null`.
- Status icons: `check_circle` PASS, `cancel` FAIL, `radio_button_unchecked` NOT_TESTED, `block` NOT_SUPPORTED, `lock` PERMISSION_REQUIRED, `help` UNKNOWN.
- Labs use consistent pairings: `usb`, `nfc`, `wifi`, `bluetooth`, `sensors`, `battery_full`, `memory`, `apps`, `bug_report` (developer), `description` (reports).

## Motion rules

- Standard Material motion; `spring` for cards entering, `tween(150)` for state fades.
- Live sensor readouts update in place — no animation on value change.
- Respect `animatorDurationScale` — test with animations off.

## Themes

- `lightColorScheme` + `darkColorScheme` in `:core:ui` `DroidKitTheme`.
- Dynamic color: opt-in via Settings (default OFF for predictability on odd OEM palettes).
- Status colors fixed (not dynamic) — a PASS must look like a PASS on every device.
- Contrast floor: WCAG AA for all status text (4.5:1 body, 3:1 large).
