# Accessibility Audit — DK-040/041/042 (V1)
Date: 2026-10-20 · Scope: static/ automated checks runnable without a device.
Device-bound items are listed at the end and remain unverified.

## Static verification performed

### Semantics (DK-040)
- `SectionHeader` exposes `heading()` — screen readers announce section boundaries (Feedback.kt).
- `SemanticPanel` titles (OfflineState / UnsupportedState / SetupRequiredState) expose `heading()`.
- `ToolCard` favorite/info IconButtons carry explicit `contentDescription` including the tool title ("Add X to favorites").
- Non-interactive status tags use plain `Surface`+`Text` — no fake click semantics advertised to assistive tech.
- `AppOps` back IconButton labeled "Back" (was a bare "←" glyph).
- `InfoRow` copy action labeled "Copy {label} to clipboard" (was bare "Copy").
- `DroidKitWordmark` and `Settings`/`About` icons carry `contentDescription`s.
- Inventory: every `Icon(`/`IconButton` call site in `feature/*` + `app` either sets `contentDescription` or is text-wrapped — grep-verified 2026-10-20.

### Interaction/layout (DK-041) — statically checkable portion
- Status is never color-only: `ResultBadge`/`StatusBadge` render a wording label next to the tint; `TestResultState` values map to explicit labels (Pass/Fail/Not tested/Not supported/Permission required/Unknown).
- Touch targets: interactive controls are `Card(onClick)`, `TextButton`, `IconButton` (M3 defaults ≥48dp) — no custom sub-48dp hit areas found in shared components.
- Large text: shared components use `MaterialTheme.typography` scale — no fixed `sp`/`dp`-sized text. Per-screen overflow under fontScale ≥1.3 is device-verification territory.

### Friendly state coverage (DK-020 + V5)
- `UiProblemCard` renders title + message + expandable technical details + recovery buttons — errors are never raw exceptions.
- Semantic states exist for Loading/Offline/Unsupported/SetupRequired/FriendlyError/DiagnosticProgress.
- NOT_SUPPORTED and PERMISSION_REQUIRED are mapped to distinct semantic kinds, never to FAIL.

## Known gaps (tracked, not fixed here)
- `Tools`/`Settings`/`Permissions` surfaces are owned by parallel in-flight UX work (`docs/01_plans/droidkit_ux_plan/`, esp. `11_ACCESSIBILITY.md`) — their a11y pass lands there.
- `BlockedReasonList`/`EmptyState` in `Feedback.kt` carry no icons; acceptable but could gain imagery later.

## Device verification checklist — REQUIRED before DK-042 can close
On a physical device (TalkBack on):
- [ ] TalkBack traversal order on Home/Tools/Diagnostics/Reports matches visual order; section headings announced.
- [ ] Blocked-tool dialog: problem title, message, technical-details expander, and each recovery button are reachable and announced.
- [ ] RuntimePermissionGate: denied/permanent-denied states announce correctly; "Open app settings" works.
- [ ] fontScale 1.3: no clipped text on Dashboard, Tools, Session Review, Reports; InfoRow copy button still reachable.
- [ ] Display size (large): primary surfaces remain usable; no overlapping bento cards.
- [ ] Contrast: status colors on `surfaceVariant` meet WCAG AA for text (verify `ResultBadge` labels on-device).
- [ ] Touch exploration: every card/button announces role + state (e.g., "favorite, not selected").
- [ ] Session flow: GuidedSession progress bar announces progress updates.

## Evidence
- `feature/appops/AppOpsScreen.kt`, `core/ui/InfoRow.kt`, `core/ui/components/SemanticStates.kt` — semantics added this audit.
- `core/ui/components/ToolCard.kt`, `Feedback.kt` — labels/headings (parallel session).
- `:core:ui:lintDebug` — 0 errors, 0 warnings post-change.
