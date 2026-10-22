# V5 UI Audit — Dark Instrument reskin

Date: 2026-02-14. Scope: reskin the production app to the `mockups/improved-index.html`
"Dark Instrument" design direction (v5 spec `docs/03_design_system/uiux_v5/`).

## Inventory (pre-migration)

- ~55 files used raw `material3.Card`, ~48 used buttons (`Button`/`OutlinedButton`),
  26 `FilterChip`, 16 `AlertDialog`, 38 `OutlinedTextField` call sites across
  `feature/` and `app/`. One diagnostics screen used Material3 segmented buttons
  with shape-specific behavior — intentionally left on the platform widget.
- Theme was default Material You (`dynamicColor = true`, no brand scheme).
- Shared layer in `:core:ui` already exposed `SectionCard`, `SectionHeader`,
  `InfoRow`, `ToolCard`, `UiProblemCard`, `StatusBadge`/`ResultBadge`,
  `SemanticPanel`, `EmptyState`, `ToolIcon`, `DroidKitTopAppBar`,
  `DroidKitNavigationSuite` — restyled in place rather than renamed.

## Token map (mockup → `SemanticColors.kt`)

ink `#040808` → `background`; panel `#0B1517`/`#0F1D20`/`#15262B` → `panel`/`raise`/
`surfaceContainer*`; brand `#3CE88A`/`#14B8CC` → `brand`/`brand2` + `--grad`
equivalent; text `#EAF2F0`/`#8FA5A7`/`#6E8385`; statuses ok/warn/err/info/unsup/
setup mirror the mockup. Light scheme included; dynamic color now opt-in
(`DroidKitTheme(dynamicColor = false)` default).

## What shipped

- New wrappers: `DroidKitCard`, `DroidKitButton`, `DroidKitOutlinedButton`,
  `DroidKitFilterChip`, `DroidKitSegmentedRow`, `DroidKitTextField`,
  `DroidKitAlertDialog` (all in `core/ui/components/`).
- New signature surfaces: `HeroCard`, `SpecTile`, `StatTile`, `GateCard`,
  `AiSurfaceCard`, `MiniTileIcon`, `DroidKitProgressLine`, spinner/skeleton/dots.
- Mechanical migration: 56 files across `feature/` + `app/` renamed to the
  wrappers via codemod; `LabIndexScreen` migrated manually. Two string-literal
  false positives (`"Smart Card"`, `"Card I/O error"`) found in self-review and
  reverted.
- Signature screens restyled: dashboard hero/spec grid/vdots; Tools `.toolstat`
  + chips + badges; Diagnostics presets/quick-test/history + `TestResultBar` +
  `PermissionGate`; Settings theme segmented control + provider dots + AI card;
  Insights stat tiles; AI chat (`.msg`/`.avatar`/`.prop`/`.chatform`);
  onboarding (`.obic`/`.obdot`/gradient washes).
- `mockups/index.html` synced to the improved design (byte-identical to
  `improved-index.html` except title); `mockups/README.md` updated.

## State/error gaps still open (not ticked in V5 checklist)

- `strings_errors.xml` catalog + `FriendlyError` model/mapper (plan Task 6 named
  artifacts) — shared `FriendlyErrorState`/`UiProblemCard` cover presentation
  but not the catalog resource.
- `RiskBadge`, `ConsequencePanel`, `ConfirmationSheet` — not created.
- Capability Center screen — does not exist.
- Production Candidate-9 assets, `core-splashscreen` cold-start splash.
- `J03_DESIGN_TOKENS.md` v5-name mapping appendix.
- V5-6 QA (light theme on device, window sizes, font scale, TalkBack,
  screenshots) — requires device/emulator runs.
