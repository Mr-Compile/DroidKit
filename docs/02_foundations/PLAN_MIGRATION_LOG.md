# PLAN_MIGRATION_LOG

Use one entry per resolved conflict. Template source: `docs/00_specs/feature_plans_v2/31_PLAN_MIGRATION_LOG_TEMPLATE.md`.

## Entry
Date: 2026-10-18
Feature: Build config
Old document/decision: Master Plan — minSdk 26
Improved v2 decision: Skeleton design spec — minSdk 28
Repository reality: no code yet; spec approved by owner
Conflict classification: breaking plan change (raises minimum supported Android)
Decision: minSdk 28 wins (skeleton design spec, Level: current sprint)
Owner approval required?: already approved via design spec
Files/docs updated: `docs/00_specs/2026-10-18-droidkit-app-skeleton-design.md`, `01_plans/2026-10-18-droidkit-app-skeleton.md`
Migration completed? [x]
Notes: recorded in master execution plan §1.

## Entry
Date: 2026-10-18
Feature: Build config
Old document/decision: Skeleton design spec — Kotlin 2.2.20
Improved v2 decision: Skeleton implementation plan — Kotlin 2.3.20 with 2.2.20 fallback
Repository reality: plan controls toolchain versions + cached Gradle
Conflict classification: wording refinement / toolchain pin
Decision: plan wins — Kotlin 2.3.20, fall back to 2.2.20 only if resolution fails (then KSP `2.2.20-2.0.3`)
Owner approval required?: no
Files/docs updated: `docs/01_plans/2026-10-18-droidkit-app-skeleton.md` (Global Constraints)
Migration completed? [x]
Notes: recorded in master execution plan §1.

## Entry
Date: 2026-10-18
Feature: Milestone naming
Old document/decision: Planning package v2 — M0–M10 / DK-xxx IDs
Improved v2 decision: Playbook — J01–J20 journeys + SemVer map
Repository reality: playbook is canonical for milestones
Conflict classification: wording refinement (naming scheme)
Decision: J-numbering is canonical; M/DK IDs map onto journeys, no second backlog
Owner approval required?: no
Files/docs updated: `docs/01_plans/2026-10-18-droidkit-master-execution-plan.md` §1
Migration completed? [x]
Notes: playbook `25_VERSIONING_RELEASE_MANAGEMENT.md` holds the J→SemVer map.

## Entry
Date: 2026-10-20
Feature: Error/problem UX + design tokens
Old document/decision: Per-feature ad-hoc error text + `BlockedReasonList` dialog; duplicated `AiError`→message maps in 4 files
Improved v2 decision: uiux_v5 semantic state model (`semantics/03`) — unified problem/recovery
Repository reality: v5 spec is canonical for UX
Conflict classification: approach refinement / supersession
Decision: v5 wins — `UiProblem`/`RecoveryAction` + `UiProblemCard` + `AiError.describe()` are canonical; `BlockedReasonList` removed as dead code; semantic status tokens in `theme/Tokens.kt`
Owner approval required?: no
Files/docs updated: `core/common/UiProblem.kt`, `core/ui/components/*`, `core/ui/theme/Tokens.kt`, tracker DK-020/030/031/032
Migration completed? [x]
Notes: Tools/Settings surface adoption deferred to parallel UX workstream (droidkit_ux_plan).

## Entry
Date: 2026-10-20
Feature: Brand identity + splash
Old document/decision: uiux_v4 + uiux_v5 — Logo Candidate 4 (geometric hex mark); baked splash_loading.png artwork
Improved v2 decision: Owner selected Logo Candidate 9 (futuristic glowing Android head) + Compose-drawn splash with loading indicator
Repository reality: brand mark, launcher icon, and splash rebuilt to the Candidate-9 direction in this session
Conflict classification: owner-driven design change (supersedes the v4/v5 candidate selection)
Decision: Candidate 9 wins; brand kit, v5 canonical docs, code assets updated; superseded v4 docs left as history
Owner approval required?: yes — given in session
Files/docs updated: `docs/03_design_system/branding/*`, `docs/03_design_system/uiux_v5/{README.md, foundation/01, implementation/21,23}`, `core/ui/.../theme/Brand.kt`, `app/.../SplashScreen.kt`, `ic_launcher_foreground.xml`, `themes.xml`, `colors.xml`, `mockups/index.html`, master journey checklist; deleted `splash_loading.png` + stale branding PNG
Migration completed? [x]
Notes: splash tagline switched to the candidate's own "Deeper insights. Better devices."; old primary tagline retained as alternate for onboarding/store copy.

## Entry
Date: 2026-10-21
Feature: Saved Wi-Fi passwords (root) + Device Inspector depth + battery-health measured values
Old document/decision: Frozen V1 scope (playbook/03 + feature_plans_v2/30) — root capabilities deferred to J20/V2; Network Lab V1 has no credential access; Device Inspector lists basic Build fields
Improved v2 decision: Owner-approved "unlock the capabilities of the phone" scope — root-only saved Wi-Fi viewer, elevated deep detail, measured battery health where the fuel gauge exposes it
Repository reality: `RootProvider` already landed de-facto; `ElevatedOps` had no way to pin an action to one provider, so a root-only action could silently run as the shell identity and fail; battery fuel-gauge WIP already in tree
Conflict classification: scope extension beyond the frozen V1 boundary (partial J20 de-freeze, owner-approved)
Decision: land it now with hard gates — `ElevatedAction.requiredProvider` pins `ReadWifiConfigStore` to ROOT (never Shizuku/ADB); secrets are display-only (FLAG_SECURE screen, reveal-on-tap, no clipboard/export/audit/AI); `Tool` is `Capability.ROOT` + `ToolPhase.V2`; elevated Device Inspector detail is read-only, on-demand, labeled by provider; battery health stays measured-vs-estimated honest
Owner approval required?: yes — given in session ("include, root-only (recommended)", "on-device-only, reveal-on-tap, never stored/exported/AI-sent")
Files/docs updated: `core/privileges/.../ElevatedOps.kt` (+`requiredProvider`, `ReadWifiConfigStore`, read-only dump actions), `feature/network/.../{SavedWifiParser,SavedNetworksViewModel,SavedNetworksScreen,NetworkFeature,NetworkScreen,NetworkViewModel}.kt`, `core/system/.../DeviceSystemProbe.kt` (deeper fields + fuel-gauge nodes), `feature/deviceinfo/.../{DeviceDeepProbe,DeviceInfoViewModel,DeviceInfoScreen}.kt`, `feature/battery/.../{FuelGauge,BatteryHealth,BatteryViewModel,BatteryScreen,BatteryFeature}.kt`, tests, `mockups/index.html`
Migration completed? [x]
Notes: Wi-Fi store paths vary by Android/OEM (apexdata `WifiConfigStore.xml` on API 29+, `wpa_supplicant.conf` pre-10); root may still be denied by the user's root manager — the tool reports `requires ROOT` honestly instead of degrading to shell identity. Hex PSKs are labeled "derived key", never presented as the passphrase.
