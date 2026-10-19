# Logo Candidates — SVG Explorations

SVG recreations of all 9 logo candidates from the original candidate board
(`../../uiux_v4_superseded/assets/references/droidkit_logo_candidates_reference.png`),
plus a matching splash loading screen for each.

These are review/exploration assets only. The production brand kit lives one
level up (`../droidkit-mark.svg`, `../droidkit-logo.svg`) — Candidate 9 is the
selected direction per `23_ASSET_RULES.md`.

## Files

| Candidate | Style | Logo | Splash |
|---|---|---|---|
| 01 | Modern Android + Tool (Gradient) | `candidate-01-logo.svg` | `candidate-01-splash.svg` |
| 02 | Minimal & Clean (Letter D + Android) | `candidate-02-logo.svg` | `candidate-02-splash.svg` |
| 03 | 3D Style (Friendly & Trusted) | `candidate-03-logo.svg` | `candidate-03-splash.svg` |
| 04 | Abstract / Geometric | `candidate-04-logo.svg` | `candidate-04-splash.svg` |
| 05 | Orbit / Tech Focus | `candidate-05-logo.svg` | `candidate-05-splash.svg` |
| 06 | Flat & Simple | `candidate-06-logo.svg` | `candidate-06-splash.svg` |
| 07 | Letter D + Wrench (Modern) | `candidate-07-logo.svg` | `candidate-07-splash.svg` |
| 08 | Circular / Gear (Professional) | `candidate-08-logo.svg` | `candidate-08-splash.svg` |
| 09 | Futuristic / Tech Glow — selected | `candidate-09-logo.svg` | `candidate-09-splash.svg` |

## Reviewing

Open `index.html` in a browser for a side-by-side grid of every logo and
splash. Splash spinners animate via SMIL (works in Chrome/Edge/Firefox;
static first frame elsewhere). Dark splashes use splash ink `#040808` and
light splashes `#F4F7F6`, matching each candidate's board mood.

Splash layout follows `../../uiux_v4_superseded/assets/references/droidkit_splash_reference.png`:
centered mark + wordmark + tagline, rotating progress ring, "Initializing
DroidKit..." caption, corner gradient washes, faint Android silhouette,
and the "YOUR ANDROID TOOLKIT COMPANION" footer block.

## Notes

- Wordmark/taglines are `<text>` elements (Segoe UI → system-ui → Roboto
  fallback). Convert to outlines before production export if a fixed
  typeface is required.
- Punched-out details (eyes, wrench cutouts) use the tile/splash background
  color — keep them in sync if the background changes.
- These SVGs are approximations of raster concepts; refine geometry before
  any candidate is promoted to a production asset.
