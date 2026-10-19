# DroidKit V1 Completion Tracker

Statuses: NOT STARTED / IN PROGRESS / BLOCKED / CODE COMPLETE / DEVICE VERIFIED / DONE

| ID | Task | Priority | Initial Status | Completion evidence |
|---|---|---|---|---|
| DK-001 | Fix sensors.live parameterized launch route | P0 | CODE COMPLETE | `Tool` init now rejects `{arg}` launch routes (invariant by construction); `sensors.live` routes to new `sensors/live` chooser → typed `sensors/live/{type}` kept for internal nav; ToolRegistryTest 6/6 green; `:feature:sensors`+`:app` assembleDebug green. Device nav check pending. |
| DK-002 | Fix App Inspector package visibility | P1 | CODE COMPLETE | `<queries>` MAIN+LAUNCHER added to `feature/apps` + `feature/packages` manifests; verified present in merged app manifest; no QUERY_ALL_PACKAGES (frozen decision); visibility caveat text already in UI. API30+ device verification pending. |
| DK-010 | Minimal CI | P0 | NOT STARTED | PR workflow green |
| DK-011 | Lint/static analysis baseline | P1 | NOT STARTED | Local + CI lint green |
| DK-012 | Regression/architecture gates | P1 | NOT STARTED | Invariant tests green |
| DK-020 | Unified problem/recovery UX model | P1 | NOT STARTED | Unit tests + representative screens migrated |
| DK-021 | Permission flow hardening | P1 | NOT STARTED | State tests + device deny/permanent-deny verification |
| DK-022 | Reactive provider/capability status | P2 | NOT STARTED | State updates without restart |
| DK-023 | Room DAO/migration tests | P2 | NOT STARTED | Upgrade/migration tests green |
| DK-030 | V5 design tokens | P2 | NOT STARTED | core:ui tokens used by major surfaces |
| DK-031 | V5 shared components | P2 | NOT STARTED | repeated patterns consolidated |
| DK-032 | Semantic state components | P2 | NOT STARTED | Loading/Error/Unsupported/Permission/provider states consistent |
| DK-033 | Apply V5 to primary surfaces | P2 | NOT STARTED | Home/Tools/Diagnostics/Reports/Settings verified |
| DK-040 | Semantics pass | P1 | NOT STARTED | accessibility checks + TalkBack |
| DK-041 | Interaction/layout accessibility | P1 | NOT STARTED | touch/focus/font/contrast verified |
| DK-042 | Accessibility verification | P1 | NOT STARTED | automated + manual checklist |
| DK-050 | Phone Doctor catalog closure | P1 | NOT STARTED | every catalog entry classified/wired honestly |
| DK-051 | Settings V1 completion | P3 | NOT STARTED | requirements-based settings matrix |
| DK-052 | Dead code/dependency cleanup | P3 | NOT STARTED | build/test/lint after removals |
| DK-053 | strings.xml/i18n readiness | P2 | NOT STARTED | production UI copy resource-backed |
| DK-060 | Shizuku real-device gate | P0 | NOT STARTED | device evidence |
| DK-061 | Wireless ADB real-device gate | P0 | NOT STARTED | device evidence |
| DK-062 | Root real-device gate | P0 | NOT STARTED | rooted-device evidence |
| DK-070 | API-level compatibility matrix | P1 | NOT STARTED | matrix + evidence |
| DK-071 | OEM compatibility matrix | P1 | NOT STARTED | reproduced OEM findings |
| DK-072 | Performance/lifecycle hardening | P2 | NOT STARTED | profiling evidence |
| DK-073 | Security/privacy review | P1 | NOT STARTED | review checklist + fixes |
| DK-080 | Release build/R8 config | P0 | NOT STARTED | release build/runtime verified |
| DK-081 | Secure signing strategy | P0 | NOT STARTED | secrets externalized |
| DK-082 | APK/AAB + upgrade verification | P1 | NOT STARTED | clean + upgrade installs pass |
| DK-083 | Release CI | P1 | NOT STARTED | controlled RC artifact workflow |
| DK-084 | Final V1 acceptance | P0 | NOT STARTED | no unresolved P0/P1 + acceptance report |
