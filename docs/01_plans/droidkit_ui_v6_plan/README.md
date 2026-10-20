# DroidKit UI v6 — AI Implementation Package

## Purpose
UI v6 is a consolidation and polish phase. It improves the existing DroidKit UI v4/v5 rather than replacing it.

## Mandatory AI workflow
1. Audit the repository and existing UI v4/v5 implementation first.
2. Read every numbered file in order.
3. Reuse existing Compose components, theme tokens, navigation, ToolRegistry/capability architecture, and assets.
4. Do not redesign unrelated screens or add duplicate systems.
5. Implement in small coherent phases and validate the changed scope.
6. Never claim a status, capability, permission, provider connection, or operation result that was not verified.
7. Finish with `26_FINAL_AI_AUDIT.md`.

## Source-of-truth priority
Repository reality > platform/safety constraints > existing DroidKit architecture > UI v6 plan > visual preference.

## Target outcome
One coherent DroidKit experience across Home, Tools, Diagnose, Settings, Search, Favorites/Recents, detail screens, dialogs, sheets, statuses, permissions and provider flows.
