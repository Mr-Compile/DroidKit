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
Date: 2026-10-20
Feature: Responsive/adaptive UI shell
Old document/decision: uiux_v5 execution plan Task 7 — adaptive navigation (bottom bar / rail) + Home bento redesign + Tools search as one shell task
Improved v2 decision: `01_plans/2026-10-20-responsive-adaptive-ui.md` — adaptive shell absorbed into a dedicated responsive pass; bento + Tools search stay with v5
Repository reality: fixed `NavigationBar` in `MainActivity`; no window-size classes anywhere; display-cutout letterboxing was the suspected cause of "app bar sits too low" on newer punch-hole phones
Conflict classification: scope split / partial supersession — adaptive nav shell, cutout hardening, and expanded list-detail land here; Home bento + Tools search remain open under V5-3/V5-4
Decision: responsive plan wins for shell, insets, and list-detail scope; v5 spec stays canonical for bento and per-screen migrations
Owner approval required?: yes — given in session (absorb shell, skip bento; list-detail only where spec'd; landscape works correctly, not redesigned)
Files/docs updated: `app/.../MainActivity.kt`, `app/.../values/themes.xml`, `core/ui/adaptive/*`, `core/ui/navigation/*`, `core/ui/components/DroidKitTopAppBar.kt`, `feature/{apps,usb,reports}/…` (two-pane), dense-screen `contentWidthCap` adoption, diagnostics test-screen top insets, `OnboardingScreen`, `mockups/index.html`, `01_plans/2026-10-20-responsive-adaptive-ui.md`, master journey checklist V5-3
Migration completed? [x]
Notes: `NavigationSuiteScaffold` picks NavigationBar vs NavigationRail from the window; `DroidKitListDetail` wraps `ListDetailPaneScaffold` for Apps/USB/Reports at ≥840dp; `contentWidthCap` (840dp) caps dense content; `shortEdges` cutout mode set in theme + window.

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

## Entry
Date: 2026-10-21
Feature: Device Overview → About-phone parity
Old document/decision: `dashboard.overview` "Device Overview" tool routed to `home` (Home tab); `deviceinfo.overview` "Device Inspector" held the real spec screen; SIM/carrier/status fields and restricted identifiers were out of scope
Improved v2 decision: owner request — "Device Overview" must be the About-phone-grade screen: remove the Home alias, retitle Device Inspector, add standard-tier fields, a READ_PHONE_STATE-gated SIM & network section, and elevated reads for restricted identifiers (serial, MACs, SELinux, best-effort IMEI)
Repository reality: `dashboard.overview` only ever switched tabs to Home (one-line `probe.summary()` card); Device Inspector already had deep standard + elevated fields
Conflict classification: scope extension beyond frozen V1 (runtime-permission SIM section + restricted-identifier elevated reads), owner-approved
Decision: `dashboard.overview` removed from the tool catalog (recents/favorites degrade gracefully via `mapNotNull`); `deviceinfo.overview` retitled "Device Overview"; new `ElevatedAction`s are unpinned param-free reads; identifiers + SIM numbers + Android ID excluded from AI fields, share, and section-copy; SIM section renders Grant/PERMISSION_REQUIRED honestly
Owner approval required?: yes — given in session (all four recommended options chosen)
Files/docs updated: `feature/dashboard/.../{DashboardFeature,DashboardScreen}.kt`, `feature/deviceinfo/.../{DeviceInfoFeature,DeviceInfoScreen,DeviceInfoViewModel,DeviceDeepProbe,SimStatusReader(new)}.kt`, `feature/deviceinfo/AndroidManifest.xml` (READ_PHONE_STATE + READ_BASIC_PHONE_STATE), `core/system/.../DeviceSystemProbe.kt` (device name, Android ID, codename, SDK extension, Play system update, GMS, IPs, encryption, eSIM, modems, GLES, screen inches, boot time, debug flags), `core/privileges/.../ElevatedOps.kt` (GetSeLinuxStatus, ReadLinkAddresses, ReadDeviceSettings, DumpPhoneIdentity + serial props), tests, `mockups/index.html`, master journey checklist
Migration completed? [x]
Notes: IMEI via `dumpsys phone` grep is best-effort — most modern builds print none, so the row usually shows "Not exposed"; per-slot network/service reads vary by OEM and fail per-field, not per-screen.

## Entry
Date: 2026-10-21
Feature: Elevated access — Shizuku provider removed
Old document/decision: J18 (playbook/20, plan `01_plans/2026-10-19-j18-shizuku.md`, spec `feature_plans_v2/features/23`) — Shizuku as the preferred elevated provider; `ElevatedOps` order SHIZUKU → ROOT → ADB; `Capability.SHIZUKU_SERVICE`; `ProviderKind.SHIZUKU`
Improved v2 decision: Owner direction — no third-party app dependency; DroidKit's own wireless-ADB client (`WirelessAdbProvider`) already reaches the same `shell` (uid 2000) channel, so Shizuku is dropped entirely
Repository reality: `privilege:shizuku` deleted (module, `dev.rikka.shizuku:api` dep, ProGuard keeps); provider order is now ROOT → ADB; `SHIZUKU_SERVICE` capability and `ProviderKind.SHIZUKU` removed; `permissions.inspector` tool re-declared `ELEVATED_SHELL`; `providerKindFor(ELEVATED_SHELL)` falls back to ADB
Conflict classification: breaking plan change vs frozen V1/J18 scope — supersedes the Shizuku journey; spec/playbook docs left as history per this log
Decision: removal wins — elevated features keep working through the wireless-ADB pairing flow (ADB Lab / guided pairing) or root; the "no external app" path is now the primary story
Owner approval required?: yes — given in session ("remove shizuku its okay as long as its working ... wireless debugging and the droidkit connections")
Files/docs updated: `settings.gradle.kts`, `app/build.gradle.kts`, `app/proguard-rules.pro`, `app/.../AppModule.kt`, `app/.../OnboardingScreen.kt`, `core/common/.../{Tool,UiProblem}.kt`, `core/privileges/.../{CapabilityProvider,CapabilityManager,ElevatedOps}.kt`, `privilege/adb/.../{WirelessAdbProvider,AdbPairingService}.kt` (comments), `privilege/shizuku/` (deleted), `feature/{deviceinfo,battery,developer,appops,packages,network,settings,tools,permissions}/…` (strings + `PackagesViewModel.requestAccess`), tests (`ElevatedOpsRoutingTest`, `CapabilityManagerTest`, `UiProblemMappersTest`, `ReportBuilderTest`, `DroidKitDatabaseDaoTest`, `AiActionValidatorTest`), `mockups/index.html`, `AGENTS.md`, checklist J18, `DEVICE_VALIDATION_RUNBOOK.md`, `CHANGELOG.md`
Migration completed? [x]
Notes: Root stays first in preference order — when present it is the broader channel (uid 0 covers the /data paths shell cannot read). Root-only `ElevatedAction`s (e.g. `ReadWifiConfigStore`) are unaffected: pinning is by `ProviderKind`, not by provider identity.

## Entry
Date: 2026-10-22
Feature: NFC Lab — expanded record set, tag ops, tech inspector
Old document/decision: J13 plan (`01_plans/2026-10-19-j13-nfc-network.md`) — NDEF parse limited to text/URI; writer was a single text/URI record picker; erase writes empty-TNF with NdefFormatable fallback; `makeReadOnly` excluded in V1
Improved v2 decision: owner direction (audit vs Play Store "NFC Tools") — "we need to add more choices and improve UX and UI and offer more compare to them"; owner picked the full parity write set, Copy + Format utilities, a read-only technology detail dump, and auto-scan on screen entry
Repository reality: implementation already had reader-mode scan, tech list, text/URI decode, single-record write, erase, confirm dialogs and Room history — everything else was new
Conflict classification: scope extension beyond frozen V1/J13 — write goes from 2 record types to 11 families + multi-record compose; new user-facing Duplicate and Format armed ops; Ultralight page dump
Decision: extension wins. Record composer covers Text, Link, Phone, SMS, Email, Contact (vCard), Wi-Fi (WSC credential), Geo, App (AAR), MIME and Raw (hex TNF/type/payload); decode adds Smart Poster, vCard, WSC Wi-Fi, external/AAR, MIME summary; Duplicate copies the scanned tag's raw NDEF message to the next presented tag (UID/locked regions never copied); Format is a distinct armed op on `NdefFormatable` tags; `NfcTechProbe` adds read-only ATQA/SAK/protocol/page-dump details; scan auto-starts on resume with pause/resume
Owner approval required?: yes — given in session (all four recommended options chosen)
Files/docs updated: `feature/nfc/.../{NdefRecordSpec,NfcTechProbe}.kt` (new), `NfcTagParser.kt`, `NfcViewModel.kt`, `NfcScreen.kt`, `NfcWriteScreen.kt`, `NfcFeature.kt`, tests (`NdefRecordSpecTest`, `NfcTechProbeTest`, `NfcTagReportTest`, `NfcTagParserTest`), `mockups/index.html`, `mockups/improved-index.html`, spec `feature_plans_v2/features/15_NFC_LAB_V2.md`, journey `playbook/15`, master checklist J13, `CHANGELOG.md`
Migration completed? [x]
Notes: `makeReadOnly` stays excluded — the J13 irreversible-op exclusion stands. Bank/transit/access-control/secure-element cloning remains a hard boundary; MIFARE Classic sectors are reported as key-protected, never read. The physical-tag release gate stays open — hardware validation is still pending.

## Entry
Date: 2026-10-21
Feature: Phone Doctor location/GPS test — live coordinate read
Old document/decision: J02 feasibility (`J02_TOOL_RECORDS.md`) — "ACCESS_FINE_LOCATION needed only for an actual fix (not in V1 diagnostic)"; `connectivity.location` shipped capability-only (feature flag + `isLocationEnabled`)
Improved v2 decision: owner direction — "add coordinates identify"; extend the existing test with an instant last-known fix plus an on-demand live fix
Repository reality: `ConnectivityCheckScreen` reads capability synchronously and auto-emits; `PermissionGate`/`rememberRuntimePermission` already provide the denial → PERMISSION_REQUIRED flow used by camera/mic/flash
Conflict classification: scope extension beyond frozen V1 diagnostic behavior — adds a runtime permission and an async location read to a previously synchronous check
Decision: extension wins — dedicated `LocationCheckScreen` behind a new `PhoneDoctorRoute.LocationCheck`; guided-runner emit stays instant and capability-based; the live fix is user-triggered only; raw coordinates stay on screen (report/share/AI notes carry provider/accuracy/age only, matching the identifier-exclusion decision)
Owner approval required?: yes — given in session (design presented, approved "sure ok")
Files/docs updated: `docs/01_plans/2026-10-21-location-gps-fix.md` (spec); implementation files pending
Migration completed? [ ]
Notes: checkbox closes when the screen ships; `mockups/index.html` updates in the same commit per AGENTS.md.
