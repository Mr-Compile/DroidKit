# DroidKit V1 Completion — Master AI Execution Prompt

You are working inside the existing DroidKit Android repository. This is NOT a greenfield project.

## Mission
Move the current audited DroidKit codebase toward a verified, accessible, polished, release-ready V1 while preserving good existing architecture and avoiding duplicate systems.

## Non-negotiable operating rules
1. Inspect relevant code before editing it.
2. Repository code is the implementation source of truth. Planning docs describe intent, not proof.
3. Do not recreate systems that already exist.
4. Prefer the smallest coherent change that solves the task.
5. Do not perform unrelated refactors.
6. Do not add dependencies unless necessary and justified.
7. Never fabricate device/API values or capability availability.
8. Preserve local-first/privacy behavior.
9. Preserve feature-module boundaries and the rule that feature modules do not directly depend on privilege implementation modules.
10. Privileged operations must remain behind the existing abstractions and allow-listed actions.
11. User-facing failure states must be truthful and recoverable where possible.
12. Every behavior change requires appropriate verification/tests.
13. Never mark a task complete merely because code compiles.
14. Do not silently broaden V1 scope.
15. At the end of each phase, STOP and report. Do not begin the next phase unless explicitly authorized.

## Architecture to PRESERVE, not rebuild
Preserve and extend the existing equivalents of:
- ToolRegistry + FeatureContribution multibinding
- CapabilityManager / ToolResolution
- ElevatedAction allow-list
- ElevatedOps provider fallback chain and audit logging
- Standard/Shizuku/ADB/Root provider abstractions
- Room schema/migrations
- SettingsRepository/DataStore
- DeviceSystemProbe and honest sentinel behavior
- SessionEngine and diagnostic persistence
- ReportBuilder/export pipeline
- InsightsCalculator
- AI privacy/BYOK pipeline
- Hilt wiring
- existing feature module boundaries

Do not create parallel replacements such as ToolManager, second registry, second capability engine, duplicate permission framework, or a new navigation system unless the current system is proven inadequate and the change is explicitly approved.

## Mandatory pre-change audit for every phase
Before editing:
- inspect git status
- identify exact affected modules/files
- search for existing implementations/utilities/components
- search for TODO/FIXME/placeholders related to the task
- identify existing tests
- state what will be changed and what will intentionally remain untouched

If the repository has materially changed since the supplied audit, adapt to current code and explain the difference.

## Verification after every phase
Use the project's actual Gradle tasks. At minimum where available:
- compile/assemble debug
- relevant unit tests
- full unit test suite for cross-cutting changes
- lint/static analysis once configured
- relevant instrumentation/device tests when required

If a check cannot run, report CANNOT VERIFY rather than PASS.

## Completion report format
At the end of each phase output:
1. Phase and status: COMPLETE / PARTIAL / BLOCKED
2. Files changed
3. What changed
4. Why it changed
5. Tests added/updated
6. Commands executed and exact result
7. Remaining risks
8. Manual/device verification still required
9. Architecture-preservation check
10. Recommended next phase
Then STOP.

# Execution order

## PHASE 0 — Immediate correctness defects [P0/P1]
Goal: remove known correctness issues before broader work.

### DK-001 — Fix parameterized `sensors.live` tool navigation
Audit the current sensor registry/navigation implementation first. The prior audit found a registered tool route equivalent to `sensors/live/{type}` being navigated literally from generic tool entry points while the destination expects an integer argument.

Requirements:
- generic ToolRegistry destinations must be directly navigable
- parameterized destination patterns must not be exposed as literal launch routes
- preserve internal sensor-list navigation to a specific sensor type
- choose the smallest architecture-consistent fix
- add a regression test where practical; at minimum validate registry launch routes do not contain unresolved placeholders
- search the entire registry for other `{...}` launch-route hazards and report them

Definition of Done:
- tapping the Sensors live-tool entry cannot navigate with an unresolved route placeholder
- existing sensor-specific navigation still works
- no other registry launch route contains an unsafe unresolved argument, or each exception is explicitly justified
- build/tests pass

