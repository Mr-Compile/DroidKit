# DroidKit Complete Development Playbook

This package is the end-to-end operating plan for vibe-coding DroidKit with an AI coding agent.

## How to use it
At the beginning of every AI coding session:
1. Give the AI `01_AI_OPERATING_CONTRACT.md`.
2. Give it `02_MASTER_JOURNEY.md`.
3. Give it the current journey document.
4. Tell it to audit the repository before changing code.
5. Tell it to update the current journey checklist only after validation.
6. Do not allow it to skip a journey gate just because scaffolding exists.

## Golden workflow
AUDIT -> PLAN -> IMPLEMENT -> VALIDATE -> REVIEW -> DOCUMENT -> CHECK OFF -> VERSION/RELEASE WHEN APPLICABLE

## Source of truth priority
1. Current repository behavior and verified Android APIs
2. AI Operating Contract
3. Frozen V1 scope / approved journey documents
4. Architecture and design foundations
5. Backlog/checklists
6. Ideas/future notes

When documents conflict with verified Android behavior, stop, document the conflict, and revise the plan rather than forcing an impossible implementation.

## Package map
- 01: AI rules and operating contract
- 02: Master journey and gates
- 03: Product and V1 scope
- 04: Android feasibility and compatibility
- 05: Design foundation
- 06: Engineering architecture foundation
- 07: Repository bootstrap
- 08-14: Versioned feature development
- 15-17: Alpha, beta/RC, stable release
- 18: Maintenance
- 19: Shizuku
- 20: Wireless ADB
- 21: Future/root/V2
- 22: QA and device matrix
- 23: Security/privacy/safety
- 24: Versioning/release management
- 25: AI audit templates
- 26: Definition of Done
- 27: Backlog and issue template
- 28: Final project completion checklist
- AGENTS.md: copy-ready repository agent rules
