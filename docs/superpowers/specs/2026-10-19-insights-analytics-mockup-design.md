# Insights & Analytics — Mockup Design

Date: 2026-10-19
Scope: `mockups/index.html` only (design-review replica). No Kotlin/app changes.
Status: approved approach (single-screen Insights, home card + catalog entry).

## Goal

Give DroidKit an "Insights" screen in the HTML mockup so the dashboard-and-analytics
concept can be reviewed visually before any app work. Combines device health
trends with on-device activity stats.

## Placement

- Route: `insights` (single screen, scrollable — one route, not split).
- Entry points:
  - Compact "Insights" summary card on Home (latest session result + trend hint,
    tap-through to `insights`). Rendered only when at least one completed session
    exists.
  - Tool `analytics.insights` in the REPORTS category ("Device Insights").
  - Flow-map node under "Diagnostics flow".

## Screen content (top → bottom)

1. Range chips: `30 days` / `All time` (filters seeded session history).
2. **Device health card** — verdict donut (PASS / FAIL / skipped+unsupported)
   for the most recent completed session in range, with pass count + date and a
   short delta line vs the previous session.
3. **Pass-rate trend** — SVG bar chart, one bar per session (share of PASS
   results), date labels underneath.
4. **Tests needing attention** — per-test rows aggregated over the range:
   test title, FAIL and skipped/unsupported counts, small severity bar.
   Top 4 by severity.
5. **Storage card** — sparkline of free space across snapshots (mock snapshots
   gain a `free` field; seeded values trending down), current free + delta.
   Hidden/empty-stated when fewer than 2 snapshots.
6. **Activity section** — 2×3 stat tile grid (Sessions, Reports saved, Tools
   used, NFC scans, Snapshots, Elevated actions — all computed from mock state)
   + "Most used tools" top-3 list from a new `S.toolUse` counter.
7. Footer note: stats computed on-device, nothing leaves the phone (matches
   local-first stance).

## Data / mock-state changes

- Seed two more completed sessions inside the existing `seed()` IIFE so the
  trend chart has ~4 points: an older session >30 days out (excluded by the
  30-day chip) and one mid-October session with a microphone FAIL, continuing
  the "touch grid repaired on Oct 19" narrative.
- `S.snapshots` entries gain `free` (bytes); `snap-capture` stores
  `DEVICE.storageFree`; seed 3 snapshots with decreasing free space.
- `S.toolUse = {toolId: count}` incremented in `recordRecent()`; seeded with a
  few plausible counts.
- `S.insightRange = "30d" | "all"` for the range chips.

## Charts

Hand-rolled inline SVG helpers (`donutSvg`, `barsSvg`, `sparkSvg`) — zero
dependencies, themed via the existing CSS custom properties (`--primary`,
`--error`, `--outline`, `--sub`, `--card2`) so dark/light toggle works.
PASS = primary, FAIL = error, skipped/unsupported = outline (same convention
as the review screen's `pri`/`err`/`sub` classes).

## Honesty rules (mockup mirrors app rules)

- No fabricated aggregates: every number derives from `S.sessions`,
  `S.snapshots`, `S.nfc.history`, `S.elevLog`, `S.toolUse`.
- Empty states instead of fake data ("Run a diagnostic to build insights",
  "Capture storage snapshots to see a trend").
- Deleting all reports / live sessions update Insights reactively.

## Out of scope (mockup)

- Real Kotlin implementation, Room aggregation queries, battery-history
  recording (no background collection in the app), export/share of insights.
- These get decided in a future app-side spec if the design is approved.