### DK-002 — Fix Android 11+ package visibility for App Inspector
Audit the current PackageManager queries and merged manifests before editing. The prior audit found empty feature manifests and `getInstalledPackages()` behavior that may be filtered on API 30+.

Requirements:
- determine exactly which packages DroidKit legitimately needs visibility into
- implement the narrowest Android package-visibility declaration consistent with the App Inspector's intended behavior and Play policy
- do NOT add QUERY_ALL_PACKAGES unless the product requirement, distribution model, and policy eligibility clearly justify it
- verify merged manifest behavior
- ensure UI does not claim exhaustive visibility when Android policy/API prevents it
- add testable logic/documentation around visibility limitations where appropriate

Definition of Done:
- intended launcher/inspectable package visibility works within Android's rules
- no unjustified broad package permission is introduced
- limitations are truthfully represented
- build/tests pass

### Phase 0 gate
Run build + relevant tests. Report and STOP.

---

## PHASE 1 — Automated quality foundation [P0/P1]
Goal: prevent future AI changes from silently regressing working code.

### DK-010 — Minimal CI
Implement repository-appropriate CI (GitHub Actions if this is a GitHub repository) for pull requests and main-branch changes.
Required minimum gates:
- clean Gradle setup/cache appropriate for Android
- debug compile/assemble
- JVM unit tests
- lint once configured
- upload useful build/test reports/artifacts on failure where practical

Do not place signing secrets in repository files.

### DK-011 — Lint/static-analysis baseline
First determine what Android lint already provides. Configure a useful baseline without turning the first run into hundreds of arbitrary style changes.
- Android lint is mandatory
- add ktlint/detekt only if justified by existing project conventions and maintenance cost
- fail CI on newly introduced meaningful violations
- do not mass-refactor unrelated code merely to satisfy cosmetic rules

### DK-012 — Regression gates
Add inexpensive architecture/invariant tests where valuable, including checks such as:
- registry launch routes are valid/directly navigable
- no feature module imports privilege implementation packages (if not already automatically enforced)
- elevated actions remain allow-listed/validated

Definition of Done:
- a fresh PR automatically verifies build + tests + lint
- a failure blocks the quality workflow
- no credentials/signing material are committed
- documentation explains local equivalents of CI commands

### Phase 1 gate
Run the same commands locally where possible. Report and STOP.

---

## PHASE 2 — Core UX/system hardening [P1/P2]
Goal: extend existing infrastructure rather than create parallel systems.

### DK-020 — Unified user-facing problem/recovery model
Audit existing ToolResolution, ElevatedResult, AiError, Result usage, BlockedReasonList, PermissionGate, and feature-local errors.
Design the smallest shared model needed for consistent UI recovery. It may resemble a `UiProblem`/`UiError` with:
- user-friendly title/message
- severity/type
- optional technical details
- structured recovery actions (retry, grant permission, open settings, setup provider, dismiss, etc.)

Requirements:
- do not erase useful subsystem-specific typed errors
- map exceptions at appropriate boundaries
- do not show raw exception text/stderr as the primary message to ordinary users
- technician/advanced details may remain available behind an explicit details affordance
- avoid generic "Something went wrong" where a specific explanation is known

### DK-021 — Permission flow hardening
Extend the existing permission architecture.
Support and distinguish as appropriate:
- granted
- requestable denial
- permanently denied / rationale no longer available
- requires Settings recovery
- missing hardware/unsupported API
- Shizuku/ADB/root provider requirement remains a capability/provider concern, not falsely modeled as Android runtime permission

Requirements:
- contextual permission requests only
- no startup permission dump
- reusable request/recovery UX
- test state transitions where feasible

### DK-022 — Reactive capability/provider state
Audit how provider status is currently read. Add the minimum reactive mechanism so UI can update after Shizuku grant, ADB connect/disconnect, root availability changes, etc., without requiring accidental recomposition/restart.
Preserve CapabilityManager/ElevatedOps rather than replacing them.

### DK-023 — Room DAO/migration verification
Add focused tests for important DAOs and migrations, especially upgrade paths used by existing released/tagged schemas. Do not rewrite a working schema solely for test convenience.

