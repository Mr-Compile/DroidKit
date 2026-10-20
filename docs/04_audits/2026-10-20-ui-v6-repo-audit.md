# UI v6 — Repository & UI v4/v5 Audit (plan file 01 deliverable)

Date: 2026-10-20. Scope: what exists in the repo today vs what plan files 02–25 ask for.

## Audit table

| Area | Existing | Reuse | Extend | Refactor | Remove duplicate | Risk |
|---|---|---|---|---|---|---|
| Theme/tokens | `DroidKitTheme` (M3 + dynamic color always on for API 31+), `DroidKitSpacing`, `DroidKitStatusColors`, `DroidKitBrand`/`DroidKitMark` vector | Tokens, brand assets | Dynamic-color user setting (none exists — theme follows only Light/Dark/System) | — | — | None |
| Shared components | `SectionHeader`, `EmptyState`, `SectionCard`, `InfoRow`, `LabIndexScreen`, `UiProblemCard`, `ToolInfoDialog`, `SemanticStates` (Loading/Offline/Unsupported/SetupRequired/FriendlyError/DiagnosticProgress), `StatusBadge`/`ResultBadge`/`StatusKind`, `RuntimePermissionGate`, `DroidKitWordmark` | Whole set | `DroidKitTopAppBar` (title + optional Up), tool-icon container | `ToolCard` (add icon, collapse noisy chips to one status), `AvailabilityBadge` (reason-aware label), `EmptyState` (optional action) | — | Touching ToolCard affects Home/Tools/Insights |
| App bars/scaffolds | Raw `TopAppBar` in ~41 files; back arrow only in the About branch (3 files) | Pattern itself | Standard app-bar component; Up affordance on all secondary screens | Migrate screens to shared bar with `onBack` | — | Mechanical, wide diff |
| Tool cards/rows | `ToolCard` (title, description, category+safety+phase tag chips, availability badge, info/favorite buttons) | Card + actions | Leading semantic icon | Chip row → category subtitle + single status badge | — | Low |
| Chips/badges | `StatusBadge`/`ResultBadge` (icon-less but labeled), `TagChip` non-interactive | StatusKind model | Reason-aware availability label | — | — | Low |
| States | Semantic states + `UiProblem` + `RecoveryAction` mapping | Whole model | — | — | — | None |
| NavHost/routes | Single `DroidKitNavHost`, per-feature `*Graph(nav)` fns, typed args on 5 detail routes, route contract doc + placeholder-rejection test | Structure | `launchSingleTop` on generic `onOpenTool` navigations; friendly state for invalid `diagnose/test/{testId}` | — | — | Low |
| Bottom nav | Standard `popUpTo`/`saveState`/`launchSingleTop`/`restoreState`; visible on every route except `onboarding` | Options builder | Restrict visibility to top-level routes | — | — | Behavior change; verify tab restore still works |
| Icons/drawables | material-icons-extended already a dependency of every Compose module; only launcher + brand vectors as resources; ~14 distinct Material icons in use | Icon dependency | `Tool.iconKey` metadata + `ToolIcons` map in core:ui | — | — | Icon name typos — covered by unit test |
| Tool metadata | `Tool` (id/title/description/category/route/safetyLevel/capabilities/permissions/features/minSdk/phase/keywords), `ToolRegistry` (flatten+sort, `search`, `byCategory`, `byId`) | Whole model | `iconKey` field, `searchEntries` on `FeatureContribution`, deterministic ranked search | — | — | `init` contract must stay navigable |
| Search | `ToolRegistry.search` (contains, unranked), `SearchPages` (10 hand-listed page destinations), search field inside Tools screen | Entry point + pages list | Ranked scoring, keywords on all tools, diagnostics tests as results via `searchEntries` | Fold `SearchPages` into `FeatureContribution.searchEntries` on `ToolsFeature` | — | Low |
| Favorites/recents | `SettingsRepository` DataStore: favorites set, recents list (cap 8), use-counts; Home pins + recents; Tools toggle | Whole flow | — | — | — | None |
| Permission/provider state | `CapabilityManager.resolve` → `ToolResolution`, `ProviderStatusBus` reactive map, `rememberRuntimePermission` (grant/denied/permanent, resume-refresh), Settings grant buttons, `BlockedReason.toUiProblem` | Whole pipeline | — | — | — | None |
| Semantics/a11y | Heading markers on section headers/panels, described IconButtons, non-interactive TagChip, no decorative announcements | Approach | Decorative tool icon (`contentDescription=null`), `stateDescription` on badges where cheap | — | — | Low |
| Diagnostics search | 21 `PhoneDoctorTest`s reachable via `diagnose/test/{testId}` (navigable concrete route exists) | Route builder | Expose tests as DIAGNOSTIC search entries | — | — | Low |

## Smallest migration path (per plan file 24)

1. `core:common` — `Tool.iconKey`, `SearchEntry`/`SearchResultType`, `FeatureContribution.searchEntries`, ranked `search`/`searchEntries`, registry validation.
2. `core:ui` — `ToolIcons` + icon container, `DroidKitTopAppBar`, `ToolCard` v6, reason-aware `AvailabilityBadge`.
3. Feature `*Feature.kt` files — `iconKey` + keywords on all tools; `DiagnosticsFeature.searchEntries` for the 21 tests; `ToolsFeature` absorbs `SearchPages`.
4. Screens — `DroidKitTopAppBar` + `onBack` wiring through every graph; bottom-bar visibility gate; `launchSingleTop`; invalid-arg friendly states.
5. Settings — dynamic-color toggle (`SettingsRepository` pref → `MainActivity` → `DroidKitTheme`).
6. Mockup sync (`mockups/index.html`), unit tests, `26_FINAL_AI_AUDIT` report.

## Explicitly out of scope (per plan non-goals / repo reality)

- No deep links exist in `AndroidManifest.xml` — deep-link back-stack fallback is NOT APPLICABLE beyond internal route hops.
- No second navigation framework, registry, or capability model.
- No custom SVG tool icons — material-icons-extended covers every needed glyph; brand mark untouched.
- Bottom sheets: none exist; no new sheet surface added (dialogs remain the decision pattern).
- Full-screen diagnostic tests (DisplayColor, TouchGrid, Multitouch, Flash…) are intentionally immersive — no app bar added there.
