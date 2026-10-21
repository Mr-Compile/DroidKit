# Humanized AI responses — rich text, status icons, no markdown symbols

Date: 2026-10-21
Owner direction: AI replies must read like a human wrote them — no literal
`**`, `__`, `##`, `--` on screen — while staying rich: real bullets/numbered
lists, verdict pills, and tinted status icons per judged value (normal →
green check, high/unusual → amber warning, failing → red cross, pass/fail
mapped the same way). Material/SVG-style icons, not unicode emoji.

Approved design decisions (owner, this session):
- Markdown markers are **converted to real styling**, not stripped or shown.
- Status markers are **Material icons tinted by `StatusKind` colors**.
- Scope = **all AI output**: summary cards, Explain dialog, chat bubbles.
- Prompts also steer a **warmer conversational tone** (second person, plain
  English, short sentences).

## Scope

### core:ai
- New `AiTextFormat` (pure Kotlin, unit-testable like `AiProposalParser`):
  raw model text → typed `AiBlock`s — `Verdict` (ALL GOOD / WORTH CHECKING /
  NEEDS ATTENTION opener → `StatusCue` + label), `Heading`, `Bullet`,
  `Numbered`, `Paragraph`, `Mono` (``` fence), `Divider` (`---`/`___`/`***`).
  Inline: `**`/`__` → `Strong`, `*`/`_` → `Emphasis` (underscores need a
  non-word outer context so `snake_case` survives), `` ` `` → `Code`.
  Dangler cleanup drops unpaired markers, `--` → `—`, ` {2,}` → ` `.
  Per-line `StatusCue` from word-boundary keyword sets
  (NEGATIVE > WARNING > POSITIVE > INFO); negated phrases are scrubbed
  first so "nothing abnormal" → POSITIVE and "not normal" → WARNING.
  Leading status emoji on an icon-bearing line is consumed by the icon;
  emoji elsewhere pass through.
- New `AiRichText` composable: verdict → `StatusBadge` pill + rest text;
  bullets/numbered → 20dp marker slot (cue icon or `•`/`n.`) + styled text;
  `Paragraph` carries an icon when its cue fires; `Mono` keeps the chat
  code-block look; icons reuse `StatusKind` colors, `CheckCircle`/`Warning`/
  `Cancel`/`Info` (material-icons-extended, already a dependency of every
  Compose module).
- `AiUi.kt`: `AiSummaryContent` result and `AiResultDialog` body render via
  `AiRichText`; dialog also runs `stripStructuredBlocks` so raw
  `{"proposals":…}` JSON can never be shown verbatim.
- `AiChat.kt`: `MessageText` non-code segments for assistant turns render
  via `AiRichText`; user bubbles stay literal; fenced command output keeps
  its monospace block.
- `AiPrompts.kt`: `OUTPUT_STYLE` clause in `BASE` — conversational voice +
  "never use Markdown or formatting symbols" + judge values in words.
  `CONTEXTUAL_CHAT`/`TOOL_RECOMMENDER` mark the JSON block as the only
  exception. `SUMMARIZE_VALUES` keeps its asserted contract strings and now
  asks for one `- ` line per value ending in its verdict word.

### Mockup (`mockups/index.html`, same commit)
- `ICONS` gains `check_circle`/`warning`/`cancel`; CSS gains `.ai-line`,
  `.ai-ico` (ok/warn/bad/inf), `.ai-verdict` pills, `.ai-h`, `.ai-hr`,
  `.ai-dot`/`.ai-num`.
- `fmtAi`/`fmtAiProse`/`fmtInline`/`aiCue` mirror the Kotlin parser;
  wired into `fmtChat` assistant path, `aiSumCard`, `ai-explain` dialog and
  the report `ai-summary` text. `VSUM` + `sendAi` sample text now exercise
  verdict pills + per-line icons.
- User chat turns stay escaped plain text.

### Docs/tracker
- This plan file; checklist AI-3 note updated.

## Constraints / honest limits
- Renderer is the guarantee; the prompt only reduces noise. Models may still
  emit markdown occasionally — the parser converts or drops it deterministically.
- Status cues are keyword-based cosmetics: a miss shows a plain line, a false
  hit shows a slightly wrong icon — never a wrong verdict word.
- `elevatedOutcome` (raw command stdout) intentionally stays plain text.
- No new outbound data; `AiContextBuilder`/`PrivacyFilter` path unchanged.
- AI disabled/no key/offline behavior untouched — gating lives upstream.

## Validation
- `gradlew :core:ai:testDebugUnitTest` — new `AiTextFormatTest` (15 tests):
  marker→span conversion, dangler stripping, snake_case/2*3 survival,
  headings/bullets/numbered/dividers, verdict cues + labels, keyword cues,
  negation flips, leading-emoji consumption, fenced Mono, proposal-strip
  integration, empty input, prompt contract.
- `gradlew :core:ai:assembleDebug` — module compiles (all surfaces are
  inside core:ai; feature modules untouched).
- `mockups/index.html` → battery screen AI summary + AI chat show pills and
  per-line icons with no symbols.
- Self-review per `playbook/27` DoD.
