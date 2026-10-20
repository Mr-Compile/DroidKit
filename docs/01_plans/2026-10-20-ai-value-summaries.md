# AI Value Summaries — plain-language verdicts on result screens

Date: 2026-10-20
Owner direction: as long as the user has AI enabled, result/diagnostic screens
summarize the values they show and say whether each value looks normal,
unusual, or failing vs typical standards — understandable by non-techy users.

Approved design decisions (owner, this session):
- Trigger = Both: new `ai_auto_summarize` setting; auto-fires when on, manual
  "Summarize with AI" button when off.
- Coverage = all result screens.
- Placement = inline card (not the existing Explain dialog).

## Scope

### core:ai
- `AiSettingsStore`: `autoSummarize: Flow<Boolean>` (default false) +
  `setAutoSummarize()`.
- `PromptRegistry`: `SUMMARIZE_VALUES` — plain-language, per-value verdict
  (normal / unusual / failing vs typical), honest "can't judge" fallback,
  one-line overall verdict (ALL GOOD / WORTH CHECKING / NEEDS ATTENTION),
  <= ~150 words.
- `AiUseCase`: `summarizeValues(screenTitle, fields)` via existing gates
  (DISABLED → NO_KEY → provider errors) + `AiContextBuilder`/`PrivacyFilter`.
- `AiAssistViewModel`: separate summary state (`AiSummaryState(busy, result)`)
  so summaries do not trip `AiResultDialog`; `summarize(title, fields, auto)`
  with once-per-instance auto gating (`autoFired` flag).
- `AiUi.kt`: `AiSummaryCard(screenTitle, fields)` — renders nothing when AI
  off/no key; auto-fires once when fields first non-blank and autoSummarize
  on; Refresh icon re-runs manually; states: button → busy → result → error+retry.

### Settings
- "Auto-summarize results" switch in the Intelligence card (visible when AI
  enabled) + explainer copy (sends shown values to Groq, uses key more often).
- `SettingsViewModel`: expose `aiAutoSummarize` + setter.

### Screen wiring (fields = same values already rendered)
- Existing AI screens get the card added (Explain buttons stay):
  DeviceInfo, SessionReview, AppDetail, UsbDetail, Nfc, Network.
- New coverage: battery, storage, sensors/live-sensor/orientation, display
  modes, audio routing, insights, diagnostics tests with real readings
  (charging, connectivity, sensor readings). Human-observation tests (color
  grid, multitouch, flash, speaker, mic, camera) keep user-judged results —
  card still allowed on screens that show measured values only.

### Mockup + docs
- `mockups/index.html`: AI summary card + settings toggle (same-commit rule —
  file currently carries unrelated wireless-ADB WIP; commit strategy noted below).
- Checklist AI-3 status note updated after validation.

## Constraints / honest limits
- Auto fires once per screen view on the first populated snapshot — live
  screens (sensors, charging) are not re-summarized per reading; Refresh
  takes a new snapshot. Prevents API-quota burn.
- "Normal vs standard" is the model's general judgment, not measured
  baselines; prompt forces "can't judge" instead of guessing.
- No AI action proposals on summary results; summary is read-only text.
- AI disabled/no key/offline → card hidden or typed error, core unaffected.
- Uncommitted wireless-ADB WIP existed in the tree at plan time — landed as
  `b71e554` before this feature's commit, so no staging hazard remained.

## Validation
- `gradlew :core:ai:testDebugUnitTest` (new prompt + gating tests where feasible)
- `gradlew` compile of every touched feature module
- Self-review per `playbook/27` DoD

## Results
- `./gradlew assembleDebug` — BUILD SUCCESSFUL (all touched modules compile).
- `./gradlew :core:ai:testDebugUnitTest` — BUILD SUCCESSFUL; new
  `AiSummarizeValuesTest` (3 tests): context drops blank/null fields,
  redacts key-shaped values, prompt enforces verdict labels + honest
  fallback.
- Self-review: duplicate auto `LaunchedEffect` in `AiSummaryCard` removed
  (delegates to `AiSummaryContent`); card-free `AiSummaryContent` padding
  parameterized for embedding; PermissionCenter runtime requests hoisted
  once and shared between rows and summary; live sensor summarizes a
  captured snapshot, not every reading.
