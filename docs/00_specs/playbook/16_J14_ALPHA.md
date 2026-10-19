# Journey 14 - Integrated Alpha

Recommended: v0.9.0-alpha.1, alpha.2, ...

- [x] V1 features integrated — J05–J13 through `v0.8.0` on `main`
- [x] Cross-module navigation — all graphs registered, routes resolve
- [x] Permission-flow audit — see `docs/04_audits/J14_INTEGRATION_REVIEW.md`
- [x] Crash/error audit
- [x] Database upgrade/clean-install checks — Room v1, no migrations required
- [x] Accessibility pass — static pass done; device pass pending
- [x] Performance pass — static pass done
- [x] Battery/background behavior — no background work exists
- [x] Export/share — FileProvider + clipboard flows verified in code
- [x] Offline behavior — local-first; honest no-network states
- [ ] OEM/device matrix — requires devices
- [ ] Alpha feedback process — requires testers
- [ ] Defect severity system — requires testers/issues
- [~] Blockers fixed — 2026-10-19 re-audit found + fixed: audio/camera/display lab stubs wired to live Phone Doctor tests, Developer Lab V1 subset (intent launcher + display metrics) shipped, app-icon `contentDescription` added; device run may surface more

Alpha exit:
- [ ] No critical blocker — none found statically; device run pending
- [ ] Core flows work on representative devices — requires devices
- [x] Known limitations documented — audit doc + per-screen honesty notes
