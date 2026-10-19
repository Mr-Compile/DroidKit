# Paste This First — DroidKit Navigation Phase NAV-0/NAV-1

You are working on the existing DroidKit Android repository. Do NOT redesign the app and do NOT implement unrelated features.

Your task is limited to NAV-0 and NAV-1 from the Navigation Hardening plan:

1. Audit the repository's navigation from app start through DroidKitNavHost, top-level destinations, every FeatureContribution/Tool.route, feature graph, and parameterized route.
2. Produce a route inventory and identify route/back-stack defects with file/class/function evidence.
3. Verify the previously reported `sensors.live` issue rather than assuming it is still present.
4. If confirmed, fix it using the smallest architecture-consistent solution. Do not navigate a literal `{type}` placeholder and do not invent a fake sensor ID simply to silence the crash.
5. Add a regression invariant/test so direct ToolRegistry routes cannot contain unresolved required placeholders, or model route templates separately if the repository architecture requires templates.
6. Inspect for similar defects across ALL registered tools before declaring the fix complete.
7. Run `assembleDebug` and `testDebugUnitTest` after the change. Run any navigation tests you add.
8. Do not touch ToolRegistry/CapabilityManager/ElevatedOps/Room/SessionEngine architecture except where the route contract minimally requires it.
9. Do not begin top-level navigation refactors, V5 UI, permissions, CI, accessibility, or provider changes in this task.
10. STOP after NAV-1 and report evidence.

Required response:
- Audit findings
- Route inventory
- Confirmed defects
- Files changed
- Exact fix and reasoning
- Tests added
- Build/test commands and results
- Remaining navigation risks
- Recommendation for NAV-2

Completion rule: compilation alone is not proof. The direct entry path that previously failed must be covered by a regression test where feasible.