### Phase 2 gate
Full JVM tests + build + lint. Report and STOP.

---

## PHASE 3 — DroidKit V5 design system and semantic states [P2]
Goal: implement the existing V5 direction on top of working screens without redesigning business logic.

Before coding, inspect `docs/03_design_system/uiux_v5/` and current `core:ui`.

### Visual direction
- Material 3 foundation
- clean/minimal technical utility aesthetic
- Aurora green/teal DroidKit identity
- Bento-style information hierarchy where it improves scanning
- restrained glass/translucent effects only where readable and performant
- dark theme first-class; light theme remains coherent if supported
- dynamic color compatible, with user control if appropriate

### DK-030 — Design tokens
Centralize:
- semantic colors
- spacing
- shapes
- typography usage
- elevation/surface treatment
- motion constants where useful

Do not mechanically replace every dp value; prioritize shared/systemic values.

### DK-031 — Shared components
Consolidate reusable DroidKit components where repetition exists: cards, status badges, sections, tool rows/cards, diagnostics cards, permission/recovery cards, dialogs/sheets, etc.
Do not create wrappers that add no semantic/design value over Material 3.

### DK-032 — Semantic state components
Provide reusable patterns/components for:
- loading
- empty
- error/problem
- unsupported
- permission required
- provider setup required
- unavailable/offline where relevant
- success where a persistent state is useful

Recovery actions must integrate with Phase 2 rather than duplicate it.

### DK-033 — Apply incrementally
Apply V5 to shared shell and highest-traffic surfaces first (Home, Tools/search, Diagnostics, Reports, Settings), then feature screens. Preserve functionality while changing presentation.

Definition of Done:
- core tokens/components are centralized
- major surfaces use the design system
- semantic states are consistent
- no regression in navigation/data/capability behavior
- dark/light/dynamic-color behavior verified as supported

### Phase 3 gate
Build + tests + lint + Compose/UI checks available in repo. Report and STOP.

---

## PHASE 4 — Accessibility [P1]
Goal: make accessibility part of the component system rather than screen-by-screen patches.

### DK-040 — Semantics pass
Audit and correct:
- meaningful `contentDescription`
- decorative elements excluded appropriately
- `stateDescription`
- roles
- headings
- live regions for important dynamic state/progress
- merged/clear semantics where custom cards behave as controls

### DK-041 — Interaction/layout accessibility
Verify:
- touch targets
- logical focus order
- keyboard/switch access where applicable
- font scaling / large text
- contrast
- status is never color-only
- dialogs/sheets announce meaningful context

### DK-042 — Accessibility verification
Add automated Compose accessibility checks where supported and maintain a manual TalkBack checklist for flows automation cannot prove.

Definition of Done:
- shared components carry correct semantics by default
- main V1 journeys are usable with TalkBack
- 200%/large font does not make primary journeys unusable
- no important status relies on color alone

### Phase 4 gate
Run automated checks; report manual TalkBack/device checks separately. STOP.

---

## PHASE 5 — V1 feature closure [P1/P3]
Goal: remove remaining V1 placeholders/gaps without expanding scope.

### DK-050 — Phone Doctor catalog audit
Enumerate every catalog entry and map it to its actual screen/implementation.
For each entry classify: implemented, intentionally unsupported, post-V1, or accidentally unwired.
- wire genuine V1 entries
- move/remove entries that are not V1 rather than pretending they work
- keep honest unsupported states for hardware/API limitations

### DK-051 — Settings completion
Reconcile current Settings with actual V1 requirements. Candidates from prior audit:
- dynamic-color preference if product wants control
- privacy information
- reset preferences
- licenses/OSS
- onboarding/help revisit path
- advanced-mode setting only if it has a clear behavioral purpose
Do not add settings merely because the audit listed them; each must serve real V1 behavior.

### DK-052 — Dead-code/dependency cleanup
After verifying no use:
- remove obsolete FeatureStubScreen if truly dead
- remove unused WorkManager dependency if truly unused
- centralize dependency versions such as Shizuku if consistent with project catalog conventions
Do not perform broad cleanup unrelated to V1.

