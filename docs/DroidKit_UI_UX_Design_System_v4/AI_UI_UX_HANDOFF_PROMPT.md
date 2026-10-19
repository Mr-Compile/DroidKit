# Copy-Paste AI Handoff Prompt

You are implementing DroidKit UI/UX v4.

First audit the repository; do not assume the planning files match the current code. Read AGENTS.md, the DroidKit AI Operating Contract, this package README, UI/UX AI Operating Rules, semantics/accessibility contract, component system, navigation plan, and the relevant screen specification.

Compare:
1. current code/UI,
2. older DroidKit plan,
3. v4 UI/UX target.

Do not change domain architecture merely to reproduce a mockup. Adapt the v4 design to existing ViewModels, ToolRegistry, CapabilityManager, PermissionManager, and feature contracts.

Before editing, report current state, conflicts, reusable components, files expected to change, and targeted validation.

Implement the smallest coherent UI slice. Use semantic tokens, shared components, explicit states, adaptive layouts, and Compose accessibility semantics. Never hard-code tool availability or privilege state. Never expose secrets.

After editing, identify changed files first and validate only the affected scope unless shared theme/navigation/component changes justify broader checks. End with changed files, validation actually run, accessibility checks, remaining limitations, and next recommended UI task.
