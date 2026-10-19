# DroidKit Brand Kit

Source of truth for the DroidKit logo, colors, and taglines. The mark is
Candidate 4 (abstract / geometric): a bold, vertically stretched hexagonal
band — split into a large left C-piece (wrapping the top vertex, left side,
and bottom vertex) and a right bracket, with gaps at the upper-right edge
and the bottom vertex — around a light-green vertical capsule "tool bit"
glyph, drawn with a green→teal gradient.

## Files

| File | Use |
|---|---|
| `droidkit-mark.svg` | Mark only, transparent background. Avatars, favicons, docs. |
| `droidkit-logo.svg` | Vertical lockup on ink tile: mark + wordmark + primary tagline. README headers, splash/about graphics, store assets. |

In-app counterparts:

- `core/ui/.../theme/Brand.kt` — `DroidKitBrand` colors + `DroidKitMark` Compose `ImageVector`.
- `core/ui/.../components/DroidKitWordmark.kt` — mark + two-tone wordmark for top bars.
- `app/src/main/res/mipmap-anydpi-v26/` — adaptive launcher icon (foreground vector + ink background).

## Colors

| Token | Value | Use |
|---|---|---|
| Brand Green | `#3CE88A` | Gradient start, "Kit" wordmark on dark |
| Brand Teal | `#14B8CC` | Gradient end |
| Green (light surfaces) | `#0E9E6B` | "Kit" wordmark on light themes (contrast) |
| Capsule Green | `#86F595` | "tool bit" capsule inside the mark |
| Ink | `#0C1416` | Icon background, lockup tile |

## Taglines

| Role | Tagline | Where |
|---|---|---|
| Primary | **Tools for a healthier Android.** | Lockup, onboarding, README, store listing short description |
| Alternate | The Android technician's toolkit. | Store listing, press blurbs |
| Alternate | Diagnose. Inspect. Repair. | Marketing hero lines, splash cards |
| Alternate | Honest diagnostics, native tools. | Feature summaries, About |

Rules: sentence case in prose, ALL-CAPS letterspaced only inside the lockup
where it is rendered small. Keep the primary tagline attached to the logo —
don't mix alternates into the lockup.
