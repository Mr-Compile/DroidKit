# Paste This First — DroidKit Phase 0

Read the repository's AGENTS.md/project instructions first, then execute ONLY Phase 0 of the DroidKit V1 Completion Plan below.

Do not begin CI, design-system work, accessibility refactors, permission redesign, or any later phase in this run.

## Task A — sensors.live navigation defect
The prior repository audit found that the generic tool entry for live sensors may expose a route pattern equivalent to `sensors/live/{type}`. Generic ToolRegistry navigation can therefore attempt to navigate the literal placeholder even though the destination expects an integer.

1. Re-audit the current ToolRegistry, SensorsFeature registration, NavHost/sensors graph, and all generic `tool.route` navigation call sites.
2. Confirm whether the defect still exists in current HEAD.
3. If confirmed, implement the smallest architecture-consistent fix so every registry tool entry has a directly navigable route while sensor-specific navigation can still supply a concrete type.
4. Search all registered tool routes for unresolved placeholders and fix/report any equivalent hazard.
5. Add a regression test/invariant where practical so generic launch routes cannot silently contain unresolved placeholders.

## Task B — App Inspector package visibility
The prior audit found that package-related feature manifests lacked `<queries>` declarations while App Inspector uses PackageManager enumeration on Android 11+.

1. Re-audit current manifests, merged manifest, PackageManager calls, and intended App Inspector behavior.
2. Determine the narrowest legitimate package visibility declaration required.
3. Do NOT add `QUERY_ALL_PACKAGES` unless policy eligibility and actual product requirement clearly justify it; if broad enumeration is impossible under the intended distribution policy, make the UI/documentation honest about the limitation instead.
4. Implement the minimal compliant fix.
5. Verify the merged manifest and relevant API behavior as far as the environment allows.

## Required preservation
Do not redesign or replace ToolRegistry, CapabilityManager, ElevatedOps, SessionEngine, Room schema, DataStore, Hilt architecture, or feature-module boundaries.

## Required verification
Run the appropriate debug build and tests. If a stale Gradle daemon/file lock occurs, diagnose it as an environment issue rather than modifying production code to work around it.

## Required final response
Return:
- CONFIRMED/FALSE POSITIVE for each original defect
- files changed
- exact implementation
- tests added/changed
- commands run + results
- any device verification still required
- any new P0/P1 issue discovered
- architecture-preservation confirmation
Then STOP. Do not start Phase 1.
