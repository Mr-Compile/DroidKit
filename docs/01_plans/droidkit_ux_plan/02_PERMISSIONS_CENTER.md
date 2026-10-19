# Permissions & Access Center

## Destination
Settings → Permissions & Access

## Goal
Give users one understandable place to see which Android permissions/special accesses DroidKit uses, their current status, why they are needed and which tools depend on them.

## Requirements
- Dynamically reflect actual permission state.
- List only permissions/access types relevant to implemented DroidKit functionality.
- Separate normal runtime permissions from special Android access when useful.
- For each item show purpose, affected tools and status.
- Provide contextual Manage/Grant/Open Settings actions.
- Request permissions at point of use rather than aggressively on first launch.
- Explain denied/permanently denied states without blame.
- Handle Android-version differences safely.
- Never claim permission is required when the tool can operate without it.

Potential categories include Files/Media, Notifications, Location, Camera, Usage Access, Overlay, Accessibility Service, VPN or other special access ONLY if the existing app actually uses them.

Add appropriate semantics and full-row touch targets.