### DK-053 — String resources/i18n readiness
Move user-facing production copy into Android resources in a controlled way. Preserve wording and tests. This establishes localization readiness; actual translation is not required for V1 unless product scope says otherwise.

### Phase 5 gate
Build/test/lint and produce a final V1-tool matrix. STOP.

---

## PHASE 6 — Real-device verification [P0]
Goal: prove the code works on Android hardware. This phase cannot be declared complete from static analysis or JVM tests.

Create a reproducible device test matrix. Prefer at least:
- one Pixel/AOSP-like device or emulator for baseline
- one major OEM physical device when available
- supported min/representative/latest Android API levels where practical

Verify main V1 journeys plus:
- runtime permission grant/deny/permanent denial
- missing hardware
- camera/CameraX
- microphone/audio routing
- BLE scan/connect-relevant flows
- NFC read/write where supported
- USB host/device permission flows
- package visibility/App Inspector
- report export/share/FileProvider
- clean install + process restart persistence
- rotation/config changes where relevant

### DK-060 — Shizuku gate
On a real supported setup verify:
- unavailable state
- installed/running detection
- permission grant/denial
- successful allow-listed read
- successful intended mutation with confirmation
- disconnect/reconnect UI update
- fallback/error behavior

### DK-061 — Wireless ADB gate
Verify:
- pairing
- connect/disconnect
- key persistence/security behavior
- allowed command execution
- failure handling
- logcat path if V1-relevant

### DK-062 — Root gate
On a rooted test environment verify:
- detection
- grant denial/grant
- allow-listed execution
- audit log
- confirmation for mutation
- fallback when unavailable

Never mark these PASS without real device evidence.

### Phase 6 gate
Produce device-by-device evidence matrix and defect list. STOP.

---

## PHASE 7 — Compatibility and hardening [P1/P2]
Goal: close defects discovered during device verification.

### DK-070 — API-level matrix
Verify behavior across supported API levels, especially permission and platform behavior changes.

### DK-071 — OEM matrix
Document and harden meaningful OEM differences found during Phase 6. Do not add speculative vendor hacks without reproduced evidence.

### DK-072 — Performance/lifecycle
Profile important screens/diagnostics for main-thread work, leaks, repeated probes, excessive recomposition, receiver/listener cleanup, and cancellation. Fix measured issues, not hypothetical micro-optimizations.

### DK-073 — Security/privacy review
Re-check:
- exported components
- FileProvider paths
- logs
- report redaction
- AI privacy filtering
- secret handling
- shell argument validation
- elevated confirmation/audit
- permissions minimization

### Phase 7 gate
All P0/P1 device defects closed or explicitly accepted. STOP.

---

## PHASE 8 — Release engineering [P0/P1]
Goal: produce a reproducible V1 release candidate.

### DK-080 — Release build configuration
Implement appropriate release build type, R8/minification/resource shrinking decisions, and keep rules based on actual reflection/framework requirements. Verify release runtime behavior.

### DK-081 — Signing strategy
Configure signing through secure environment/CI secrets. Never commit private keys/passwords.

### DK-082 — APK/AAB and upgrade verification
Verify:
- clean install
- upgrade from representative previous tagged version(s)
- Room/DataStore preservation
- release APK/AAB installability
- versionCode/versionName discipline

### DK-083 — Release CI
Extend CI to create release-candidate artifacts under controlled conditions and retain reports/checksums as appropriate.

### DK-084 — Final V1 acceptance
Require:
- debug and release builds green
- JVM tests green
- lint/static analysis green
- required instrumented/UI tests green
- accessibility checklist passed
- device matrix passed or documented exceptions accepted
- Shizuku/ADB/root claims match actual verified support
- privacy/security review passed
- no unresolved P0/P1 defects
- changelog/release notes accurate

Then produce a release-readiness report. Do NOT publish or distribute unless explicitly instructed.

# Final rule
Do not optimize for the appearance of completion. Optimize for evidence. A feature is complete only when its implementation, UX states, tests, and required device verification support the claim.
