# J03 Core Components

Date: 2026-10-19
The 16 reusable components from `playbook/05`. "Exists" = skeleton `:core:ui` Task 9; "New" = added by feature journeys.

| # | Component | Status | Purpose | States | Key props | A11y |
|---|---|---|---|---|---|---|
| 1 | `DroidKitScaffold` | Exists (app scaffold) | Screen frame: top bar + content + bottom nav | normal | `title`, `actions`, `content` | nav labels |
| 2 | `ScreenHeader` | Exists (SectionHeader covers) | Screen/section title row | normal | `title`, `subtitle?`, `actions?` | heading semantics |
| 3 | `ToolCard` | Exists | Registry/search result row for a tool | ready / blocked / needs-setup | `tool`, `resolution`, `onClick` | state announced |
| 4 | `StatusBadge` | Exists | Small status pill | pass/fail/not-tested/not-supported/permission/unknown | `state: TestResultState` | icon+label, no color-only |
| 5 | `InfoRow` | New (J07 plan Task 2) | Label–value pair, copyable | value / not-exposed | `label`, `value: String?`, `copyable` | copy action labeled |
| 6 | `SectionCard` | New (J07 plan Task 2) | Grouped card with section header | normal | `title`, `content` | — |
| 7 | `PermissionPrompt` | Exists (BlockedStateView covers generic) | Explains why + request CTA | prompt / permanently-denied | `permission`, `rationale`, `onRequest`, `onOpenSettings` | focus on CTA |
| 8 | `UnsupportedState` | Exists (`BlockedStateView`) | Hardware/feature absent | — | `reason: BlockedReason`, `toolName` | — |
| 9 | `EmptyState` | Exists | No data yet | — | `title`, `body?`, `action?` | — |
| 10 | `ErrorState` | Partially (`BlockedStateView` variant) | Unexpected failure + retry | — | `message`, `onRetry?` | alert role |
| 11 | `LoadingState` | New (small) | Indeterminate progress in content area | — | `label?` | `progressbar` semantics |
| 12 | `RiskConfirmationDialog` | New (needed by NFC write J13, mutations) | Consequence + target + confirm by risk level | shown | `riskLevel (0–4)`, `consequence`, `target`, `onConfirm/onCancel`; level ≥3 needs typed or delayed confirm | modal semantics, confirm last-focused |
| 13 | `DiagnosticInstructionCard` | New (J08) | Step instructions for human tests | shown | `title`, `steps`, `image?` | read order |
| 14 | `ResultControls` (Pass/Fail/Skip) | New (J08) | Verdict buttons for manual tests | idle / selected | `onPass/onFail/onSkip`, `retry?` | equal targets |
| 15 | `SearchField` | Exists (Tools search) | Query input + clear | normal | `query`, `onQuery`, `placeholder` | label |
| 16 | `FilterChips` | Exists (category chips) | Category/status multi-filter | selected/unselected | `options`, `selected`, `onToggle` | selected announced |

## Usage contracts

- Screens compose `SectionCard` + `InfoRow` for inspection UIs — no bespoke cards for standard data.
- Any absent value renders through the same path (`value: String?` → `Not exposed`) — never hand-rolled strings like "N/A"/"unknown" in feature code (UI consistency = honesty consistency).
- `ToolCard` badges come only from `CapabilityManager.resolve(tool)` — never re-derived in feature code.
- `RiskConfirmationDialog` is mandatory for every `safetyLevel ≥ SYSTEM_CHANGE` action; text states consequence + target verbatim.
