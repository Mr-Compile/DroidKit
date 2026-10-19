# DroidKit UI/UX Design System v4

This package is the UI/UX source-of-truth layer for DroidKit. It is designed for AI/vibe coding and adapts to the existing Kotlin + Jetpack Compose, Clean Architecture/MVVM, ToolRegistry, CapabilityManager, PermissionManager, Room/DataStore, and capability-provider plans.

## Visual direction
The approved direction is based on:
- Logo Candidate 4: abstract/geometric DroidKit mark with green -> teal technology gradient.
- Approved splash reference: dark near-black surface, controlled green/teal glow, centered DroidKit identity, restrained geometric decoration.

Reference images are included under `assets/references/`.

## Important
The splash artwork is a **visual reference**, not a requirement to reproduce every decorative pixel. The production Android splash must respect current Android splash-screen behavior, startup performance, accessibility, and theme support.

## AI reading order
1. `docs/00_UI_UX_AI_OPERATING_RULES.md`
2. `docs/01_BRAND_AND_VISUAL_FOUNDATION.md`
3. `docs/02_DESIGN_TOKENS.md`
4. `docs/03_SEMANTICS_ACCESSIBILITY.md`
5. `docs/04_COMPONENT_SYSTEM.md`
6. `docs/05_NAVIGATION_INFORMATION_ARCHITECTURE.md`
7. Relevant file in `docs/screens/`
8. `docs/20_UI_IMPLEMENTATION_JOURNEY.md`
9. `docs/21_UI_UX_DEFINITION_OF_DONE.md`

## Conflict rule
Verified code/API reality > repository AGENTS/security rules > this v4 UI/UX package for presentation/interaction > improved feature plans > older UI notes.

Do not change domain architecture merely to match a mockup. Adapt UI to existing domain contracts unless an architecture change is explicitly approved.
