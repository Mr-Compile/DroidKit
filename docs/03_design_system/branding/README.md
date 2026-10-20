# DroidKit Brand Kit

Source of truth for the DroidKit logo, colors, and taglines. The mark is
Candidate 9 (futuristic / tech glow): a glass Android head — dark domed
skull with a green→teal gradient rim and sheen, two antennae, glowing green
eyes — presented on near-black ink with a soft green glow halo.

## Files

| File | Use |
|---|---|
| `droidkit-mark.svg` | Mark only, transparent background. Avatars, favicons, docs. |
| `droidkit-logo.svg` | Vertical lockup on ink tile: glow halo + mark + wordmark + primary tagline. README headers, splash/about graphics, store assets. |
| `../uiux_v5/assets/references/logo_candidate9_selected.png` | Candidate board crop of the selected direction (mood reference only — not a production asset). |

In-app counterparts:

- `core/ui/.../theme/Brand.kt` — `DroidKitBrand` colors + `DroidKitMark` Compose `ImageVector`.
- `core/ui/.../components/DroidKitWordmark.kt` — mark + two-tone wordmark for top bars.
- `app/src/main/res/mipmap-anydpi-v26/` — adaptive launcher icon (head + glow foreground vector, ink background).
- `app/.../SplashScreen.kt` — Compose splash: ink background, pulsing glow halo, mark, wordmark, tagline, loading indicator.

## Colors

| Token | Value | Use |
|---|---|---|
| Brand Green | `#3CE88A` | Gradient start, "Kit" wordmark on dark, glow |
| Brand Teal | `#14B8CC` | Gradient end |
| Green (light surfaces) | `#0E9E6B` | "Kit" wordmark on light themes (contrast) |
| Glow | `#3CE88A` @ 35% | Halo behind the mark (splash, launcher, lockup) |
| Head Glass | `#1A2C31` → `#0A1214` | Dome fill (top→bottom), keeps the head dark so rim + eyes glow |
| Ink | `#0C1416` | Icon background, lockup tile |
| Splash Ink | `#040808` | Splash + system splash window background |

## Taglines

| Role | Tagline | Where |
|---|---|---|
| Primary | **Deeper insights. Better devices.** | Lockup, splash, README, store listing short description |
| Alternate | Tools for a healthier Android. | Onboarding, store listing, press blurbs |
| Alternate | The Android technician's toolkit. | Store listing, press blurbs |
| Alternate | Diagnose. Inspect. Repair. | Marketing hero lines, splash cards |
| Alternate | Honest diagnostics, native tools. | Feature summaries, About |

Rules: sentence case in prose, ALL-CAPS letterspaced only inside the lockup
where it is rendered small. Keep the primary tagline attached to the logo —
don't mix alternates into the lockup.
