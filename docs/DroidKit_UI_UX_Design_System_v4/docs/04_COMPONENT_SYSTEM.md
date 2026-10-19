# Compose Component System

Create/reuse components through `core:ui` (or the actual existing shared UI module after audit).

## Foundation components
- `DroidKitScaffold`
- `DroidKitTopBar`
- `DroidKitBottomBar`
- `DroidKitNavigationRail`
- `DroidKitSection`
- `DroidKitCard`
- `DroidKitListItem`
- `DroidKitInfoRow`
- `DroidKitTechnicalValue`
- `DroidKitDivider`

## Tool components
- `ToolCard`
- `ToolCompactRow`
- `ToolCategoryHeader`
- `ToolAvailabilityBadge`
- `FavoriteButton`
- `RecentToolRow`

## State components
- `LoadingState`
- `EmptyState`
- `ErrorState`
- `UnsupportedState`
- `PermissionRequiredState`
- `SetupRequiredState`
- `OfflineState`

## Diagnostic components
- `DiagnosticProgress`
- `DiagnosticInstructionCard`
- `DiagnosticResultBadge`
- `PassFailSkipControls`
- `SensorLiveValue`
- `TestSummaryRow`

## Risk components
- `RiskBadge`
- `ConsequencePanel`
- `ConfirmationSheet`
- `TargetSummary`

## AI components
- `AIInsightCard`
- `AIContextDisclosure`
- `AIStatusBadge`
- `AIRecommendationAction`
AI content must look distinct from measured DroidKit data.

## Rules
Components accept semantic/domain-neutral presentation models where practical. Do not make shared UI directly call repositories, system APIs, Groq, Shizuku, or ADB.
